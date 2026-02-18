package com.github.dotenvloader.execution

/**
 * ランコンフィグへの env 注入ストラテジ。
 * inject() は注入に成功したら復元用ラムダを、失敗/非対象なら null を返す。
 */
internal interface RunConfigInjector {
    fun canInject(runProfile: Any): Boolean
    fun inject(runProfile: Any, envVars: Map<String, String>): (() -> Unit)?
}
