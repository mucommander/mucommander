repositories.mavenCentral()

dependencies {
    api(project(":barebones-commons-file"))
    implementation(libs.json.smart)

    testImplementation(libs.testng)
}
