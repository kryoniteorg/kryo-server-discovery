plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.1"
    jacoco
}

group = "net.citybuild"
version = "0.1.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://nexus.velocitypowered.com/repository/maven-public/")
}

dependencies {
    implementation("io.fabric8:kubernetes-client:5.11.1")
    compileOnly("com.velocitypowered:velocity-api:3.1.0")
    annotationProcessor ("com.velocitypowered:velocity-api:3.0.1")

    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")

    testImplementation("org.slf4j:slf4j-simple:1.7.32")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("org.mockito:mockito-junit-jupiter:4.2.0")
    testImplementation("com.velocitypowered:velocity-api:3.0.1")
    testAnnotationProcessor ("com.velocitypowered:velocity-api:3.0.1")

    testCompileOnly("org.projectlombok:lombok:1.18.20")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.20")
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
