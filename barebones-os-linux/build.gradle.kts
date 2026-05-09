repositories.mavenCentral()

dependencies {
    api(project(":barebones-core"))
    api(project(":barebones-os-api"))
    api(project(":barebones-process"))

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
