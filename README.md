## NyaaCore
Common library used for all NyaaCat plugin.  
Provides infrastructures to simplify plugin development.

[![Build Status](https://travis-ci.org/NyaaCat/NyaaCore.svg?branch=master)](https://travis-ci.org/NyaaCat/NyaaCore)
## Component List

- Annotation based command dispatcher
- Annotation based configuration serializer
- Annotation based database serializer
- JSON message builder

## Use as dependency in Gradle

```
repositories {
    maven {
        name 'NyaaCore'
        url 'https://raw.githubusercontent.com/NyaaCat/NyaaCore/maven-repo'
    }
}

dependencies {
    compile('cat.nyaa:nyaacore:4.0-SNAPSHOT') {
        transitive = false
    }
}
```

If you need extra server side I18n/L10n stuff, consider using [LangUtils](https://github.com/MascusJeoraly/LanguageUtils):

```
repositories {
    maven {
        url 'https://raw.github.com/MascusJeoraly/LanguageUtils/mvn-repo/'
    }
}

dependencies {
    compile('com.meowj:LangUtils:1.9') {
        transitive = false
    }
}
```
