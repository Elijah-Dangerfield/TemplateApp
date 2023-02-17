package com.templateapp.convention.shared

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import java.io.File
import java.util.Properties

@Suppress("UnstableApiUsage")
fun Project.getLibVersion(name: String): String = extensions.getByType<VersionCatalogsExtension>()
    .named("libs")
    .findVersion(name).get()
    .requiredVersion

/**
 * Get array of source path for all modules
 */
fun Project.getModuleSources(vararg excludeModules: String = emptyArray()): Array<String> {
    val sources = mutableListOf<String>()

    rootProject.subprojects.forEach { project ->
        if (!excludeModules.contains(project.name)) {
            val path = project.path.substring(1).replace(":", "/")
            sources.add("$path/src")
        }
    }

    return sources.toTypedArray()
}

/**
 * Configure Compose-specific options
 */
internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension<*, *, *, *>,
) {
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

    commonExtension.apply {
        buildFeatures {
            compose = true
        }

        composeOptions {
            kotlinCompilerExtensionVersion = libs.findVersion("androidxComposeCompiler").get().toString()
        }

        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + buildComposeMetricsParameters()
        }

        dependencies {
            val bom = libs.findLibrary("androidx-compose-bom").get()
            add("implementation", platform(bom))
            add("androidTestImplementation", platform(bom))
        }
    }
}

private fun Project.buildComposeMetricsParameters(): List<String> {
    val metricParameters = mutableListOf<String>()
    val enableMetricsProvider = project.providers.gradleProperty("enableComposeCompilerMetrics")
    val enableMetrics = (enableMetricsProvider.orNull == "true")
    if (enableMetrics) {
        val metricsFolder = File(project.buildDir, "compose-metrics")
        metricParameters.add("-P")
        metricParameters.add(
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" + metricsFolder.absolutePath
        )
    }

    val enableReportsProvider = project.providers.gradleProperty("enableComposeCompilerReports")
    val enableReports = (enableReportsProvider.orNull == "true")
    if (enableReports) {
        val reportsFolder = File(project.buildDir, "compose-reports")
        metricParameters.add("-P")
        metricParameters.add(
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" + reportsFolder.absolutePath
        )
    }
    return metricParameters.toList()
}

fun Project.getVersionName(): String? = loadAppProperty("versionName")

fun Project.getVersionCode(): Int? = loadAppProperty("versionCode").toInt()

@Suppress("TooGenericExceptionCaught")
fun Project.loadAppProperty(property: String): String = Properties().let {
    val file = File(appPropertiesPath)
    it.load(file.inputStream())
    @Suppress("SwallowedException")
    try {
        it.getProperty(property)
    } catch (e: NullPointerException) {
        @Suppress("TooGenericExceptionThrown")
        throw Error(
            """No app property found named: $property. 
                Please make sure this property is listed exactly as \"$property\" 
                in $appPropertiesPath
            """.trimMargin()
        )
    }
}

@Suppress("TooGenericExceptionCaught")
fun Project.loadGradleProperty(property: String): String? = Properties().let {
    val file = File(gradlePropertyPath)
    it.load(file.inputStream())
    runCatching { it.getProperty(property) }.getOrNull()
}

val Project.appPropertiesPath: String
    get() = "$rootDir/app.properties"

val Project.gradlePropertyPath: String
    get() = "$rootDir/gradle.properties"
