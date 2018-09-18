<?php
$user_agent = 'curl 7.35.0';
$home = '/home/travis';
$cache_dir = "{$home}/cache";
echo "cache dir: {$cache_dir}\n";
preg_match('/lib\/(spigot-.*\.jar)/', file_get_contents(dirname(__FILE__) . '/build.gradle'), $matches);
$jar_name = $matches[1];
$cache_spigot_jar = "{$cache_dir}/spigot.jar";
$lib_spigot = dirname(__FILE__) . '/' . $matches[0];
$tmp = $cache_spigot_jar . '.tmp';
$local_etag_file = $cache_spigot_jar . '.etag';
$cookies_file_path = $cache_dir . '/cookies.txt';
if (!is_dir('lib')) {
    mkdir('lib');
}
if (!is_dir($cache_dir)) {
    mkdir($cache_dir);
}
$ch = curl_init();
$url = "https://downloads.nyaacat.com/dailybuild/{$jar_name}";

if (is_file($lib_spigot)) {
    unlink($lib_spigot);
}
$fp = fopen($tmp, 'w+');
$response_headers = array();
$request_headers = array();
$request_headers[] = 'Connection: keep-alive';
$request_headers[] = 'Accept-Encoding: gzip, deflate';
if (is_file($cache_spigot_jar) && filesize($cache_spigot_jar) > 0) {
    $local_mtime = unix_timestamp_to_date(filemtime($cache_spigot_jar));
    if ($local_mtime > 0) {
        $request_headers[] = "If-Modified-Since: {$local_mtime}";
    }
    if (is_file($local_etag_file)) {
        $request_headers[] = 'If-None-Match: ' . file_get_contents($local_etag_file);
    }
}
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_HTTPHEADER, $request_headers);
curl_setopt($ch, CURLOPT_USERAGENT, $user_agent);
curl_setopt($ch, CURLOPT_COOKIEFILE, $cookies_file_path);
curl_setopt($ch, CURLOPT_COOKIEJAR, $cookies_file_path);
curl_setopt($ch, CURLOPT_FILE, $fp);
curl_setopt($ch, CURLOPT_NOPROGRESS, false);
curl_setopt($ch, CURLOPT_VERBOSE, true);
curl_setopt($ch, CURLOPT_FILETIME, true);

curl_setopt($ch, CURLOPT_HEADERFUNCTION, function ($ch, $string) use (&$response_headers) {
    $len = strlen($string);
    $string = explode(':', $string, 2);
    $name = strtoupper(trim($string[0]));
    if ($name != null && isset($string[1])) {
        $response_headers[$name] = trim($string[1]);
    }
    return $len;
}
);

curl_exec($ch);

fclose($fp);
print_r($response_headers);

$response_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$remote_mtime = curl_getinfo($ch, CURLINFO_FILETIME);
curl_close($ch);
$remote_etag = empty($response_headers['ETAG']) ? null : $response_headers['ETAG'];
$remote_size = isset($response_headers['CONTENT-LENGTH']) ? $response_headers['CONTENT-LENGTH'] : null;
if ($response_code == 200) {
    /*if ($remote_size != null && $remote_size != filesize($tmp)) {
        exit(1);
    }*/
    if ($remote_etag != null) {
        file_put_contents($local_etag_file, $remote_etag);
    }
    test_jar($tmp);
    if (is_file($cache_spigot_jar)) {
        unlink($cache_spigot_jar);
    }
    if (is_file($lib_spigot)) {
        unlink($lib_spigot);
    }
    rename($tmp, $cache_spigot_jar);
    link($cache_spigot_jar, $lib_spigot);
    if ($remote_mtime > 0) {
        touch($cache_spigot_jar, $remote_mtime, $remote_mtime);
    }
} elseif ($response_code == 304) {
    link($cache_spigot_jar, $lib_spigot);
    //test_jar($lib_spigot);
} else {
    exit(1);
}


function unix_timestamp_to_date($time)
{
    return gmdate('D, d M Y H:i:s T', $time);
}

function test_jar($path)
{
    $zip = new ZipArchive();
    $success = $zip->open($path, ZipArchive::CHECKCONS);
    if ($success !== TRUE) {
        echo 'error code: ' . $success;
        exit(1);
    }
    print $zip->getFromName('META-INF/MANIFEST.MF');
    $zip->close();
}