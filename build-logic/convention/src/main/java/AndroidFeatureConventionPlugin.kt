import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("templateapp.android.library")
                apply("templateapp.android.hilt")
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            // Libraries Shared Between All Features
            dependencies {
                add("implementation", libs.findLibrary("androidx.core").get())
                add("implementation", libs.findLibrary("androidx.lifecycle.ext").get())
                add("implementation", libs.findLibrary("androidx.lifecycle.vm").get())
                add("implementation", libs.findLibrary("androidx.fragment.ktx").get())
                add("implementation", libs.findLibrary("androidx.constraintlayout").get())
                add("implementation", libs.findLibrary("androidx.appcompat").get())
            }
        }
    }
}
