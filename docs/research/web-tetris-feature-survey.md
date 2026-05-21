# Web 版テトリスのメタ機能 網羅調査

> 本ドキュメントは「双方向重力テトリス」を Web サービスとして運用するために、ゲームプレイ本体**以外**で必要・一般的・推奨されるメタ機能を網羅的に整理したもの。実装の意思決定は行わず、選択肢の提示にとどめる。
>
> リファレンス対象: TETR.IO / Jstris / Tetris Friends(旧) / Tetris Effect: Connected / Puyo Puyo Tetris 2

---

## 0. 用語整理

- **メタ機能**: 個々のゲームプレイの外側にある機能群(アカウント、ランキング、設定、SNS など)
- **クライアント**: 本プロジェクトの場合 TeaVM で生成された Web ビルド + JavaFX デスクトップ版の双系統
- **サーバ機能**: 何らかのサーバ(自前 / Firebase / Supabase などの BaaS)を必要とする機能
- **ローカル機能**: ブラウザ内 LocalStorage / IndexedDB だけで完結する機能

本プロジェクトは現状ローカル保存のみ。サーバ導入は新規判断が必要。

---

## 1. ユーザーが既に挙げた 4 機能の深掘り

### 1-1. アカウント作成・ログイン

**何のためにあるか**
- スコアやリプレイ、設定をユーザーに紐付けて永続化する
- ランキング・対戦・実績などサーバ系機能の前提
- 「自分のもの」という所有感がユーザーの継続率に効く

**人気タイトルの実装パターン**

| タイトル | 認証方式 | ゲストプレイ | 補足 |
|---------|---------|------------|------|
| TETR.IO | ユーザー名 + パスワード。メール登録は「任意」だが、未設定だと忘れたパスワードの復旧不可。「1 アカウント 1 人」を建前ルールで明示 | 名前を入力するだけ・空欄ランダム名で開始可。匿名のままだとリーダーボード提出やマッチメイキング不可 | 「最低限の摩擦で遊ばせ、本格的に遊ぶならアカウントへ昇格させる」設計 |
| Jstris | 単純なユーザー名 + パスワード登録 | 登録なしでも一通り遊べる。ただしリーダーボード非掲載・統計やリプレイ未保存 | 「未登録 = ゲスト」「登録 = 全機能」の二段階のみで OAuth 等は持たない |
| Tetris Friends(旧) | 独自アカウント + Facebook 連携 | フリーミアム + プレミアム通貨「ruby」が存在 | Flash 時代の典型的 SNS 連携 |
| Tetris Effect / PPT2 | プラットフォーム(Steam / PSN / Switch / Xbox Live)のアカウントに従属 | 不可(プラットフォーム前提) | Web には直接の参考は薄い |

**実装の典型的なやり方(Web の場合)**
- パターン A: ユーザー名 + パスワードのみ(Jstris 流)。最小コストだが、パスワード忘れリスクが高く、紛失したら復旧不能と割り切る
- パターン B: メール + パスワード + メールリンク検証(TETR.IO 風)。一般的だが SMTP / メール送信サービス(SendGrid, Mailgun, Resend など)が必要
- パターン C: OAuth 連携(Google, GitHub, Discord 等)。実装は OIDC ライブラリで簡素化できるが、外部依存が増える
- パターン D: マジックリンク方式(パスワードレス)。Supabase Auth / Auth0 / Firebase Auth が標準サポート
- パターン E: 匿名アカウント先行発行(Firebase Auth の Anonymous など)→ 後から正規アカウントへ昇格

**Web ならではの落とし穴**
- パスワードハッシュは必ずサーバ側で行う(クライアントだけでハッシュ化しても無意味)
- セッションは Cookie の `HttpOnly + Secure + SameSite` を使う
- TeaVM 経由でも fetch / WebSocket は呼べるが、CORS 設定の検討が必須
- 「匿名スコアでもとりあえずローカルだけは保存」のような UX があると離脱率が下がる(TETR.IO もこれに近い)

### 1-2. ランキング機能

**何のためにあるか**
- 比較対象を作ることでモチベーションが生まれる
- コミュニティ形成(誰がトップか分かる)
- 競技性の核

**人気タイトルの実装パターン**

| タイトル | ランキングの軸 | スコープ | 仕様の要点 |
|---------|------------|---------|----------|
| TETR.IO | TETRA LEAGUE = Glicko-2 ベースの TR(対戦レート)。40 LINES / BLITZ などソロモードは時間 / スコアのトップ N。XP 累計と AR(実績累計)も独立リーダーボード | グローバル + 国別フィルタ | レートは RD(rating deviation)も併せ持つ。RD が高いと「不安定」マーク。シーズン制あり |
| Jstris | Sprint(20L / 40L / 100L / 1000L), Cheese(10 / 18 / 100 / 無限), Ultra, Survival, 20TSD などモード × ライン数ごとに個別ボード | 全期間 / 各種フィルタ | 未登録ユーザーのスコアは除外。クリーンを売りにしている |
| Tetris Friends(旧) | 友達ランキング + グローバルランキング、トークン獲得 | 友達のみ・グローバル両方 | ソーシャル(友達ランキング)を強く打ち出していた |
| Tetris Effect: Connected | プラットフォーム標準のリーダーボード + Weekend Ritual の累積寄与ランキング | プラットフォーム依存 | イベント期間ランキングという形態 |
| PPT2 | Tetris League / Puyo Puyo League / Skill Battle League の 3 系統 | グローバル | モードごとに league を分離する設計 |

**実装の典型的なやり方**
- DB のテーブル設計: `score(id, user_id, mode, value, achieved_at, replay_id, validated)` のような形
- クエリ: モード x 期間(all-time / monthly / weekly / daily)別に top N を引く
- インデックス: `(mode, value)` 降順 + `(user_id, mode)` ユニーク(個人ベストだけを残すなら)
- 「個人ベストのみ表示」と「全提出履歴」を切り替えられる UI(TETRA CHANNEL の拡張ボード)

**Web ならではの落とし穴**
- 不正スコアが必ず入る前提で設計する(後述 4 章参照)
- 数百万件規模になりうるので、ページネーション or 仮想スクロールを最初から
- 「自分の順位」を周辺の数件と一緒に表示できると満足度が高い

### 1-3. キーバインド(操作キー自由設定)

**何のためにあるか**
- 競技プレイヤーは指の長さや既存ゲームの慣れに応じて完全カスタムしたい
- アクセシビリティ(片手プレイ・ゲームパッド対応)
- 競技志向タイトルの「必須」機能

**人気タイトルの実装パターン**

| タイトル | 設定範囲 |
|---------|---------|
| TETR.IO | キー配置 / DAS / ARR / SDF / handling / sound / video / interface / notifications / background。設定は TTC ファイルにエクスポート/インポートできる。WASD トグルあり |
| Jstris | Settings メニューから全キー再配置可能。DAS デフォルト 133ms / ARR デフォルト 10ms。「ARR=0(瞬間移動)」も許容 |
| Tetris Effect | プラットフォームのコンフィグに準じる(コントローラー前提だがキーボードも対応) |
| PPT2 | 各プラットフォームの標準コンフィグ |

**重要な差**: TETR.IO の DAS と Jstris の DAS は数値の解釈が異なる(TETR.IO ARR = Jstris ARR、TETR.IO DAS = Jstris DAS + Jstris ARR)。設定 UI で「どの方式の数値か」を明示する必要がある。

**実装の典型的なやり方**
- JSON で「アクション名 → KeyCode」のマップを保持。LocalStorage に永続化
- Web の場合は `KeyboardEvent.code`(物理キー位置)を使うのが鉄則(`key` だとレイアウト依存で日本語キーボードでバグる)
- アクション一覧の例: `MOVE_LEFT, MOVE_RIGHT, SOFT_DROP, HARD_DROP, ROTATE_CW, ROTATE_CCW, ROTATE_180, HOLD, RESET, PAUSE` + 本プロジェクト固有 `SELECT_UP_PIECE, SELECT_DOWN_PIECE`
- DAS / ARR / SDF も別途数値設定として用意
- プリセット(Default / Speed / Comfort 等)をいくつか用意すると初心者が迷わない
- 設定エクスポート / インポート(TETR.IO の TTC 方式)があると競技層に評価される

**落とし穴**
- ブラウザ既定のショートカット(Ctrl+W, F5 等)と衝突するキーは UI で警告
- ゲームパッド対応(Gamepad API)はオプション
- IME 入力中(チャットなどで日本語入力中)のキーをゲームに食わせない処理

### 1-4. チュートリアル

**何のためにあるか**
- 初見ユーザーの離脱を防ぐ
- 本プロジェクトのように「通常テトリスとは違う固有ルール」があると特に重要
- 中級テクニック(T-Spin など)への橋渡し

**人気タイトルの実装パターン**

| タイトル | チュートリアル |
|---------|--------------|
| TETR.IO | 体系的なチュートリアル UI は強くは持たず、9 Beginner Tips のような外部動画にコミュニティが委ねる傾向。Custom / Zen で設定を試させる導線が事実上のチュートリアル |
| Jstris | サイト上に Guide ページ(`/guide`)を別途用意し、基本機能 → モード → 上級テク を文章とスクショで案内。ゲーム内に分岐分けされたインタラクティブチュートリアルはほぼ無し |
| Tetris Effect: Connected | Journey モードのプロローグが事実上のチュートリアル。Zone 機能や Connected 共闘の説明がストーリー進行に組み込まれている |
| Puyo Puyo Tetris / PPT2 | Lesson モードでルール解説。Adventure モードが緩やかなチュートリアルを兼ねる |
| 外部ツール(参考) | Four.lol(SRS や T-Spin を視覚的に教える)、tspin-practice(T-Spin 練習)など、別サイトで補完するのもよくあるパターン |

**実装の典型的なやり方**
- パターン A: 別ページ(Markdown レンダリングだけの静的ページ)に「遊び方」を書く(Jstris 方式)。実装コスト最小
- パターン B: 初回起動時にツアー(spotlight + 説明吹き出し)を出す。`Shepherd.js` などのライブラリ
- パターン C: 専用「Lesson」モードを作り、段階的なお題をクリアさせる(PPT2 方式)。コストは高いが教育効果は最大
- パターン D: 動画 / GIF を埋め込む(中央崩落の挙動など、文章では伝えづらいものに有効)

**本プロジェクト固有の注意**
- 「Q / E で次のミノの方向を選ぶ」「中央境界線消去で大連鎖」など独自概念は文章だけでは絶対伝わらない。動画 / GIF / インタラクティブが推奨
- 上下双方向の操作感は SRS や T-Spin より「珍しさ」が高く、最初の 30 秒で理解させる UX を最優先するべき

---

## 2. その他の必須メタ機能(これがないと Web サービスとして不自然)

### 必須(これがないと「未完成」に見える)

| 機能 | 何のためにあるか | 採用タイトル | 典型的な実装 |
|------|--------------|------------|------------|
| プロフィールページ | 自分の戦績・実績を一覧できる場所 | TETR.IO の userpage / TETRA CHANNEL、Jstris の user 統計ページ | `/u/<username>` のような公開 URL。プレイ統計と直近リプレイへのリンク |
| 設定画面(ゲーム設定全般) | キー・音量・グラフィック・ハンドリングを一画面で | TETR.IO の CONFIG タブ(handling, sound, gameplay, video, interface, notifications, background) | タブ式 / アコーディオン式 UI。LocalStorage + サーバ同期 |
| 一時停止 / 再開 | ライフ的に当たり前 | 全タイトル | キー割り当て + メニューオーバーレイ |
| 結果画面の詳細統計 | プレイ後の振り返り | 全タイトル | Score, Lines, PPS(pieces per second), KPP(keys per piece), Finesse, T-Spins, B2B, Combo, Garbage 等 |
| 個人ハイスコア記録 | 「自己ベスト更新」の演出 | 全タイトル | サーバまたはローカルに保存。「ベスト更新!」の演出を入れる |
| 利用規約 / プライバシーポリシー | 法令遵守(後述 4-3) | 全 Web サービス | フッターに常時リンク |

### あると良い(モダンな Web テトリスは大抵持っている)

| 機能 | 何のためにあるか | 採用タイトル | 典型的な実装 |
|------|--------------|------------|------------|
| リプレイ機能 | 自己分析 / 共有 / 不正検証 | TETR.IO (.ttr ファイル), Jstris(クリーンさのアピール) | 入力イベント列 + 初期シードを保存し、決定論的に再生 |
| リプレイ共有 URL | SNS でのバズ起点 | TETR.IO の replay ID(top 10 までは保持。それ以下は期限切れ) | `/replay/<id>` で他人も再生可能 |
| 統計ダッシュボード | 上達曲線の可視化 | TETRA CHANNEL / Tetra Stats(サードパーティ) / Tetris Metrics(個人ダッシュボード例) | プレイ回数・PB 遷移・KPP/PPS 推移をグラフで |
| 実績(Achievement) | 長期的な目標を与える | TETR.IO は 5 階級(Bronze〜Diamond)+ AR ランキング、最大 3 つをプロフィールに表示 | 達成条件 → トリガー → DB に記録 |
| 経験値 / レベル | プレイ継続インセンティブ | TETR.IO の XP(モードごとに獲得量が違う、Zen は分速 100XP・1 セッション 3000XP cap) | XP テーブル + level up 演出 |
| カラーテーマ / 配色 | 個人の好み + アクセシビリティ | TETR.IO の background、Jstris の skin | CSS 変数の切替 + プリセットセレクタ |
| サウンド・BGM 設定 | ゲームの没入感調整 | 全タイトル | マスター/SE/BGM 別ボリューム + ミュート |
| ニュース / 更新履歴 | 運営感の演出、変更点の通知 | TETR.IO の patch notes、ch.tetr.io の news | Markdown ベースの記事リスト |
| お知らせバナー | メンテナンス・新機能の周知 | TETR.IO のシステム通知 | サーバから取得 or 静的 JSON |
| ゲーム内チャット | コミュニティ醸成 | TETR.IO(部屋・フレンドリスト・グローバル) | WebSocket + モデレーション機構 |
| フレンド機能 | 1 対 1 / 小グループでの遊び | TETR.IO(フレンドリスト・DM・非フレンド DM タブ)、Tetris Friends | 申請 → 承認 → リスト管理 |
| ブロック / 通報 | 健全性維持 | TETR.IO | DB に block_user テーブル、通報キュー |

### 凝るなら(競技志向や長期運用なら検討)

| 機能 | 何のためにあるか | 採用タイトル |
|------|--------------|------------|
| シーズン制 | 区切りでランキングをリセットしてフレッシュさを保つ | TETR.IO(TETRA LEAGUE はシーズン制) |
| 季節イベント / 期間限定モード | エンゲージメント維持 | TETR.IO(April Fools' / Holiday Season で見た目を変える、event achievement は unranked) / Tetris Effect の Weekend Ritual |
| 観戦 / Spectate | 観る楽しみ・実況配信補助 | TETR.IO の spectating ページ |
| カスタム部屋(ルール改変) | コミュニティ駆動の遊び | TETR.IO の Custom Game(攻撃テーブル・previews・チーム部屋など改変可能) / Jstris の Private Room |
| マッププレイ(自作お題) | UGC によるコンテンツの自動増殖 | Jstris の Map Room / Live Maps |
| Bot / AI 相手 | オフライン練習 | Jstris は外部の Misamino bot を活用するパターン |
| プレミアム課金 / 寄付 | 運営費の補填、サポーター特典 | TETR.IO の Supporter(€20 / €80 / €250 / €1000 で段階特典、すべてコスメティック) |
| バッジ / 称号 | ゲーミフィケーション | TETR.IO の Huge Supporter バッジなど |
| Discord 連携 / Rich Presence | 外部コミュニティとの接続 | TETR.IO は Discord Rich Presence サポート |
| API 公開 | サードパーティの統計ツール / Bot を育てる | TETR.IO の TETRA CHANNEL API は公開、Tenchi の Tetra Stats などが育っている |

---

## 3. Web ならではの機能

| 機能 | 何のためにあるか | 採用タイトル / 実装例 | 典型的な実装 |
|------|--------------|--------------------|------------|
| URL ルーティング | ページ遷移を URL で共有可能に | TETRA CHANNEL の `/u/<user>` `/replay/<id>` | History API / フレームワークのルータ。本プロジェクトは TeaVM なので JS と橋渡しが要る |
| リプレイ共有 URL | 「俺の神プレイ」を Twitter / X に貼れる | TETR.IO は top 10 までサーバ保持、それ以外は期限切れ | クライアントで .ttr 相当のファイルを DL させる + サーバ側で短期保管 |
| 状態の URL パラメータ化 | 特定の部屋設定 / 初期盤面を URL で共有 | TETR.IO は私室の URL 共有が要望挙がっている | クエリパラメータ or ハッシュフラグメントに JSON を圧縮して埋める |
| SNS シェアボタン | バイラル流入 | Tetris Friends は Facebook 連携で広まった | OGP + Twitter Card + シェア URL ボタン |
| OGP 画像生成 | リンクが SNS で目立つ | 一般的 | プレイ後にスコア画像を動的生成(Canvas → PNG)してアップロード |
| PWA(Progressive Web App) | ホーム画面追加、オフラインプレイ | Just Tetris / PWA-Tetris(Vue) / Tetris.im / Hextris(Lighthouse 96) など | `manifest.json` + Service Worker。TeaVM のビルド成果物に Service Worker を載せる |
| オフライン対応 | ネット切断時もシングルプレイは続行可 | 上記 PWA テトリス | Service Worker で JS / アセットをキャッシュ。スコアはオンライン復帰時に同期 |
| モバイル / タッチ対応 | スマホユーザーを取り込む | Just Tetris / Tetris.im / TETR.IO PLUS の touch サポート | スワイプ = 左右、上スワイプ = ハードドロップ、タップ = 回転、長押し = ホールド 等が一般的 |
| 仮想ゲームパッド | タッチ操作の代替 | Tetris.im | 画面に半透明ボタンを表示。透明度・サイズ調整可 |
| レスポンシブ UI | 画面サイズに応じて自動調整 | Just Tetris | CSS の min/max + JS のリサイズイベント |
| ゲストプレイ | 登録ハードルの除去 | TETR.IO / Jstris | アカウントなしで遊ばせ、スコア提出だけ制限 |
| ブラウザ通知 | フレンドのオンライン通知など | TETR.IO の notifications 設定 | Notification API + Permission リクエスト |
| Web Share API | モバイルでネイティブ共有 | 一般的 | `navigator.share({...})` でフォールバックは URL コピー |
| Web Audio | 効果音・BGM の精密制御 | 全 Web ゲーム | Web Audio API。TeaVM 経由でも JS interop で扱える |
| アクセシビリティ | 視覚障害・運動障害対応 | (上位タイトルは弱い領域) | 色覚モード(プリセット色を切替)、ハイコントラスト、文字サイズ、ARIA ラベル、TTS 連携、キーボードナビゲーション |
| 国際化(i18n) | 海外ユーザーの取り込み | Jstris は `jstris-multilang` プロジェクトで多言語化 | JSON 翻訳ファイル + 言語選択 UI。少なくとも英語 / 日本語が望ましい |
| ダークモード / ライトモード | OS 設定追従 | 一般的 | `prefers-color-scheme` メディアクエリ + UI トグル |
| アンチロック(離脱防止) | 誤ってタブを閉じる事故防止 | 一般的 | `beforeunload` でゲーム中なら警告 |
| 自動セーブ | ブラウザクラッシュ対策 | 一般的 | LocalStorage / IndexedDB へ定期的に進行状態書き出し |

---

## 4. 不正対策・運用面

### 4-1. スコア改ざん防止

**現実的にやれること(段階的)**

1. **HTTPS 必須**(中間者攻撃の遮断)
2. **共有秘密 + リクエスト署名**: ゲームコード内に秘密文字列を持たせ、スコアと一緒に HMAC を送る。素朴な改ざんを弾く。ただしクライアント解析されると破られる
3. **サーバ側の妥当性チェック**: 「このモードで 1 秒 100 ライン」のような物理的にあり得ないスコアを弾く。平均から大きく外れたら自動で suspicious フラグ
4. **リプレイ提出義務**: スコアと一緒に入力イベント列 + 初期シード(乱数種)を送らせ、サーバまたはクライアント側で再生して検証(TETR.IO は不審なリプレイを自動マーク → スタッフがレビュー、合格すれば警告解除)
5. **決定論的シミュレーション**: ゲームロジックを「同じ入力 + 同じシード = 必ず同じ結果」になるよう設計。サーバで再シミュレーションしてスコアの妥当性を確認
6. **手動管理ツール**: 怪しいスコアをスタッフが flag / hide / ban できる管理画面(参考: 一般的なスコア管理パターン)

**学術的アプローチ(参考)**: クライアントのコードを記号実行して制約を抽出し、サーバ受信メッセージが「あり得る入力で説明可能か」を制約ソルバで検証する手法。オープンソースのマルチプレイテトリスで研究実装あり。

**Web 固有の注意**
- JS は必ず解析されるので「秘密鍵をクライアントに置く」モデルは時間稼ぎにしかならない
- TeaVM で生成された JS も同様(難読化はされるが完全ではない)
- 重要なのは「サーバが最終判断者」であること

### 4-2. レート制限 / Bot 対策

- API レベルのレート制限(1 ユーザー / IP あたり N req/min)
- ログイン試行制限(ブルートフォース対策)
- CAPTCHA: 登録時 / 怪しいスコア提出時に挟む
- WAF / Cloudflare 等のフロントに置く

### 4-3. 法的・運用ドキュメント

| ドキュメント | 内容 |
|-----------|------|
| 利用規約 (ToS) | 禁止行為・アカウント停止条件・責任範囲 |
| プライバシーポリシー | 何のデータをどう使うか、保持期間、第三者提供、Cookie 利用 |
| GDPR 対応(EU 圏ユーザーを受け入れるなら) | データアクセス権・削除権(忘れられる権利)・データポータビリティの実装 |
| COPPA / GDPR-K(子ども向けの場合) | 13 歳(EU 国により 16 歳)未満のユーザーから個人情報を集める場合、保護者同意が必要。年齢確認 UI が要る。テトリスは年齢制限ゲームではないので、「13 歳未満は登録不可」の規約で逃げるサービスが多い |
| Cookie バナー | EU 向けに Cookie の同意取得 |
| Contact / DMCA | 問い合わせ窓口・著作権申告窓口 |

### 4-4. データバックアップ・障害対策

- DB の自動バックアップ(無料 DB の場合プランの制限を要確認: Supabase 無料枠は 7 日、Firebase の Firestore は手動 export 等)
- ユーザーへの「データエクスポート」機能(GDPR 要件にもなる)
- 障害発生時のステータスページ(Statuspage.io / 静的ページ)

### 4-5. モデレーション

- 通報機能 + 通報キュー
- 不適切ユーザー名のフィルタ(NG ワード辞書)
- チャット履歴の保持(問題発生時の調査用)
- BAN / シャドウバン機構

### 4-6. 監視・運用

- エラー収集: Sentry, Rollbar 等
- アクセス解析: Plausible, Umami(プライバシー配慮系)/ GA4(機能豊富)
- メトリクス: 同接 / 提出スコア数 / アクティブユーザー数

---

## 5. 本プロジェクト「双方向重力」固有で考えるべきこと

通常テトリスにはない「中央境界線」「上下同時崩落」「Q/E で次のミノの方向を選ぶ」をメタ機能にどう織り込むか。

### 5-1. ランキングの軸の作り方

通常のテトリスは「Sprint(40 ライン消去タイム)」「Ultra(2 分でスコア)」「Blitz」などが標準。双方向ならではの軸候補:

- **中央崩落回数**: 一定時間内に何回中央境界線消去を発火できたか
- **大連鎖スコア**: 中央崩落から始まる連鎖の最大連鎖数
- **モード別**: SPEC の「ランダム / 交互 / ユーザー選択」の 3 モードそれぞれにランキングを持つかどうか
- **上下バランス指標**: 上下どちらのミノをどれだけ使ったか(プロフィール統計向け)

ランキングが多すぎると分散して「誰もトップ取れない」状態になる。最初は **ユーザー選択モードの Marathon スコアと中央崩落回数の 2 軸** に絞るのが現実的(後から追加は容易)。

### 5-2. チュートリアルでの強調点

文章だけでは絶対伝わらない要素:

1. **盤面が上下対称で、上下からミノが出る**: 通常テトリスを知っている人ほど「上下が逆の意味」を勘違いする
2. **Q / E で次のミノの方向を選ぶ**: SPEC 6.3 の独自概念。0.5 秒の猶予の存在
3. **中央境界線が揃うと上下が同時崩落する**: 大連鎖の核。GIF か動画必須
4. **DOWN ミノは row 10 を越えられない / UP ミノは row 10 を越えられない**: 上下が独立した盤面のように振る舞う場面と、中央で噛み合う場面の区別

推奨アプローチ: **ステップ式のインタラクティブチュートリアル**(PPT2 の Lesson モード方式)。各ステップで盤面を固定セットアップしてプレイヤーに操作させ、達成したら次へ。

### 5-3. リプレイへの織り込み

- 入力イベント記録に **「次のミノ方向の選択」イベント** を含める必要がある
- ランダム / 交互モードの場合は方向選択は決定論的(シード or 規則ベース)。ユーザー選択モードの場合のみイベントとして残す
- リプレイ再生 UI で **中央崩落のハイライト**(発火した瞬間に画面エフェクトやマーカー)があると本作らしさが伝わる

### 5-4. 統計ダッシュボード固有指標

- 中央崩落発火率(全クリアラインに対する中央境界線消去の割合)
- 上下使用比(DOWN:UP の比率)
- 連鎖の長さ分布(ヒストグラム)
- モード別プレイ回数

### 5-5. SNS シェアの差別化

「上下同時崩落の瞬間」を GIF / 動画自動生成して共有できると本作ならではのバズ要素になる。OGP 画像も中央崩落をビジュアル化したものを動的生成すると目立つ。

---

## 6. 推奨優先順位(個人開発・無料 BaaS 想定)

### 前提

- 開発リソース: 個人(または少人数)
- バックエンド: 無料枠の BaaS(Supabase / Firebase / Cloudflare Pages + D1 など)を想定
- クライアント二系統(JavaFX デスクトップ + TeaVM Web)が並存
- 既に Phase 5 までゲーム本体は完成

### フェーズ A: Web サービスの「足回り」(まず無いと話にならない)

優先度順:

1. **PWA 化 + モバイルレスポンシブ + 仮想ゲームパッド**: TeaVM 出力に Service Worker と `manifest.json` を追加。これだけで「ちゃんとした Web ゲーム」感が一気に出る
2. **設定画面の整備**: キーバインド、DAS/ARR/SDF、音量、テーマ。LocalStorage に保存。アカウント不要で完結
3. **チュートリアル(まずは静的ページ版)**: GIF + テキストで本作独自ルールを説明する `/guide` を用意。Markdown + 静的ホスティングで十分
4. **利用規約・プライバシーポリシー・連絡先のフッター整備**: BaaS 契約前に必須

### フェーズ B: サーバを使う最小機能(アカウント + ランキング)

5. **アカウント登録 / ログイン**: BaaS Auth(Supabase Auth が Java/TeaVM と相性悪くなければ第一候補。Firebase Auth は JS SDK が前提)。匿名ログイン → 正規ユーザー昇格パスを最初から組む
6. **ランキング(まずは Marathon スコアの 1 種類のみ)**: サーバ提出 + 個人ベストの管理。リプレイ提出は後回しでもよいが、不正対策の素朴な署名 + サーバ側の上限チェックは最初から入れる
7. **プロフィールページ**: 個人統計(プレイ回数、ベストスコア、最高連鎖)+ 直近プレイの一覧

### フェーズ C: 体験を厚くする

8. **リプレイ機能(ローカル保存 + サーバ共有)**: 入力イベント列 + シードを保存し、決定論的再生。共有 URL の発行
9. **統計ダッシュボード**: プロフィールに上達曲線・モード別統計を追加
10. **実績・XP**: ゲーミフィケーション要素
11. **多言語化(英語追加)**: 海外ユーザー受け入れ
12. **アクセシビリティ強化**: 色覚モード・ハイコントラスト・キーボードナビゲーション

### フェーズ D: コミュニティ化(運用余力があるなら)

13. **フレンド / DM / チャット**: モデレーション工数とセットなので慎重に
14. **カスタム部屋 / 観戦**: WebSocket サーバが必要
15. **シーズン制・季節イベント**: 運用負荷大
16. **API 公開**: サードパーティの育成を狙うなら

### 後回しでよいもの(本プロジェクトの非ゴール「対戦モード」と関わる)

- 対戦マッチメイキング、ランクレート(Glicko-2 など)、Battle Royale: SPEC の非ゴールに該当する可能性が高いので、本プロジェクトの方針確認が必要

---

## 7. 機能チェックリスト(網羅性確認用)

備忘のための一覧。実装決定は別途。

### アカウント・認証
- [ ] ゲストプレイ
- [ ] アカウント登録(ユーザー名 + パスワード / メール)
- [ ] ログイン / ログアウト / セッション管理
- [ ] パスワードリセット
- [ ] メール検証
- [ ] OAuth / ソーシャルログイン
- [ ] 匿名 → 正規アカウントへの昇格
- [ ] アカウント削除(GDPR)
- [ ] データエクスポート(GDPR)

### ランキング
- [ ] グローバルリーダーボード
- [ ] 期間別(全期間 / 月間 / 週間 / 日間)
- [ ] 国別フィルタ
- [ ] モード別
- [ ] フレンドランキング
- [ ] 自分の周辺順位表示

### 設定
- [ ] キーバインド
- [ ] DAS / ARR / SDF
- [ ] 音量(マスター / SE / BGM)
- [ ] テーマ(配色 / 背景)
- [ ] 言語
- [ ] グラフィック設定(低 / 中 / 高)
- [ ] ゴーストピース表示
- [ ] グリッド表示
- [ ] 操作ガイド表示
- [ ] 設定エクスポート / インポート

### 統計・プロフィール
- [ ] プロフィールページ(公開 URL)
- [ ] プレイ統計(回数 / ベスト / 平均)
- [ ] 結果画面の詳細(PPS / KPP / Finesse / 中央崩落回数 等)
- [ ] 上達曲線グラフ
- [ ] 実績 / XP / レベル

### リプレイ
- [ ] ローカル保存
- [ ] サーバ保存
- [ ] 共有 URL
- [ ] ダウンロード
- [ ] 観戦モード(他人プレイのライブ)

### チュートリアル
- [ ] 初回起動ツアー
- [ ] 遊び方ページ(静的)
- [ ] インタラクティブレッスン
- [ ] 動画・GIF 埋め込み

### Web 機能
- [ ] PWA / manifest.json
- [ ] Service Worker / オフライン
- [ ] モバイルレスポンシブ
- [ ] タッチ操作 / 仮想ゲームパッド
- [ ] OGP / Twitter Card
- [ ] SNS シェアボタン
- [ ] Web Share API
- [ ] ブラウザ通知
- [ ] 自動保存 / 復帰

### コミュニティ
- [ ] フレンドリスト
- [ ] DM / メッセージ
- [ ] 公開 / 私室部屋
- [ ] チャット
- [ ] 通報 / ブロック
- [ ] 観戦
- [ ] イベント / シーズン
- [ ] お知らせ / ニュース

### 運用・法務
- [ ] 利用規約
- [ ] プライバシーポリシー
- [ ] Cookie バナー(EU)
- [ ] 連絡先 / 問い合わせ
- [ ] DMCA 申告窓口
- [ ] ステータスページ

### 不正対策
- [ ] HTTPS 強制
- [ ] HMAC / 署名
- [ ] サーバ側妥当性チェック
- [ ] リプレイ検証
- [ ] レート制限
- [ ] CAPTCHA
- [ ] 管理者用フラグ / hide / ban ツール

### アクセシビリティ
- [ ] 色覚モード
- [ ] ハイコントラスト
- [ ] 文字サイズ調整
- [ ] スクリーンリーダー対応(ARIA)
- [ ] キーボードナビゲーション
- [ ] 字幕 / 音声無効時のヒント

---

## 参考文献

調査時のアクセス日は 2026-05-21。

### TETR.IO 関連
- [TETR.IO - TetrisWiki](https://tetris.wiki/TETR.IO)
- [TETR.IO - Wiki for TETR.IO (tetrio.wiki.gg)](https://tetrio.wiki.gg/wiki/TETR.IO)
- [TETR.IO - Hard Drop Tetris Wiki](https://harddrop.com/wiki/TETR.IO)
- [TETRA CHANNEL](https://ch.tetr.io/)
- [TETRA LEAGUE - Wiki for TETR.IO](https://tetrio.wiki.gg/wiki/TETRA_LEAGUE)
- [QUICK PLAY - Wiki for TETR.IO](https://tetrio.wiki.gg/wiki/QUICK_PLAY)
- [Achievements - Wiki for TETR.IO](https://tetrio.wiki.gg/wiki/Achievements)
- [XP - Wiki for TETR.IO](https://tetrio.wiki.gg/wiki/XP)
- [Supporter - Wiki for TETR.IO](https://tetrio.wiki.gg/wiki/Supporter)
- [Chat - Wiki for TETR.IO](https://tetrio.wiki.gg/wiki/Chat)
- [Custom Room - Wiki for TETR.IO](https://tetrio.wiki.gg/wiki/Custom_Room)
- [TETR.IO Seasonal Events - TetrisWiki](https://tetris.wiki/TETR.IO/Seasonal_Events)
- [TETR.IO FAQ - Personalization](https://tetrio.github.io/faq/personalization.html)
- [TETR.IO FAQ - Mechanics](https://tetrio.github.io/faq/mechanics.html)
- [TETR.IO Privacy Policy](https://tetr.io/about/privacy/)
- [TETR.IO Patch Notes](https://tetr.io/about/patchnotes/)
- [TTR File Format](https://docs.fileformat.com/game/ttr/)
- [TETRA CHANNEL - Spectating](https://ch.tetr.io/u/spectating)
- [TETR.IO PLUS (browser extension)](https://addons.mozilla.org/en-US/firefox/addon/tetrio-plus/)

### Jstris 関連
- [Jstris](https://jstris.jezevec10.com/)
- [Jstris Guide](https://jstris.jezevec10.com/guide)
- [Jstris About](https://jstris.jezevec10.com/about)
- [Jstris Versions changelog](https://jstris.jezevec10.com/about/versions)
- [Jstris Replay viewer](https://jstris.jezevec10.com/replay)
- [Jstris - TetrisWiki](https://tetris.wiki/Jstris)
- [Jstris - Hard Drop Tetris Wiki](https://harddrop.com/wiki/JSTris)
- [jstris-guide on GitHub](https://github.com/jezevec10/jstris-guide/blob/master/guide.md)
- [jstris-multilang on GitHub](https://github.com/jezevec10/jstris-multilang)

### その他タイトル
- [Tetris Friends - Wikipedia](https://en.wikipedia.org/wiki/Tetris_Friends)
- [Tetris Friends - TetrisWiki](https://tetris.wiki/Tetris_Friends)
- [Tetris Effect - TetrisWiki](https://tetris.wiki/Tetris_Effect)
- [Tetris Effect: Connected Weekend Rituals (Steam)](https://store.steampowered.com/news/app/1003590/view/2987562614932273158)
- [Puyo Puyo Tetris 2 - Wikipedia](https://en.wikipedia.org/wiki/Puyo_Puyo_Tetris_2)
- [Puyo Puyo Tetris 2 - Puyo Nexus](https://puyonexus.com/wiki/Puyo_Puyo_Tetris_2)
- [Puyo Puyo Tetris 2 - TetrisWiki](https://tetris.wiki/Puyo_Puyo_Tetris_2)
- [Deep dive on Skill Battle (PlayStation Blog)](https://blog.playstation.com/2020/10/21/deep-dive-on-puyo-puyo-tetris-2s-new-skill-battle-mode/)

### サードパーティ / 周辺ツール
- [Tetra Stats](https://ts.dan63.by/)
- [TETRIO Statistics by Tenchi](https://www.tetrio.team2xh.net/)
- [Tetris Metrics (Benjamin Heng)](https://hbenjamin.com/post/tetris-metrics)
- [inoue (TETR.IO replay downloader)](https://github.com/szymonszl/inoue)
- [awesome-tetrio](https://github.com/Sup3rFire/awesome-tetrio)
- [Four.lol (SRS visualization)](https://four.lol/)
- [T-Spin Practice tool](https://himitsuconfidential.github.io/downstack-practice/tspin-practice.html)

### PWA / Web ゲームの参考
- [Just Tetris (PWA Tetris)](https://github.com/cch01/just-tetris)
- [HTML Tetris (PWA)](https://synacek.org/code/fun/html-tetris/)
- [pwa-tetris (Vue)](https://github.com/maurop123/pwa-tetris)
- [Tetris.im (PWA + Touch)](https://tetris.im/)
- [Hextris (PWA Lighthouse 96)](https://love2dev.com/blog/pwa-hextris/)

### 不正対策・運用
- [Server-side verification of client behavior in online games (ACM)](https://dl.acm.org/doi/10.1145/2043628.2043633)
- [6 ways to slow down cheaters with server side validation (3e8 Development)](https://3e8.io/2016/slowdown-cheaters-with-server-side-validation/)
- [How Game Developers Detect and Stop Cheating in Real-Time (Medium)](https://medium.com/@amol346bhalerao/how-game-developers-detect-and-stop-cheating-in-real-time-0aa4f1f52e0c)

### 法務 / プライバシー
- [COPPA / GDPR-K compliance guide (fish in a bottle)](https://www.fishinabottle.com/blog/what-does-coppa-and-gdpr-k-compliance-mean-for-childrens-games-fish-in-a-bottle)
- [Legal Requirements for Children's Gaming Apps (TermsFeed)](https://www.termsfeed.com/blog/childrens-gaming-apps-legal-requirements/)
- [COPPA (FTC official)](https://www.ftc.gov/legal-library/browse/rules/childrens-online-privacy-protection-rule-coppa)
- [Kids Web Services - COPPA & GDPR-K (Epic Games)](https://www.epicgames.com/help/en-US/c-Kids_Web_Services/c-General_Questions/what-is-coppa-and-gdpr-k-a000086180)

### アクセシビリティ
- [Video Game Accessibility: Examples & Best Practices (Accessibly)](https://accessiblyapp.com/blog/video-game-accessibility/)
- [Why Accessibility Testing is Crucial in Video Games (TestDevLab)](https://www.testdevlab.com/blog/accessibility-testing-in-video-games)
- [A Practical Guide to Game Accessibility (AbleGamers)](https://accessible.games/wp-content/uploads/2018/11/AbleGamers_Includification.pdf)

---

## 改訂履歴

| 日付 | 内容 |
|------|------|
| 2026-05-21 | 初版作成。Web テトリスサービスのメタ機能を網羅整理 |
