package com.github.dotenvloader.execution

import com.github.dotenvloader.parser.DotEnvParser
import com.github.dotenvloader.settings.DotEnvLoaderSettings
import com.intellij.execution.RunConfigurationExtension
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.JavaParameters
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.openapi.diagnostic.Logger
import java.io.File

class DotEnvRunConfigurationExtension : RunConfigurationExtension() {

    private val logger = Logger.getInstance(DotEnvRunConfigurationExtension::class.java)

    override fun isApplicableFor(
        configuration: RunConfigurationBase<*>
    ): Boolean = true

    override fun isEnabledFor(
        applicableConfiguration: RunConfigurationBase<*>,
        runnerSettings: RunnerSettings?
    ): Boolean {
        val settings = DotEnvLoaderSettings.getInstance(applicableConfiguration.project)
        return settings.state.enabled
    }

    override fun <T : RunConfigurationBase<*>> updateJavaParameters(
        configuration: T,
        params: JavaParameters,
        runnerSettings: RunnerSettings?
    ) {
        // Environment injection is handled in patchCommandLine
    }

    override fun patchCommandLine(
        configuration: RunConfigurationBase<*>,
        runnerSettings: RunnerSettings?,
        cmdLine: GeneralCommandLine,
        runnerId: String
    ) {
        val project = configuration.project
        val settings = DotEnvLoaderSettings.getInstance(project)

        if (!settings.state.enabled) return

        val envFilePath = settings.state.envFilePath
        if (envFilePath.isNullOrBlank()) return

        val basePath = project.basePath ?: return
        val envFile = File(basePath, envFilePath)

        if (!envFile.exists()) {
            logger.warn("Dotenv Loader: .env file not found at ${envFile.absolutePath}")
            return
        }

        try {
            val envVars = DotEnvParser.parse(envFile)
            val commandLineEnv = cmdLine.environment

            for ((key, value) in envVars) {
                if (!commandLineEnv.containsKey(key)) {
                    commandLineEnv[key] = value
                }
            }

            logger.info("Dotenv Loader: injected ${envVars.size} variables from ${envFile.name}")
        } catch (e: Exception) {
            logger.error("Dotenv Loader: failed to parse ${envFile.absolutePath}", e)
        }
    }
}
