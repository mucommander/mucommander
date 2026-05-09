repositories.mavenCentral()

dependencies {
    api(project(":barebones-commons-file"))
    api(project(":apache-bzip2"))

    implementation(libs.commons.compress)

    testImplementation(libs.testng)
}
