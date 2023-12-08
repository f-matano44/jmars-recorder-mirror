# jMARS Recorder
（自分の考える最強の）音声コーパス収録用レコーダー

<img src="doc/imgs/screenshot.png" width="40%">

## 起動方法
### 動作環境
* Java 17 が動作する GUI 環境
* 注意: macOS において，セキュリティ機能によりマイク等が適切に認識されない場合があります．
    * その場合は端末から `java -jar` コマンドから実行してください．
    * example: `java -jar ~/Downloads/jMARS_Recorder-1.0.0.jar`

### Java Runtime Environment のインストール
本アプリは Java 17 に基づいて動作します．もしまだインストールされていない場合は，以下のリンクから最適な実行環境をダウンロードし，インストールしてください．`Operating System` → `Architecture` の順に選択すると探しやすいと思います．
* https://adoptium.net/temurin/releases/?package=jre&version=17

### jMARS のダウンロード
以下のリンクからダウンロード及び解答し，`jMARS_Recorder-x.y.z.jar` を好きな場所に移動してください．本アプリは直接実行する形式のため，インストールの必要はありません．
* https://www.f-matano44.jp/jmars-recorder/jMARS_Recorder-latest.tar.gz

### 実行
jar ファイルをダブルクリックして実行します．


## 収録方法
本アプリでは専用の保存形式などを使用しません．標準設定ではデスクトップ（Windows, macOSのみ．それ以外はホームフォルダ）上に `jMARS_Recorder/` フォルダ（以下，これをプロジェクトフォルダと呼びます）及び保存用フォルダ `jMARS_Recorder/wav/` を生成して，その中に収録番号を割り振った `wav` を保存します．

### マイク・スピーカーの選択方法
OS 上で設定されたマイク，スピーカーを使用します．PC 上の設定アプリから使用する機器を設定してください．アプリ起動中の設定変更が可能です．

### 収録用文章について
初期設定ではモーラバランスの取れた ROHAN コーパス\[1\] の ENDSVILLE400 サブセットが起動時に読み込まれます．もし変更したい場合はプロジェクトフォルダ内の `script.txt` を変更してください．現在は ITA コーパス形式にのみ対応しています．

### リファレンス音声の再生
収録を進めているとカタカナ語のような「どのように発音すればよいかわからない」単語に出くわす場合があります．その場合は，すでに収録された音声コーパスをダウンロードし，プロジェクトフォルダ内の `reference/` に `reference/corpus_0001.wav` のような形式で展開してください．すると `Play ref.` ボタンが有効化され，リファレンス音声を再生できるようになります．

収録済みの ROHAN コーパスとしては，以下のようなものが挙げられます．ライセンスの関係上アプリに添付できないため，もし必要な場合は各自でダウンロード・展開してください．
* https://zunko.jp/multimodal_dev/login.php
* https://voiceseven.com/7rdev/login.php

### 収録
`Start recording` ボタンを押すと録音開始され，もう一度押すと録音終了します．録音終了時，音声は自動的に発話区間を推定し，保存されます．もし，推定された発話区間が誤っている場合はスライダーを調整して修正してください．`Play` ボタンを押すと，収録された音声の（推定・調整された）発話区間のみが再生されます．

再度録音する場合はもう一度 `Start recording` ボタンを押してください．その時，先に収録したデータはメモリ上に蓄積されたままとなります．聴き比べる場合は `<< Prev` & `Next >>` ボタンを使用してください．過去の収録を呼び出した場合，現在表示されている波形のデータが自動的に保存されます．

### Next >>
次の文を収録する場合は `Next >>` ボタンを押してください．ただし現在の文章に対する録音データは保存されたもの（つまり現在表示されているもの）以外は破棄されます．

## 上級者向け情報
アプリの設定を変更したい場合は `${HOME}/.jmars_recorder.yaml` を編集してください．ただしこちらの設定に関しては，再起動するまで有効化されません．設定変更後，再起動してください．

## 開発者向け情報
Git の log を見るとバージョンの命名規則が最悪なことになっていますが，それは開発時のアプリ名が現在と違ったこと（と公開直前まで命名規則に悩んでいた）に由来します．またソースコード内に違ったアプリ名があった場合も同じです．あまり気にしないでください，単に恥ずかしいので・・・．

### 開発環境
* macOS 14.1.1 23B81 arm64 \(MacBookAir10,1\) 
* openjdk version "17.0.9" 2023-10-17
* OpenJDK Runtime Environment Homebrew (build 17.0.9+0)
* OpenJDK 64-Bit Server VM Homebrew (build 17.0.9+0, mixed mode, sharing)

## 参考文献
1. 森勢将雅：ROHAN：テキスト音声合成に向けたモーラバランス型日本語コーパス，日本音響学会誌, vol. 79, no. 1, pp. 9-17, Jan. 2023.
