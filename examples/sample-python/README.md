# Dotenv Loader プラグイン - 動作確認サンプル (Python)

Dotenv Loader プラグインの動作を確認するための Python サンプルプロジェクトです。

## 前提条件

- PyCharm、または Python プラグインをインストール済みの IntelliJ IDEA
- Dotenv Loader プラグインがインストール済み
- Python 3.8+

## セットアップ手順

### 1. プラグインをインストール

プラグインプロジェクトをビルドしてインストールします:

```bash
# プラグインプロジェクトのルートで実行
cd ../..
./gradlew build
```

生成された `build/distributions/Dotenv-Loader-*.zip` を
`Settings > Plugins > ⚙ > Install Plugin from Disk...` でインストールし、IDE を再起動。

### 2. このプロジェクトを IDE で開く

`File > Open` でこの `sample-python/` ディレクトリを開きます。

### 3. .env ファイルを確認

`.env` ファイルがプロジェクトルートに存在することを確認:

```
APP_NAME=Dotenv Sample App (Python)
APP_ENV=development
DATABASE_URL=jdbc:postgresql://localhost:5432/mydb
...
```

### 4. プラグインを有効化

`Settings > Tools > Dotenv Loader`:
- [x] Enable .env file loading
- File path: `.env`

### 5. Run Configuration を作成して実行

`main.py` を開き、`if __name__ == "__main__":` 横の ▶ をクリックして実行。

## 期待される出力

**プラグイン有効時:**
```
=== Dotenv Loader Plugin - 動作確認サンプル (Python) ===

--- 環境変数の読み込み結果 ---
  [OK] APP_NAME = Dotenv Sample App (Python)
  [OK] APP_ENV = development
  [OK] DATABASE_URL = jdbc:postgresql://localhost:5432/mydb
  [OK] DATABASE_PORT = 5432
  [OK] API_KEY = my-s***************
  [OK] API_SECRET = supe***********
  [OK] DEBUG = true
  [OK] LOG_LEVEL = debug

結果: 8 / 8 件の変数を検出

✓ Dotenv Loader プラグインが正常に動作しています！
```

**プラグイン無効時:**
```
  [--] APP_NAME = (未設定)
  [--] APP_ENV = (未設定)
  ...
結果: 0 / 8 件の変数を検出

⚠ 環境変数が読み込まれていません。
```
