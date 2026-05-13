# Claude Code 向けプロジェクト指示書

このプロジェクトは Java + JavaFX による双方向テトリス(スタンドアロンゲーム)です。詳細仕様は `SPEC.md` を参照してください。

## プロジェクト概要

中央基準の双方向重力を持つ新感覚テトリス。上下から同時にミノが現れ、中央で噛み合う。中央境界線消去で上下同時崩落の大連鎖が発火する。

## 技術スタック

- Java 21 / JavaFX 21+
- Gradle 8.x
- JUnit 5 + AssertJ
- Jackson(永続化用)
- SLF4J + Logback

## 開発フェーズ

`SPEC.md` の「13. 開発フェーズ」を厳守。Phase 1 から順に実装し、各 Phase 完了時点で動作する状態を保つ。

```
Phase 1: 片方向落下のテトリス
Phase 2: 双方向落下
Phase 3: ライン消去とカスケード(★コアの面白さ)
Phase 4: モード・Hold・NEXT
Phase 5: 仕上げ
```

## Agent Team 構成

このプロジェクトは `.claude/agents/` 配下に定義されたサブエージェントを使ったマルチエージェント開発を採用しています。

### エージェント一覧

| エージェント | 役割 | 主な担当領域 |
|-------------|------|----------------|
| **tetris-researcher** | テトリス仕様・人気タイトルの機能調査 | `docs/research/` 配下 |
| **game-logic-engineer** | ゲームロジック実装 | `domain/`, `game/`, `persistence/` 配下 |
| **ui-engineer** | JavaFX UI 実装 | `ui/`, `input/` 配下、`App.java`、`build.gradle` |
| **test-engineer** | ユニットテスト作成 | `src/test/` 配下 |

### オーケストレーションのルール

メインの Claude Code セッション(オーケストレータ)が以下の役割を担う:

1. ユーザーの依頼を受け、適切なエージェントにタスクを委譲する
2. エージェント間の境界を尊重する(担当外のファイルを直接編集させない)
3. フェーズの進行管理と統合テストを実施
4. 仕様書 (`SPEC.md`) と乖離が起きていないかをチェック
5. 実装の前に調査が必要な場合は **tetris-researcher** を先に呼ぶ
6. 調査結果(`docs/research/`)を実装エージェントに参照させる

**重要なルール**: 各エージェントは自分の担当領域のみを編集する。担当外のファイルを編集する必要が出た場合、オーケストレータに報告して、適切なエージェントに委譲する。

### 典型的なワークフロー

#### 例1: シンプルな実装(調査不要)

「Phase 1 の左右移動を実装したい」

```
Orchestrator
  ├─ test-engineer に依頼: 左右移動のテストケース作成
  ├─ game-logic-engineer に依頼: 移動ロジックの実装
  ├─ test-engineer に依頼: テストを実行して通るか確認
  └─ ui-engineer に依頼: キー入力で動作するか確認
```

#### 例2: 調査が必要な実装

「Phase 4 で T-Spin を実装したい」

```
Orchestrator
  ├─ tetris-researcher に依頼: T-Spin の判定アルゴリズムを調査
  │   → docs/research/t-spin-detection.md が生成される
  ├─ ユーザーに確認: 「Mini T-Spin も実装するか」等の判断を仰ぐ
  ├─ test-engineer に依頼: T-Spin 判定のテストケース作成
  ├─ game-logic-engineer に依頼: 調査結果を参照しつつ実装
  ├─ test-engineer に依頼: テスト実行
  └─ ui-engineer に依頼: T-Spin 達成時の表示を追加
```

#### 例3: 仕様判断に迷ったとき

「現代テトリスの DAS / ARR の標準値が分からない」

```
Orchestrator
  ├─ tetris-researcher に依頼: 主要タイトルの DAS / ARR を調査
  │   → docs/research/das-arr-standards.md
  └─ ユーザーに「どの値を採用するか」を提示して判断を仰ぐ
```

### 調査エージェントを呼ぶ判断基準

実装エージェントが以下のケースに遭遇したら、コードを書く前にオーケストレータ経由で **tetris-researcher** に調査を依頼すること:

- 仕様書に書かれていない標準テトリスのルールが必要になった
- SRS の壁蹴りテーブルなど、正確な数値・テーブルが必要
- 「現代テトリスでは普通こうする」というデファクトを知りたい
- 人気タイトルの実装方法を参考にしたい
- 用語や略語の正確な定義が必要(T-Spin Mini, B2B, REN 等)

逆に、**調査不要なケース**:

- `SPEC.md` に明記されている内容
- すでに `docs/research/` に調査済みファイルがある内容
- 一般的な Java / Gradle / JavaFX の使い方(エンジニアリングの常識)

## やること

- 実装前に必ず `SPEC.md` の該当セクションを読む
- 必要に応じて `docs/research/` の既存調査も参照する
- 各エージェントは担当領域のみ編集
- テストファースト(`game-logic-engineer` の前に `test-engineer` がテストを書く)
- 1 コミット 1 トピック、Phase 単位でブランチを切る
- マジックナンバーは必ず定数化

## 絶対にやらないこと

- マジックナンバーをコードに直接書く
- UI 層からドメインロジックを呼ばずに直接盤面を変更する
- ネットワーク通信機能を追加する(オフライン厳守)
- 外部 DB を要求する仕様変更
- テストなしでゲームロジック (`game/` 配下) をマージする
- `SPEC.md` の非ゴール(BGM、対戦モード、リプレイ等)への着手
- 調査結果を確認せずに、推測で SRS や T-Spin 等の標準ルールを実装する

## 確認を求めること

以下のいずれかに該当する変更を加える前に、コードを書く手を止めてユーザーに確認すること:

1. 新しい外部ライブラリの追加
2. `SPEC.md` の仕様変更を伴う実装
3. パッケージ構造の変更
4. キーバインドの追加・変更
5. スコア計算式の変更
6. 調査結果に複数の選択肢があり、どれを採用するか判断が必要な場合

## コーディング規約

`SPEC.md` の「14. コーディング規約」を参照。要点:

- パッケージ: 小文字単数形
- 値オブジェクトは `final` で immutable
- UI とドメインロジックの分離を厳守
- ログは SLF4J、ユーザー向けメッセージと内部ログを分離

## 起動手順

```bash
./gradlew run            # 開発実行
./gradlew test           # テスト
./gradlew shadowJar      # 配布用 fat JAR ビルド
```

## ディレクトリ構造

```
src/main/java/com/example/tetris/
  ├─ App.java                       ← ui-engineer の担当
  ├─ domain/                        ← game-logic-engineer の担当
  ├─ game/                          ← game-logic-engineer の担当
  ├─ ui/                            ← ui-engineer の担当
  ├─ input/                         ← ui-engineer の担当
  ├─ persistence/                   ← game-logic-engineer の担当
  └─ util/

src/test/java/                      ← test-engineer の担当

.claude/agents/                     エージェント定義
  ├─ tetris-researcher.md
  ├─ game-logic-engineer.md
  ├─ ui-engineer.md
  └─ test-engineer.md

docs/
  ├─ research/                      ← tetris-researcher の担当
  │   ├─ srs-wall-kick.md
  │   ├─ t-spin-detection.md
  │   └─ ...
  └─ decisions/                     ADR(設計判断の記録)

SPEC.md                             仕様書(変更時は要相談)
CLAUDE.md                           本ファイル
```

## 質問・相談

実装で迷ったら、推測で進めずユーザーに質問すること。特に標準テトリスのルールに関する不確実性は、tetris-researcher を通じて調査することを優先。
