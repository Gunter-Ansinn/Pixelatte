plugins {
    id("java")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

group = "net.ansinn"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("Pixelatte - MAIN") {
    group = "application"
    description = "Runs the Pixelatte parser"

    mainClass = "net.ansinn.pixelatte.Main"

    classpath = sourceSets["main"].runtimeClasspath
    jvmArgs = listOf("-Xms512m", "-Xmx1024m")
    args = listOf("help", "")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("--enable-preview", "--add-modules", "jdk.incubator.vector"))
}

tasks.withType<JavaExec> {
    jvmArgs = listOf("--enable-preview", "--add-modules", "jdk.incubator.vector")
}

tasks.withType<Test> {
    jvmArgs = listOf("--enable-preview", "--add-modules", "jdk.incubator.vector")
}