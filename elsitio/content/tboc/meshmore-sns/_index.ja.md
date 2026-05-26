+++
outputs = ["Reveal"]
title = "Meshmore SNS"
+++

{{< slide transition="zoom" transition-speed="fast" >}}

# Meshmore SNS

### *ネットワークが暗転したときの SNS。*

MeshCore 向けの Flutter クライアント — 主張があり、テーマ可換、AI を堂々と回路に組み込んで構築。

---

## 概要

- **オープンソース** の MeshCore コンパニオンアプリ、Flutter 製（iOS + Android + Linux + macOS）。
- **Pure-Dart MeshCore コーデック** ライブラリ（`packages/meshcore/`）— プロトコル準拠、エンコード/デコード層に Bluetooth やプラットフォーム依存なし。CLI、テスト、他アプリから利用可能。
- **UI 研究** — 公式アプリの移植から始めない場合、メッシュネイティブな UX はどう見えるか。

---

<section data-state="scrollable">

## 設計目標

- **オフラインファースト。** ログイン無し、クラウド無し、見知らぬ相手へのテレメトリ送信無し。表示されるものは全て電波経由か、デバイスの BLE リンク経由のみ。
- **未来のアスティ。** NERV 風 HUD、ターミナル等幅タイポグラフィ、信号認識ビジュアル。メッシュは画面上の *物理オブジェクト*、チャット一覧ではない。
- **精度に誠実。** ピアの到達距離が分かれば描く。分からなければ目に見えて劣化させる（破線、「?」帯）。捏造しない。
- **言語の継ぎ目。** 英語と日本語が同期、i18n は骨組み、追加品ではない。
- **サイロではなくフック。** AI 統合、機器間通信、テーマカスタマイズ — 三つの一級拡張点として組み込む。
- **出自に透明性。** Claude と共同制作。下の「開示」参照。

</section>

---

<section data-state="scrollable">

## 重視する三点

1. **メッシュを空間として見せる。** Globe view、fabric-survey の織物、街路マップ、標高プロファイル、equal-grid HUD — 各画面はピアを「位置付き・信号特性付き」のオブジェクトとして扱う。
2. **到達距離について真実を語る。** ピア毎の信号バジェット円（RSSI + 距離 + LoRa 感度フロア + リピータ型）、実際の `outPath` リピータチェーンを通すトポロジ描画。自分への星形では無い。
3. **邪魔をしない。** バックグラウンド維持はオンにした時のみ。位置自動公開は明示的オプトインのみ。アナリティクス無し、リモート設定無し。

</section>

---

## スクリーンショット

<section data-noprocess>
  <small>（プレースホルダ — <code>static/images/meshmore/</code> にスクリーンショットを置けばここに表示されます。）</small>
  <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 1em; margin-top: 1em;">
    <div><img src="/images/meshmore/dashboard.png" alt="Dashboard" onerror="this.style.opacity=0.2"/><small>ダッシュボード</small></div>
    <div><img src="/images/meshmore/globe.png" alt="Globe view" onerror="this.style.opacity=0.2"/><small>地球儀</small></div>
    <div><img src="/images/meshmore/grid.png" alt="Equal grid" onerror="this.style.opacity=0.2"/><small>等間隔グリッド</small></div>
    <div><img src="/images/meshmore/fabric.png" alt="Fabric survey" onerror="this.style.opacity=0.2"/><small>面調査</small></div>
    <div><img src="/images/meshmore/elevation.png" alt="Elevation profile" onerror="this.style.opacity=0.2"/><small>標高プロファイル</small></div>
    <div><img src="/images/meshmore/dm.png" alt="DM" onerror="this.style.opacity=0.2"/><small>ダイレクトメッセージ</small></div>
  </div>
</section>

---

<section data-state="scrollable">

## ロードマップ

**リリース済み（1.0.x 系）**

- BLE コンパニオンリンク + 再接続 + バックグラウンド維持。
- チャンネル + DM、永続チャット履歴。
- ノード検出、advert 取込、コンタクト同期。
- LoRa 地域プリセット + ロケール別自動選択。
- Globe view、equal-grid HUD、街路マップ、面調査。
- 標高プロファイル（R45）。
- 位置自動公開（R36）。
- セルフ + ピアテレメトリ、標高ルーティング（R47）。
- ピア毎の信号バジェット到達円（R48）。
- `outPath` リピータチェーンによるトポロジ描画（R49）。

**次（`meshmore-sns-spec.md` の草案）**

- AI フック：受信 DM 用のプラガブル「インテント」検出器（要約 / 翻訳 / 分類、*全て端末内 LLM*）。
- 機器間通信：スキーマとハンドラ付きの構造化ペイロードチャンネル。
- テーマスタジオ：色、フォント、サウンドパック、アニメーションカーブを自前で。
- マップ代替案（F2）— ベクタータイル、オフラインタイルバンドル。
- 永続テレメトリチャート（ピア毎のバッテリー / 温度 / 気圧推移）。

</section>

---

<section data-state="scrollable">

## 設計された三つの拡張フック

**AI 統合** — 受信 DM / チャンネルメッセージは、表示前にローカルのインテントハンドラを通過可能。ハンドラは Dart プラグイン、本人署名、ネットワークアクセス無し。用途：日英自動翻訳、長文要約、「緊急」分類、返信下書き。

**機器間（M2M）** — チャットチャンネルの隣に並ぶ型付きペイロードチャンネル。スキーマは JSON-Schema、ハンドラはスキーマ URI で登録。気象局が publish、ロガーが subscribe、互いを意識しなくて良い。

**テーマ** — 全てをリスキン可能。配色、タイポグラフィ、サウンドパレット、アニメーションカーブ、ボトムシートの動作まで。テーマは `themes/` 配下のバンドル（TOML + アセット）；同梱は NERV デフォルト + クリーンな「オフィス」代替案。

主張：メッシュ無線をトランシーバとして考えるのを止めよう。これはプログラム可能で、永続的で、低帯域の基板。

</section>

---

# 開示

## Claude と共同制作。

簡潔で誠実なセクション。

---

<section data-state="scrollable">

## Claude と共同制作 — 無加工版

- Dart MeshCore コーデック、Flutter UI、テスト、（そう）このスライドも、Claude Code と *協働* で書いている。
- ループ内の人間が方向を示し、変更を受諾/拒否し、ハードウェア絡みの挙動をデバッグし、マージボタンを所有する。全てのコミットは land 前にレビュー。
- プロトコル実装は公式 MeshCore ファームウェアソースと **クロスチェック** — 全オペコード、全フィールドレイアウト。コーデックは「AI 支援、人間検証」であり、「AI 捏造」ではない。
- テストカバレッジが安全網：200+ テスト、実機の hex キャプチャに対する適合性ベクタを含む。コーデックがずれればスイートが捕捉する。
- これは **about 画面** と **ここ** で開示する。利用や拡張を検討する誰もが、情報を持って判断できる。

これは [MeshCore「分裂」](/tboc/meshcore-intro/#/9) が照らし出した立場 — 当方は、それを引き起こした側とは開示軸の反対端を選ぶ。選択に同意しないのは構わないが、隠していると言うことはできない。

</section>

---

## 入手先

<a class="deck-link" href="https://github.com/IoTone/offlinesns/tree/main/meshmore-sns">**ソース** — `IoTone/offlinesns` (tree/main/meshmore-sns)
<small>Flutter アプリ + pure-Dart `packages/meshcore` ライブラリ。MIT ライセンス。</small></a>

<a class="deck-link" href="https://github.com/IoTone/offlinesns/blob/main/meshmore-sns/meshmore-sns-spec.md">**仕様** — `meshmore-sns-spec.md`
<small>生きた設計ドキュメント — リリース済み、草案、保留中の三層。</small></a>

<a class="deck-link" href="https://github.com/IoTone/offlinesns/blob/main/meshmore-sns/meshmore-sns-UX-brief.md">**UX ブリーフ** — `meshmore-sns-UX-brief.md`
<small>NERV アスティと 6 つのコンセプトに繋がったビジュアル / インタラクションブリーフ。</small></a>

---

# おわり

<a class="deck-link" href="/tboc/">← TBOC に戻る</a>
<a class="deck-link" href="/tboc/intro/">Intro から再スタート →</a>
