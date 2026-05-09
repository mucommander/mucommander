repositories.mavenCentral()

dependencies {
    compileOnly(project(":barebones-core-preload"))

    testImplementation(libs.testng)
}
