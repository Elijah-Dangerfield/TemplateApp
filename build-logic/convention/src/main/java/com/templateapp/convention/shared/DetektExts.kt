package com.templateapp.convention.shared
import org.gradle.api.Project

@Suppress("ObjectPropertyName") private var _detektVersion: String? = null
val Project.detektVersion get() = _detektVersion ?: getLibVersion("detekt").also { _detektVersion = it }
