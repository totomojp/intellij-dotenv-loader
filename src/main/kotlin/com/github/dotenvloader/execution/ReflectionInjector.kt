package com.github.dotenvloader.execution

import com.intellij.openapi.diagnostic.Logger

@Suppress("UNCHECKED_CAST")
internal class ReflectionInjector : RunConfigInjector {
    private val logger = Logger.getInstance(ReflectionInjector::class.java)

    // サポートする getter/setter ペア (優先順)
    // - getEnvs/setEnvs: CommonRunConfigurationParameters (Python, Node.js 等)
    // - getCustomEnvironment/setCustomEnvironment: GoRunConfigurationBase (Go)
    private val methodPairs = listOf(
        "getEnvs" to "setEnvs",
        "getCustomEnvironment" to "setCustomEnvironment",
    )

    override fun canInject(runProfile: Any): Boolean =
        methodPairs.any { (getter, _) ->
            try { runProfile.javaClass.getMethod(getter); true }
            catch (_: NoSuchMethodException) { false }
        }

    override fun inject(runProfile: Any, envVars: Map<String, String>): (() -> Unit)? {
        for ((getterName, setterName) in methodPairs) {
            try {
                val getter = runProfile.javaClass.getMethod(getterName)
                val setter = runProfile.javaClass.getMethod(setterName, Map::class.java)
                val original = (getter.invoke(runProfile) as? Map<String, String>) ?: emptyMap()
                val merged = original.toMutableMap()
                for ((k, v) in envVars) if (!merged.containsKey(k)) merged[k] = v
                setter.invoke(runProfile, merged)
                logger.info("Dotenv Loader: injected via $getterName into ${runProfile.javaClass.simpleName}")
                return { setter.invoke(runProfile, original) }
            } catch (_: NoSuchMethodException) { /* 次のペアを試みる */ }
            catch (e: Exception) {
                logger.warn("Dotenv Loader: injection failed via $getterName: ${e.message}")
                return null
            }
        }
        return null
    }
}
