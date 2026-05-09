repositories.mavenCentral()

dependencies {
    api(project(":barebones-commons-file"))
    api(project(":apache-bzip2"))

    testImplementation(libs.testng)
}
