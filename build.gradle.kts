plugins {
    kotlin("multiplatform") version "1.7.10"
    id("maven-publish")
    id("signing")
}

group = "dev.sitar"
version = "0.3.0"

val javadocJar = tasks.register("javadocJar", Jar::class.java) {
    archiveClassifier.set("javadoc")
}

// https://github.com/gradle/gradle/issues/26091
val signingTasks = tasks.withType<Sign>()
tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(signingTasks)
}

val sonatypeUsername: String? = System.getenv("SONATYPE_USERNAME")
val sonatypePassword: String? = System.getenv("SONATYPE_PASSWORD")

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()

    jvm()
    linuxX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("dev.sitar:kio:1.1.0")

                api("io.ktor:ktor-network:2.1.1")
                implementation("io.ktor:ktor-client-core:2.1.1")
                implementation("io.ktor:ktor-client-cio:2.1.1")
            }
        }
    }
}

publishing {
    publications {
        repositories {
            maven {
                name="oss"
                val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

                credentials {
                    username = sonatypeUsername
                    password = sonatypePassword
                }
            }
        }

        withType<MavenPublication> {
            artifact(javadocJar)

            pom {
                name.set("dnskotlin")
                description.set("A Kotlin Multiplatform DNS client.")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                url.set("https://github.com/lost-illusi0n/dnskotlin")

                issueManagement {
                    system.set("Github")
                    url.set("https://github.com/lost-illusi0n/dnskotlin/issues")
                }

                scm {
                    connection.set("https://github.com/lost-illusi0n/dnskotlin.git")
                    url.set("https://github.com/lost-illusi0n/dnskotlin.git")
                }

                developers {
                    developer {
                        name.set("Marco Sitar")
                        email.set("marco+oss@sitar.dev")
                    }
                }
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}