package com.emlang.intellij.filetype

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.util.io.ByteSequence
import com.intellij.openapi.vfs.VirtualFile

class EmlangFileTypeDetector : FileTypeRegistry.FileTypeDetector {

    companion object {
        private val VALID_EXTENSIONS = setOf("yaml", "yml", "emlang")
        private const val SLICES_PREFIX = "slices:"
        private const val MAX_BYTES_TO_CHECK = 4096
    }

    override fun detect(file: VirtualFile, firstBytes: ByteSequence, firstCharsIfText: CharSequence?): FileType? {
        // Check extension first
        val extension = file.extension?.lowercase() ?: return null
        if (extension !in VALID_EXTENSIONS) {
            return null
        }

        // Check content for "slices:" at the beginning (after comments and empty lines)
        val content = firstCharsIfText?.toString() ?: return null
        if (startsWithSlices(content)) {
            return EmlangFileType.INSTANCE
        }

        return null
    }

    override fun getDesiredContentPrefixLength(): Int = MAX_BYTES_TO_CHECK

    private fun startsWithSlices(content: String): Boolean {
        val lines = content.lineSequence()
        for (line in lines) {
            val trimmed = line.trim()
            // Skip empty lines
            if (trimmed.isEmpty()) {
                continue
            }
            // Skip YAML comments
            if (trimmed.startsWith("#")) {
                continue
            }
            // Check if line starts with "slices:"
            return trimmed.startsWith(SLICES_PREFIX)
        }
        return false
    }
}
