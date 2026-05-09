repositories.mavenCentral()

evaluationDependsOn(":barebones-commons-file")

val commonsFileTestOutput = project(":barebones-commons-file")
    .extensions.getByType(JavaPluginExtension::class.java)
    .sourceSets["test"].output

dependencies {
    api(project(":barebones-commons-file"))
    api(project(":barebones-protocol-api"))
    api(project(":barebones-translator"))

    implementation(libs.jsch.mwiede)

    testImplementation(libs.testng)
    testImplementation(files(commonsFileTestOutput))
}
