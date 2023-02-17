package com.templateapp.convention.shared

/**
 * Utility to check if the current build is either executed on CI
 * https://docs.github.com/en/actions/learn-github-actions/environment-variables#default-environment-variables
 *
 */
object BuildEnvironment {
    val isCIBuild = System.getenv("CI") == "true"
}
