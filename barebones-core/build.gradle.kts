repositories.mavenCentral()

dependencies {
    compileOnly(project(":barebones-commons-file"))
    compileOnly(project(":barebones-commons-conf"))
    compileOnly(project(":barebones-commons-collections"))
    compileOnly(project(":barebones-commons-io"))
    compileOnly(project(":barebones-archiver"))
    compileOnly(project(":barebones-command"))
    compileOnly(project(":barebones-encoding"))
    compileOnly(project(":barebones-preferences"))
    compileOnly(project(":barebones-process"))
    compileOnly(project(":barebones-translator"))
    compileOnly(project(":barebones-protocol-api"))
    compileOnly(project(":barebones-os-api"))
    compileOnly(project(":barebones-viewer-api"))
    compileOnly(project(":barebones-core-preload"))
    compileOnly(project(":barebones-secret-store"))

    compileOnly(libs.flatlaf)
    compileOnly(libs.jetbrains.annotations)

    implementation(libs.logback.core)
    implementation(libs.logback.classic)
    implementation(libs.commons.collections4)
    implementation(libs.json.smart)

    implementation(libs.unix4j.command)

    testImplementation(libs.testng)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(project(":barebones-commons-file"))
    testImplementation(project(":barebones-commons-conf"))
    testImplementation(project(":barebones-commons-collections"))
    testImplementation(project(":barebones-commons-io"))
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.test {
    useTestNG()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
