package com.emlang.intellij.editor

import com.emlang.intellij.filetype.EmlangFileType
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class EmlangEditorProvider : FileEditorProvider, DumbAware {

    override fun accept(project: Project, file: VirtualFile): Boolean {
        return file.fileType == EmlangFileType.INSTANCE
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        val textEditor = TextEditorProvider.getInstance().createEditor(project, file) as TextEditor
        val previewPanel = EmlangPreviewPanel(project, file)
        return EmlangSplitEditor(textEditor, previewPanel)
    }

    override fun getEditorTypeId(): String = "emlang-split-editor"

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
}
