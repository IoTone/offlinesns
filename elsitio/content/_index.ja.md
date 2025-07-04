+++
outputs = ["Reveal"]
title = "Offline SNS"

+++

{{< slide transition="zoom" transition-speed="fast" >}}

# Meashtastic / Offline SNS

---

-   メッシュネットワーク 101
-   Meshtastic の概要
-   デモ
-   プロジェクト（コーディングもできます）
-   行動への呼びかけ

---

メッシュネットワーク 101

---

<section data-noprocess>
  <p>典型的なメッシュネットワーク構成
  <img src="/images/meshexamples.jpg" />
</section>

---

<section data-noprocess>
  <p>これらは単なる抽象画です
  <img src="/images/mesh_fabric.jpg" />
</section>

---

前の写真について考えてみましょう

-   メッシュ内の交差する各点は、他の任意の点から X ホップです
-   メッシュファブリックは破れに耐えられるため強力です
-   他のメッシュ「ネット」が、一部の領域が弱い場合に負荷を運ぶことができます
-   ネットの「エッジ」は何かに接続される可能性があります
-   人を使ってメッシュの動作を示すクイックデモ

---

メッシュネットワークの長所・短所

-   +理論上、インフラストラクチャフリー
-   +理論上、ゼロコンフィグ
-   +自己修復能力
-   -帯域幅が低い
-   -エネルギー効率が低い可能性
-   -プロトコルオーバーヘッドが高く、スペクトラムがより早く飽和する
-   -専用ハードウェアが必要な場合がある

---

# Meshtastic 概要

---

## 問題提起

メッセージ、位置情報、テレメトリーを共有し、タイムリーな情報を伝達するための、常に利用可能で、公共（プライバシー重視）のネットワークが必要です。これは使用コストゼロで、自然や人的介入に耐性があり、有線相互接続なしでメトロ・WAN 規模のエリアで動作できるものです。

---

## 多くの解決策（問題を解決しない）

-   ~~5G~~
-   ~~WiFi~~
-   ~~BLE~~
-   ~~イーサネット~~

---

<section data-noprocess data-background-color="#80d280">
  <H1>解決策：Meshtastic（LoRaベース）</H1>
  <p>「手頃な価格の低電力デバイス上で動作するよう構築された、オープンソース、オフグリッド、分散型、メッシュネットワーク」
  <img src="/images/meshtastic_typelogo.svg" />
</section>

---

## LoRa をベースとしたオープンソース

https://lora-alliance.org/

---

<section data-noprocess>
  <H1>LoRaはグローバル仕様</H1>
  <img src="/images/lora-topology.webp" />
</section>

---

## 5G や WiFi と比較して LoRa が特別な理由

-   使用にライセンスが不要
-   使用にサブスクリプションが不要
-   理論的な範囲は 2 つのノード間で 9km
-   真のメッシュトポロジーで動作可能
-   エンドツーエンドでセキュア・プライベート可能

---

## LoRa の弱点

-   オープン標準だが、アプリケーション層を指定せず、MAC 層（OSI 用語）で止まる
-   非常に低いデータレートと高いレイテンシー
-   ネットワークが 4G/5G や WiFi ほど普及していない
-   最も一般的なコンピューティングデバイスには LoRa ラジオがない

---

## Meshtastic は複数の問題を解決

1. 多くの LoRa ラジオデバイスで動作するファームウェアを提供
2. P2P ネットワーキングのためのアプリケーションプロトコルを提供
3. PC、Mac、Linux、iOS、Android、Chrome で動作するクライアントアプリケーションを提供

---

## 全てのコードは GitHub にあります

https://github.com/meshtastic

---

## 使用例

### 1. プライバシー中心のネットワーク

「通信を所有していないなら、それは無料ではない」

---

## 使用例

### 2. 緊急通信

日本は天候や環境による災害の機会が多いです。
これがおそらく人々がデバイスを手元に置く主な理由です。

---

## 使用例

### 3. P2P チャット

これは一般ユーザーによって挙げられる主なアプリケーションです...友人や家族との会話。
注意：サバイバルゲーム/ペイントボールをプレイする際のライブゲームマップに使用する人もいます。

---

## 使用例

### 4. 2 点間の長距離通信

よく言及される使用例の一つは、遠隔地との IoT 通信の設定です。WiFi ネットワークの拡張は可能ですが、電力要件が低い必要がある場合はより高価でおそらくオーバーキルです。

---

## 使用例

### 5. エクストリームスポーツ/アウトドアアドベンチャー

日本では 5G ネットワークが機能しない場所を見つけるのは難しいです。しかし海上や洞窟などでは、確実な通信手段を持つことが良いでしょう。アメリカでは、アウトドアスポーツ（ハイキング、スキーなど）中に位置を共有することが非常に一般的な使用例として挙げられます。

---

## 使用例

### 6. 産業 IoT/貨物・物流

簡単な計算により、IoT デプロイメントでの N 個のノードについて、mBIoT/5G サービスの使用と比較して、無料の Meshtastic/LoRa ネットワークの使用による節約は大きいことが示されます。すべての使用例に完璧ではありませんが、多くの使用例には十分です。

---

## 使用例

### 8. 市民データ/HAM ラジオサイドチャネル

最近人気の使用例は個人データ収集プロジェクトです

---

## 使用例

### 9. イベント通信

大規模な祭り、政治的抗議などでは、仲間との通信を維持することが困難な場合があります。皆が携帯電話を使おうとすると、セルラー通信が不安定になることがよくあります。Meshtastic はある程度スケールできますが、1000 以上のアクティブノードが一箇所にあると、飽和状態になる可能性があります。

---

## 使用例

### 10. ゼロコスト/サブスクリプションフリーネットワーク

WiFi や Bluetooth のように、Meshtastic はサブスクリプションなしで動作します。プライベートファンドネットワークで動作していない限り、支払う「キャリア」はありません。唯一のコストはネットワークの構築です：アンテナ、ルーター、リピーター、エッジデバイス、ケーブル、ソーラー。

---

## 通信の近接性（ハイパーローカルと MWAN スケール）

-   < 1m OK（0 ホップ）：近くの単純なデバイス通信に適している
-   1m-300m（0 ホップ）：シグナリング、ロギング、チャットにおいて WiFi や BLE に代わる適切な選択肢
-   < 1km（>= 1 ホップ）：小規模ネットワーク、2 つのサイト間のポイントツーポイント通信
-   < 5km（>= 2 ホップ）：マルチサイトネットワーク、近隣地域のカバレッジ
-   < 9km（>= 3 ホップ）：メトロエリアネットワーク規模、移動するものの追跡、堅牢

---

## デバイスズー

いくつかのデバイスを見てみましょう

---

{{< slide background-image="/images/WiFi-LoRa-32-structure-chart-1024x610.png" >}}

---

{{< slide background-image="/images/T-ECHO_3-558631079.jpg" >}}

---

{{< slide background-image="/images/lilygo-tdeck.jpg" >}}

---

{{< slide background-image="/images/RAKMeshtasticStarterKit_5.webp" >}}

---

{{< slide background-image="/images/wouldnt-something-like-this-be-awesome-v0-1dkm167bzpl91.webp" >}}

---

{{< slide background-image="/images/solar-vehicle-trackers-v0-jqyqfmz9vcaf1.webp" >}}

---

{{< slide background-image="/images/solar-vehicle-trackers-v0-jqyqfmz9vcaf1.webp" >}}

---

{{< slide background-image="/images/built-my-first-node-to-accompany-my-uconsoles-lora-board-v0-ss5kf2a9mx9f1.webp" >}}

---

## デバイスを入手する

-   Amazon：ここで全て入手できます https://www.amazon.co.jp/s?k=meshtastic
-   Heltec：元のメーカーです https://heltec.org/
-   Lilygo：素晴らしいデザインを持っています https://lilygo.cc/collections/lora-or-gps
-   Alibaba：商品を見つけるのは困難ですが、すべてが販売されています。デバイスを注文しているのか、デバイス用の靴下を注文しているのかを確認するのが困難です（説明で紹介されています）。
-   IoTone Japan：8 台の在庫があります sales@iotone.jp

---

## デモ

-   ピアツーピアチャット/範囲テスト
-   Chrome を使用した Meshtastic ファームウェアのフラッシュ
-   ケースの 3D プリント
-   Meshtastic CLI デモ
-   その他？

---

## プロジェクト（1 ページ目）

-   Meshtastic iOS の日本語化
-   Meshtastic ドキュメントの日本語化（おそらく AI が助けてくれるでしょう...巨大なタスクです）
-   学習用：プライベートネットワークのセットアップ
-   電話の代わりにデバイスを操作する Meshtastic CLI のセットアップ
-   ハードウェア 3D ケース

---

## プロジェクト（2 ページ目）

-   HeltecV3「例外」の「技適」マークの申請：https://www.tele.soumu.go.jp/e/sys/equ/tech/
-   任意の Meshtastic ネットワーク上での Pub/Sub プロトコル用アプリケーション層の作成（ファームウェア変更なし）
-   チャットチャンネルを使用して AI 同士が対話し、IoT デバイスの活動を調整できる「ロボット」通信言語の作成

---

## 初の九州 WAN を構築しましょう

「高い場所」にいくつかのノードが必要です：

-   九州大学
-   福岡タワー
-   博多タワー
-   あなたの「マンション」、オフィスの屋上

---

## リソース

-   http://meshtastic.org
-   世界のノードリスト（日本は 7 未満）https://meshmap.net/
-   iOS アプリに JA ローカライゼーションを追加する PR https://github.com/meshtastic/Meshtastic-Apple/pull/1292
-   IoTone Japan のオフライン SNS ノート https://note.com/truedata_iotone
-   日本の Meshtastic グループ（FB）：https://www.facebook.com/groups/1749997532422254
-   グローバルコミュニティ：https://github.com/meshtastic/meshtastic/blob/master/docs/community/local-groups.mdx

## その他のリソース

-   クライアント設定、リピーター設定などの素晴らしいヒント：https://pole1.co.uk/meshtastic-roles/

## Github リポジトリ / Meshtastic

-   興味深い Olama/NodeJS 統合 https://github.com/NerdsCorp/meshtastic-controller

---

## 動画

-   高速スタート：https://www.youtube.com/watch?v=gH-K9fRuhfQ&t=8s
-   入門：https://www.youtube.com/watch?v=DUz6cVSaSl4
-   完全なオフグリッドセットアップ：https://www.youtube.com/watch?v=_v11m2FQQZU&t=466s
-   入門/アンテナ：https://www.youtube.com/watch?v=F6w4QtYE6L8
-   Meshtastic vs Meshcore：https://www.youtube.com/watch?v=tXoAhebQc0c

## 行動への呼びかけ

-   参加してください：Github：https://github.com/IoTone/offlinesns
-   連絡先：（David）djk @ iotone.jp

---

{{< slide background-image="/images/meshtastic_wide.png" >}}

---

ありがとうございました
Thank you
