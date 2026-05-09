repositories.mavenCentral()

dependencies {
    api(project(":barebones-commons-util"))
    api(project(":barebones-core"))
    api(project(":barebones-os-api"))
    api(project(":barebones-process"))
    api(project(":barebones-translator"))

    implementation(libs.jna.platform)
    implementation(libs.dd.plist)

    // the java.desktop module that contains macOS-specific extensions
    compileOnly(files("libs/java.desktop.jar"))

    testImplementation(libs.testng)
}

tasks.test {
    useTestNG()
}
