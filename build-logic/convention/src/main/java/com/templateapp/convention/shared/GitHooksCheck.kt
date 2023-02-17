package com.templateapp.convention.shared

import org.gradle.api.Project

internal fun Project.configureGitHooksCheck() {

    fun checkHooksInstalled() {
        project.rootProject.file("config/git-hooks")
            .also {
                check(it.isDirectory && it.listFiles()?.isNotEmpty() ?: false) {
                    "Git hooks have not been set up yet. Please create them under config/git-hooks."
                }
            }
            .listFiles()
            ?.forEach {
                val installedGitHook = project.rootProject.file(".git/hooks/${it.nameWithoutExtension}")

                check(installedGitHook.isFile || BuildEnvironment.isCIBuild) {
                    """
                        The project requires a ${installedGitHook.name} git hook to be installed. 
                        Either run `./scripts/install-git-hooks.sh` to install the default git hooks 
                        or install your own. Check the GitHooksCheck for more info
                    """.trimIndent()
                }
            }
    }

    project.gradle.projectsEvaluated {
        checkHooksInstalled()
    }
}
