package com.emlang.intellij.settings

import com.emlang.intellij.EmlangBundle
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.io.File
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class EmlangSettingsConfigurable : Configurable {

    private var binaryPathField: TextFieldWithBrowseButton? = null
    private var configPathField: TextFieldWithBrowseButton? = null
    private var autoRefreshCheckbox: JBCheckBox? = null
    private var refreshDelaySpinner: JSpinner? = null
    private var mainPanel: JPanel? = null

    override fun getDisplayName(): String = EmlangBundle.message("settings.display.name")

    override fun createComponent(): JComponent {
        binaryPathField = TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(
                EmlangBundle.message("settings.binary.path.title"),
                EmlangBundle.message("settings.binary.path.description"),
                null,
                FileChooserDescriptorFactory.createSingleFileDescriptor()
            )
        }

        configPathField = TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(
                EmlangBundle.message("settings.config.path.title"),
                EmlangBundle.message("settings.config.path.description"),
                null,
                FileChooserDescriptorFactory.createSingleFileDescriptor("yaml")
            )
        }

        autoRefreshCheckbox = JBCheckBox(EmlangBundle.message("settings.auto.refresh"))

        refreshDelaySpinner = JSpinner(SpinnerNumberModel(500, 100, 5000, 100))

        mainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(
                JBLabel(EmlangBundle.message("settings.binary.path.label")),
                binaryPathField!!,
                1,
                false
            )
            .addLabeledComponent(
                JBLabel(EmlangBundle.message("settings.config.path.label")),
                configPathField!!,
                1,
                false
            )
            .addComponent(autoRefreshCheckbox!!, 1)
            .addLabeledComponent(
                JBLabel(EmlangBundle.message("settings.refresh.delay.label")),
                refreshDelaySpinner!!,
                1,
                false
            )
            .addComponentFillVertically(JPanel(), 0)
            .panel

        return mainPanel!!
    }

    override fun isModified(): Boolean {
        val settings = EmlangSettings.getInstance().state
        return binaryPathField?.text != settings.binaryPath ||
                configPathField?.text != settings.configPath ||
                autoRefreshCheckbox?.isSelected != settings.autoRefresh ||
                (refreshDelaySpinner?.value as? Int) != settings.refreshDelayMs
    }

    override fun apply() {
        val settings = EmlangSettings.getInstance().state
        settings.binaryPath = binaryPathField?.text ?: ""
        settings.configPath = configPathField?.text ?: ""
        settings.autoRefresh = autoRefreshCheckbox?.isSelected ?: true
        settings.refreshDelayMs = (refreshDelaySpinner?.value as? Int) ?: 500
    }

    override fun reset() {
        val settings = EmlangSettings.getInstance().state
        binaryPathField?.text = settings.binaryPath
        configPathField?.text = settings.configPath
        autoRefreshCheckbox?.isSelected = settings.autoRefresh
        refreshDelaySpinner?.value = settings.refreshDelayMs
    }

    override fun disposeUIResources() {
        binaryPathField = null
        configPathField = null
        autoRefreshCheckbox = null
        refreshDelaySpinner = null
        mainPanel = null
    }
}
