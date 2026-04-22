
import com.vanniktech.maven.publish.SonatypeHost
import java.time.Year

plugins {
    id("com.vanniktech.maven.publish") version "0.28.0"
    signing
    `maven-publish`
    `java-library`
}

java {
    withJavadocJar()
    withSourcesJar()
}

signing {
    val gpgSecret = project.findProperty("gpg.secret").toString()
    val gpgPassphrase = project.findProperty("gpg.passphrase").toString()

    useInMemoryPgpKeys(gpgSecret, gpgPassphrase)
    sign(publishing.publications)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, true)
    coordinates(
        groupId = property("project.group").toString(),
        artifactId = property("project.name").toString(),
        version = property("project.version").toString()
    )
    pom {
        name = property("project.name").toString()
        description = property("project.description").toString()
        inceptionYear = "${Year.now().value}"
        url = property("project.url").toString()
        licenses {
            license {
                name = property("project.license").toString()
                url = property("project.license.url").toString()
                distribution = property("project.url").toString()
            }
        }
        developers {
            developer {
                id = property("project.developer.id").toString()
                name = property("project.developer.name").toString()
                email = property("project.developer.email").toString()
                url = property("project.developer.url").toString()
            }
        }
        scm {
            url = property("project.url.scm").toString()
            connection = "scm:git:git://github.com/${property("project.developer.id")}"
            developerConnection = "scm:git:ssh://git@github.com/${property("project.developer.id")}"
        }
    }
}

tasks.named("generateMetadataFileForMavenPublication") {
    dependsOn(tasks.named("kotlinSourcesJar"))
}