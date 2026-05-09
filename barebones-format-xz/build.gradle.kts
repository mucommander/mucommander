repositories.mavenCentral()

dependencies {
    api(project(":barebones-commons-file"))

    implementation(libs.xz)

    testImplementation(libs.testng)
}
