package com.github.dotenvloader.execution

import com.intellij.execution.ExecutionListener
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import java.util.concurrent.ConcurrentHashMap

class DotEnvExecutionListener(private val project: Project) : ExecutionListener {

    private val injectors = listOf(ExternalSystemInjector(), ReflectionInjector())
    private val restoreActions = ConcurrentHashMap<Int, () -> Unit>()

    override fun processStartScheduled(executorId: String, env: ExecutionEnvironment) {
        val envVars = DotEnvLoader.load(project) ?: return
        val runProfile = env.runProfile
        val injector = injectors.firstOrNull { it.canInject(runProfile) } ?: return
        val restore = injector.inject(runProfile, envVars) ?: return
        restoreActions[System.identityHashCode(env)] = restore
    }

    override fun processStarted(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler) {
        restore(env)
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
