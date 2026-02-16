package com.github.dotenvloader.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*

class DotEnvLoaderConfigurable(private val project: Project) :
    BoundConfigurable("Dotenv Loader") {

    private val settings = DotEnvLoaderSettings.getInstance(project)

    private var envFilePath: String
        get() = settings.state.envFilePath ?: ".env"
        set(value) { settings.state.envFilePath = value }

    override fun createPanel(): DialogPanel = panel {
        group("Dotenv Loader") {
            row {
                checkBox("Enable .env file loading")
                    .bindSelected(settings.state::enabled)
            }
            row("File path:") {
                textFieldWithBrowseButton(
                    "Select .env File",
                    project,
                    FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
                )
                    .bindText(::envFilePath)
                    .columns(COLUMNS_LARGE)
                    .comment("Path relative to project root (e.g., .env)")
            }
        }
    }
}
