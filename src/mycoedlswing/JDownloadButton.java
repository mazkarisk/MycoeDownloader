package mycoedlswing;

import java.awt.event.ActionEvent;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.SwingWorker;

import util.FileUtil;
import util.HttpDownloadManager;
import util.Logger;

public class JDownloadButton extends JButton {

	private final String dlUrl;
	private final String dlDestination;
	private final String tempDestination;
	private final String finalDestination;

	private final boolean alreadyDownloaded;
	private boolean downloading;
	private boolean downloadSucceeded;

	/**
	 * コンストラクタ
	 * @param filepath このボタンを押したときに再生させたい音声ファイルのパス
	 * @param dlDestination DLしたZIPを配置したいパス
	 * @param tempDestination 一時フォルダ側のモデルフォルダのパス
	 * @param finalDestination 本番のモデルフォルダのパス
	 */
	public JDownloadButton(String dlUrl, String dlDestination, String tempDestination, String finalDestination) {
		this.dlUrl = dlUrl;
		this.dlDestination = dlDestination;
		this.tempDestination = tempDestination;
		this.finalDestination = finalDestination;

		alreadyDownloaded = MycoeLogic.isModelExists(this.finalDestination);

		downloading = false;
		downloadSucceeded = false;

		addActionListener(e -> onClick(e));
		update();
	}

	/**
	 * ボタン押下時の処理
	 * @param event ActionEventオブジェクト
	 */
	private void onClick(ActionEvent event) {

		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			public Void doInBackground() {
				Logger.put("●モデルダウンロードボタン押下");
				Logger.put("モデルを\"" + dlUrl + "\"からダウンロード＆解凍開始");

				// ボタンを現在のサイズで固定
				setPreferredSize(getSize());

				// ボタンの状態を更新
				downloading = true;
				update();

				// ダウンロード
				String downloadedFilepathString = null;
				try (HttpDownloadManager httpDownloadManager = HttpDownloadManager.connect(dlUrl, Paths.get(dlDestination)); JDownloadDialog dialog = new JDownloadDialog(httpDownloadManager);) {
					// DL先ディレクトリを作成しておく
					Files.createDirectories(Paths.get(tempDestination));

					downloadedFilepathString = httpDownloadManager.getOutputFilepathString();
					while (httpDownloadManager.next()) {
						dialog.updateDialog(httpDownloadManager);
					}

				} catch (Exception exception) {
					Logger.put(exception.toString());
					exception.printStackTrace();
					return null;
				}

				// ダウンロードしたファイルの解凍
				try {
					setText("解凍中");
					FileUtil.unzipFile(Paths.get(downloadedFilepathString), tempDestination);
					downloadSucceeded = true;
				} catch (Exception exception) {
					Logger.put(exception.toString());
					exception.printStackTrace();
					return null;
				}

				return null;
			}

			public void done() {
				// ボタンのサイズ固定を解除
				setPreferredSize(null);

				// ボタンの状態を更新
				downloading = false;
				update();

				Logger.put("モデルを\"" + dlUrl + "\"からダウンロード＆解凍完了");
			}
		};
		worker.execute();
	}

	/**
	 * ボタンテキストと有効状態の自動切換処理
	 */
	private void update() {
		String text = "";
		boolean enabled = false;

		// ダウンロード状況による分岐
		if (alreadyDownloaded) {
			text = "ダウンロード済み";
		} else if (dlUrl == null || dlUrl.trim().isEmpty()) {
			text = "【ＵＲＬ未記載】";
		} else if (downloading) {
			text = "ダウンロード中…";
		} else if (downloadSucceeded) {
			text = "ダウンロード成功";
		} else {
			text = "ダウンロードする";
			enabled = true;
		}

		setText(text);
		setEnabled(enabled);
	}
}
