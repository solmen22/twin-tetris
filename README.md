# プロジェクトセットアップ手順

このディレクトリには、双方向テトリスの開発を Claude Code(Agent Team構成)で進めるための仕様書とエージェント定義一式が入っています。

## ファイル一覧

```
SPEC.md                                ゲーム仕様書(全要件)
CLAUDE.md                              プロジェクト指示書(Claude Code 用)
agents/
  ├─ tetris-researcher.md              テトリス機能・仕様調査担当エージェント
  ├─ game-logic-engineer.md            ゲームロジック担当エージェント
  ├─ ui-engineer.md                    UI 担当エージェント
  └─ test-engineer.md                  テスト担当エージェント
```

## 配置先

プロジェクトディレクトリ(例: `C:\Users\rushi\projects\bidirectional-tetris\`)を作成して、以下のように配置:

```
bidirectional-tetris/                  プロジェクトルート
├─ CLAUDE.md                           ← CLAUDE.md をここに配置
├─ SPEC.md                             ← SPEC.md をここに配置
├─ .claude/
│   └─ agents/                         ← .claude/agents/ ディレクトリを作成
│       ├─ tetris-researcher.md        ← この 4 つを配置
│       ├─ game-logic-engineer.md
│       ├─ ui-engineer.md
│       └─ test-engineer.md
└─ docs/
    └─ research/                       ← tetris-researcher が出力先として使用
                                          (自動で作成されてもよい)
```

## セットアップ手順(Windows PowerShell)

```powershell
# 1. プロジェクトディレクトリを作成
mkdir C:\Users\rushi\projects\bidirectional-tetris
cd C:\Users\rushi\projects\bidirectional-tetris

# 2. .claude/agents ディレクトリを作成
mkdir .claude\agents

# 3. docs/research ディレクトリを作成
mkdir docs\research

# 4. ファイルを配置(ダウンロードしたファイルを移動)
# (手動で SPEC.md, CLAUDE.md, agents/*.md をそれぞれの場所にコピー)

# 5. Git リポジトリ初期化
git init

# 6. VS Code で開く
code .
```

## Claude Code での起動

1. VS Code で上記プロジェクトディレクトリを開く
2. 「Yes, I trust this folder」を選択
3. Claude Code 拡張を起動
4. プロンプトで以下のように指示:

```
SPEC.md と CLAUDE.md を読んで、プロジェクトの全体構造を理解してください。
.claude/agents/ 配下に 4 つのエージェントが定義されています。

その後、Phase 1 の開発を始めましょう。
まずは Gradle プロジェクトを初期化してください(JavaFX プラグイン込み)。
```

Claude Code は自動的に `.claude/agents/` 配下のエージェント定義を認識し、必要に応じて適切なエージェントに作業を委譲します。

## 開発の進め方

### フェーズ単位で進める

```
Phase 1: 片方向落下のテトリス     ← まずここから
Phase 2: 双方向落下
Phase 3: ライン消去とカスケード   ← コアの面白さ
Phase 4: モード・Hold・NEXT
Phase 5: 仕上げ
```

各 Phase の完了時点でゲームが動く状態を保ちます。

### 4 エージェントの使い分け

| エージェント | いつ呼ぶか |
|----|----|
| **tetris-researcher** | 標準ルール(SRS、T-Spin等)や人気タイトルの機能を正確に知りたいとき |
| **game-logic-engineer** | `domain/`, `game/`, `persistence/` の実装が必要なとき |
| **ui-engineer** | `ui/`, `input/`, `App.java`, `build.gradle` の実装が必要なとき |
| **test-engineer** | ユニットテストの作成・実行が必要なとき |

### マルチエージェント運用例

#### 例1: シンプルな機能実装

「Phase 1 のライン消去を実装したい」

1. **test-engineer** に `LineClearService` のテストケース作成を依頼
2. **game-logic-engineer** に実装を依頼
3. **test-engineer** にテスト実行と結果確認
4. **ui-engineer** に画面更新が機能するか確認

#### 例2: 調査が必要な機能実装

「Phase 4 で T-Spin を実装したい」

1. **tetris-researcher** に T-Spin の判定アルゴリズムを調査依頼
   → `docs/research/t-spin-detection.md` に結果が保存される
2. 調査結果をユーザーと確認、採用ルールを決定
3. **test-engineer** にテストケース作成を依頼
4. **game-logic-engineer** に調査結果を参照しつつ実装を依頼
5. **test-engineer** にテスト実行
6. **ui-engineer** に T-Spin 達成時の表示を追加

各エージェントは自分の担当領域のファイルのみを編集します。これにより、コードと調査資料の一貫性が保たれます。

## 動作確認

```bash
./gradlew run            # 開発実行
./gradlew test           # テスト実行
./gradlew shadowJar      # 配布用 JAR ビルド
```

## トラブルシューティング

### エージェントが認識されない

`.claude/agents/` ディレクトリの場所が正しいか確認。プロジェクトルート直下の `.claude/agents/` でないと認識されません。

### エージェントが担当外のファイルを編集してしまう

CLAUDE.md と該当エージェントの定義を再度読ませて、領域を再確認させる:

```
CLAUDE.md と該当エージェントの定義を再読してください。
担当領域を逸脱した編集を行っていないか確認してください。
```

### tetris-researcher が間違った情報を書いた

調査結果は必ず複数のソースでクロスチェックされる仕様ですが、それでも誤りがあれば:

```
docs/research/<該当ファイル> の内容を再調査してください。
複数の信頼できる情報源で再確認し、ソースを必ず明記してください。
```

### Java/JavaFX のセットアップでエラー

- Java 21 (JDK) がインストールされているか: `java -version`
- `JAVA_HOME` 環境変数が設定されているか
- JavaFX は Gradle プラグイン (`org.openjfx.javafxplugin`) 経由で導入するので、別途 SDK 配置は不要

## 補足

- 仕様書 (`SPEC.md`) は実装の絶対的な参照元です。仕様変更があれば、まず SPEC.md を更新してから実装を進めます
- 調査結果 (`docs/research/`) は参考資料であり、決定事項ではありません。何を採用するかの判断はユーザーが行います
- 重要な設計判断は `docs/decisions/` に ADR として残すことを推奨
- 各 Phase の終了時に動作確認とコミットを行うのが理想
