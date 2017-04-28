## NyaaCore
Common library used for all NyaaCat plugin.  
Provides infrastructures to simplify plugin development.

Current version: Minecraft 1.11.2

## Component List

- Annotation based command dispatcher
- Annotation based configuration serializer
- Annotation based database serializer
- JSON message builder
- Server side I18n support

## Use as dependency in Gradle

```
repositories {
    maven {
        name 'NyaaCore'
        url 'https://raw.githubusercontent.com/NyaaCat/NyaaCore/maven-repo'
    }
}

dependencies {
    compile('cat.nyaa:nyaacore:2.0-SNAPSHOT') {
        transitive = false
    }
}
```
