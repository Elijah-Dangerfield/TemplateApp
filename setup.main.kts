#!/usr/bin/env kotlin

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.Properties

val red = "\u001b[31m"
val green = "\u001b[32m"
val reset = "\u001b[0m"

// purposely including .idea and build src
val doNotCopy = arrayListOf(".git", "README.md", "setup.main.kts", ".gradle", ".idea", "build")

fun printRed(text: String) {
    println(red + text + reset)
}

fun printGreen(text: String) {
    println(green + text + reset)
}

fun main() {
    if (checkForHelpCall()) return

    val appname = args[0]

    if (!isValidAppName(appname)) {
        printRed("The app name $appname is kinda dumb. Pick a better one. No spaces pls")
    }

    val newProject = File("../$appname")
    newProject.mkdir()

    File(".").copyRecursively(newProject, overwrite = true)

    updateTemplateWithAppName(newProject, appname)

    // deleting after the update, as some directories may be added to the doNotCopy
    doNotCopy.forEach {
        val normalizedPath = it.removePrefix(appname)
        val dir = File("$newProject/$normalizedPath")
        if (dir.exists()) dir.deleteRecursively()
    }

    resetVersionCodeAndName("../$appname")

    printGreen(
        """
        A directory named $appname was created  in the parent directory of this template.
        
        Next Steps:
        
        1. Set up signing config for CI: 
           Steps:
           - Generate a key store named release.keystore, write down the alias, key store password and key password. 
             save those as github secrets named KEYALIAS KEYSTORE_PASSWORD and KEY_PASSWORD respectively
           - run: gpg -c --armor release.keystore
           - enter whatever passphrase you want and save it as a github secret under RELEASE_KEYSTORE_PASSPHRASE
           - copy and past the content from the generated asc file into a secret named RELEASE_KEYSTORE
           
           Doing this will allow CI to sign the app.
           
        2. On github under settings -> developer settings -> personal access tokens, create a fine grained
           access token for your repo with access to read and write at least pull requests. 
           Save that token as a secret in the repository named REPO_SCOPED_TOKEN
           
        3. On github under project -> settings -> actions -> general, set the Workflow permissions to be read & write
                   
        """.trimIndent()
    )
}

fun isValidAppName(appname: String): Boolean = appname.isNotEmpty() && !appname.contains(" ")

fun checkForHelpCall(): Boolean {
    val isHelpCall = args.isEmpty() || args[0] == "-h" || args[0].contains("help")
    if (isHelpCall) {
        @Suppress("MaxLineLength")
        printGreen(
            """
               This script sets up a new project based off this template 
               
               Usage: ./setup.main.kts [app name]
               
            """.trimIndent()
        )
    }

    return isHelpCall
}

fun resetVersionCodeAndName(newProjectDir: String) {
    val properties = Properties()
    val reader = BufferedReader(FileReader("$newProjectDir/app.properties"))
    properties.load(reader)
    reader.close()

    properties.setProperty("versionCode", "1")
    properties.setProperty("versionName", "1.0")

    val writer = BufferedWriter(FileWriter("$newProjectDir/app.properties"))
    writer.write(
        """
    # These properties are referenced in: \n
    # AndroidApplicationConventionPlugin.kt  \n
    # and set by out CI workflow -> .github/workflows/create-release..
    # This is to make finding.updating the app properties with ci much easier
    # These values are set by our CI exclusively
    # The version code matches the CI build number, this helps us distinguish between multiple builds of the same
    # version name
    # The version name is set by the set_version_name script which is triggered by a github action
    
        """.trimIndent()
    )
    writer.newLine()
    properties.store(writer, null)
    writer.close()
}

@Suppress("NestedBlockDepth")
fun updateTemplateWithAppName(directory: File, appName: String) {
    val files = directory.listFiles()
    files?.forEach { file ->
        if (file.isDirectory && doNotCopy.contains(file.name)) {
            doNotCopy.add(file.path)
        } else if (file.isDirectory) {
            updateDirectoryName(file, appName)
            updateTemplateWithAppName(file, appName)
        } else if (file.isFile && !doNotCopy.contains(file.name)) {
            val originalContents = file.readText()
            val updatedContents = originalContents
                .replace("templateapp", appName.lowercase())
                .replace("TemplateApp", appName)

            updateFileContents(file, updatedContents, appName)
        }
    }
}

main()

fun updateFileContents(file: File, updatedContents: String, appName: String) {
    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    try {
        file.writeText(updatedContents)
    } catch (e: Exception) {
        printRed(
            """
                    Could not update the file named ${file.name} to replace "templateapp" with "$appName". 
                    you may need to make this update manually. 
            """.trimMargin()
        )
    }
}

fun updateDirectoryName(file: File, appName: String) {
    if (file.name.contains("templateapp") || file.name.contains("TemplateApp")) {
        val newName = file.name
            .replace("templateapp", appName.lowercase())
            .replace("TemplateApp", appName)

        file.renameTo(File("${file.parent}/$newName"))
    }
}
