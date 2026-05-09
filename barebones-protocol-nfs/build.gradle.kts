repositories.mavenCentral()

evaluationDependsOn(":barebones-commons-file")

val commonsFileTestOutput = project(":barebones-commons-file")
    .extensions.getByType(JavaPluginExtension::class.java)
    .sourceSets["test"].output

dependencies {
    api(project(":barebones-commons-file"))
    api(project(":barebones-protocol-api"))
    api(project(":barebones-translator"))
    api(project(":sun-net-www"))

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.testng)
    testImplementation(project(":barebones-commons-file"))
    testImplementation(files(commonsFileTestOutput))
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
