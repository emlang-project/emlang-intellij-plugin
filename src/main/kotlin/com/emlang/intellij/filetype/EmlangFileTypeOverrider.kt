package com.emlang.intellij.filetype

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.impl.FileTypeOverrider
import com.intellij.openapi.vfs.VirtualFile
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class EmlangFileTypeOverrider : FileTypeOverrider {

    companion object {
        private val YAML_EXTENSIONS = setOf("yaml", "yml")
        private const val SLICES_PREFIX = "slices:"
        private const val MAX_LINES_TO_CHECK = 50
    }

    override fun getOverriddenFileType(file: VirtualFile): FileType? {
        if (!file.isValid || file.isDirectory) return null

        val extension = file.extension?.lowercase() ?: return null
        if (extension !in YAML_EXTENSIONS) return null

        return try {
            val found = BufferedReader(InputStreamReader(file.inputStream, file.charset)).use { reader ->
                var linesRead = 0
                var line = reader.readLine()
                while (line != null && linesRead < MAX_LINES_TO_CHECK) {
                    linesRead++
                    val trimmed = line.trim()
                    if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed == "---" || trimmed == "...") {
                        line = reader.readLine()
                        continue
                    }
                    return@use trimmed.startsWith(SLICES_PREFIX)
                }
                false
            }
            if (found) EmlangFileType.INSTANCE else null
        } catch (_: IOException) {
            null
        }
    }
}
