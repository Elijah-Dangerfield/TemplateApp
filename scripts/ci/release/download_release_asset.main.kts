#!/usr/bin/env kotlin

@file:DependsOn("org.kohsuke:github-api:1.125")

import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.net.URL

val red = "\u001b[31m"
val green = "\u001b[32m"
val reset = "\u001b[0m"

fun printRed(text: String) {
    println(red + text + reset)
}

fun printGreen(text: String) {
    println(green + text + reset)
}

/**
 * This script is used by our ci to download the aab (fallback to apk) file from the release tied to the given
 * tag name (we have 1 tag per release).
 *
 * When this file is finished downloading we store its location in the provided outputEnvFile with a given outKey
 *
 * Other scripts can then use the outputEnvFile and key to obtain the file location.
 */
@Suppress("MagicNumber")
fun main() {
    val githubRepoInfo = args[0] // in the format: "REPO_OWNER/REPO_NAME"
    val githubToken = args[1]
    val tagName = args[2]
    val outPutEnvFile = File(args[3])
    val outKey = args[4]

    val aabContentType = "application/vnd.android.package-archive"
    val repo = getRepository(githubRepoInfo, githubToken)
    val release = repo.getReleaseByTagName(tagName)

    val aabReleaseAsset = release.listAssets().firstOrNull {
        it.contentType == aabContentType && it.name.contains("release") && it.name.contains("aab")
    }

    val apkReleaseAsset = release.listAssets().firstOrNull {
        it.contentType == aabContentType && it.name.contains("release") && it.name.contains("apk")
    }

    val releaseAsset = aabReleaseAsset ?: apkReleaseAsset

    check(releaseAsset != null) {
        "No release asset could found in release with tag name $tagName"
    }

    val url = URL(releaseAsset.browserDownloadUrl)
    val inputStream = url.openStream()
    val file = File(releaseAsset.name)
    val outputStream = FileOutputStream(file)
    inputStream.use { it.copyTo(outputStream) }
    outputStream.close()

    printGreen("release asset downloaded: ${file.name}")

    FileWriter(outPutEnvFile, true).let {
        it.write("$outKey=${file.path}")
        it.close()
    }
}

fun getRepository(githubRepoInfo: String, githubToken: String): GHRepository =
    GitHub.connectUsingOAuth(githubToken).getRepository(githubRepoInfo)

main()
