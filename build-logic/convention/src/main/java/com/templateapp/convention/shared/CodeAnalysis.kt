package com.templateapp.convention.shared

internal object CodeAnalysis {

    const val reportPath = "build/reports/codestyle"

    /**
     * Sources to exclude from code analysis
     */
    val excludes = arrayOf("build/")
}
