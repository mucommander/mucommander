repositories.mavenCentral()

dependencies {
    api(project(":barebones-commons-file"))
    api(project(":barebones-protocol-api"))
    api(project(":barebones-translator"))

    implementation(platform(libs.aws.sdk.bom))
    implementation(libs.aws.sdk.s3)

    testImplementation(libs.testng)
}
