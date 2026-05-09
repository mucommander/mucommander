repositories.mavenCentral()

dependencies {
    api(project(":barebones-core"))
    api(project(":barebones-commons-file"))
    api(project(":barebones-viewer-api"))
    api(project(":barebones-os-api"))
    api(project(":barebones-translator"))
    api(project(":barebones-encoding"))
    api(project(":barebones-preferences"))

    compileOnly(libs.jsr305)
    implementation(libs.rsyntaxtextarea)

    testImplementation(libs.testng)
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
