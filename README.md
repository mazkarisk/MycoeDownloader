# MycoeDownloader
## 概要
2024年7月25日現在、通常のMYCOEIROINKは利用者が手動でダウンロードし、必要に応じてスタイルの統合作業などを行う必要があります。<br>
このソフトウェアは、特定のMYCOEIROINKについてダウンロードとスタイル統合作業を自動化するものです。<br>
利用したいMYCOEIROINKの配布ページなどでこのソフトウェアについての記載がある場合のみ、このソフトウェアでのダウンロードが可能です。<br>
ほとんどのMYCOEIROINK配布者はこのソフトウェアの存在すら知らないはずです。<br>
MYCOEIROINK配布者へ「あなたのMYCOEIROINKをこのソフトウェアで利用したいので方法を教えて」などと問い合わせないようお願いします。<br>
<br>

## 注意事項
一般的に、**音声合成ソフトウェアの利用規約は読み飛ばされることを想定していません**。<br>
MYCOEIROINKを利用する前に、**絶対に**そのMYCOEIROINKの利用規約を確認の上同意してください。<br>
このソフトウェアは自己責任でご利用ください。<br>
ディスク容量に余裕があれば、利用前に\"speaker_info\"フォルダのバックアップを取っておくことを推奨します。<br>
<br>

## ダウンロード方法
GitHubの画面の右の方にある「Releases」の最新版(「v〇.〇」の数字が一番大きいもの)をクリックし、遷移先の画面からダウンロードしてください。<br>
<br>

## 利用方法(MYCOEIROINK利用者側)
基本的には上から順に操作していけばいい感じになるはずです。<br>
「DL設定のDL用URL」には、MYCOEIROINK提供者から指定されたURLを入力してください。<br>
なお、松嘩りすくの場合は以下です。<br>
<https://www.dropbox.com/scl/fi/zi9w1x37ohrkqlh8agtmr/resources.csv?rlkey=nx2bbmyecqmi5cxmq8e35fi2q&dl=0>

「作業用一時フォルダの作成先」は、通常は自動入力される場所でよいはずです。<br>
アクセス権限や容量などの問題があれば手動で変更してください。<br>
ファイル移動時間の節約になるため、後述の「"speaker_info"フォルダのパス」と同じドライブが望ましいです。<br>

「"speaker_info"フォルダのパス」は、ダウンロード後にMYCOEIROINKを使用したいCOEIROINKの"speaker_info"フォルダのパスを入力してください。<br>
<br>

## 利用方法(MYCOEIROINK提供者側)
まず前提として、「ブラウザでアクセスしたら直ちにファイルダウンロードが始まるURL」を発行できるクラウドストレージサービスの利用が必要となります。<br>
開発者はDropboxを想定しています。<br>

### ①「except_model」フォルダの準備
「except_model」フォルダを準備します。<br>
このフォルダには、全スタイルが揃った状態のMYCOEIROINKフォルダからmodelフォルダの中身のみを除外したものを格納します。<br>
このフォルダはMycoeDownloaderの作業開始時点ですべてダウンロードされ、スタイルの一覧表示やサンプル音声の視聴に使用されます。<br>
「松嘩りすく」を例に挙げると以下のようなフォルダ構成となります。**「model」フォルダが空になっていることに注意してください。**<br>
```
📁except_model (親フォルダ)
　└📁d31b5442-dddb-11ec-82ba-0242ac1c0002
　　├📁icons
　　│└(全スタイル分のアイコン)
　　├📁model
　　│└(空)※中身をすべて削除する
　　├📁voice_samples
　　│└(全スタイル分のボイスサンプル)
　　├📄LICENSE.txt
　　├📄metas.json
　　├📄policy.md
　　└🖼portrait.png
```
このフォルダをzip圧縮してDLできるURLを用意しておきます。<br>
Dropboxの場合、zip圧縮はDL時点で自動で行われるはずです。<br>

### modelの準備
スタイルごとのmodelフォルダを作成します。<br>
要するにMYCOEIROINKの「model」フォルダの中身です。<br>
「松嘩りすく」の「アナウンス」スタイルを例に挙げると以下のようなフォルダ構成となります。<br>
```
📁685839221 (親フォルダ)
　├📄100epoch.pth
　└📄config.yaml
```
このフォルダをスタイルごとに、zip圧縮してDLできるURLを用意しておきます。<br>
Dropboxの場合、zip圧縮はDL時点で自動で行われるはずです。<br>

### ③「resources.csv」の準備
CSVファイル「resources.csv」を作成します。
このファイルは上記①のフォルダや各modelのダウンロードURL、および説明文を記載します。
「松嘩りすく」を例に挙げると以下のような記載となります。<br>
```
systemver,1
except_model,https://www.dropbox.com/scl/fo/6z22rkfcarrjbfjpfruck/APOFgbLSH013QagWa_ucKBA?rlkey=4uqgpg73cpe6gpw1zd9im99sw&dl=0
685839221,https://www.dropbox.com/scl/fo/lhuqckshtwfn4mrdzint3/AEkf05PEPCLYvzLaGOsTW1E?rlkey=c20a450l5yxlvqkkbj11volyi&dl=0,,,記念すべき第一声。
685839222,https://www.dropbox.com/scl/fo/y0xlk4q5g9rgdl7diyd1s/ALCrtaM2t35FS2hL3od8osI?rlkey=l7clt1kjh9swxwixv9jtpvoud&dl=0
685839223,https://www.dropbox.com/scl/fo/rdr3hgf47ayx1anb2u9wm/AIV-ya6mWOOSkcE66fsAeaU?rlkey=99x7fr8ee31ywuenfc755pt0i&dl=0
685839224,https://www.dropbox.com/scl/fo/fns1i0edlh3xnjaz6znv2/ANwE9T9mFKZqwbANYgKPRuk?rlkey=ne2vvs9m83q77a85bbogij8cc&dl=0,ｵｽｽﾒ!,,ニュートラルな話し声。
※以下省略
```

それぞれの項目には、以下の内容を記載してください。<br>
```
systemver,【固定値"1"】
except_model,【①で準備した、except_modelをDLできるURL】
【1スタイル目のID】,【②で準備した、1スタイル目のmodelフォルダをDLできるURL】,【1スタイル目の説明(左上)】,【1スタイル目の説明(左下)】,【1スタイル目の説明(右上)】,【1スタイル目の説明(右下)】
【2スタイル目のID】,【②で準備した、2スタイル目のmodelフォルダをDLできるURL】,【2スタイル目の説明(左上)】,【2スタイル目の説明(左下)】,【2スタイル目の説明(右上)】,【2スタイル目の説明(右下)】
【3スタイル目のID】,【②で準備した、3スタイル目のmodelフォルダをDLできるURL】,【3スタイル目の説明(左上)】,【3スタイル目の説明(左下)】,【3スタイル目の説明(右上)】,【3スタイル目の説明(右下)】
【4スタイル目のID】,【②で準備した、4スタイル目のmodelフォルダをDLできるURL】,【4スタイル目の説明(左上)】,【4スタイル目の説明(左下)】,【4スタイル目の説明(右上)】,【4スタイル目の説明(右下)】
※以下省略
```
説明以降は省略できます。実際、上記の『松嘩りすく』の設定ファイルでは省略を多用しています。<br>
どのような内容にするかは自由ですが、区切り文字に使われるカンマ(,)は使用しないでください。<br>
参考までに「松嘩りすく」では「左上」に「ｵｽｽﾒ!」を入れて、「左下」は未使用、「右上」や「右下」にスタイルの説明を入れています。<br>
<br>
最後に、このファイルをDLできるURLを準備しておきます。<br>

### ④「resources.csv」のDLリンクの公開
③で準備した「resources.csv」をDLできるURLを、あなたのMYCOEIROINKをDLするときの「DL設定のDL用URL」として公開します。<br>
<br>

## 連絡先
不明点や改善要望などありましたら、このソフトウェアの入手元までお願いします。

このソフトウェアの入手元が「まつかりすく」名義である場合、問い合わせ先は以下の通りです。
1. X(旧Twitter)の「@mazkarisk」
2. X(旧Twitter)の「@mazkariskpr」
3. ぼすきーの「@mazkarisk」
4. Blueskyの「@mazkarisk.bsky.social」
5. そのほか、「mazkarisk」あるいは「まつかりすく」名義のアカウント

※上記は「よく通知を見る順」です。メンションやDMなど、連絡方法は問いません。
