#!/usr/bin/env kotlin

@file:DependsOn("org.kohsuke:github-api:1.125")

import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import java.io.File

val red = "\u001b[31m"
val green = "\u001b[32m"
val reset = "\u001b[0m"

@Suppress("MagicNumber")
val minArgs = 4

fun printRed(text: String) {
    println(red + text + reset)
}

fun printGreen(text: String) {
    println(green + text + reset)
}

val isHelpCall = args.isNotEmpty() && (args[0] == "-h" || args[0].contains("help"))
if (isHelpCall || args.size < minArgs) {
    @Suppress("MaxLineLength")
    printRed(
        """
        This script either creates or updates a draft PR for a release 
        
        Usage: ./update_release_draft.main.kts [GITHUB_REPO] [GITHUB_TOKEN] [TAG_NAME] [BODY] [ASSET_PATHS]
        
        [GITHUB_REPO] - REPO_OWNER/REPO_NAME, provided by github actions as env variable
        [GITHUB_TOKEN] - token to interact with github provided by github actions as env variable or use PAT
        [TAG_NAME] - The name of the tag associated with the draft release created for this PR
        [BODY] - The file path to the release notes
        [Asset Paths] - comma separated list of assets to upload to release draft "example1.apk, example2.apk"
       
        """.trimIndent()
    )

    @Suppress("TooGenericExceptionThrown")
    throw Exception("See Message Above")
}

@Suppress("MagicNumber")
fun main() {
    val githubRepoInfo = args[0] // in the format: "REPO_OWNER/REPO_NAME"
    val githubToken = args[1]
    val tagName = args[2]
    val body = File(args[3]).readText()
    val assetPaths = args.slice(4.until(args.size))

    val repo = getRepository(githubRepoInfo, githubToken)

    val vXyzMatcher = "v\\d+\\.\\d+\\.\\d+".toRegex()
    val vXyMatcher = "v\\d+\\.\\d+".toRegex()

    val releaseTitle = (vXyzMatcher.find(tagName) ?: vXyMatcher.find(tagName))?.value ?: tagName

    val releaseDraft = repo
        .listReleases()
        .firstOrNull { it.tagName == tagName && it.isDraft }
        ?: repo
            .createRelease(tagName)
            .draft(true)
            .body(body)
            .name(releaseTitle)
            .create()

    releaseDraft.listAssets().forEach { it.delete() }

    assetPaths.forEach { path ->
        val asset = File(path)
        releaseDraft.uploadAsset(asset, asset.getContentType())
    }
}

fun File.getContentType() = toURI().toURL().openConnection().contentType

fun getRepository(githubRepoInfo: String, githubToken: String): GHRepository =
    GitHub.connectUsingOAuth(githubToken).getRepository(githubRepoInfo)

main()
