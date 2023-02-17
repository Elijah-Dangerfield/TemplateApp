
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

class AndroidCheckstyleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {

        with(target) {
            pluginManager.apply("checkstyle")
            extensions.getByType<CheckstyleExtension>().apply {
                toolVersion = "8.13"
                isIgnoreFailures = false
                maxWarnings = 0
            }

            @Suppress("SpreadOperator")
            tasks.withType(Checkstyle::class) {
                classpath = files()
                exclude("**/build/**")

                reports {
                    html.required.set(true)
                    html.outputLocation.set(file("build/reports/codestyle/checkstyle.html"))
                    xml.required.set(true)
                    xml.outputLocation.set(file("build/reports/codestyle/checkstyle.xml"))
                }
            }

            /**
             * Runs checkstyle for all Java and XML files.
             * Kotlin files are not supported by Checkstyle.
             */
            tasks.register<Checkstyle>("checkstyleAll") {
                group = "verification"
                description = "Check Java code style for all files."
                source(project.projectDir)
                include("**/java/**/*.java", "**/res/**/*.xml")
            }
        }
    }
}
