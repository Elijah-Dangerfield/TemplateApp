import com.android.build.api.dsl.ApplicationExtension
import com.templateapp.convention.shared.BuildEnvironment
import com.templateapp.convention.shared.SharedConstants
import com.templateapp.convention.shared.buildConfigField
import com.templateapp.convention.shared.configureGitHooksCheck
import com.templateapp.convention.shared.configureKotlinAndroid
import com.templateapp.convention.shared.getVersionCode
import com.templateapp.convention.shared.getVersionName
import com.templateapp.convention.shared.loadGradleProperty
import com.templateapp.convention.shared.printDebugSigningWarningIfNeeded
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<ApplicationExtension> {

                configureKotlinAndroid(this)

                defaultConfig.apply {
                    targetSdk = SharedConstants.targetSdk
                    versionName = getVersionName()
                    versionCode = getVersionCode()
                    buildConfigField("VERSION_CODE", versionCode)
                    buildConfigField("VERSION_NAME", versionName)
                }

                project.afterEvaluate {
                    tasks.getByName("assembleRelease") {
                        doFirst { printDebugSigningWarningIfNeeded() }
                        doLast { printDebugSigningWarningIfNeeded() }
                    }

                    tasks.getByName("bundleRelease") {
                        doFirst { printDebugSigningWarningIfNeeded() }
                        doLast { printDebugSigningWarningIfNeeded() }
                    }
                }

                buildTypes.forEach {

                    val isLocalReleaseBuild = !it.isDebuggable && !BuildEnvironment.isCIBuild
                    val releaseDebugSigningEnabled =
                        loadGradleProperty("com.templateapp.releaseDebugSigningEnabled").toBoolean()

                    if (isLocalReleaseBuild && releaseDebugSigningEnabled) {
                        // set signing config to debug so that devs can test release builds locally without signing
                        it.signingConfig = signingConfigs.getByName("debug")
                        // prefix apk with indicator that the signing is invalid
                        archivesName.set("debugsigned-${archivesName.get()}")
                    }
                }
            }

            configureGitHooksCheck()
        }
    }
}
