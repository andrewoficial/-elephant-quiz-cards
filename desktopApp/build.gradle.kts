import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)

    // Провайдер SLF4J, чтобы не было "No SLF4J providers were found"
    runtimeOnly(libs.slf4j.nop)
}

compose.desktop {
    application {
        mainClass = "com.elephantquizcards.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.elephantquizcards"
            packageVersion = "1.0.0"
        }
    }
}