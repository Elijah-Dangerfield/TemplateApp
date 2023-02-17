package com.templateapp.convention.shared

const val RED = "\u001b[31m"
const val GREEN = "\u001b[32m"
const val RESET = "\u001b[0m"

fun printRed(text: String) {
    println(RED + text + RESET)
}

fun printGreen(text: String) {
    println(GREEN + text + RESET)
}
