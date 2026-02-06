package com.emlang.intellij.filetype

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.impl.FileTypeOverrider
import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException

class EmlangFileTypeOverrider : FileTypeOverrider {

    companion object {
        private val YAML_EXTENSIONS = setOf("yaml", "yml")
        private const val SLICES_PREFIX = "slices:"
        private const val MAX_BYTES_TO_CHECK = 4096
    }

    override fun getOverriddenFileType(file: VirtualFile): FileType? {
        val extension = file.extension?.lowercase() ?: return null
        if (extension !in YAML_EXTENSIONS) return null

        val content = try {
            file.inputStream.use { stream ->
                val bytes = stream.readNBytes(MAX_BYTES_TO_CHECK)
                String(bytes, file.charset)
            }
        } catch (_: IOException) {
            return null
        }

        return if (startsWithSlices(content)) EmlangFileType.INSTANCE else null
    }

    private fun startsWithSlices(content: String): Boolean {
        for (line in content.lineSequence()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            if (trimmed.startsWith("#")) continue
            return trimmed.startsWith(SLICES_PREFIX)
        }
        return false
    }
}
