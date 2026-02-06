package com.emlang.intellij

import com.emlang.intellij.filetype.EmlangFileType
import com.emlang.intellij.filetype.EmlangFileTypeDetector
import com.intellij.openapi.util.io.ByteSequence
import com.intellij.openapi.vfs.VirtualFile
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class EmlangFileTypeDetectorTest {

    private lateinit var detector: EmlangFileTypeDetector

    @Before
    fun setUp() {
        detector = EmlangFileTypeDetector()
    }

    private fun createMockFile(extension: String): VirtualFile {
        val file = mock(VirtualFile::class.java)
        `when`(file.extension).thenReturn(extension)
        return file
    }

    private fun createByteSequence(content: String): ByteSequence {
        val bytes = content.toByteArray()
        return object : ByteSequence {
            override fun subSequence(start: Int, end: Int): ByteSequence {
                val subBytes = bytes.copyOfRange(start, end)
                return object : ByteSequence {
                    override fun subSequence(start: Int, end: Int): ByteSequence = this
                    override fun length(): Int = subBytes.size
                    override fun byteAt(index: Int): Byte = subBytes[index]
                    override fun toBytes(): ByteArray = subBytes
                }
            }
            override fun length(): Int = bytes.size
            override fun byteAt(index: Int): Byte = bytes[index]
            override fun toBytes(): ByteArray = bytes
        }
    }

    @Test
    fun `detect returns EmlangFileType for yaml file starting with slices`() {
        val file = createMockFile("yaml")
        val content = "slices:\n  - name: test"
        val bytes = createByteSequence(content)

        val result = detector.detect(file, bytes, content)

        assertEquals(EmlangFileType.INSTANCE, result)
    }

    @Test
    fun `detect returns EmlangFileType for yml file starting with slices`() {
        val file = createMockFile("yml")
        val content = "slices:\n  - name: test"
        val bytes = createByteSequence(content)

        val result = detector.detect(file, bytes, content)

        assertEquals(EmlangFileType.INSTANCE, result)
    }

    @Test
    fun `detect returns EmlangFileType for emlang file starting with slices`() {
        val file = createMockFile("emlang")
        val content = "slices:\n  - name: test"
        val bytes = createByteSequence(content)

        val result = detector.detect(file, bytes, content)

        assertEquals(EmlangFileType.INSTANCE, result)
    }

    @Test
    fun `detect ignores comments before slices`() {
        val file = createMockFile("yaml")
        val content = "# This is a comment\n# Another comment\nslices:\n  - name: test"
        val bytes = createByteSequence(content)

        val result = detector.detect(file, bytes, content)

        assertEquals(EmlangFileType.INSTANCE, result)
    }

    @Test
    fun `detect ignores empty lines before slices`() {
        val file = createMockFile("yaml")
        val content = "\n\n\nslices:\n  - name: test"
        val bytes = createByteSequence(content)

        val result = detector.detect(file, bytes, content)

        assertEquals(EmlangFileType.INSTANCE, result)
    }

    @Test
    fun `detect returns null for yaml file not starting with slices`() {
        val file = createMockFile("yaml")
        val content = "name: test\nversion: 1.0"
        val bytes = createByteSequence(content)

        val result = detector.detect(file, bytes, content)

        assertNull(result)
    }

    @Test
    fun `detect returns null for non-yaml extension`() {
        val file = createMockFile("txt")
        val content = "slices:\n  - name: test"
        val bytes = createByteSequence(content)

        val result = detector.detect(file, bytes, content)

        assertNull(result)
    }

    @Test
    fun `detect returns null for null extension`() {
        val file = mock(VirtualFile::class.java)
        `when`(file.extension).thenReturn(null)
        val content = "slices:\n  - name: test"
        val bytes = createByteSequence(content)

        val result = detector.detect(file, bytes, content)

        assertNull(result)
    }

    @Test
    fun `detect returns null for null content`() {
        val file = createMockFile("yaml")
        val bytes = createByteSequence("")

        val result = detector.detect(file, bytes, null)

        assertNull(result)
    }

    @Test
    fun `detect handles mixed case extension`() {
        val file = createMockFile("YAML")
        val content = "slices:\n  - name: test"
        val bytes = createByteSequence(content)

        val result = detector.detect(file, bytes, content)

        assertEquals(EmlangFileType.INSTANCE, result)
    }

    @Test
    fun `detect handles slices with spaces`() {
        val file = createMockFile("yaml")
        val content = "  slices:\n  - name: test"
        val bytes = createByteSequence(content)

        val result = detector.detect(file, bytes, content)

        assertEquals(EmlangFileType.INSTANCE, result)
    }
}
