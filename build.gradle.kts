import java.util.Properties

val jacksonVersion = "2.16.1"
val ktorVersion = "2.3.7"

plugins {
    kotlin("jvm") version "1.9.22"
    signing
    `maven-publish`
}

group = "me.kuku"
version = "2.3.7.0"

repositories {
    mavenCentral()
}

dependencies {
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    api("io.ktor:ktor-client-core:$ktorVersion")
    api("io.ktor:ktor-client-okhttp:$ktorVersion")
    api("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    api("io.ktor:ktor-serialization-jackson:$ktorVersion")
    api("io.ktor:ktor-client-websockets:$ktorVersion")
    api("io.ktor:ktor-client-logging:$ktorVersion")
    compileOnly("com.alibaba:fastjson:1.2.83")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

val sourcesJar by tasks.registering(Jar::class) {
    from(sourceSets["main"].allSource)
    archiveClassifier.set("sources")
}

val docJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

val properties = Properties()
properties.load(File("publish.properties").inputStream())
ext.set("signing.keyId", properties.getProperty("signing.keyId"))
ext.set("signing.password", properties.getProperty("signing.password"))
ext.set("signing.secretKeyRingFile", properties.getProperty("signing.secretKeyRingFile"))

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(sourcesJar)
            artifact(docJar)
            artifactId = "utils"
            pom {
                name.set("utils")
                description.set("my utils")
                url.set("https://github.com/kukume/utils")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("kuku")
                        name.set("kuku")
                        email.set("kuku@kuku.me")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/kukume")
                    developerConnection.set("scm:git:ssh://github.com/kukume")
                    url.set("https://github.com/kukume")
                }
            }
            from(components["java"])
        }
    }

    repositories {
        maven {
            url = uri("https://nexus.kuku.me/repository/maven-releases/")
            credentials {
                username = properties.getProperty("kuku.username")
                password = properties.getProperty("kuku.password")
            }
        }

        if (properties.getProperty("sonatype.username") != null && properties.getProperty("sonatype.username").isNotEmpty()) {
            maven {
                url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = properties.getProperty("sonatype.username")
                    password = properties.getProperty("sonatype.password")
                }
            }
        }

    }

    if (properties.getProperty("signing.keyId") != null && properties.getProperty("signing.keyId").isNotEmpty()) {
        signing {
            sign(publishing.publications)
        }
    }
}
