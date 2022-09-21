plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.sonarqube") version "3.4.0.2513"
    id("io.freefair.lombok") version "6.5.1"
    checkstyle
    jacoco
}

group = "org.kryonite"
version = "1.0.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://nexus.velocitypowered.com/repository/maven-public/")
}

dependencies {
    val junitVersion = "5.9.1"
    val velocityVersion = "3.1.0"

    implementation("io.fabric8:kubernetes-client:5.12.2")
    compileOnly("com.velocitypowered:velocity-api:$velocityVersion")
    annotationProcessor("com.velocitypowered:velocity-api:$velocityVersion")

    testImplementation("org.slf4j:slf4j-simple:2.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:4.8.0")
    testImplementation("com.velocitypowered:velocity-api:$velocityVersion")
    testAnnotationProcessor("com.velocitypowered:velocity-api:$velocityVersion")
}

tasks.test {
    finalizedBy("jacocoTestReport")
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withJavadocJar()
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
}

checkstyle {
    toolVersion = "9.2.1"
    config = project.resources.text.fromUri("https://kryonite.org/checkstyle.xml")
}

sonarqube {
    properties {
        property("sonar.projectKey", "kryoniteorg_kryo-server-discovery")
        property("sonar.organization", "kryoniteorg")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

val tokens = mapOf("VERSION" to project.version)

tasks.withType<ProcessResources> {
    filesMatching("*.yml") {
        filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokens)
    }
}

publishing {
    publications {
        create<MavenPublication>("java") {
            groupId = project.group.toString()
            artifactId = project.name.toLowerCase()
            version = project.version.toString()

            from(components["java"])
        }
    }
}
