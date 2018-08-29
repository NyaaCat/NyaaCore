## NyaaCore
Common library used for all NyaaCat plugin.  
Provides infrastructures to simplify plugin development.

[![Build Status](https://travis-ci.org/NyaaCat/NyaaCore.svg?branch=master)](https://travis-ci.org/NyaaCat/NyaaCore)
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
        name 'NyaaCentral'
        url 'https://raw.githubusercontent.com/NyaaCat/NyaaCentral/maven-repo'
    }
}

dependencies {
    compile('cat.nyaa:nyaacore:6.2-SNAPSHOT') {
        transitive = false
    }
}
```
