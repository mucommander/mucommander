repositories.mavenCentral()

dependencies {
    api(project(":barebones-commons-io"))
    api(project(":barebones-commons-runtime"))
    api(project(":barebones-commons-util"))

    implementation(libs.jna)
    implementation(libs.jna.platform)
    implementation(libs.commons.collections4)
    implementation(libs.commons.lang3)

    implementation(libs.icu4j)

    testImplementation(libs.testng)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(project(":barebones-format-zip"))
}

tasks.test {
    useTestNG()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
