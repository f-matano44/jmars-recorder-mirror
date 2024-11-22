# jMARS Recorder

**j**ava **MA**tano's user\-f**R**iendly corpu**S** **Recorder**

[\[JMARS\]](https://jmars.asu.edu/) や [\[日本メイラード学会（JMARS）\]](http://www.maillard.umin.jp/) などの組織・プロジェクトとは関係ありません.


* 音声コーパス収録用レコーダー
* ダウンロードはこちら → [\[gitlab.io\]](https://jmars-recorder-f-matano44-c1b89be0a6cc184def2f5c56a8ae3f5241af6.gitlab.io/jMARS_Recorder-latest.tgz)
    * ローカルでビルドする方法は README の最下部に記載しています。
* [\[If you want to read this in English, please use a machine translation...\]](https://translate.google.com/translate?sl=ja&tl=en&u=https://gitlab.com/f-matano44/jmars-recorder)

![macOS 上で動作している開発版アプリのスクリーンショット](doc/imgs/screenshot.png)

* リポジトリ: https://gitlab.com/f-matano44/jmars-recorder
* ミラー: https://github.com/f-matano44/jmars-recorder-mirror


## 起動方法

### 動作環境
* Java 8 が動作する GUI 環境 \(Windows, macOS etc.\)
    * ダウンロードはこちらから → [\[java.com\]](https://www.java.com/ja/)
* リファレンス音声の再生機能を使用するには VLC のインストールが必要です
    * ダウンロードはこちらから → [\[videolan.org\]](https://www.videolan.org/vlc/index.ja.html)


#### 既知の不具合 と その対策
* 倍率変更されたディスプレイにおいて表示が崩れる現象が確認されました \(全環境\)
    * `Window` \> `Reset window size` で初期状態に復帰します
* セキュリティ機能によりマイク等が認識されない現象が確認されました \(macOS\)
    * その場合はターミナルから `java -jar` で実行してください
    * 例：`$ java -jar jMARS_Recorder-20240101.jar`
* 特定のハードウェア構成においてリファレンス音声が正常に現象が確認されました \(Linux\)
    * VLC もしくはドライバ由来の問題なので現状根本的な修正方法がありません
    * ハードウェア由来の問題なので音声出力先を変更することにより対処可能です
* 特定のハードウェア構成において音声が小さく収録される現象が確認されました \(Linux\)
    * ドライバ由来の問題なので現状根本的な修正方法がありません
    * これはもう収録後に正規化することで対処するしかないとおもいます
    * そのためクリッピング判定が正常に動作しません
* Ubuntu 公式リポジトリ上の `openjdk-8-jre` を使用した場合に，収録した音声が再生できない現象が確認されました \(Ubuntu\)
    * Java Runtime に由来する問題ですので，他の JRE を使用することで回避可能です
    * [\[Azul JDKs\]](https://www.azul.com/downloads/?version=java-8-lts&os=ubuntu&architecture=x86-64-bit&package=jre#zulu) を使用することで当該問題が発生しないことを確認しました

### jMARS のダウンロード
アプリをダウンロード及び展開し，jar ファイルを好きな場所に配置してください．
本アプリは直接実行する形式のため，インストールはされません．


### 実行
jar ファイルをダブルクリックして起動します．


## 収録方法
本アプリの標準設定では，デスクトップ（Windows，macOSのみ．それ以外はホームフォルダ）上に
`jMARS_Recorder/` \(以下，プロジェクトフォルダ\) と，
`jMARS_Recorder/wav/` \(以下，保存用フォルダ\) を生成します．
収録された音声は保存用フォルダに，`wav` 形式として保存します．


### マイク・スピーカーの選択方法
OS 上で設定されたマイク，スピーカーを使用します．
そのため PC の設定アプリから使用する機器を設定してください．
アプリ起動中の設定変更も可能です．


### 収録用文章について
初期設定ではモーラバランスの取れた ROHAN コーパス \[森勢，2023\] の ENDSVILLE400 サブセットを読み込みます．
もし変更したい場合はプロジェクトフォルダ内の `script.txt` を変更してください．
現在は ITA コーパス形式にのみ対応しています．


### リファレンス音声の再生
コーパス収録の際にカタカナ語のような「どのように発音すればよいかわからない」単語が出現する場合があります．
その場合は，すでに収録された音声コーパスをダウンロードし，
プロジェクトフォルダ内の `reference/` に `corpus_0001.wav`, `corpus_0002.wav`,... のような形式で展開してください．
すると `Play ref.` ボタンが有効化され，リファレンス音声を再生できるようになります．

収録済みの ROHAN コーパスとしては，以下のようなものが挙げられます．
ライセンス上添付できないため，必要な場合は御自身でダウンロードしてください．

* https://zunko.jp/multimodal_dev/login.php
* https://voiceseven.com/7rdev/login.php


### 1 番の音声を再生
複数回に分けて音声コーパスを収録する場合，音声の品質や喋り方などが時関経過と共に変化してしまうことがあります.
そのような状況に対処するため，収録した音声の一番若い番号の音声を再生する機能があります.
参考のためにご利用ください.


### 収録
`Start recording` ボタンを押すと録音開始され，もう一度押すと録音終了します．
録音終了時，音声は自動的に発話区間を推定し，保存されます．
もし，推定された発話区間が誤っている場合はスライダーを調整して修正してください．
`Play` ボタンを押すと，収録された音声の（推定・調整された）発話区間のみが再生されます．

再度録音する場合はもう一度 `Start recording` ボタンを押してください．
その時，先に収録したデータはメモリ上に蓄積されたままとなります．
聴き比べる場合は `< Prev` ボタン， `Next >` ボタンを使用してください．
過去の収録を呼び出した場合も，現在表示されている波形のデータが自動的に保存されます．


### Next >>
次の文を収録する場合は `Next >>` ボタンを押してください．
ただし現在の文章に対する録音データは保存されたもの（つまり現在表示されているもの）以外は破棄されます．


### 上級者向け情報
アプリの設定を変更したい場合は `${HOME}/.jmars_recorder.yaml` を編集してください．
設定変更後はアプリ再起動の必要があります．


## ライセンス
[![GPLv3+](doc/imgs/gplv3-or-later.svg)](https://gitlab.com/f-matano44/jmars-recorder/-/blob/main/LICENSE.txt)


### 含まれる外部プロジェクト(アルファベット順)
* [\[Checkstyle for Java / Google Java Style (Apache License Version 2.0)](https://github.com/checkstyle/checkstyle/blob/1de91bc2e79d13860f841e8cddd85fdc54d4c1a4/src/main/resources/google_checks.xml)
* [\[jFloatWavIO (GNU LGPL Version 3)\]](https://gitlab.com/f-matano44/jfloatwavio)
* [\[ROHAN コーパス (CC0)\]](https://github.com/mmorise/rohan4600)
* [\[SnakeYAML (Apache License Version 2.0)\]](https://bitbucket.org/snakeyaml/snakeyaml/)
* [\[vlcj (GNU GPL Version 3)\]](https://github.com/caprica/vlcj)
* [\[VL-PGothic (Modified BSD License)\]](https://github.com/daisukesuzuki/VLGothic/blob/main/LICENSE.ja)


### アプリのスクリーンショットについて
README 内のスクリーンショットは
[\[Apple商標および著作権使用に関するガイドライン\]](https://www.apple.com/jp/legal/intellectual-property/guidelinesfor3rdparties.html)
の **Apple製品の描写** に基づき使用しています。


### 旧バージョン（v20231209）についてのお詫び
version 20231209 で使用していたフォントのライセンス（SIL OFL）が本アプリのライセンス（GPLv3+）と両立しないことが判明したため，BFG Repo-Cleaner を使用し過去のバージョンを含めて削除しました．
申し訳ありません．
そのためお手数ですが，version 20231209 を使用している方がいらっしゃいましたらアプリの更新（jar ファイルの置き換え）をお願いします．


## 引用するには / If you want to do cite this...

### 日本語（査読なし論文）
> 俣野文義, 森勢将雅,
``jMARS Recorder: コーパス朗読に特化した音声収録アプリの制作と検討,’’
日本音響学会 第 151 回 (2024 年春季) 研究発表会, pp.1061--1062 (2024.03).

### English (Non-peer-reviewed)

**\(A correction on November 12, 2024\)** I apologize for the mistake in the English translation of the Japanese conference name and have corrected it.

> F. Matano, M. Morise,
``jMARS Recorder: Development and consideration of a speech-database-focused recording application,''
2024 Spring Meeting Acoustical Society of Japan, pp.1061--1062 (2024.03) (in Japanese).


## 開発者向け情報
Git の log を見るとバージョンの命名規則が不規則になっていますが，開発時は現在と違ったこと（及び公開直前まで命名規則に悩んでいたこと）に由来します．
またソースコード内に違ったアプリ名があった場合も同様ですので，気にしないでください，


### 開発環境
* Java 8 + Gradle \(Kotlin DSL\)
* [\[VSCodium\]](https://github.com/VSCodium/vscodium) + [\[Checkstyle for Java\]](https://github.com/jdneo/vscode-checkstyle)


### ビルド方法
以下のコマンドを実行すると `app/build/libs/` に `jar` が生成されます．

```sh
gradle build jar
```
