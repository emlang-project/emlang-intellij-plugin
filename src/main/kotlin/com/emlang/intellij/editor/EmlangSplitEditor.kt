package com.emlang.intellij.editor

import com.emlang.intellij.EmlangBundle
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorWithPreview

class EmlangSplitEditor(
    editor: TextEditor,
    private val previewPanel: EmlangPreviewPanel
) : TextEditorWithPreview(
    editor,
    previewPanel,
    EmlangBundle.message("editor.name"),
    Layout.SHOW_EDITOR_AND_PREVIEW
) {
    init {
        // Set up document listener for auto-refresh
        editor.editor.document.addDocumentListener(previewPanel.documentListener)
    }

    override fun dispose() {
        previewPanel.dispose()
        super.dispose()
    }
}
