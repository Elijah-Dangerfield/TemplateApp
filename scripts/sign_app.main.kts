#!/usr/bin/env kotlin

import java.io.File
import java.io.FileWriter

val red = "\u001b[31m"
val green = "\u001b[32m"
val reset = "\u001b[0m"
val yellow = "\u001b[33m"

fun printYellow(text: String) {
    println(yellow + text + reset)
}

fun printRed(text: String) {
    println(red + text + reset)
}

fun printGreen(text: String) {
    println(green + text + reset)
}

val isHelpCall = args.isNotEmpty() && (args[0] == "-h" || args[0].contains("help"))
@Suppress("MaxLineLength")
if (isHelpCall) {
    printGreen(
        """
            This script signs the app based on the apk or aab with a given path. This is mainly used by our CI. 
            
            If you are running this locally you will need to get the key store info from someone with access. 
            
            and make sure that the gradle.property releaseDebugSigningEnabled is set to false otherwise the build
            will be debug signed
            
            if you are setting this up to get the keystore info from github you can follow: 
            https://stefma.medium.com/how-to-store-a-android-keystore-safely-on-github-actions-f0cef9413784
            
            Usage: ./sign_app.main.kts [assetPath] [keyStoreFile] [storePassword] [keyAlias] [keyPassword] [outputFileName] [outputKey*] [envFile*]
            [assetPath] - path to apk or aab to sign
            [keyStoreFile] - the path to the key store file
            [storePassword] - password for keystore
            [keyAlias] - alias for keystore
            [keyPassword] - the signing key
            [outputFileName] - the name of the resulting signed asset
            [outputKey*] - OPTIONAL, if using env output the key in the key value pairing for output to the ENV file
            [envFile*] - OPTIONAL. the env file to output the final path to the signed apk. 
        """.trimIndent()
    )

    @Suppress("TooGenericExceptionThrown")
    throw Exception("See Message Above")
}

@Suppress("ThrowsCount", "MagicNumber")
fun main() {
    val assetPath = args[0]
    val keyStorePath = args[1]
    val storePassword = args[2]
    val keyAlias = args[3]
    val keyPassword = args[4]
    val outputFileName = args[5]
    val outputKeyName = args.getOrNull(6)
    val envFile = args.getOrNull(7)?.let { File(it) }

    val keystoreFile = File(keyStorePath)

    check(!assetPath.contains("debugsigned")) {
        """
            This asset is already signed by a debug signing config. 
            
            Please make sure that the gradle.property releaseDebugSigningEnabled is set to false BEFORE
            assembling the release. 
            
            Otherwise all release builds will automatically be signed with the debug signing config
        """.trimIndent()
    }

    val assetFile = File(assetPath).also { if (!it.isFile) throw FileDoesNotExistError(it.absolutePath) }

    val signedFilePath = when (assetFile.extension) {
        "apk" -> signApk(keystoreFile, storePassword, keyAlias, keyPassword, assetFile, outputFileName)
        "aab" -> signAab(keystoreFile, storePassword, keyAlias, keyPassword, assetFile, outputFileName)
        else -> throw FileExtensionError(assetFile.extension)
    }

    if (outputKeyName != null && envFile != null) {
        writePathToEnv(signedFilePath, outputKeyName, envFile)
    }
}

@Suppress("LongParameterList")
fun signApk(
    keystoreFile: File,
    keystorePassword: String,
    keyAlias: String,
    keyPassword: String,
    inputFile: File,
    outputFileName: String
): String {

    val outputFile = File(inputFile.parent, outputFileName)

    val alignedApkFile = File(inputFile.parent, inputFile.name.replace(".apk", "-aligned.apk"))

    val buildToolsPath = getBuildToolsPath()

    // zip apk first, makes resulting app more efficient
    // https://developer.android.com/topic/performance/reduce-apk-size
    runCommandLine("$buildToolsPath/zipalign", "-f", "-v", "4", inputFile.path, alignedApkFile.path)

    printGreen("Signing APK")

    val command = listOf(
        "$buildToolsPath/apksigner",
        "sign",
        "--ks", keystoreFile.path,
        "--ks-key-alias", keyAlias,
        "--ks-pass", "pass:$keystorePassword",
        "--key-pass", "pass:$keyPassword",
        "--out", outputFile.path,
        alignedApkFile.path
    )

    val output = runCommandLine(command)

    if (output.contains("Error") || output.contains("Exception")) {
        printRed(
            """
            README: The signing of the APK did not succeed. 
            Please make sure that the gradle.property releaseDebugSigningEnabled is set to false BEFORE
            assembling the release. 
            Otherwise all release builds will automatically be signed with the debug signing config
            """.trimIndent()
        )
    } else {
        printGreen("APK Signed. You will find it under ${inputFile.parent}")
    }

    return outputFile.path
}

@Suppress("LongParameterList")
fun signAab(
    keystoreFile: File,
    keystorePassword: String,
    keyAlias: String,
    keyPassword: String,
    inputFile: File,
    outputFileName: String
): String {

    printGreen("Signing AAB")

    val command = listOf(
        "jarsigner",
        "-keystore", keystoreFile.path,
        "-storepass", keystorePassword,
        "-keypass", keyPassword,
        inputFile.absolutePath,
        keyAlias
    )

    val output = runCommandLine(command)

    if (output.contains("Error") || output.contains("Exception")) {
        printRed(
            """
            README: The signing of the AAB did not succeed. 
            Please make sure that the gradle.property releaseDebugSigningEnabled is set to false BEFORE
            building the release. 
            Otherwise all release builds will automatically be signed with the debug signing config
            """.trimIndent()
        )
    } else {
        printGreen("AAB Signed. You will find it under ${inputFile.parent}")
    }

    val renamedFile = File(inputFile.parent, outputFileName)
    inputFile.renameTo(renamedFile)
    return renamedFile.path
}

fun getBuildToolsPath(): String {
    if (System.getenv("ANDROID_HOME").isEmpty()) {
        installAndroidTools()
    }

    val androidHome = System.getenv("ANDROID_HOME")

    val buildToolVersion = File("$androidHome/build-tools")
        .listFiles { child -> child.isDirectory }
        ?.map { it.name }
        ?.maxByOrNull { it }

    printGreen("Found build tools version of $buildToolVersion")

    check(buildToolVersion != null) { "No build tools version could be found" }

    return "$androidHome/build-tools/$buildToolVersion"
}

fun installAndroidTools() = when {
    runCatching { runCommandLine("apt-get") }.isSuccess -> "apt-get -y install android-sdk-build-tools"
    runCatching { runCommandLine("yum") }.isSuccess -> "yum install android-tools"
    runCatching { runCommandLine("brew") }.isSuccess -> "brew install androidtool"
    else -> throw IllegalStateException(
        """
        No package manager could be found on this system to install android tools
        """.trimIndent()
    )
}.let { command -> runCommandLine(command) }

class FileDoesNotExistError(path: String) : Exception("The file $path does not exist.")

class FileExtensionError(ext: String) : Exception("File ext $ext does not match aab or apk. ")

fun runCommandLine(vararg commands: String) = runCommandLine(commands.toList())

@Suppress("SpreadOperator")
fun runCommandLine(command: String) = runCommandLine(command.split("\\s".toRegex()).toTypedArray().toList())

@Suppress("SpreadOperator")
fun runCommandLine(command: List<String>): String {
    val process = ProcessBuilder(command)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    val output = process.inputStream.bufferedReader().readText()
    val error = process.errorStream.bufferedReader().readText()

    if (error.isNotEmpty()) {
        printRed("\n\n$error\n\n")
        if (error.contains("Error:") || error.contains("error:")) {
            throw IllegalStateException(error)
        }
    }

    if (output.isNotEmpty()) {
        printYellow("\n\n$output\n\n")
        if (output.contains("Error:") || output.contains("error:")) {
            throw IllegalStateException(error)
        }
    }

    process.waitFor()

    return output + error
}

fun writePathToEnv(path: String, outPutName: String, envFile: File) {
    val writer = FileWriter(envFile, true)
    writer.write("$outPutName=$path")
    writer.write("\n")
    writer.close()
}

main()
