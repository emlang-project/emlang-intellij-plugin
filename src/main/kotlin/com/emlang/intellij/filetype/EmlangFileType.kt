package com.emlang.intellij.filetype

import com.emlang.intellij.EmlangBundle
import com.emlang.intellij.EmlangIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import org.jetbrains.yaml.YAMLLanguage
import javax.swing.Icon

class EmlangFileType private constructor() : LanguageFileType(YAMLLanguage.INSTANCE) {

    override fun getName(): String = "Emlang"

    override fun getDescription(): String = EmlangBundle.message("filetype.description")

    override fun getDefaultExtension(): String = "emlang"

    override fun getIcon(): Icon = EmlangIcons.FILE

    companion object {
        @JvmField
        val INSTANCE = EmlangFileType()
    }
}
