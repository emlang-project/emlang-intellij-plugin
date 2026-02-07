package com.emlang.intellij.editor

import com.emlang.intellij.EmlangBundle
import com.emlang.intellij.runner.EmlangRunner
import com.emlang.intellij.settings.EmlangSettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import java.awt.BorderLayout
import java.beans.PropertyChangeListener
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JComponent
import javax.swing.JEditorPane
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingConstants
import javax.swing.Timer

class EmlangPreviewPanel(
    private val project: Project,
    private val file: VirtualFile
) : UserDataHolderBase(), FileEditor, Disposable {

    private val mainPanel = JPanel(BorderLayout())
    private var browser: JBCefBrowser? = null
    private var fallbackPane: JEditorPane? = null
    private val isJcefSupported: Boolean
    private var refreshTimer: Timer? = null
    private val isRefreshing = AtomicBoolean(false)
    private val runner = EmlangRunner()

    val documentListener: DocumentListener = object : DocumentListener {
        override fun documentChanged(event: DocumentEvent) {
            val settings = EmlangSettings.getInstance()
            if (settings.state.autoRefresh) {
                scheduleRefresh(settings.state.refreshDelayMs)
            }
        }
    }

    init {
        isJcefSupported = try {
            JBCefApp.isSupported().also {
                if (it) {
                    browser = JBCefBrowser()
                    mainPanel.add(browser!!.component, BorderLayout.CENTER)
                }
            }
        } catch (e: Exception) {
            false
        }

        if (!isJcefSupported) {
            fallbackPane = JEditorPane().apply {
                contentType = "text/html"
                isEditable = false
            }
            mainPanel.add(JScrollPane(fallbackPane), BorderLayout.CENTER)
        }

        // Initial refresh
        refresh()
    }

    private fun scheduleRefresh(delayMs: Int) {
        refreshTimer?.stop()
        refreshTimer = Timer(delayMs) {
            refresh()
        }.apply {
            isRepeats = false
            start()
        }
    }

    fun refresh() {
        if (!isRefreshing.compareAndSet(false, true)) {
            return
        }

        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val result = runner.runEmlang(file.path)
                val html = if (result.success) {
                    wrapHtmlFragment(result.output)
                } else {
                    createErrorHtml(result.error)
                }

                ApplicationManager.getApplication().invokeLater({
                    updateContent(html)
                    isRefreshing.set(false)
                }, ModalityState.any())
            } catch (e: Exception) {
                ApplicationManager.getApplication().invokeLater({
                    updateContent(createErrorHtml(e.message ?: "Unknown error"))
                    isRefreshing.set(false)
                }, ModalityState.any())
            }
        }
    }

    private fun updateContent(html: String) {
        if (isJcefSupported && browser != null) {
            browser!!.loadHTML(html)
        } else {
            fallbackPane?.text = html
        }
    }

    private fun wrapHtmlFragment(fragment: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>body { background: #ffffff; }</style>
            </head>
            <body>
                $fragment
            </body>
            </html>
        """.trimIndent()
    }

    private fun createErrorHtml(error: String): String {
        val settings = EmlangSettings.getInstance()
        val binaryPath = settings.state.binaryPath

        val message = if (binaryPath.isBlank()) {
            EmlangBundle.message("preview.error.no.binary")
        } else {
            error
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        padding: 20px;
                        color: #cc0000;
                    }
                    .error-container {
                        background: #fff0f0;
                        border: 1px solid #ffcccc;
                        border-radius: 4px;
                        padding: 16px;
                    }
                    h3 { margin-top: 0; }
                    pre {
                        background: #f5f5f5;
                        padding: 10px;
                        border-radius: 4px;
                        overflow-x: auto;
                        white-space: pre-wrap;
                        word-wrap: break-word;
                    }
                </style>
            </head>
            <body>
                <div class="error-container">
                    <h3>${EmlangBundle.message("preview.error.title")}</h3>
                    <pre>${escapeHtml(message)}</pre>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    override fun getComponent(): JComponent = mainPanel

    override fun getPreferredFocusedComponent(): JComponent? = null

    override fun getName(): String = EmlangBundle.message("preview.name")

    override fun setState(state: FileEditorState) {}

    override fun isModified(): Boolean = false

    override fun isValid(): Boolean = file.isValid

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {}

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}

    override fun dispose() {
        refreshTimer?.stop()
        browser?.let { Disposer.dispose(it) }
    }

    override fun getFile(): VirtualFile = file
}
