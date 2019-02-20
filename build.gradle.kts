import org.gradle.jvm.tasks.Jar

plugins {
    `build-scan`
    `maven-publish`
    kotlin("jvm") version "1.3.21"
    id("org.jetbrains.dokka") version "0.9.17"
    signing
}

group = "com.ccebrecos"
version = "0.0.1"

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("junit:junit:4.12")
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}

tasks.dokka {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles kotlin docs with Dokka"
    classifier = "javadoc"
    from(tasks.dokka)
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allJava)
    archiveClassifier.set("sources")
}

val sonatypeUsername = if (project.findProperty("sonatypeUsername") == null) System.getenv("sonatypeUsername") else project.findProperty("sonatypeUsername").toString()
val sonatypePassword = if (project.findProperty("sonatypePassword") == null) System.getenv("sonatypePassword") else project.findProperty("sonatypePassword").toString()

publishing {
    publications {
        create<MavenPublication>("default") {
            from(components["java"])
            artifact(dokkaJar)
            artifact(tasks["sourcesJar"])
            pom {
                name.set("Kotlin JVM sample library")
                description.set("A sample JVM library to test maven")
                url.set("https://github.com/ccebrecos/my-kotlin-library")
                licenses {
                    license {
                        name.set("The GNU General Public License v3.0")
                        url.set("https://www.gnu.org/licenses/gpl.txt")
                    }
                }
                developers {
                    developer {
                        id.set("ccebrecos")
                        name.set("Carlos GC")
                        email.set("mail@ccebrecos.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/ccebrecos/my-kotlin-library.git")
                    developerConnection.set("scm:git:ssh://github.com/ccebrecos/my-kotlin-library.git")
                    url.set("https://github.com/ccebrecos/my-kotlin-library")
                }
            }
        }
    }
    repositories {
        maven {
            url = uri("$buildDir/repository")
            name = "local"
        }
        maven {
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            name = "mavenCentral"
            credentials {
                username = sonatypeUsername
                password = sonatypePassword
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["default"])
}
