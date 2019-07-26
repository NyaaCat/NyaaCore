## NyaaCore
Common library used for all NyaaCat plugin.  
Provides infrastructures to simplify plugin development.

[![Build Status](https://ci.nyaacat.com/job/NyaaCore/badge/icon)](https://ci.nyaacat.com/job/NyaaCore/)
## Component List

- Annotation based command dispatcher
- Annotation based configuration serializer
- Annotation based database serializer
- JSON message builder
- Database middleware
- Http client
- Http server based on [timboudreau/netty-http-client](https://github.com/timboudreau/netty-http-client)

## Use as dependency in Gradle

```
repositories {
    maven {
        name 'NyaaCat'
        url 'https://ci.nyaacat.com/maven/'
    }
}

dependencies {
    compile('cat.nyaa:nyaacore:7.0-SNAPSHOT')
}
```

## Version History
- 6.4.x: Minecraft 1.14.3
- 7.0.x: Minecraft 1.14.4

Older versions can be found in [Github Release Page](https://github.com/NyaaCat/NyaaCore/releases)
