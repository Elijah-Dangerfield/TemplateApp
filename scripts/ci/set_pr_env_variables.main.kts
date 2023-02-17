#!/usr/bin/env kotlin

@file:DependsOn("com.google.code.gson:gson:2.8.6")
@file:Import("util/GithubActionsUtil.main.kts")

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.OutputStreamWriter
import java.util.Properties

val red = "\u001b[31m"
val green = "\u001b[32m"
val reset = "\u001b[0m"

fun printRed(text: String) {
    println(red + text + reset)
}

fun printGreen(text: String) {
    println(green + text + reset)
}

@Suppress("ComplexCondition", "MagicNumber")
if (args.size < 2 || args[0] == "-h" || args[0] == "--help" || args[0].contains("help")) {
    printRed(
        """
        This script sets env variables for a pr workflow run
        
        usage: ./set_pr_env_variables.main.kts [branch_name] [env_file]
        [branch_name] - branch that triggered the workflow using this script
        [env_file] - env file used to store output of this script 
        [pull_number] - the number of the pull request that triggered this 
        
        """.trimIndent()
    )

    @Suppress("TooGenericExceptionThrown")
    throw Exception("See Message Above")
}

@Suppress("UseCheckOrError", "ThrowsCount")
fun main() {
    val branchName = args[0]
    val envFile = File(args[1])
    val pullNumber = args[2]

    val writer = envFile.writer()

    setReleaseVariables(writer, branchName)
    setReleaseNotes(writer, pullNumber)
    setPullRequestLink(writer, pullNumber)

    writer.close()
}

fun setPullRequestLink(writer: OutputStreamWriter, pullNumber: String) {
    writer.writeEnvValue("pullRequestLink", "https://github.com/Elijah-Dangerfield/templateapp/$pullNumber")
}

@Suppress("MaxLineLength")
fun setReleaseNotes(writer: OutputStreamWriter, pullNumber: String) {
    val releaseNotes = """
        :warning: :warning: :warning: 
        ```diff
        - Please update this release draft with notes about the included changes before publishing.
        ```
        :warning: :warning: :warning:
        
        ## [PR that triggered this draft](https://github.com/Elijah-Dangerfield/templateapp/pull/$pullNumber)
        When you publish, please merge the above Pull Request back into main.
                 
    """.trimIndent()

    val releaseNotesFile = File("release_notes_temp.md").also { it.createNewFile() }

    releaseNotesFile.writer().apply {
        write(releaseNotes)
        close()
    }

    writer.writeEnvValue("releaseNotesFile", releaseNotesFile.path)
}

@Suppress("UseCheckOrError", "ThrowsCount")
fun setReleaseVariables(writer: OutputStreamWriter, branchName: String) {
    // matches "release-words-dashes_underscores-and-num6ers/vx.y.z

    val vXyzMatcher = "v\\d+\\.\\d+\\.\\d+".toRegex()
    val vXyMatcher = "v\\d+\\.\\d+".toRegex()

    val containsReleaseVersion = vXyMatcher.containsMatchIn(branchName) || vXyzMatcher.containsMatchIn(branchName)
    val isRelease = branchName.contains("release") && containsReleaseVersion

    writer.writeEnvValue("isRelease", "$isRelease")

    if (!isRelease) return

    val branchVersion = (vXyzMatcher.find(branchName) ?: vXyMatcher.find(branchName))?.value
        ?: throw IllegalStateException("Branch detected to be a release but could not extract the version.")

    val appVersionName = getVersionName()

    if (!branchVersion.contains(appVersionName)) {
        throw IllegalStateException(
            """
            Branch name lists version as $branchVersion which does not contain the apps version $appVersionName.
            """.trimIndent()
        )
    }

    writer.writeEnvValue("releaseVersion", "$branchVersion")
    writer.writeEnvValue("releaseTagName", "templateapp/$branchVersion")
}

fun getVersionName(): String {
    val properties = Properties()
    val reader = BufferedReader(FileReader("app.properties"))
    properties.load(reader)
    reader.close()

    return properties.getProperty("versionName").toString()
}

main()
