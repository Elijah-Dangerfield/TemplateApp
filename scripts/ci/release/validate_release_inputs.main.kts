#!/usr/bin/env kotlin

val red = "\u001b[31m"
val green = "\u001b[32m"
val reset = "\u001b[0m"

fun printRed(text: String) {
    println(red + text + reset)
}

fun printGreen(text: String) {
    println(green + text + reset)
}

if (args.isEmpty() || args[0] == "-h" || args[0].contains("help")) {
    printRed(
        """
        This script validates the inputs to the create_release_branch.yml script 
        
        Usage: ./validate_release_inputs.main.kts [versionName] 
        versionName (required): "1.2.4", "1.5.19", ...
        """.trimIndent()
    )

    @Suppress("TooGenericExceptionThrown")
    throw Exception("See Message Above")
}

val appVersion = args[0]

class ValidationError(validationErrorMessage: String) : Exception(validationErrorMessage)

fun isAppVersionValid(): Boolean {
    // Regular expression to match a version code of the form "x.y.z"
    val xyzMatcher = "\\d+\\.\\d+\\.\\d+".toRegex()
    // Regular expression to match a version code of the form "x.y"
    val xyMatcher = "\\d+\\.\\d+".toRegex()

    return xyzMatcher.matches(appVersion) || xyMatcher.matches(appVersion)
}

val appVersionValidationError = ValidationError(
    """
    The app version "$appVersion" is invalid. 
    Please make sure you enter an app version in the format x.y.z (major, minor, patch [optional])
    """.trimIndent()
)

if (!isAppVersionValid()) throw appVersionValidationError
else printGreen("Good news. The inputs to create the release are valid. ")
