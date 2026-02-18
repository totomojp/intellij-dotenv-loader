import os

def main():
    print("=== Dotenv Loader Plugin - 動作確認サンプル (Python) ===")
    print()

    vars = [
        "APP_NAME",
        "APP_ENV",
        "DATABASE_URL",
        "DATABASE_PORT",
        "API_KEY",
        "API_SECRET",
        "DEBUG",
        "LOG_LEVEL",
    ]

    print("--- 環境変数の読み込み結果 ---")
    loaded = 0
    for name in vars:
        value = os.environ.get(name)
        if value is not None:
            if "KEY" in name or "SECRET" in name:
                display = value[:4] + "*" * (len(value) - 4) if len(value) > 4 else value
            else:
                display = value
            print(f"  [OK] {name} = {display}")
            loaded += 1
        else:
            print(f"  [--] {name} = (未設定)")

    print()
    print(f"結果: {loaded} / {len(vars)} 件の変数を検出")

    if loaded == 0:
        print()
        print("⚠ 環境変数が読み込まれていません。")
        print("  Settings > Tools > Dotenv Loader でプラグインが有効になっているか確認してください。")
    else:
        print()
        print("✓ Dotenv Loader プラグインが正常に動作しています！")

if __name__ == "__main__":
    main()
