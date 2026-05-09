repositories.mavenCentral()

dependencies {
    api(project(":barebones-commons-file"))
    api(project(":barebones-protocol-api"))
    api(project(":barebones-translator"))
    implementation(libs.json.smart)

    testImplementation(libs.testng)
}
