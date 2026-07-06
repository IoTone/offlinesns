+++
outputs = ["Reveal"]
title = "デバイス 2025–2026"
+++

{{< slide transition="zoom" transition-speed="fast" >}}

# デバイス 2025–2026

インフラ構築のためのハードウェア — 屋外・ソーラー対応・新世代デバイス

---

## なぜハードウェアが重要か

実環境で機能するメッシュネットワークを構築するには、ソフトウェアより先にハードウェア層を正しく選ぶ必要があります。

- **屋外ノード** — 日光・雨・結露・温度変化に耐える必要があります
- **ソーラーノード** — 屋上や電柱に設置し、バッテリー交換なしで永続稼働
- **リピーター** — 山頂や建物間のカバレッジには高TX出力とアンテナゲインが必要
- **フォームファクターはプロトコル非依存** — 同じエンクロージャーとソーラーキットがMeshCoreとMeshtasticの両方で使用可能

2025–2026年のハードウェア世代で明確な主要プラットフォームが確立: **nRF52840 + SX1262**

---

<section data-state="scrollable">

## プラットフォーム基盤: nRF52840 + SX1262

現在出荷されている屋外・ソーラーデバイスのほぼすべてがこの組み合わせで構築されています。

| | 採用理由 |
|---|---|
| **nRF52840** (Nordic) | 低消費電力ARM Cortex-M4、BLE 5.3対応、大容量フラッシュ/RAM |
| **SX1262** (Semtech) | SX1276の後継。同消費電流で+3 dBm高効率、低受信電流、LoRa 2.0対応 |

**新興技術:** **LR1121** (Semtech, 2024)はサブGHz+2.4 GHzの同時動作に対応。ハードウェアは登場しているが、2026年中頃時点でMeshCoreとMeshtastic両方のファームウェアサポートはまだアルファ段階。

</section>

---

# 屋外対応 / IP防塵防水

屋外環境に耐えるインフラノード

---

<section data-state="scrollable">

## Heltec MeshTower V2

**電柱設置インフラの最有力候補 — IP66、30 dBm、ソーラー統合**

| スペック | 値 |
|---|---|
| IP等級 | IP66 |
| チップセット | nRF52840 + SX1262 |
| TX出力 | 30 dBm (1 W) |
| GPS | あり |
| ソーラー | 10Wパネル + 8.4 Ah LiFePO₄ |
| ディスプレイ | なし（ヘッドレスインフラノード） |
| 価格 | 約$109–129 |
| プロトコル | MeshCore ✓、Meshtastic ✓ |

</section>

---

<section data-state="scrollable">

## Seeed SenseCAP Solar P1 Pro

**コスパ最良のソーラーノード — MeshCore専用SKU、$89.90**

| スペック | 値 |
|---|---|
| IP等級 | IPX5 |
| チップセット | nRF52840 + SX1262 |
| バッテリー | 13,400 mAh |
| ソーラーパネル | 5W |
| GPS | あり |
| 価格 | $89.90 |
| プロトコル | MeshCore専用SKU ✓、Meshtastic ✓ |

seeedstudio.comではMeshCore設定済みSKUを販売。`Public`チャンネルで出荷時設定済み。

---

## RAK WisMesh Repeaterシリーズ

| | **Mini** | **PRO** |
|---|---|---|
| IP等級 | IP67 | IP67 |
| TX出力 | 22 dBm | 30 dBm |
| ソーラー | 蓋内蔵パネル | 10W外部パネル |
| 価格 | ~$99 | ~$149 |
| プロトコル | MeshCore ✓ | MeshCore ✓ |

WisBlockエコシステムで環境センサーやRS-485などの拡張が可能。

</section>

---

<section data-state="scrollable">

## 新型ハンドヘルド 2025–2026

| デバイス | 価格 | 特徴 | リリース |
|---|---|---|---|
| LilyGO T-LoRa Pager | ~$83 | QWERTYキーボード、GNSS、NFC | 2025年8月 |
| Heltec Mesh Node T096 | $30–34 | 28 dBm、L1+L5 GPS、ソーラー入力 | 2026年4月 |
| LilyGO T-Echo Plus | ~$64 | 2,400 mAh、三脚マウント、e-paper | 2025年12月 |
| muzi works R1 Neo | ~$89 | アルミ筐体、デュアルGPS | 2025年 |
| LilyGO T-Watch Ultra | ~$78 | IP65、AMOLED、腕時計型 | 2026年4月 |

**Heltec Mesh Node T096**はMeshCore v1.15（2026年4月）で追加。28 dBmとデュアルバンドGPSがこの価格帯では際立つ。

</section>

---

<section data-state="scrollable">

## インフラ構築の推奨構成（グローバル）

| 目的 | 推奨デバイス | 理由 |
|---|---|---|
| 恒久的屋外リピーター | Heltec MeshTower V2 | IP66、30 dBm、ソーラー一体型 |
| 低コスト屋外ノード | Seeed SenseCAP Solar P1 Pro | $89.90、MeshCore SKU、13.4 Ah |
| 高密度都市クラスター | RAK WisMesh Repeater Mini | IP67、WisBlockエコシステム |
| 山頂高出力 | RAK WisMesh Repeater PRO | IP67、30 dBm、10Wパネル |
| DIY防水ノード | Heltec Solar Kit + WiFi LoRa 32 V4 | IP67エンクロージャー |
| 携帯フィールド端末 | Heltec Mesh Node T096 | $30–34、28 dBm、L1+L5 GPS |

**アンテナについて:** 全屋外ノードで付属ゴムアンテナをチューニング済みグラスファイバー無指向性アンテナに変えるだけで実効範囲が約2倍になることが多い。

⚠️ **日本国内での使用は次のスライドを必ず確認すること。** HeltecはJPバンド対応品なし。

</section>

---

# 日本向け — 技適・周波数・購入先

---

<section data-state="scrollable">

## 技適は必須

日本の920 MHz帯（ARIB STD-T108）には固有のルールがあります。電波法上、技術基準適合証明（技適）のない無線機器の使用は違法です。趣味・テスト目的でも例外はありません。

| ルール | 内容 |
|---|---|
| **技適必須** | 全ての送信機器に技術基準適合証明が必要 |
| **Meshtasticリージョン設定** | **JP**を選択（AS923ではなく、ARIB準拠の日本専用チャンネルプラン） |
| **最大EIRP** | 20 mW（免許不要局）。LBT（送信前キャリアセンス）必須 |

購入前に必ず確認: [総務省 技適データベース](https://www.tele.soumu.go.jp/giteki/)

</section>

---

<section data-state="scrollable">

## 日本対応状況（2026年中頃）

| デバイス | 技適 | 備考 |
|---|---|---|
| **Seeed SenseCAP P1 Pro** | ✅ 技適済 | seeedstudio.comより直接購入可 |
| **Seeed Wio-SX1262キット** | ✅ 201-250230 | モジュール単体；マルツ/DigiKey JPで在庫あり |
| **LilyGO T-Echo [日本認証版]** | ✅ 技適済 | **「For Japan Certification」SKUを必ず選ぶこと** |
| **SenseCAP T1000-E** | ⏳ 審査中 | Amazon.co.jp (B0DJ6KGXKB)；コミュニティ報告によると審査中 |
| **M5Stack C6L Meshtastic** | ✅ 在庫あり | スイッチサイエンス ¥4,290；SX1262 + ESP32-C6 |
| **RAK4631 WisBlock Core** | ⚠️ 未確認 | マルツに在庫あり；使用前にRAKに技適を確認 |
| **Heltec MeshTower V2** | ❌ JP版なし | EU/USバンドのみ — 日本での使用不可 |
| **Heltec Mesh Node T096** | ❌ JP版なし | AS923/JP SKUなし |
| **Heltec WiFi LoRa 32 V4** | ❌ JP版なし | 同上 |
| **muzi works R1 Neo** | ❌ JP版なし | 915/868 MHzのみ |
| **LilyGO T-Echo Plus 920MHz** | ⚠️ 認証ラベルなし | 920 MHzハードウェアだが日本認証SKUではない |
| **RAK WisMesh Repeater** | ⚠️ 未確認 | AS923ファームウェアSKUあり；技適未確認 |

</section>

---

<section data-state="scrollable">

## 日本国内での購入先

**国内在庫あり（日本から発送）:**

| ショップ | 取扱品 | URL |
|---|---|---|
| **スイッチサイエンス** | M5Stack C6L Meshtastic（¥4,290、技適済） | switch-science.com |
| **マルツ / DigiKey JP** | Seeed Wio-SX1262モジュール（¥1,451、技適 201-250230）、RAK4631コア（¥4,523） | marutsu.co.jp |
| **Amazon.co.jp** | LilyGO T-Echo [日本認証版]（4バリアント）、SenseCAP T1000-E（審査中） | amazon.co.jp |

**直輸入（海外発送、2〜3週間）:**

| ショップ | 取扱品 | 備考 |
|---|---|---|
| **seeedstudio.com** | SenseCAP P1 Pro（約¥13,000）、Wio-SX1262キット、T1000-E | JP認証SKUを選択 |
| **lilygo.cc / AliExpress** | T-Echo「920-923MHz [For Japan Certification]」 | T-Echo PlusとT-LoRa PagerはこのラベルなしのためNG |
| **store.rakwireless.com** | WisMesh Repeater（AS923 SKU）、WisBlockモジュール | 注文前にRAKへ技適確認 |

**日本向け2026年推奨:**

屋外ソーラーノードなら **SenseCAP P1 Pro**（技適済、直接購入）。ハンドヘルドなら **LilyGO T-Echo [日本認証版]**（Amazon.co.jp）。Heltec製品は現時点で日本での合法運用不可。

</section>

---

# リージョン設定

法的要件とコミュニティ標準設定の地域別まとめ

---

<section data-state="scrollable">

## 法規制要件（リージョン別）

以下の周波数・出力・デューティサイクル・LBT要件はMeshtasticとMeshCore両方に適用されます。

| リージョン | 周波数帯 (MHz) | 最大TX (dBm) | デューティサイクル | LBT | 対象地域 |
|---|---|---|---|---|---|
| **JP** | 920.8–927.8 | **16** | 100% | **必須 (ARIB)** | 日本 |
| **US** | 902–928 | 30 | 100% | 不要 | 米国・カナダ |
| **EU_868** | 869.4–869.65 | 27 | **10%**（直近1時間） | 不要 | EU・英国・スイス |
| **EU_433** | 433.0–434.0 | 12 | **10%** | 不要 | EU (433 MHz) |
| **ANZ** | 915–928 | 30 | 100% | 不要 | 豪州・NZ |
| **KR** | 920–923 | — | 100% | 不要 | 韓国 |
| **TW** | 920–925 | 27 | 100% | 不要 | 台湾 |
| **CN** | 470–510 | 19 | 100% | 不要 | 中国 |
| **MY_919** | 919–924 | 27 | 100% | 不要 | マレーシア |
| **SG_923** | 917–925 | 20 | 100% | 不要 | シンガポール |
| **TH** | 920–925 | 16 | 100% | 不要 | タイ |
| **IN** | 865–867 | 30 | 100% | 不要 | インド |
| **RU** | 868.7–869.2 | 20 | 100% | 不要 | ロシア |
| **UA_868** | 868.0–868.6 | 14 | **1%** | 不要 | ウクライナ |
| **LORA_24** | 2400–2483.5 | 10 | 100% | 不要 | 全世界 (2.4 GHz) |

**JPリージョン補足:** 無線機器の送信出力16 dBm（約40 mW）だが、ARIB規定のEIRP上限は20 mW（約13 dBm）。利得0 dBiのアンテナを使用するか、送信出力を下げてEIRPを20 mW以下に抑えること。LBTはARIB STD-T108で義務付け。

</section>

---

<section data-state="scrollable">

## MeshCore vs Meshtastic — リージョン別無線デフォルト設定

両エコシステムとも標準LoRAを使用するが、デフォルト設定が異なる。同じ地域でも設定が異なるため**相互通信不可**。

| リージョン | **MeshCore**コミュニティ標準 | **Meshtastic**デフォルト |
|---|---|---|
| **US / ANZ** | 910.525 MHz · SF7 · BW 62.5 kHz · CR 4/5 | Long Fast (SF11 · 250 kHz) |
| **EU_868** | 869.618 MHz · SF8 · BW 62.5 kHz · CR 4/5 · 22 dBm · デューティ10% | 869.525 MHz · Long Fast (SF11 · 250 kHz) |
| **JP** | 公式プリセットなし（2026年中頃）— 920–927 MHz手動設定 | JPリージョン · Long Fast |
| **共通** | コミュニティ単位で1つの周波数・BW・SFを共有 | プリセット+リージョンの組み合わせ |

**主な違い:**

- **MeshCore**: コミュニティごとに1つの周波数・BW・SFを設定。プリセット層なし — 同じネットワークの全ノードが同じ値を使う必要あり。柔軟性は低いが設定ズレが起きにくい。
- **Meshtastic**: リージョン（周波数帯）とモデムプリセット（SF・BW）を別々に設定。同じリージョンでもプリセットが違えば通信不可。

</section>

---

<section data-state="scrollable">

## Meshtasticモデムプリセット一覧

| プリセット | BW | SF | CR | データレート | リンクバジェット | 備考 |
|---|---|---|---|---|---|---|
| Short Turbo | 500 kHz | 7 | 4/5 | 21.9 kbps | 140 dB | EU不可（BW超過） |
| Short Fast | 250 kHz | 7 | 4/5 | 10.9 kbps | 143 dB | |
| Short Slow | 250 kHz | 8 | 4/5 | 6.3 kbps | 145.5 dB | |
| Medium Fast | 250 kHz | 9 | 4/5 | 3.5 kbps | 148 dB | |
| Medium Slow | 250 kHz | 10 | 4/5 | 2.0 kbps | 150.5 dB | |
| Long Turbo | 500 kHz | 11 | 4/8 | 1.3 kbps | 150 dB | EU不可 |
| **Long Fast** | **250 kHz** | **11** | **4/5** | **1.1 kbps** | **153 dB** | **デフォルト・パブリックネット標準** |
| Long Moderate | 125 kHz | 11 | 4/8 | 0.34 kbps | 156 dB | |
| Long Slow | 125 kHz | 12 | 4/8 | 0.18 kbps | 158.5 dB | 最長距離 |

**日本 (JP) の選択指針:** EIRP 20 mW制限内で最大到達距離を得るには Long Slow または Long Moderate。コミュニティ互換性を優先するなら Long Fast を送信出力下げて使用。

**MeshCore比較:** BW 62.5 kHz + SF7–9 ≈ Medium Fastと同等の到達距離・データレートだが、より狭い帯域でISMバンドの混雑に強い。

</section>

---

# 終わり

<a class="deck-link" href="/tboc/">← TBOCに戻る</a>
<a class="deck-link" href="/tboc/meshcore-intro/">← MeshCore入門</a>
