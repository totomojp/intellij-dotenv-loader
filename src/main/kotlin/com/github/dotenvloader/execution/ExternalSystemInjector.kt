package com.github.dotenvloader.execution

import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration

internal class ExternalSystemInjector : RunConfigInjector {
    override fun canInject(runProfile: Any) =
        runProfile is ExternalSystemRunConfiguration

    override fun inject(runProfile: Any, envVars: Map<String, String>): (() -> Unit)? {
        runProfile as ExternalSystemRunConfiguration
        val original = runProfile.settings.env.toMap()
        val merged = original.toMutableMap()
        for ((k, v) in envVars) if (!merged.containsKey(k)) merged[k] = v
        runProfile.settings.env = merged
        return { runProfile.settings.env = original }
    }
}
