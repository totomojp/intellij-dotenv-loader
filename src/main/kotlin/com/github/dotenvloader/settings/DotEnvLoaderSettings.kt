package com.github.dotenvloader.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@State(
    name = "DotEnvLoaderSettings",
    storages = [Storage("dotenv-loader.xml")]
)
class DotEnvLoaderSettings :
    SimplePersistentStateComponent<DotEnvLoaderSettings.State>(State()) {

    class State : BaseState() {
        var enabled by property(true)
        var envFilePath by string(".env")
    }

    companion object {
        fun getInstance(project: Project): DotEnvLoaderSettings {
            return project.getService(DotEnvLoaderSettings::class.java)
        }
    }
}
