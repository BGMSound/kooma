import org.jreleaser.model.Active
import java.time.Year

plugins {
    id("org.jreleaser") version "1.23.0"
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = property("project.group").toString()
            artifactId = property("project.name").toString()
            version = property("project.version").toString()

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
            from(components["kotlin"])
        }
    }
    repositories {
        maven(url = layout.buildDirectory.dir("staging-deploy"))
    }
}

jreleaser {
    gitRootSearch = true
    deploy {
        maven {
            mavenCentral {
                register("sonatype") {
                    url = "https://central.sonatype.com/api/v1/publisher"
                    applyMavenCentralRules = true

                    stagingRepository("target/staging-deploy")
                }
            }
        }
    }
    signing {
        active = Active.ALWAYS
    }
    release {
        github {
            repoOwner = property("project.developer.id").toString()
            repoUrl = property("project.name").toString()
            token = System.getenv("JRELEASER_GITHUB_TOKEN")
        }
    }
}