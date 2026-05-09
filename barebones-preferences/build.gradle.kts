repositories.mavenCentral()

dependencies {
    api(project(":barebones-commons-conf"))
    api(project(":barebones-commons-file"))
    api(project(":barebones-commons-io"))

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
