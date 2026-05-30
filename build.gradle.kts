plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "com.regularizacion"
version = "1.0.0"

description = "Regularizacion Topicos Programacion: sistema JavaFX con Firebase para ordenes de reparacion y mantenimiento de una agencia automotriz."

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

javafx {
    version = "21.0.5"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("com.regularizacion.topicosprogramacion.Main")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

dependencies {
    implementation("com.google.firebase:firebase-admin:9.4.3")
    implementation("org.apache.pdfbox:pdfbox:3.0.3")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}
