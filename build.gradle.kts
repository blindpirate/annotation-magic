import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask

plugins {
    `java-library`
    `maven-publish`
    id("checkstyle")
    id("jacoco")
    id("signing")
    id("com.github.kt3k.coveralls") version ("2.10.1")
    id("com.github.spotbugs") version ("4.4.4")
}

rootProject.group = "com.gradle.blindpirate"
rootProject.version = "0.2"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}


repositories {
    jcenter()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
}

val test by tasks.getting(Test::class) {
    // Use junit platform for unit tests

    mustRunAfter("checkstyleMain", "spotbugsMain", "spotbugsTest")
    useJUnitPlatform()
}

apply(from = "config/coverage.gradle")


/************** checkstyle **************/

tasks.withType<Checkstyle>().configureEach {
    tasks.getByName("check").dependsOn(this)
    reports {
        xml.isEnabled = false
        html.isEnabled = true
    }
}

/************** spotbugs **************/
tasks.withType<SpotBugsTask>().configureEach {
    tasks.getByName("check").dependsOn(this)
    effort.set(Effort.MAX)
    reports.maybeCreate("xml").isEnabled = false
    reports.maybeCreate("html").isEnabled = true
}

/************** release ***************/
java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "com.github.blindpirate"
            artifactId = "annotation-magic"
            version = rootProject.version.toString()

            from(components["java"])
            artifact("sourcesJar")
            artifact("javadocJar")

            pom {
                name.set("annotation-magic")
                description.set("Empower your Java annotations with magic.")
                url.set("https://github.com/blindpirate/annotation-magic")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("blindpirate")
                        name.set("Bo Zhang")
                        email.set("zhangbodut@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/blindpirate/annotation-magic.git")
                    developerConnection.set("scm:git:git@github.com:blindpirate/annotation-magic.git")
                    url.set("https://github.com/blindpirate/annotation-magic")
                }
            }
        }
    }

    repositories {
        maven {
            val repoUrl = if (version.toString().endsWith("SNAPSHOT")) findProperty("SNAPSHOT_REPOSITORY_URL") else findProperty("RELEASE_REPOSITORY_URL")
            setUrl(repoUrl ?: "")

            authentication {
                create<BasicAuthentication>("basic") {
                    credentials {
                        username = findProperty("NEXUS_USERNAME")?.toString() ?: ""
                        password = findProperty("NEXUS_PASSWORD")?.toString() ?: ""
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

tasks.named("publishMavenJavaPublicationToMavenRepository") {
    dependsOn("check")
}