package com.templateapp.convention.shared

import com.android.build.api.dsl.VariantDimension
import org.gradle.api.provider.Provider

/**
 * Simplify adding BuildConfig fields to build variants
 */
@Suppress("UnstableApiUsage")
fun VariantDimension.buildConfigField(name: String, value: Any?) {
    when (value) {
        null -> buildConfigField("String", name, "null")
        is String -> buildConfigField("String", name, "\"$value\"")
        is Boolean -> buildConfigField("boolean", name, value.toString())
        is Int -> buildConfigField("int", name, value.toString())
        is Provider<*> -> buildConfigField(name, value.get())
        else -> throw IllegalArgumentException("Unknown type for $value")
    }
}
