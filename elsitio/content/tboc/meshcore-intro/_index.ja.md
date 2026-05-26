+++
outputs = ["Reveal"]
title = "MeshCore 入門"
+++

{{< slide transition="zoom" transition-speed="fast" >}}

# MeshCore 入門

ディープダイブ — その正体、比較、そして気まずい部分。

---

## MeshCore とは？

- LoRa メッシュプロトコル + ファームウェアスタック — サブ GHz、長距離、低ビットレート。
- コンパニオンアプリ方式 — 無線機が「デバイス」、スマートフォンは BLE 経由の UI。
- インフラ非依存・遅延許容の通信を狙う — チャット、位置、テレメトリ。
- オープンソースファームウェア、主要な LoRa ボードに対応。

---

<section data-state="scrollable">

## MeshCore vs Meshtastic — 要点

| | **Meshtastic** | **MeshCore** |
|---|---|---|
| パケット形式 | Protobuf、フィールド拡張可能 | コンパクトなバイナリ、オペコードごとに固定 |
| ルーティング | ホップカウント・フラッド + 暗黙的重複排除 | パス指向 — フラッドで経路発見、ユニキャストで追従 |
| コンパニオンリンク | BLE/Serial/TCP 上の Protobuf | BLE/Serial 上の長さ区切りバイナリオペコード |
| ロール | 汎用ノード + 任意のルーター | 明示的なロール — chat / repeater / room / sensor |
| オンボーディング | 接続して既定チャンネルへ | 同様、ただし名前付きチャンネル（`Public` がよく知られる） |
| テレメトリ | バッテリー / 環境 / GPS をメッセージバスにネイティブ | 専用 req/resp 経由の Cayenne LPP |
| 暗号 | チャンネル毎 AES-CTR + DM 毎 ECDH | チャンネル毎 AES-CTR + DM 毎 ECDH、署名付き advert |

どちらが「優れている」と一概には言えない — 異なるトレードオフ。MeshCore は電波上で軽量、Meshtastic はノード当たりの機能が豊富。

</section>

---

## ロールについて

- **Chat (1)** — ハンドヘルド。人と話す。
- **Repeater (2)** — リレー。通常はマスト設置、商用電源、大型アンテナ。
- **Room server (3)** — サーバ級のノード、名前付き「ルーム」を提供。
- **Sensor (4)** — テレメトリ専用ノード（BME280、GPS、INA219 など）。

Advert にロールバイトが含まれるので、どのクライアントでも色分け / 重み付けできる。

---

## コンパニオンプロトコルの構造

フレームは `[opcode][payload]`、BLE 通知 1 件 = 1 フレーム。

主なオペコード：

- `0x01` `APP_START` — ハンドシェイク。
- `0x02` / `0x03` — DM / チャンネルメッセージ送信。
- `0x05` `SELF_INFO` — デバイス側 ID + 無線パラメータ。
- `0x07` — 自身の advert を送信（flood か直送）。
- `0x0E` `SET_ADVERT_LATLON` — 位置情報を radio に push。
- `0x14` `BATT_AND_STORAGE` — バッテリーテレメトリ。
- `0x27` `SEND_TELEMETRY_REQ` — peer（または self）のテレメトリ取得。
- `0x83` / `0x88` / `0x8B` — 非同期 push — メッセージ待機 / RF ログ / テレメトリ応答。

加えて、companion v3 フレームは SNR / RSSI をインライン搬送 — 信号認識 UI に必須。

---

## ハードウェアエコシステム

- **T1000-E** (Seeed) — 代表的ハンドヘルド。GPS、e-ink、小型バッテリー。
- **Heltec / LilyGO** — リピーター / ルームサーバの定番。
- **WisBlock / RAK** — センサー / 据置設置。
- **SX1262/SX1276 搭載機なら何でも** — コミュニティ移植が存在。

ファームウェアは単一の C++ コードベース、ボード別バリアントは `variants/` 配下。

---

# セクション区切り

## 「分裂」について

2026 年 4 月に何が起こったか、詳細。

---

<section data-state="scrollable">

## 分裂 — 背景

2026 年 4 月末、MeshCore コミュニティは分裂した。「the split」とは、コアチーム（Scott ら）と、それまで活発な貢献者だった Andy Kirby との公的な決別を指す。

チームの説明： [blog.meshcore.io/2026/04/23/the-split](https://blog.meshcore.io/2026/04/23/the-split)

この争点を理解する価値は二つある：

1. **誰が何を所有しているか**が明確になる — 公式サイト `meshcore.io`、GitHub org、商標、Discord、それぞれの所有が争われている、または新規構築。
2. 論争の本質 — **AI 生成コードはどこまで開示すべきか** — は、Claude / Copilot 等を使うあらゆるプロジェクトが直面する問い。

</section>

---

<section data-state="scrollable">

## 分裂 — 何が起きたか

- MeshCore は小チームで進行：**Scott**（創始者、ファームウェアリード）、**Liam Cottle**（アプリ）、**Recrof**（地図）、**FDLamotte**（Python ツール）、**Oltaco**（ブートローダ）。**Andy Kirby** もメンバー。
- Andy は **Claude Code を多用** して MeshCore スタック横断のコンポーネントを開発 — デバイス、モバイルアプリ、Web フラッシャ、設定ツールなど — それを **チームに伏せていた**。
- **2026-03-29**、Andy は **MeshCore 商標を申請**、チームには通知せず、議論を求めると応答を絶った。
- Andy は元の `meshcore.co.uk` ドメインと Discord サーバを保持、チーム曰くビジュアルデザインも複製。
- チームは「内部の人間がロボットと弁護士と組んだ」と表現し、今後は明確に「人間の手で書かれたソフトウェア」を旨とすると宣言。
- `meshcore.io`、`blog.meshcore.io`、新しい Discord で再出発。商標の決着は未了。

チームの立場は **「人間の手で書かれた」** を価値の中核に置く。そのため分裂は、コードの著者性そのものというより **開示** の問題として読める — チームが問題視したのは、AI 関与を長期間秘匿していた点。

</section>

---

<section data-state="scrollable">

## 分裂 — 複数の読み方

合理的な読者でも見方は異なる。緊張感のある複数の枠組み：

- **信頼と開示が先。** 最も強い反論は AI 利用が *秘匿* されていた点。Andy が最初から開示していれば、信頼関係も商標の経緯も違ったかもしれない。
- **著作と責任。** デバイスで動くファームウェアでは「人間の手で書かれた」は擁護できる姿勢 — どの行も人間が *理解* し、説明責任を負える意味合いがある。AI 支援コードは、深夜 3 時の不可解な障害を誰がデバッグするのかという問いを残す。
- **実用的な反論。** 現代のインフラは LLM との共著が進む。明確な境界を引くと、すでに主流の生産性向上を排除することになる — コミュニティが使うコンパイラやリンタも、ある意味では「ロボット」。
- **商標 vs オープンソース。** 確立されたオープンソースプロジェクトに対する商標取得（誰によるものでも）は、コードの執筆方法とは別問題。両方を同時に検討する必要がある。

どこに着地するかは、自分が優先する価値（透明性 / 著者性 / 実用性）次第。

</section>

---

# セクション区切り

## OpenCore — 誠実な物語

ファームウェアはオープン。公式モバイルアプリはオープンではない。

---

<section data-state="scrollable">

## OpenCore — 開いている所、閉じている所

- **ファームウェア** — [github.com/meshcore-dev/MeshCore](https://github.com/meshcore-dev/MeshCore) でオープン。MIT ライセンス。companion プロトコルの全オペコードはここから実装可能。
- **companion プロトコル仕様** — 同リポジトリ内 `docs/companion_protocol.md`。（不完全 — ソースが真実。）
- **公式 iOS / Android アプリ** — **クローズドソース。** アプリストア経由でコアチームが配布。
- **公式 Web フラッシャ** — オープンソース。
- **マップサーバ / ルームサーバ** — オープンソース。

典型的な *opencore* 構造 — プロトコルとリファレンスハードウェアは開かれており、洗練された消費者向けの面は閉じている。持続可能性の観点では理にかなうが、(a) アプリが端末で何をしているか検査したい、(b) 拡張したい、(c) 公式が意図的に作らないものを作りたい場合には不満が残る。

</section>

---

## 代替オープンクライアント

クローズドアプリの隙間を埋める二つのコミュニティ努力：

<a class="deck-link" href="https://github.com/zjs81/meshcore-open">**meshcore-open** — `zjs81/meshcore-open`
<small>サードパーティ製のオープンソース Android クライアント。実用的なコンパニオンアプリ。プラグマティックな UI。プロトコル変更に追従。</small></a>

<a class="deck-link" href="https://github.com/IoTone/offlinesns/tree/main/meshmore-sns">**Meshmore SNS** — `IoTone/offlinesns`
<small>当方の Flutter 製オープンクライアント。Pure-Dart MeshCore コーデック、NERV 風 UI、AI/M2M フック。（次のデッキ。）</small></a>

---

## なぜ二つ？

- **meshcore-open** は保守的な答え — 公式アプリの機能を、オープンに、機能毎に一致させる。優れたベースライン。
- **Meshmore SNS** は *主張のある* 答え — 移植ではなくデザインブリーフから出発して、メッシュネイティブな UX を探求。異なる設計面、異なるテーマシステム、異なる機能優先順位。

両方ともエコシステムにとって健全。一方が他方を置き換えるものではない。

---

## 続きを読む

- チームのブログ： [blog.meshcore.io](https://blog.meshcore.io)
- 分裂の投稿（原文）： [blog.meshcore.io/2026/04/23/the-split](https://blog.meshcore.io/2026/04/23/the-split)
- MeshCore ファームウェア： [github.com/meshcore-dev/MeshCore](https://github.com/meshcore-dev/MeshCore)
- meshcore-open： [github.com/zjs81/meshcore-open](https://github.com/zjs81/meshcore-open)
- Meshmore SNS： [github.com/IoTone/offlinesns](https://github.com/IoTone/offlinesns/tree/main/meshmore-sns)

---

# おわり

<a class="deck-link" href="/tboc/">← TBOC に戻る</a>
<a class="deck-link" href="/tboc/meshmore-sns/">次：Meshmore SNS →</a>
