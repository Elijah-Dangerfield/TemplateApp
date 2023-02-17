plugins {
    id("templateapp.android.library")
}

android {
    namespace = "com.dangerfield.example"
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.junit)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.google.truth)
}
