import io.papermc.paperweight.util.path
import java.util.*

plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "1.7.1"
    id("xyz.jpenilla.run-paper") version "2.3.0" // Adds runServer and runMojangMappedServer tasks for testing
}

// = = =

val pluginName = "NyaaCore"
val majorVersion = 9
val minorVersion = 4

val paperApiName = "1.21-R0.1-SNAPSHOT"

// = = =

// for Jenkins CI
val buildNumber = System.getenv("BUILD_NUMBER") ?: "x"
val mavenDirectory = System.getenv("MAVEN_DIR") ?: layout.buildDirectory.dir("repo").path.toString()
val javaDocDirectory = System.getenv("JAVADOC_DIR") ?: layout.buildDirectory.dir("javadoc").path.toString()

// Version used for distribution. Different from maven repo
group = "cat.nyaa"
//archivesBaseName = "${pluginNameUpper}-mc$minecraftVersion"
version ="$majorVersion.$minorVersion"

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    } //paper
    maven { url = uri("https://libraries.minecraft.net") } // mojang
    maven { url = uri("https://repo.essentialsx.net/releases/") } // essentials
    // maven { url = uri("https://ci.nyaacat.com/maven/") } // nyaacat

}

dependencies {
    paperweight.paperDevBundle(paperApiName)
    // paperweight.foliaDevBundle("1.21-R0.1-SNAPSHOT")
    // paperweight.devBundle("com.example.paperfork", "1.21-R0.1-SNAPSHOT")
    compileOnly("net.essentialsx:EssentialsX:2.20.1")      // soft dep
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
            version = "$majorVersion.$minorVersion.$buildNumber-mc${getMcVersion(paperApiName)}"
        }
    }
    repositories {
        maven {
            name = "PublishMaven"
            url = uri(mavenDirectory)
            val mavenUserName = System.getenv("MAVEN_USERNAME")
            val mavenPassword = System.getenv("MAVEN_PASSWORD")
            if(mavenUserName != null && mavenPassword != null) {
                credentials {
                    username = mavenUserName
                    password = mavenPassword
                }
            }
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

    // 1)
    // For >=1.20.5 when you don't care about supporting spigot
    // paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

    // 2)
    // For 1.20.4 or below, or when you care about supporting Spigot on >=1.20.5
    // Configure reobfJar to run when invoking the build task
    /*
    // Configure reobfJar to run when invoking the build task
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
    return apiNameString.split('-')[0]
}
