package cat.nyaa.nyaacore.http.client;

import com.google.gson.Gson;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.AsciiString;
import io.netty.util.ReferenceCountUtil;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class HttpClient {

    private static SslContext sslCtx;

    private static Bootstrap bootstrap;

    private static NioEventLoopGroup group;

    private HttpClient() {
        throw new IllegalStateException();
    }

    /**
     * Init the client thread loop
     *
     * @param nThreads Number of threads
     */
    public static void init(int nThreads) {
        try {
            sslCtx = SslContextBuilder.forClient().build();
        } catch (SSLException e) {
            e.printStackTrace();
        }
        if (group != null) {
            throw new IllegalStateException();
        }
        group = new NioEventLoopGroup(nThreads);
        bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline()
                                .addLast(new HttpClientCodec())
                                .addLast(new HttpContentDecompressor())
                                .addLast(new HttpObjectAggregator(512 * 1024));
                    }
                });

        Runtime.getRuntime().addShutdownHook(new Thread(HttpClient::shutdown));
    }

    /**
     * Shutdown the client
     */
    public static void shutdown() {
        if (group != null) {
            group.shutdownGracefully().syncUninterruptibly();
        }
        group = null;
        sslCtx = null;
        bootstrap = null;
    }

    /**
     * Connect to url with specified http request
     *
     * @param url          Url to connect to
     * @param httpRequest  Request
     * @param httpCallback Callback. Remember to {@link ReferenceCountUtil#release} the response in callback
     */
    public static void connect(String url, DefaultFullHttpRequest httpRequest, HttpCallback httpCallback) {
        URI uri = URI.create(url);

        String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
        String host = uri.getHost();
        if (host == null) {
            httpCallback.response(null, null, new URISyntaxException(url, "Host is unspecified"));
            return;
        }
        int port = uri.getPort();
        if (port == -1) {
            if ("http".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("https".equalsIgnoreCase(scheme)) {
                port = 443;
            }
        }

        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            httpCallback.response(null, null, new URISyntaxException(url, "Only HTTP(S) is supported."));
            return;
        }

        ChannelFuture channelFuture = bootstrap.connect(host, port);
        Channel channel = channelFuture.channel();
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                ChannelPipeline pipeline = channel.pipeline();
                if ("https".equalsIgnoreCase(scheme)) {
                    pipeline.addFirst(sslCtx.newHandler(channel.alloc()));
                }
                pipeline.addLast(new ResponseHandler(httpCallback));
                channel.writeAndFlush(httpRequest);
            }
            if (future.isCancelled()) {
                httpCallback.response(null, null, new IOException("connection refused"));
                channel.close();
            }
        });
    }

    /**
     * Get a url with headers
     *
     * @param url     URL
     * @param headers Headers
     * @return Future of response. Remember to {@link ReferenceCountUtil#release} the response
     */
    public static CompletableFuture<FullHttpResponse> get(String url, Map<String, String> headers) {
        CompletableFuture<FullHttpResponse> future = new CompletableFuture<>();
        request(url, GET, headers, null, null, (ctx, response, throwable) -> {
            if (response == null) {
                future.completeExceptionally(throwable);
                return;
            }
            if (response.status().equals(HttpResponseStatus.FOUND) ||
                    response.status().equals(HttpResponseStatus.SEE_OTHER) ||
                    response.status().equals(HttpResponseStatus.TEMPORARY_REDIRECT) ||
                    response.status().equals(HttpResponseStatus.PERMANENT_REDIRECT)) {
                String location = response.headers().get("Location");
                CompletableFuture<FullHttpResponse> redirect = get(location, headers);
                wrapFuture(future, redirect);
            } else {
                future.complete(response);
            }
        });
        return future;
    }

    /**
     * Request a url with headers by method
     *
     * @param url         URL
     * @param method      Method
     * @param headers     Headers
     * @param body        Body
     * @param contentType ContentType. See {@link HttpHeaderValues}
     * @return Future of response. Remember to {@link ReferenceCountUtil#release} the response
     */
    public static CompletableFuture<FullHttpResponse> request(String url, HttpMethod method, Map<String, String> headers, ByteBuf body, AsciiString contentType) {
        CompletableFuture<FullHttpResponse> future = new CompletableFuture<>();
        request(url, method, headers, body, contentType, (ctx, response, throwable) -> {
            if (response == null) {
                future.completeExceptionally(throwable);
                return;
            }
            if (response.status().equals(HttpResponseStatus.TEMPORARY_REDIRECT) ||
                    response.status().equals(HttpResponseStatus.PERMANENT_REDIRECT)) {
                String location = response.headers().get("Location");
                CompletableFuture<FullHttpResponse> redirect = request(location, method, headers, body, contentType);
                wrapFuture(future, redirect);
            } else {
                future.complete(response);
            }
        });
        return future;
    }

    /**
     * Post a url with header and json body
     *
     * @param url     Url
     * @param headers Headers
     * @param json    Json as body
     * @return Future of response. Remember to {@link ReferenceCountUtil#release} the response
     */
    public static CompletableFuture<FullHttpResponse> postJson(String url, Map<String, String> headers, String json) {
        CompletableFuture<FullHttpResponse> future = new CompletableFuture<>();
        postJson(url, headers, json, (ctx, response, throwable) -> {
            if (response == null) {
                future.completeExceptionally(throwable);
                return;
            }
            if (response.status().equals(HttpResponseStatus.TEMPORARY_REDIRECT) ||
                    response.status().equals(HttpResponseStatus.PERMANENT_REDIRECT)) {
                String location = response.headers().get("Location");
                CompletableFuture<FullHttpResponse> redirect = postJson(location, headers, json);
                wrapFuture(future, redirect);
            } else {
                future.complete(response);
            }
        });
        return future;
    }

    private static void wrapFuture(CompletableFuture<FullHttpResponse> future, CompletableFuture<FullHttpResponse> redirect) {
        redirect.handle((resp, exec) -> {
            if (resp != null) {
                future.complete(resp);
            }
            future.completeExceptionally(exec);
            return null;
        });
    }


    /**
     * Post a url with header and body
     *
     * @param url          URL
     * @param headers      Headers
     * @param body         Body
     * @param contentType  ContentType. See {@link HttpHeaderValues}
     * @param httpCallback Callback. Remember to {@link ReferenceCountUtil#release} the response in callback
     */
    public static void post(String url, Map<String, String> headers, String body, AsciiString contentType, HttpCallback httpCallback) {
        byte[] bodyBytes = body.getBytes(UTF_8);
        ByteBuf bodyBuf = PooledByteBufAllocator.DEFAULT.buffer(bodyBytes.length).writeBytes(bodyBytes);
        request(url, POST, headers, bodyBuf, contentType, httpCallback);
    }

    /**
     * Post a url with header and body
     *
     * @param url          URL
     * @param headers      Headers
     * @param body         Body
     * @param contentType  ContentType. See {@link HttpHeaderValues}
     * @param httpCallback Callback. Remember to {@link ReferenceCountUtil#release} the response in callback
     */
    public static void post(String url, Map<String, String> headers, ByteBuf body, AsciiString contentType, HttpCallback httpCallback) {
        request(url, POST, headers, body, contentType, httpCallback);
    }

    /**
     * Post a url with header and json body
     *
     * @param url          URL
     * @param headers      Headers
     * @param json         Json
     * @param httpCallback Callback. Remember to {@link ReferenceCountUtil#release} the response in callback
     */
    public static void postJson(String url, Map<String, String> headers, Map<String, Object> json, HttpCallback httpCallback) {
        Gson gson = new Gson();
        String jsonStr = gson.toJson(json);
        postJson(url, headers, jsonStr, httpCallback);
    }

    /**
     * Post a url with header and json body
     *
     * @param url          URL
     * @param headers      Headers
     * @param json         Json
     * @param httpCallback Callback. Remember to {@link ReferenceCountUtil#release} the response in callback
     */
    public static void postJson(String url, Map<String, String> headers, String json, HttpCallback httpCallback) {
        byte[] bodyBytes = json.getBytes(UTF_8);
        ByteBuf body = PooledByteBufAllocator.DEFAULT.buffer(bodyBytes.length).writeBytes(bodyBytes);
        post(url, headers, body, HttpHeaderValues.APPLICATION_JSON, httpCallback);
    }


    /**
     * Request a url
     *
     * @param url          URL
     * @param method       Method
     * @param headers      Headers
     * @param body         Body
     * @param contentType  ContentType. See {@link HttpHeaderValues}
     * @param httpCallback Callback. Remember to {@link ReferenceCountUtil#release} the response in callback
     */
    public static void request(String url, HttpMethod method, Map<String, String> headers, ByteBuf body, AsciiString contentType, HttpCallback httpCallback) {
        DefaultFullHttpRequest request;
        URI uri = URI.create(url);

        if (body != null) {
            request = new DefaultFullHttpRequest(HTTP_1_1, method, uri.getRawPath(), body);
            HttpUtil.setContentLength(request, request.content().readableBytes());
            request.headers().add(HttpHeaderNames.CONTENT_TYPE, contentType);
        } else {
            request = new DefaultFullHttpRequest(HTTP_1_1, method, url);
        }
        request.headers().set(HttpHeaderNames.HOST, uri.getHost());
        request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        request.headers().add(HttpHeaderNames.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:60.0; NyaaCore) Gecko/20100101 Firefox/60.0");
        writeHeaders(headers, request);
        connect(url, request, httpCallback);
    }

    private static void writeHeaders(Map<String, String> headers, DefaultFullHttpRequest request) {
        if (headers != null && !headers.isEmpty()) {
            headers.forEach((key, value) -> request.headers().set(key, value));
        }
    }

    @FunctionalInterface
    public interface HttpCallback {
        void response(ChannelHandlerContext ctx, FullHttpResponse response, Throwable throwable);
    }
}

class ResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private final HttpClient.HttpCallback httpCallback;

    ResponseHandler(HttpClient.HttpCallback httpCallback) {
        this.httpCallback = httpCallback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) {
        ReferenceCountUtil.retain(msg);
        if (ctx.channel().isActive()) {
            httpCallback.response(ctx, msg, null);
            ctx.close();
        } else {
            httpCallback.response(null, msg, null);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
        httpCallback.response(ctx, null, throwable);
        ctx.close();
    }
}