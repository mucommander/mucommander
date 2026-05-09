repositories.mavenCentral()

dependencies {
    api(project(":barebones-commons-file"))
    api(project(":barebones-commons-util"))

    compileOnly(libs.jsr305)

    testImplementation(libs.testng)
}
