import io.papermc.paperweight.util.path

plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "1.5.11"
    id("xyz.jpenilla.run-paper") version "2.2.3" // Adds runServer and runMojangMappedServer tasks for testing
}

// = = =

val pluginName = "NyaaCore"
val majorVersion = 9
val minorVersion = 3

val paperApiName = "1.20.4-R0.1-SNAPSHOT"

// = = =

// for Jenkins CI
val buildNumber = System.getenv("BUILD_NUMBER") ?: "local"
val mavenDirectory =
    System.getenv("MAVEN_DIR")
        ?: layout.buildDirectory.dir("repo").path.toString()
val javaDocDirectory =
    System.getenv("JAVADOC_DIR")
        ?: layout.buildDirectory.dir("javadoc").path.toString()

// Version used for distribution. Different from maven repo
group = "cat.nyaa"
//archivesBaseName = "${pluginNameUpper}-mc$minecraftVersion"
version =
    "$majorVersion.$minorVersion-${getMcVersion(paperApiName)}-b$buildNumber"

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    } //paper
    maven { url = uri("https://libraries.minecraft.net") } // mojang
    maven { url = uri("https://repo.essentialsx.net/releases/") } // essentials
    maven { url = uri("https://ci.nyaacat.com/maven/") } // nyaacat

}

dependencies {
    paperweightDevelopmentBundle(paperweight.paperDevBundle(paperApiName))
    // paperweight.foliaDevBundle("1.20.4-R0.1-SNAPSHOT")
    // paperweight.devBundle("com.example.paperfork", "1.20.4-R0.1-SNAPSHOT")
    compileOnly("net.essentialsx:EssentialsX:2.19.6")      // soft dep
    compileOnly("org.jetbrains:annotations:23.0.0")
    // Testing
    testImplementation(platform("org.junit:junit-bom:5.9.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.github.seeseemelk:MockBukkit-v1.19:2.132.3")
    testImplementation("org.mockito:mockito-core:4.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.8.0")
    testImplementation("org.xerial:sqlite-jdbc:3.39.3.0")
    testImplementation("ch.vorburger.mariaDB4j:mariaDB4j:2.6.0")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(getComponents()["java"])
            afterEvaluate {
                artifactId = pluginName
                groupId = "$group"
                version =
                    "$majorVersion.$minorVersion.$buildNumber-${
                        getMcVersion(
                            paperApiName
                        )
                    }"
            }
        }
    }
    repositories {
        maven {
            name = "nyaaMaven"
            url = uri(mavenDirectory)
        }
    }
}

/*
reobfJar {
     // This is an example of how you might change the output location for reobfJar. It's recommended not to do this
     // for a variety of reasons, however it's asked frequently enough that an example of how to do it is included here.
     outputJar.set(layout.buildDirectory.file("libs/PaperweightTestPlugin-${project.version}.jar"))
}
*/


tasks {
    // Configure reobfJar to run when invoking the build task
    assemble {
        dependsOn(reobfJar)
    }


    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(17)
    }
    javadoc {
        with((options as StandardJavadocDocletOptions)) {
            options.encoding =
                Charsets.UTF_8.name() // We want UTF-8 for everything
            links("https://docs.oracle.com/en/java/javase/17/docs/api/")
            links("https://guava.dev/releases/21.0/api/docs/")
            links("https://ci.md-5.net/job/BungeeCord/ws/chat/target/apidocs/")
            links("https://jd.papermc.io/paper/1.20/")
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
    return apiNameString.split('-')[0]
}
