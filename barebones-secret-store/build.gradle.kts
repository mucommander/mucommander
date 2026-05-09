repositories.mavenCentral()

dependencies {
    api(project(":barebones-commons-file"))

    // JNA for the macOS Security.framework + Linux libsecret bindings.
    // Pinned in the catalog so the version matches the rest of the
    // project (mount-helper, os-macos, etc).
    implementation(libs.jna)
    implementation(libs.jna.platform)

    testImplementation(libs.testng)
}
