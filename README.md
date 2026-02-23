# Dotenv Loader

`.env` ファイルから環境変数を読み込み、実行時に Run Configuration へ注入する IntelliJ Platform プラグインです。

## 機能

- `.env` ファイルを自動的に読み込み、Run Configuration に環境変数を注入
- Java・Python・Node.js・Go・Gradle・Maven の Run Configuration に対応
- Run Configuration に明示的に設定された環境変数が `.env` の値より優先される
- プロジェクトごとにファイルパスを設定可能（デフォルト: プロジェクトルートの `.env`）
- 全 JetBrains IDE で設定 UI を利用可能

## インストール

1. **Settings / Preferences > Plugins** を開く
2. **Dotenv Loader** を検索
3. **Install** をクリック

または [Releases](https://github.com/totomojp/intellij-dotenv-loader/releases) から ZIP をダウンロードし、**Install Plugin from Disk** でインストール。

## 使い方

1. プロジェクトルートに `.env` ファイルを作成:
   ```
   DATABASE_URL=postgres://localhost:5432/mydb
   API_KEY=your_api_key_here
   DEBUG=true
   ```
2. **Settings > Tools > Dotenv Loader** でプラグインが有効になっていることを確認
3. Run Configuration を実行すると、自動的に環境変数が注入される

## 設定

**Settings > Tools > Dotenv Loader**

| 設定項目 | デフォルト | 説明 |
|---|---|---|
| 有効化 | `true` | 注入の ON/OFF |
| Env ファイルパス | `.env` | プロジェクトルートからの相対パス |

設定はプロジェクトごとに `.idea/dotenv-loader.xml` に保存されます。

## .env の書き方

```dotenv
# コメントは無視される
APP_NAME=MyApp
APP_ENV=development

# ダブルクォート（エスケープシーケンス対応）
GREETING="Hello, World!"
MULTILINE="line1\nline2"

# シングルクォート（エスケープなし・そのままの値）
RAW_VALUE='C:\path\to\file'

# export プレフィックス対応
export API_KEY=secret

# クォートなしの値にはインラインコメントが使える
PORT=8080 # デフォルトポート
```

対応機能:
- ダブルクォート値のエスケープシーケンス（`\n`, `\t`, `\r`, `\"`, `\\`）
- シングルクォート値（エスケープなし）
- 複数行の値（ダブルクォート）
- `export` プレフィックス
- クォートなし値のインラインコメント
- 重複キー: 後に定義した値が優先

## 対応バージョン

| プラグインバージョン | IntelliJ Platform |
|---|---|
| 0.0.x | 2024.2 – 2025.3 |

## 開発

**必要環境:** JDK 21

```bash
# ビルド
./gradlew build

# テスト実行
./gradlew test

# サンドボックス IDE 起動
./gradlew runIde

# プラグイン互換性検証
./gradlew verifyPlugin
```

## アーキテクチャ

```
parser/       — DotEnvParser: .env パーサー（IntelliJ 依存なし）
settings/     — DotEnvLoaderSettings: 永続化設定
              — DotEnvLoaderConfigurable: 設定 UI
execution/    — DotEnvRunConfigurationExtension: Java 系 Run Configuration への注入
              — DotEnvExecutionListener: Python・Go・Node.js・Gradle・Maven への注入
                （ExternalSystemInjector・ReflectionInjector によるストラテジパターン）
```

## ライセンス

[Apache 2.0](LICENSE)