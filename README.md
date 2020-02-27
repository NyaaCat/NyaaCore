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
    maven { 
        name 'aikar';     
        url 'https://repo.aikar.co/content/groups/aikar/' 
    }
}

dependencies {
    compile('cat.nyaa:nyaacore:7.0-SNAPSHOT')
}
```

## Dependencies
- EssentialsX: soft depend. Used by `TeleportUtils`, with Essentials install, players can use `/back` command to return to the position before teleportion.
- Vault: soft depend. It's fine as long as you don't touch `VaultUtils`. If any downstream plugins what to use that, that plugin **MUST** list `Vault` as a required dependency.

## Version History
- 6.4.x: Minecraft 1.14.3
- 7.0.x: Minecraft 1.14.4

Older versions can be found in [Github Release Page](https://github.com/NyaaCat/NyaaCore/releases)
