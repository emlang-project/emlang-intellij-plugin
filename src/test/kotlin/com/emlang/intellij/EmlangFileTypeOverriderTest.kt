package com.emlang.intellij

import com.emlang.intellij.filetype.EmlangFileType
import com.emlang.intellij.filetype.EmlangFileTypeOverrider
import com.intellij.openapi.vfs.VirtualFile
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

class EmlangFileTypeOverriderTest {

    private lateinit var overrider: EmlangFileTypeOverrider

    @Before
    fun setUp() {
        overrider = EmlangFileTypeOverrider()
    }

    private fun createMockFile(extension: String, content: String): VirtualFile {
        val file = mock(VirtualFile::class.java)
        `when`(file.extension).thenReturn(extension)
        `when`(file.charset).thenReturn(StandardCharsets.UTF_8)
        `when`(file.inputStream).thenReturn(ByteArrayInputStream(content.toByteArray(StandardCharsets.UTF_8)))
        `when`(file.isValid).thenReturn(true)
        `when`(file.isDirectory).thenReturn(false)
        return file
    }

    @Test
    fun `returns EmlangFileType for yaml file starting with slices`() {
        val file = createMockFile("yaml", "slices:\n  - name: test")
        assertEquals(EmlangFileType.INSTANCE, overrider.getOverriddenFileType(file))
    }

    @Test
    fun `returns EmlangFileType for yml file starting with slices`() {
        val file = createMockFile("yml", "slices:\n  - name: test")
        assertEquals(EmlangFileType.INSTANCE, overrider.getOverriddenFileType(file))
    }

    @Test
    fun `ignores comments before slices`() {
        val file = createMockFile("yaml", "# This is a comment\n# Another comment\nslices:\n  - name: test")
        assertEquals(EmlangFileType.INSTANCE, overrider.getOverriddenFileType(file))
    }

    @Test
    fun `ignores empty lines before slices`() {
        val file = createMockFile("yaml", "\n\n\nslices:\n  - name: test")
        assertEquals(EmlangFileType.INSTANCE, overrider.getOverriddenFileType(file))
    }

    @Test
    fun `ignores YAML document start separator before slices`() {
        val file = createMockFile("yaml", "---\nslices:\n  - name: test")
        assertEquals(EmlangFileType.INSTANCE, overrider.getOverriddenFileType(file))
    }

    @Test
    fun `ignores YAML document end separator before slices`() {
        val file = createMockFile("yaml", "...\nslices:\n  - name: test")
        assertEquals(EmlangFileType.INSTANCE, overrider.getOverriddenFileType(file))
    }

    @Test
    fun `ignores mix of comments, separators and empty lines before slices`() {
        val file = createMockFile("yaml", "---\n# comment\n\n...\n---\nslices:\n  - name: test")
        assertEquals(EmlangFileType.INSTANCE, overrider.getOverriddenFileType(file))
    }

    @Test
    fun `returns null for yaml file not starting with slices`() {
        val file = createMockFile("yaml", "name: test\nversion: 1.0")
        assertNull(overrider.getOverriddenFileType(file))
    }

    @Test
    fun `returns null for non-yaml extension`() {
        val file = mock(VirtualFile::class.java)
        `when`(file.extension).thenReturn("txt")
        `when`(file.isValid).thenReturn(true)
        `when`(file.isDirectory).thenReturn(false)
        assertNull(overrider.getOverriddenFileType(file))
    }

    @Test
    fun `returns null for null extension`() {
        val file = mock(VirtualFile::class.java)
        `when`(file.extension).thenReturn(null)
        `when`(file.isValid).thenReturn(true)
        `when`(file.isDirectory).thenReturn(false)
        assertNull(overrider.getOverriddenFileType(file))
    }

    @Test
    fun `handles mixed case extension`() {
        val file = createMockFile("YAML", "slices:\n  - name: test")
        assertEquals(EmlangFileType.INSTANCE, overrider.getOverriddenFileType(file))
    }

    @Test
    fun `handles slices with leading spaces`() {
        val file = createMockFile("yaml", "  slices:\n  - name: test")
        assertEquals(EmlangFileType.INSTANCE, overrider.getOverriddenFileType(file))
    }

    @Test
    fun `returns null for emlang extension`() {
        val file = mock(VirtualFile::class.java)
        `when`(file.extension).thenReturn("emlang")
        `when`(file.isValid).thenReturn(true)
        `when`(file.isDirectory).thenReturn(false)
        assertNull(overrider.getOverriddenFileType(file))
    }

    @Test
    fun `returns null for invalid file`() {
        val file = mock(VirtualFile::class.java)
        `when`(file.isValid).thenReturn(false)
        `when`(file.isDirectory).thenReturn(false)
        assertNull(overrider.getOverriddenFileType(file))
    }

    @Test
    fun `returns null for directory`() {
        val file = mock(VirtualFile::class.java)
        `when`(file.isValid).thenReturn(true)
        `when`(file.isDirectory).thenReturn(true)
        assertNull(overrider.getOverriddenFileType(file))
    }

    @Test
    fun `returns null when only comments and no content`() {
        val file = createMockFile("yaml", "# just a comment\n# another comment")
        assertNull(overrider.getOverriddenFileType(file))
    }

    @Test
    fun `returns null for empty file`() {
        val file = createMockFile("yaml", "")
        assertNull(overrider.getOverriddenFileType(file))
    }
}
