
plugins {
    `kotlin-dsl`
}

group = "com.dangerfield.templateapp.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:${libs.versions.detekt.get()}")
    implementation("com.google.firebase:firebase-admin:9.1.1")
    implementation("com.google.gms:google-services:4.3.14")
}

gradlePlugin {
    plugins {
        register("androidApplicationCompose") {
            id = "templateapp.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidApplication") {
            id = "templateapp.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "templateapp.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "templateapp.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "templateapp.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }

        register("androidTest") {
            id = "templateapp.android.test"
            implementationClass = "AndroidTestConventionPlugin"
        }
        register("androidHilt") {
            id = "templateapp.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }

        register("androidDetekt") {
            id = "templateapp.android.detekt"
            implementationClass = "AndroidDetektConventionPlugin"
        }

        register("androidCheckstyle") {
            id = "templateapp.android.checkstyle"
            implementationClass = "AndroidCheckstyleConventionPlugin"
        }
    }
}
