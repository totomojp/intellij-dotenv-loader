package main

import (
	"fmt"
	"os"
	"strings"
)

func main() {
	fmt.Println("=== Dotenv Loader Plugin - 動作確認サンプル (Go) ===")
	fmt.Println()

	vars := []string{
		"APP_NAME",
		"APP_ENV",
		"DATABASE_URL",
		"DATABASE_PORT",
		"API_KEY",
		"API_SECRET",
		"DEBUG",
		"LOG_LEVEL",
	}

	fmt.Println("--- 環境変数の読み込み結果 ---")
	loaded := 0
	for _, name := range vars {
		value, ok := os.LookupEnv(name)
		if ok {
			display := value
			if strings.Contains(name, "KEY") || strings.Contains(name, "SECRET") {
				if len(value) > 4 {
					display = value[:4] + strings.Repeat("*", len(value)-4)
				}
			}
			fmt.Printf("  [OK] %s = %s\n", name, display)
			loaded++
		} else {
			fmt.Printf("  [--] %s = (未設定)\n", name)
		}
	}

	fmt.Println()
	fmt.Printf("結果: %d / %d 件の変数を検出\n", loaded, len(vars))

	if loaded == 0 {
		fmt.Println()
		fmt.Println("⚠ 環境変数が読み込まれていません。")
		fmt.Println("  Settings > Tools > Dotenv Loader でプラグインが有効になっているか確認してください。")
	} else {
		fmt.Println()
		fmt.Println("✓ Dotenv Loader プラグインが正常に動作しています！")
	}
}
