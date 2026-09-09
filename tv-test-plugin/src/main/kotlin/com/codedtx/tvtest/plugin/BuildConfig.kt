package com.codedtx.tvtest.plugin

import java.util.Properties

object BuildConfig {
    val LIBRARY_VERSION: String = run {
        val props = Properties()
        BuildConfig::class.java.classLoader
            ?.getResourceAsStream("version.properties")
            ?.use(props::load)
        props.getProperty("plugin.version", "1.0.0")
    }
}
