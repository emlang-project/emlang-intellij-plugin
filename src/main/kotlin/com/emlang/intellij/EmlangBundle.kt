package com.emlang.intellij

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.EmlangBundle"

object EmlangBundle : DynamicBundle(BUNDLE) {
    @JvmStatic
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
        return getMessage(key, *params)
    }
}
