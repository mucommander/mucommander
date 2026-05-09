repositories.mavenCentral()

dependencies {
    api(project(":barebones-commons-util"))
    api(project(":barebones-preferences"))
    api(project(":barebones-translator"))

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
