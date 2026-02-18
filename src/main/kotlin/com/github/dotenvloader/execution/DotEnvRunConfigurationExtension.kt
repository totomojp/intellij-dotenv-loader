package com.github.dotenvloader.execution

import com.github.dotenvloader.settings.DotEnvLoaderSettings
import com.intellij.execution.RunConfigurationExtension
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.JavaParameters
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.openapi.diagnostic.Logger

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
        val envVars = DotEnvLoader.load(configuration.project) ?: return
        val commandLineEnv = cmdLine.environment
        for ((key, value) in envVars) {
            if (!commandLineEnv.containsKey(key)) {
                commandLineEnv[key] = value
            }
        }
        logger.info("Dotenv Loader: injected ${envVars.size} variables")
    }
}
