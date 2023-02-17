package com.templateapp.convention.shared

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.provideDelegate
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

/**
 * Configure base Kotlin with Android options
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *>,
) {
    commonExtension.apply {
        compileSdk = SharedConstants.compileSdk

        defaultConfig {
            minSdk = SharedConstants.minSdk
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
            isCoreLibraryDesugaringEnabled = true
        }

        kotlinOptions {
            // Treat all Kotlin warnings as errors (disabled by default)
            // Override by setting warningsAsErrors=true in your ~/.gradle/gradle.properties
            val warningsAsErrors: String? by project
            allWarningsAsErrors = warningsAsErrors.toBoolean()

            freeCompilerArgs = freeCompilerArgs + listOf(
                "-opt-in=kotlin.RequiresOptIn",
                // Enable experimental coroutines APIs, including Flow
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
                "-opt-in=kotlin.Experimental",
                "-Xjvm-default=enable"
            )

            // Set JVM target to 1.8
            jvmTarget = JavaVersion.VERSION_1_8.toString()
        }

        buildFeatures {
            buildConfig = true
            viewBinding = true
        }
    }

    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

    dependencies {
        add("coreLibraryDesugaring", libs.findLibrary("android.desugarJdkLibs").get())
    }
}

fun CommonExtension<*, *, *, *>.kotlinOptions(block: KotlinJvmOptions.() -> Unit) {
    (this as ExtensionAware).extensions.configure("kotlinOptions", block)
}

fun Project.printDebugSigningWarningIfNeeded() {
    val isCi = BuildEnvironment.isCIBuild
    val isLocalReleaseDebugSigningEnabled =
        loadGradleProperty("com.templateapp.releaseDebugSigningEnabled").toBoolean()

    if (!isCi && isLocalReleaseDebugSigningEnabled) {
        printRed(
            """
            This release was signed with a debug signing config.
             
            If you need a genuine signed build then you will need to make sure that the
            gradle.property com.templateapp.releaseDebugSigningEnabled is set to false and then use 
            ./script/sign_app.main.kts to sign the app (which will require the keystore)            
            """.trimIndent()
        )
    }
}
