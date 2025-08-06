import java.net.URI

plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
    id("xyz.jpenilla.run-paper") version "2.3.0" // Adds runServer and runMojangMappedServer tasks for testing
}

// = = =

val pluginName = "NyaaCore"
val paperApiName = "1.21.8-R0.1-SNAPSHOT"

// = = =

group = "cat.nyaa"
version ="9.10"

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
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
    compileOnly("net.essentialsx:EssentialsX:2.21.1") {
        exclude(group = "org.spigotmc", module = "spigot-api")
    }    // soft dep
    compileOnly("org.jetbrains:annotations:24.1.0")
    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.github.seeseemelk:MockBukkit-v1.21:3.95.1")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
    testImplementation("org.xerial:sqlite-jdbc:3.46.0.0")
    testImplementation("ch.vorburger.mariaDB4j:mariaDB4j:3.1.0")
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
        maven {
            name = "NyaaCatCILocal"
            //local maven repository
            url = uri("file://${System.getenv("MAVEN_DIR")}")
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


/*
reobfJar {
     // This is an example of how you might change the output location for reobfJar. It's recommended not to do this
     // for a variety of reasons, however it's asked frequently enough that an example of how to do it is included here.
     outputJar.set(layout.buildDirectory.file("libs/PaperweightTestPlugin-${project.version}.jar"))
}
*/


tasks {
    // ref: https://docs.papermc.io/paper/dev/userdev
    // 1)
    // For >=1.20.5 when you don't care about supporting spigot
    // set:
    paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
    // and it will eliminate the -dev suffix from the artifact name which indicates the artifact is
    // mojang-mapped and won't work on the most of the server but at 1.20.5 the paper
    // uses mojang-mapping by default, so it doesn't matter anymore.
    //

    // 2)
    // For 1.20.4 or below, or when you care about supporting Spigot on >=1.20.5
    // Configure reobfJar to run when invoking the build task
    /*
    assemble {
        dependsOn(reobfJar)
    }
    */

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
        options.release.set(21)
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
}

private fun getMcVersion(apiNameString: String): String {
    val version = apiNameString.split('-')[0]
    val versionInArray = version.split('.')
    return "${versionInArray[0]}.${versionInArray[1]}"
}
