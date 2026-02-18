package com.example.sample

fun main() {
    println("=== Dotenv Loader Plugin - 動作確認サンプル ===")
    println()

    val vars = listOf(
        "APP_NAME",
        "APP_ENV",
        "DATABASE_URL",
        "DATABASE_PORT",
        "API_KEY",
        "API_SECRET",
        "DEBUG",
        "LOG_LEVEL",
    )

    println("--- 環境変数の読み込み結果 ---")
    var loaded = 0
    for (name in vars) {
        val value = System.getenv(name)
        if (value != null) {
            // API_KEY と API_SECRET は値をマスク
            val display = if (name.contains("KEY") || name.contains("SECRET")) {
                value.take(4).padEnd(value.length, '*')
            } else {
                value
            }
            println("  [OK] $name = $display")
            loaded++
        } else {
            println("  [--] $name = (未設定)")
        }
    }

    println()
    println("結果: $loaded / ${vars.size} 件の変数を検出")

    if (loaded == 0) {
        println()
        println("⚠ 環境変数が読み込まれていません。")
        println("  Settings > Tools > Dotenv Loader でプラグインが有効になっているか確認してください。")
    } else {
        println()
        println("✓ Dotenv Loader プラグインが正常に動作しています！")
    }
}
