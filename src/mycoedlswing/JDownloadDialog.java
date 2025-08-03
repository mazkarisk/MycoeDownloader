package mycoedlswing;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import util.HttpDownloadManager;
import util.Logger;

public class JDownloadDialog extends JDialog implements AutoCloseable {

	private final JLabel labelFilename;
	private final JProgressBar progressBar;
	private final JLabel labelDownloaded;
	private final JLabel labelSpeed;
	private final JLabel labelRemain;
	//private final JButton cancelButton;
	private final long startTime;
	private long countChunks;

	public JDownloadDialog(HttpDownloadManager httpDownloadManager) {
		super(null, "Downloading...", Dialog.ModalityType.MODELESS);

		// 部品の作成
		labelFilename = new JLabel(httpDownloadManager.getFilename(), SwingConstants.LEFT);
		progressBar = new JProgressBar(0, 1000000);
		labelDownloaded = new JLabel("999999999999999 / 999999999999999 bytes (999.99%)", SwingConstants.RIGHT);
		labelSpeed = new JLabel("999999999999 bytes per second", SwingConstants.RIGHT);
		labelRemain = new JLabel("999999秒経過 - 残り999999秒", SwingConstants.RIGHT);
		//cancelButton = new JButton("中止");
		startTime = System.currentTimeMillis();
		countChunks = 0;

		// 部品の配置
		final Container dialogContainer = getContentPane();
		final GridBagLayout gblayout = new GridBagLayout();
		dialogContainer.setLayout(gblayout);

		// GridBagConstraintsの作成と共通設定
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 1; // 部品の幅は1グリッドとする
		gbc.gridheight = 1; // 部品の高さは1グリッドとする
		gbc.weighty = 1.0; // ウィンドウリサイズ時に高さを自動調整する
		gbc.fill = GridBagConstraints.BOTH; // 部品をグリッドに合わせて自動拡大する

		gbc.weightx = 1.0; // これ以降はウィンドウリサイズ時に幅を自動調整する

		JMycoeDownloaderFrame.addGridBagComponent(dialogContainer, gblayout, gbc, 0, 0, labelFilename);
		JMycoeDownloaderFrame.addGridBagComponent(dialogContainer, gblayout, gbc, 0, 1, progressBar);
		JMycoeDownloaderFrame.addGridBagComponent(dialogContainer, gblayout, gbc, 0, 2, labelDownloaded);
		JMycoeDownloaderFrame.addGridBagComponent(dialogContainer, gblayout, gbc, 0, 3, labelSpeed);
		JMycoeDownloaderFrame.addGridBagComponent(dialogContainer, gblayout, gbc, 0, 4, labelRemain);
		//JMycoeDownloaderFrame.addGridBagComponent(dialogContainer, gblayout, gbc, 0, 5, cancelButton);

		pack();
		setVisible(true);
	}

	public void updateDialog(HttpDownloadManager httpDownloadManager) {
		countChunks++;
		final long downloadedFileSize = httpDownloadManager.getDownloadedFileSize();
		final long outputFileSize = httpDownloadManager.getOutputFileSize();
		final float elapsedTime = (float) ((System.currentTimeMillis() - startTime) * 0.001);

		progressBar.setValue((int) ((float) downloadedFileSize / (float) outputFileSize * progressBar.getMaximum()));
		labelDownloaded.setText(downloadedFileSize + " / " + outputFileSize + " bytes (" + httpDownloadManager.getPercentageString() + ")");
		labelSpeed.setText((int) (downloadedFileSize / elapsedTime) + " bytes per second");
		labelRemain.setText((int) elapsedTime + "秒経過 - 残り" + (int) ((outputFileSize - downloadedFileSize) / (downloadedFileSize / elapsedTime)) + "秒");

		if (downloadedFileSize == outputFileSize) {
			Logger.put("ダウンロード完了。");
			Logger.put(labelFilename.getText());
			Logger.put(labelDownloaded.getText());
			Logger.put(labelSpeed.getText());
			Logger.put(labelRemain.getText());
			Logger.put("countChunks : " + countChunks);
		}
	}

	/**
	 * 各種リソースをクローズする。
	 */
	@Override
	public void close() {
		dispose();
	}
}
