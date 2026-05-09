import java.text.SimpleDateFormat
import java.util.Date

plugins {
    java
    application
    alias(libs.plugins.grgit)
    alias(libs.plugins.cyclonedx)
    alias(libs.plugins.spotbugs)
    alias(libs.plugins.dependencycheck)
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
    apply(plugin = "com.github.spotbugs")
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
    runtimeOnly(project(":barebones-protocol-s3"))
    runtimeOnly(project(":barebones-mount-helper"))
    runtimeOnly(project(":barebones-tailscale"))
    runtimeOnly(project(":barebones-secret-store"))
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

// CycloneDX SBOM published per release. The plugin writes
// build/reports/bom.json + bom.xml against the runtime classpath of
// the root project, which is the fat-jar's actual contents.
tasks.named<org.cyclonedx.gradle.CycloneDxTask>("cyclonedxBom") {
    setProjectType("application")
    setIncludeConfigs(listOf("runtimeClasspath"))
    setSchemaVersion("1.5")
    setOutputFormat("all")
    setOutputName("bom")
    setIncludeBomSerialNumber(true)
}

// SpotBugs + FindSecBugs across every Java subproject. Reports
// HIGH-confidence findings to SARIF (uploaded to GitHub Code
// Scanning by the CI workflow) and HTML, and **fails the build on
// any finding** that isn't suppressed by config/spotbugs/exclude.xml.
//
// The exclude file holds the Phase-9 baseline — 95 (source, pattern)
// pairs that pre-existed in the brownfield muCommander code. Each
// suppression is a real bug to fix in a follow-up; deleting a line
// from the exclude file surfaces the underlying finding.
allprojects {
    plugins.withId("com.github.spotbugs") {
        dependencies {
            add("spotbugsPlugins", rootProject.libs.findsecbugs)
        }
        extensions.configure<com.github.spotbugs.snom.SpotBugsExtension> {
            toolVersion.set("4.9.6")
            effort.set(com.github.spotbugs.snom.Effort.MAX)
            reportLevel.set(com.github.spotbugs.snom.Confidence.HIGH)
            ignoreFailures.set(false)
            excludeFilter.set(rootProject.file("config/spotbugs/exclude.xml"))
        }
        tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
            reports.create("sarif") { required.set(true) }
            reports.create("html")  { required.set(true) }
        }
    }
}

// OWASP Dependency-Check for the application's runtime classpath.
// Run weekly via .github/workflows/dependency-check.yaml; not wired
// to PR builds because the NVD download is slow and a PR shouldn't
// fail because of an unrelated CVE drop.
//
// `nvd.apiKey` is read from the NVD_API_KEY env var by the
// dependency-check plugin itself when the property isn't set; we
// don't shim a fallback here. CI sets the env var from a repo
// secret; a missing key just means slower NVD downloads.
dependencyCheck {
    failBuildOnCVSS = 7.0f
    formats = listOf("HTML", "JSON", "SARIF")
    scanConfigurations = listOf("runtimeClasspath")
    suppressionFile = rootProject.file("config/dependency-check/suppression.xml").absolutePath
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
