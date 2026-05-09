repositories.mavenCentral()

dependencies {
    api(project(":barebones-commons-file"))
    api(project(":barebones-preferences"))

    implementation(libs.snakeyaml)

    testImplementation(libs.testng)
}
