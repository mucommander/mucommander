import java.text.SimpleDateFormat
import java.util.Date

plugins {
    java
    application
    alias(libs.plugins.grgit)
}

allprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.isFork = true
    }

    group = "dev.barebones.commander"
    version = "0.1.0-SNAPSHOT"
    extra["release"] = "snapshot"
}

tasks.register("printFullVersionName") {
    doLast { println("${project.version}-${project.extra["release"]}") }
}

tasks.register("printVersionName") {
    doLast { println(project.version) }
}

repositories.mavenCentral()

subprojects {
    apply(plugin = "java-library")
    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }
    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(listOf("--release", "25"))
        options.encoding = "UTF-8"
    }
    dependencies {
        // Catalog accessors are not available inside the subprojects {} block
        // (Gradle limitation as of 8.x), so dep coordinates here are spelled
        // out long-form. Per-module build.gradle.kts files DO use the catalog.
        "implementation"("org.slf4j:slf4j-api:2.0.17")
        constraints {
            "implementation"("com.squareup.okio:okio-jvm:3.11.0")
            "implementation"("org.jetbrains.kotlin:kotlin-stdlib:2.1.20")
            "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.1.20")
            "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.20")
            "implementation"("org.jetbrains.kotlin:kotlin-stdlib-common:2.1.20")
        }
    }
    tasks.withType<Test>().configureEach {
        // Default subproject test framework to TestNG. Modules that use
        // JUnit 5 (e.g. barebones-protocol-nfs) override with
        // useJUnitPlatform() in their own build.gradle.kts.
        useTestNG()
        testLogging {
            events("failed", "standardError")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showExceptions = true
            showStackTraces = true
            showCauses = true
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("--release", "25"))
    options.encoding = "UTF-8"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

application {
    mainClass.set("dev.barebones.commander.bootstrap.Main")
}

dependencies {
    implementation(libs.jcommander)
    implementation(libs.flatlaf)

    compileOnly(libs.jsr305)

    // Compile-time deps for the new Bootstrap launcher in
    // src/main/java/dev/barebones/commander/bootstrap/. Bootstrap.java
    // discovers every other module's static register() entry point at
    // runtime via Class.forName, so non-commons-file subprojects only need
    // to be on the runtime classpath.
    implementation(project(":barebones-commons-file"))
    runtimeOnly(project(":barebones-core"))
    runtimeOnly(project(":barebones-command"))
    runtimeOnly(project(":barebones-commons-collections"))
    runtimeOnly(project(":barebones-commons-conf"))
    runtimeOnly(project(":barebones-commons-io"))
    runtimeOnly(project(":barebones-commons-runtime"))
    runtimeOnly(project(":barebones-core-preload"))
    runtimeOnly(project(":apache-bzip2"))
    runtimeOnly(project(":barebones-encoding"))
    runtimeOnly(project(":barebones-preferences"))
    runtimeOnly(project(":barebones-process"))
    runtimeOnly(project(":barebones-translator"))
    runtimeOnly(project(":barebones-protocol-sftp"))
    runtimeOnly(project(":barebones-protocol-nfs"))
    runtimeOnly(project(":barebones-format-zip"))
    runtimeOnly(project(":barebones-format-tar"))
    runtimeOnly(project(":barebones-format-bzip2"))
    runtimeOnly(project(":barebones-format-gzip"))
    runtimeOnly(project(":barebones-format-xz"))
    runtimeOnly(project(":barebones-archiver"))
    runtimeOnly(project(":barebones-viewer-text"))
    runtimeOnly(project(":barebones-os-api"))
    runtimeOnly(project(":barebones-os-macos"))
    runtimeOnly(project(":barebones-os-linux"))
}

val revision: String = if (project.hasProperty("revision")) {
    project.property("revision").toString()
} else {
    val git = org.ajoberstar.grgit.Grgit.open(mapOf("dir" to project.rootDir))
    git.head().id.substring(0, 7)
}
val arch: String = if (project.hasProperty("arch")) project.property("arch").toString()
                  else System.getProperty("os.arch")
val identity: String = if (project.hasProperty("identity")) project.property("identity").toString() else ""

extra["revision"] = revision
extra["arch"] = arch
extra["identity"] = identity

val buildDate: String = SimpleDateFormat("yyyyMMdd").format(Date())

tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(
            "Main-Class" to "dev.barebones.commander.bootstrap.Main",
            "Specification-Title" to "barebones-commander",
            "Specification-Vendor" to "barebones-commander contributors",
            "Specification-Version" to project.version,
            "Implementation-Title" to "barebones-commander",
            "Implementation-Vendor" to "barebones-commander contributors",
            "Implementation-Version" to revision,
            "Build-Date" to buildDate,
        )
    }
}

tasks.named<Test>("test") {
    useTestNG()
}

// Single fat-jar packaging for `java -jar barebones-commander.jar`.
// Bundles everything on the runtime classpath into the root jar.
tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Builds a single self-contained jar with all runtime deps."
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(
            "Main-Class" to "dev.barebones.commander.bootstrap.Main",
            "Specification-Title" to "barebones-commander",
            "Specification-Vendor" to "barebones-commander contributors",
            "Specification-Version" to project.version,
            "Implementation-Title" to "barebones-commander",
            "Implementation-Vendor" to "barebones-commander contributors",
            "Implementation-Version" to revision,
            "Build-Date" to buildDate,
        )
    }
    from(sourceSets.main.get().output)
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
}
