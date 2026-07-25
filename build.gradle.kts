import java.net.URI

plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("xyz.jpenilla.run-paper") version "3.0.2" // Adds runServer and runMojangMappedServer tasks for testing
}

// = = =

val pluginName = "NyaaCore"
val paperApiName = "26.2.build.+"

// = = =

group = "cat.nyaa"
version ="9.11"

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
    mavenCentral()
    maven ("https://papermc.io/repo/repository/maven-public/") //paper
    maven ("https://libraries.minecraft.net")  // mojang
    maven ("https://repo.essentialsx.net/releases/") // essentials
    // maven { url = uri("https://ci.nyaacat.com/maven/") } // nyaacat
}

dependencies {
    paperweight.paperDevBundle(paperApiName)
    compileOnly("net.essentialsx:EssentialsX:2.21.2") {
        exclude(group = "org.spigotmc", module = "spigot-api")
    }    // soft dep
    // Testing
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(platform("org.junit:junit-bom:6.1.0-RC1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v26.1.2:4.113.1")
    testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")
    testImplementation("org.xerial:sqlite-jdbc:3.53.1.0")
    testImplementation("ch.vorburger.mariaDB4j:mariaDB4j:3.3.1")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = group.toString()
            artifactId = pluginName.lowercase()
            version = project.version.toString()
        }
    }
    repositories {
        maven {
            name = "GithubPackage"
            url = URI(System.getenv("GITHUB_MAVEN_URL") ?: "https://github.com")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
        // Add local maven repository only when MAVEN_DIR is set to avoid converting "file://null" to a URI
        val mavenDir = System.getenv("MAVEN_DIR")
        if (mavenDir != null && mavenDir.isNotBlank()) {
            maven {
                name = "NyaaCatCILocal"
                // local maven repository
                url = uri("file://$mavenDir")
            }
        }
    }
}

// Custom tasks for publishing to specific repositories
tasks.register("publishToGithubPackage") {
    dependsOn("publishMavenJavaPublicationToGithubPackageRepository")
    // auto generated task: publish<PublicationName>PublicationTo<RepositoryName>Repository
}

tasks.register("publishToNyaaCatCILocal") {
    dependsOn("publishMavenJavaPublicationToNyaaCatCILocalRepository")
}



tasks {

    withType<ProcessResources> {
        val newProperties = project.properties.toMutableMap()
        newProperties["api_version"] = getMcVersion(paperApiName)
        filesMatching("plugin.yml") {
            expand(newProperties)
        }
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(25)
    }

    javadoc {
        with((options as StandardJavadocDocletOptions)) {
            options.encoding =
                Charsets.UTF_8.name() // We want UTF-8 for everything
            links("https://docs.oracle.com/en/java/javase/21/docs/api/")
            links("https://guava.dev/releases/21.0/api/docs/")
            links("https://ci.md-5.net/job/BungeeCord/ws/chat/target/apidocs/")
            links("https://jd.papermc.io/paper/1.21/")
            options.locale = "en_US"
            options.encoding = "UTF-8"
            (options as StandardJavadocDocletOptions).addBooleanOption(
                "keywords",
                true
            )
            (options as StandardJavadocDocletOptions).addStringOption(
                "Xdoclint:none",
                "-quiet"
            )
            (options as StandardJavadocDocletOptions).addBooleanOption(
                "html5",
                true
            )
            options.windowTitle = "$pluginName Javadoc"
        }
    }

    test {
        // Use JUnit Platform (JUnit 5) for test discovery
        useJUnitPlatform()
        // Prevent build failure if no tests are discovered in some environments
        failOnNoDiscoveredTests = false
        // MockBukkit only publishes mockbukkit-v26.1.2 so far and doesn't yet support the
        // registries introduced in Paper 26.2 (e.g. minecraft:attribute), which crashes
        // ServerMock init. Exclude these until MockBukkit ships a 26.2-compatible release.
        exclude("cat/nyaa/nyaacore/LanguageRepositoryTest.class")
        exclude("cat/nyaa/nyaacore/MessageTest.class")
        exclude("cat/nyaa/nyaacore/cmdreceiver/dispatchtest/DispatchTest.class")
    }
}

private fun getMcVersion(apiNameString: String): String {
    val version = apiNameString.split('-')[0]
    val versionInArray = version.split('.')
    return "${versionInArray[0]}.${versionInArray[1]}"
}
