package com.github.dotenvloader.execution

import com.github.dotenvloader.parser.DotEnvParser
import com.github.dotenvloader.settings.DotEnvLoaderSettings
import com.intellij.execution.ExecutionListener
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration
import com.intellij.openapi.project.Project
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class DotEnvExecutionListener(private val project: Project) : ExecutionListener {

    private val logger = Logger.getInstance(DotEnvExecutionListener::class.java)

    // ExecutionEnvironment の identity hash → 復元ラムダ
    private val restoreActions = ConcurrentHashMap<Int, () -> Unit>()

    override fun processStartScheduled(executorId: String, env: ExecutionEnvironment) {
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

        val envVars = try {
            DotEnvParser.parse(envFile)
        } catch (e: Exception) {
            logger.error("Dotenv Loader: failed to parse ${envFile.absolutePath}", e)
            return
        }

        val runProfile = env.runProfile
        val envKey = System.identityHashCode(env)

        when (runProfile) {
            is ExternalSystemRunConfiguration -> {
                // Gradle / Maven
                val original = runProfile.settings.env.toMap()
                val merged = original.toMutableMap()
                for ((k, v) in envVars) if (!merged.containsKey(k)) merged[k] = v
                runProfile.settings.env = merged
                restoreActions[envKey] = { runProfile.settings.env = original }
                logger.info("Dotenv Loader: injected ${envVars.size} variables (ExternalSystem) from ${envFile.name}")
            }
            else -> {
                // Go, Python, Node.js 等 — getEnvs/setEnvs を持つ Run Configuration に汎用対応
                injectViaReflection(runProfile, envVars, envKey, envFile.name)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun injectViaReflection(runProfile: Any, envVars: Map<String, String>, envKey: Int, fileName: String) {
        try {
            val getEnvs = runProfile.javaClass.getMethod("getEnvs")
            val setEnvs = runProfile.javaClass.getMethod("setEnvs", Map::class.java)
            val original = (getEnvs.invoke(runProfile) as? Map<String, String>) ?: emptyMap()
            val merged = original.toMutableMap()
            for ((k, v) in envVars) if (!merged.containsKey(k)) merged[k] = v
            setEnvs.invoke(runProfile, merged)
            restoreActions[envKey] = { setEnvs.invoke(runProfile, original) }
            logger.info("Dotenv Loader: injected ${envVars.size} variables (generic) from $fileName into ${runProfile.javaClass.simpleName}")
        } catch (_: NoSuchMethodException) {
            // getEnvs/setEnvs を持たない Run Configuration タイプ — 無視
        } catch (e: Exception) {
            logger.warn("Dotenv Loader: reflection-based injection failed for ${runProfile.javaClass.simpleName}: ${e.message}")
        }
    }

    override fun processNotStarted(executorId: String, env: ExecutionEnvironment) {
        restore(env)
    }

    override fun processTerminated(
        executorId: String,
        env: ExecutionEnvironment,
        handler: ProcessHandler,
        exitCode: Int
    ) {
        restore(env)
    }

    private fun restore(env: ExecutionEnvironment) {
        restoreActions.remove(System.identityHashCode(env))?.invoke()
    }
}
