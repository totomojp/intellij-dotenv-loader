package com.github.dotenvloader.execution

import com.github.dotenvloader.parser.DotEnvParser
import com.github.dotenvloader.settings.DotEnvLoaderSettings
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.io.File

internal object DotEnvLoader {
    private val logger = Logger.getInstance(DotEnvLoader::class.java)

    /**
     * プロジェクト設定を読み、.env ファイルをパースして返す。
     * 無効・ファイル不在・エラー時は null を返す。
     */
    fun load(project: Project): Map<String, String>? {
        val settings = DotEnvLoaderSettings.getInstance(project)
        if (!settings.state.enabled) return null

        val envFilePath = settings.state.envFilePath
        if (envFilePath.isNullOrBlank()) return null

        val basePath = project.basePath ?: return null
        val envFile = File(basePath, envFilePath)

        if (!envFile.exists()) {
            logger.warn("Dotenv Loader: .env file not found at ${envFile.absolutePath}")
            return null
        }

        return try {
            DotEnvParser.parse(envFile)
        } catch (e: Exception) {
            logger.error("Dotenv Loader: failed to parse ${envFile.absolutePath}", e)
            null
        }
    }
}
