package mycoedlswing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import util.FileUtil;
import util.HttpDownloadManager;
import util.Logger;

class JMycoeDownloaderFrame extends JFrame {

	private static final String TITLE = "MycoeDownloader v.3.1.0";
	private static final String SYSTEM_VER = "1";

	// 各ペイン
	JScrollPane headerScrollPane;
	JScrollPane centerScrollPane;
	JScrollPane footerScrollPane;

	// ヘッダーペインの中身
	JPanel headerPanel;
	JLabel labelResourcesUrl;
	JLabel labelTempDirDest;
	JLabel labelSpeakerInfoDir;
	JTextField textFieldResourcesUrl;
	JTextField textFieldTempDirDest;
	JTextField textFieldSpeakerInfoDir;
	JButton buttonAutoTempDirDest;
	JButton buttonAutoSpeakerInfoDir;
	JButton buttonStart;

	// センターペインの中身
	JPanel centerPanel;
	List<List<JPlayButton>> playButtons;
	List<JDownloadButton> downloadButtons;

	// フッターペインの中身
	JPanel footerPanel;
	JButton buttonCommit;
	JButton buttonRollback;

	// 設定値
	Map<String, String> resourcesSetting = null;
	MetasJson metasJson = null;

	/**
	 * エントリーポイント!!!!
	 * @param args 未使用
	 */
	public static void main(String[] args) {
		Logger.put("main method start");
		Logger.put(Logger.getLogFilePath().toString());
		new JMycoeDownloaderFrame();
		Logger.put("main method end");
	}

	/**
	 * デフォルトコンストラクタ
	 */
	public JMycoeDownloaderFrame() {

		// ウェルカムメッセージ
		final StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("MYCOEIROINKを利用する前に、<font color='red' size = '7'>絶対に</font>そのMYCOEIROINKの利用規約を確認の上同意してください。<br>");
		sb.append("一般的に、<font color='red'>音声合成ソフトウェアの利用規約は読み飛ばされることを想定していません</font>。<br>");
		sb.append("<br>");
		sb.append("このソフトウェアは自己責任でご利用ください。<br>");
		sb.append("ディスク容量に余裕があれば、利用前に\"speaker_info\"フォルダのバックアップを取っておくことを推奨します。<br>");
		sb.append("<br>");
		sb.append("上記を理解の上、ご利用ください。");
		sb.append("</html>");

		// YESを押す以外の応答があったらエラー
		if (!showWarningConfirm(sb.toString())) {
			return;
		}

		// ウィンドウの設定;
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new MycoeWindowAdapter(this));
		setTitle(TITLE);
		setSize(1000, 500);

		// アイコン設定
		ImageIcon icon = new ImageIcon("./mycoedl/icon.png");
	    setIconImage(icon.getImage());

		// ヘッダーパネルをスクロールペインでラップ
		headerScrollPane = new JScrollPane(initializeHeaderPane());
		headerScrollPane.getVerticalScrollBar().setUnitIncrement(8);

		// センターパネルをスクロールペインでラップ
		centerScrollPane = new JScrollPane();
		centerScrollPane.setViewportView(initializeCenterPane());
		centerScrollPane.getVerticalScrollBar().setUnitIncrement(8);

		// フッターパネルをスクロールペインでラップ
		footerScrollPane = new JScrollPane(initializeFooterPane());
		footerScrollPane.getVerticalScrollBar().setUnitIncrement(8);

		// 全体の組み立て
		getContentPane().setLayout(new BorderLayout());
		add(headerScrollPane, BorderLayout.NORTH);
		add(centerScrollPane, BorderLayout.CENTER);
		add(footerScrollPane, BorderLayout.SOUTH);

		// 可視化
		setVisible(true);
	}

	// ペインの初期化

	/**
	 * ヘッダーペインの中身を初期化
	 * @return Pane 作成したオブジェクト
	 */
	private JPanel initializeHeaderPane() {
		final GridBagLayout gblayout = new GridBagLayout();
		headerPanel = new JPanel();
		headerPanel.setLayout(gblayout);

		// 設定の読み込み
		final Settings settings = Settings.readSettings();

		// 部品の作成
		labelResourcesUrl = new JLabel("DL設定(resources.csv)のDL用URL : ", SwingConstants.RIGHT);
		labelTempDirDest = new JLabel("作業用一時フォルダの作成先 : ", SwingConstants.RIGHT);
		labelSpeakerInfoDir = new JLabel("\"speaker_info\"フォルダのパス : ", SwingConstants.RIGHT);
		textFieldResourcesUrl = new JTextField(settings.getResourcesUrl());
		textFieldTempDirDest = new JTextField(settings.getTempDirDest());
		textFieldSpeakerInfoDir = new JTextField(settings.getSpeakerInfoDir());
		buttonAutoTempDirDest = new JButton("自動設定");
		buttonAutoSpeakerInfoDir = new JButton("自動設定");
		buttonStart = new JButton("この設定を保存して変更作業を開始する");

		// GridBagConstraintsの作成と共通設定
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 1; // 部品の幅は1グリッドとする
		gbc.gridheight = 1; // 部品の高さは1グリッドとする
		gbc.weighty = 1.0; // ウィンドウリサイズ時に高さを自動調整する
		gbc.fill = GridBagConstraints.BOTH; // 部品をグリッドに合わせて自動拡大する

		gbc.weightx = 0.0; // これ以降はウィンドウリサイズ時に幅を自動調整しない

		addGridBagComponent(headerPanel, gblayout, gbc, 0, 0, labelResourcesUrl);
		addGridBagComponent(headerPanel, gblayout, gbc, 0, 1, labelTempDirDest);
		addGridBagComponent(headerPanel, gblayout, gbc, 0, 2, labelSpeakerInfoDir);
		addGridBagComponent(headerPanel, gblayout, gbc, 2, 1, buttonAutoTempDirDest);
		addGridBagComponent(headerPanel, gblayout, gbc, 2, 2, buttonAutoSpeakerInfoDir);

		gbc.weightx = 1.0; // これ以降はウィンドウリサイズ時に幅を自動調整する

		addGridBagComponent(headerPanel, gblayout, gbc, 1, 0, textFieldResourcesUrl);
		addGridBagComponent(headerPanel, gblayout, gbc, 1, 1, textFieldTempDirDest);
		addGridBagComponent(headerPanel, gblayout, gbc, 1, 2, textFieldSpeakerInfoDir);

		gbc.fill = GridBagConstraints.NONE; // これ以降は部品をグリッドに合わせて自動拡大しない
		gbc.gridwidth = 2; // これ以降は部品の幅を2グリッドとする

		addGridBagComponent(headerPanel, gblayout, gbc, 0, 3, buttonStart);

		// ボタン押下時のプログラム
		buttonAutoTempDirDest.addActionListener(e -> setTempDirDest());
		buttonAutoSpeakerInfoDir.addActionListener(e -> setSpeakerInfoDir());
		buttonStart.addActionListener(e -> start());

		return headerPanel;
	}

	/**
	 * センターペインの中身を初期化
	 * @return Pane 作成したオブジェクト
	 */
	private JPanel initializeCenterPane() {
		final GridBagLayout gblayout = new GridBagLayout();
		centerPanel = new JPanel();
		centerPanel.setLayout(gblayout);

		playButtons = new ArrayList<List<JPlayButton>>();
		downloadButtons = new ArrayList<JDownloadButton>();

		// metas.jsonの有効性チェック
		if (metasJson == null || !metasJson.isValid()) {
			Logger.put("センターペインの初期化をスキップします。");
			return centerPanel;
		}

		// GridBagConstraintsの作成と共通設定
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST; // 中身を左揃えにする

		// 部品を表形式で配置
		for (int i = 0; i < metasJson.getStyleIds().size(); i++) {
			final String speakerName = metasJson.getSpeakerName();
			final String speakerUuid = metasJson.getSpeakerUuid();
			final String styleId = metasJson.getStyleIds().get(i);
			final String styleName = metasJson.getStyleNames().get(i);
			final String styleSettings[] = (resourcesSetting == null || resourcesSetting.get(styleId) == null) ? new String[] { "" } : resourcesSetting.get(styleId).split(",");
			final String styleDescription1 = (styleSettings.length >= 2) ? styleSettings[1] : "";
			final String styleDescription2 = (styleSettings.length >= 3) ? styleSettings[2] : "";
			final String styleDescription3 = (styleSettings.length >= 4) ? styleSettings[3] : "";
			final String styleDescription4 = (styleSettings.length >= 5) ? styleSettings[4] : "";
			final String portraitPathString = Paths.get(textFieldTempDirDest.getText(), "temp", speakerUuid, "portrait.png").toString();
			final String iconPathString = Paths.get(textFieldTempDirDest.getText(), "temp", speakerUuid, "icons", styleId + ".png").toString();

			// サンプル音声再生ボタンの作成
			final List<JPlayButton> playButtonsInThisLine = new ArrayList<JPlayButton>();
			for (int j = 0; j < 3; j++) {
				final String sampleFileName = styleId + "_" + String.format("%03d", j + 1) + ".wav";
				final String samplePathString = Paths.get(textFieldTempDirDest.getText(), "temp", speakerUuid, "voice_samples", sampleFileName).toString();
				playButtonsInThisLine.add(new JPlayButton(samplePathString));
			}
			playButtons.add(playButtonsInThisLine);

			// ダウンロードボタンの作成
			final String dlUrl = styleSettings[0];
			final String dlDestination = Paths.get(textFieldTempDirDest.getText(), "temp").toString();
			final String tempDestination = Paths.get(textFieldTempDirDest.getText(), "temp", speakerUuid, "model", styleId).toString();
			final String finalDestination = Paths.get(textFieldSpeakerInfoDir.getText(), speakerUuid, "model", styleId).toString();
			final JDownloadButton buttonDownload = new JDownloadButton(dlUrl, dlDestination, tempDestination, finalDestination);
			downloadButtons.add(buttonDownload);

			// 配置

			int x = 0;
			addGridBagComponent(centerPanel, gblayout, gbc, ++x, i, createIconLabels(portraitPathString, 36));
			addGridBagComponent(centerPanel, gblayout, gbc, ++x, i, createVAlignedLabels(speakerName/*, speakerUuid*/));
			addGridBagComponent(centerPanel, gblayout, gbc, ++x, i, createIconLabels(iconPathString, 36));
			addGridBagComponent(centerPanel, gblayout, gbc, ++x, i, createVAlignedLabels(styleName, styleId));
			for (int j = 0; j < 3; j++) {
				addGridBagComponent(centerPanel, gblayout, gbc, ++x, i, playButtonsInThisLine.get(j));
			}
			addGridBagComponent(centerPanel, gblayout, gbc, ++x, i, buttonDownload);
			addGridBagComponent(centerPanel, gblayout, gbc, ++x, i, createVAlignedLabels(styleDescription1, styleDescription2));
			addGridBagComponent(centerPanel, gblayout, gbc, ++x, i, createVAlignedLabels(styleDescription3, styleDescription4));
		}

		return centerPanel;
	}

	/**
	 * フッターペインの中身を初期化
	 * @return Pane 作成したオブジェクト
	 */
	JPanel initializeFooterPane() {
		// 部品の作成と配置
		footerPanel = new JPanel();
		footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		buttonCommit = new JButton("変更を適用して終了");
		buttonRollback = new JButton("変更をなかったことにして終了");
		footerPanel.add(buttonCommit);
		footerPanel.add(buttonRollback);

		// ボタン押下時のプログラムを登録
		buttonCommit.addActionListener(e -> commit());
		buttonRollback.addActionListener(e -> rollback());

		// フッターパネルのボタンは初期状態では無効化しておく
		buttonCommit.setEnabled(false);
		buttonRollback.setEnabled(false);

		return footerPanel;
	}

	// ヘルパメソッド

	/**
	 * グリッド配置を簡略化するヘルパメソッド
	 * @param parent 親コンテナ
	 * @param gblayout 親コンテナに設定してあるGridBagLayoutオブジェクト
	 * @param gbc 設定済みのGridBagConstraintsオブジェクト
	 * @param gridx グリッド上の追加位置(X)
	 * @param gridy グリッド上の追加位置(Y)
	 * @param child 追加したいコンポーネント
	 */
	static void addGridBagComponent(Container parent, GridBagLayout gblayout, GridBagConstraints gbc, int gridx, int gridy, Component child) {
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gblayout.setConstraints(child, gbc);
		parent.add(child);
	}

	/**
	 * ラベルの垂直配置を簡略化するヘルパメソッド
	 * @param texts 垂直配置したラベルに表示したい文字列
	 * @return ラベル部品を垂直配置したパネル
	 */
	private static JPanel createVAlignedLabels(String... strings) {
		final JPanel panel = new JPanel();
		final BoxLayout layoutStyle = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(layoutStyle);
		for (int i = 0; i < strings.length; i++) {
			panel.add(new JLabel(" " + strings[i] + " "));
		}
		return panel;
	}

	/**
	 * 画像表示用ラベルの作成を簡略化するヘルパメソッド
	 * @param pathString ラベルに表示したい画像のパス
	 * @param height 画像をリサイズする高さ
	 * @return ラベル部品
	 */
	private static JLabel createIconLabels(String pathString, int height) {
		final ImageIcon originalIcon = new ImageIcon(pathString);
		final Image originalImage = originalIcon.getImage();
		final Image scaledImage = originalImage.getScaledInstance(-1, height, Image.SCALE_SMOOTH);
		final ImageIcon scaledIcon = new ImageIcon(scaledImage);
		final JLabel label = new JLabel(scaledIcon);
		return label;
	}

	// ボタンクリック時の処理

	/**
	 * 「作業用一時フォルダの作成先」自動設定ボタン押下時の処理
	 */
	private void setTempDirDest() {
		final String defaultTempDirDest = Settings.getDefaultTempDirDest();

		// 入力欄が空なら直ちに設定する。
		if (textFieldTempDirDest.getText().trim().length() == 0) {
			textFieldTempDirDest.setText(defaultTempDirDest);
			return;
		}

		// 入力欄に既に異なる値が入っている場合、確認後に上書きする。
		if (!defaultTempDirDest.equals(textFieldTempDirDest.getText())) {
			final String confirmMessage = "<html>作業用一時フォルダの作成先を上書きしますか？<br>変更前：" + textFieldTempDirDest.getText() + "<br>変更後：" + defaultTempDirDest + "</html>";
			if (showQuestionConfirm(confirmMessage)) {
				// OKが押されたら実行
				textFieldTempDirDest.setText(defaultTempDirDest);
			}
		}
	}

	/**
	 * 「"speaker_info"フォルダのパス」自動設定ボタン押下時の処理
	 */
	private void setSpeakerInfoDir() {
		final String defaultSpeakerInfoDir = Settings.getDefaultSpeakerInfoDir();

		// デフォルト値が取得できない場合はエラー。
		if (defaultSpeakerInfoDir.trim().length() == 0) {
			final StringBuilder sb = new StringBuilder();
			sb.append("<html>");
			sb.append("\"speaker_info\"フォルダのパスを自動設定できません。<br>");
			sb.append("自動設定するには、このソフトウェアを以下のどちらかの場所から起動してください。<br>");
			sb.append("・\"speaker_info\"フォルダがあるフォルダの中<br>");
			sb.append("・\"speaker_info\"フォルダがあるフォルダにあるフォルダの中");
			sb.append("</html>");
			showWarningMessage(sb.toString());
			return;
		}

		// 入力欄が空なら直ちに設定する。
		if (textFieldSpeakerInfoDir.getText().trim().length() == 0) {
			textFieldSpeakerInfoDir.setText(defaultSpeakerInfoDir);
			return;
		}

		// 入力欄に既に異なる値が入っている場合、確認後に上書きする。
		if (!defaultSpeakerInfoDir.equals(textFieldSpeakerInfoDir.getText())) {
			final StringBuilder sb = new StringBuilder();
			sb.append("<html>");
			sb.append("\"speaker_info\"フォルダのパスを自動設定して上書きしますか？<br>");
			sb.append("変更前：" + textFieldSpeakerInfoDir.getText() + "<br>");
			sb.append("変更後：" + defaultSpeakerInfoDir);
			sb.append("</html>");
			if (showQuestionConfirm(sb.toString())) {
				// OKが押されたら実行
				textFieldSpeakerInfoDir.setText(defaultSpeakerInfoDir);
			}
		}
	}

	/**
	 * 変更開始ボタン押下時の処理
	 */
	private void start() {
		Logger.put("●変更開始ボタン押下");

		// 設定の保存
		Settings.saveSettings(textFieldResourcesUrl.getText(), textFieldTempDirDest.getText(), textFieldSpeakerInfoDir.getText());

		// 「DL設定(resources.csv)のDL用URL」の入力チェック
		final String resourcesUrlString = textFieldResourcesUrl.getText().trim();
		if (resourcesUrlString.length() <= 0) {
			showErrorMessage("「DL設定(resources.csv)のDL用URL」に、MYCOEIROINK配布者から指示されたURLを入力してください。");
			return;
		}

		// 「作業用一時フォルダの作成先」textFieldTempDirDest.getText()
		final String tempDirDestString = textFieldTempDirDest.getText().trim();
		if (tempDirDestString.length() <= 0) {
			showErrorMessage("「作業用一時フォルダの作成先」を入力してください。");
			return;
		}
		if (!Paths.get(tempDirDestString).toFile().isDirectory()) {
			showErrorMessage("「作業用一時フォルダの作成先」で指定されたディレクトリが存在しません。");
			return;
		}

		// 「"speaker_info"フォルダのパス」の入力チェック
		final String speakerInfoDirString = textFieldSpeakerInfoDir.getText().trim();
		if (speakerInfoDirString.length() <= 0) {
			showErrorMessage("「\"speaker_info\"フォルダのパス」に、MYCOEIROINKのダウンロード先となる\"speaker_info\"フォルダのパスを入力してください。");
			return;
		}
		if (!Paths.get(speakerInfoDirString).toFile().isDirectory()) {
			showErrorMessage("「\"speaker_info\"フォルダのパス」で指定されたディレクトリが存在しません。");
			return;
		}
		if (!Paths.get(speakerInfoDirString).getFileName().toString().equals("speaker_info")) {
			showErrorMessage("「\"speaker_info\"フォルダのパス」には、名前が「speaker_info」のフォルダを指定してください。");
			return;
		}
		if (Paths.get(speakerInfoDirString).getFileName().equals(Paths.get(textFieldTempDirDest.getText().trim()).getFileName())) {
			showErrorMessage("「作業用一時フォルダの作成先」と「\"speaker_info\"フォルダのパス」には、別のフォルダを指定してください。");
			return;
		}

		// リソース設定ファイルのダウンロード
		resourcesSetting = MycoeLogic.downloadResourcesSetting(textFieldResourcesUrl.getText());
		if (resourcesSetting == null) {
			showErrorMessage("リソース設定を\"" + resourcesUrlString + "\"からダウンロードできませんでした。");
			return;
		}
		// リソース設定のバージョンチェック
		if (!SYSTEM_VER.equals(resourcesSetting.get("systemver"))) {
			final StringBuilder sb = new StringBuilder();
			sb.append("<html>");
			sb.append("リソース設定のバージョン(" + resourcesSetting.get("systemver") + ")が、<br>");
			sb.append("このソフトウェアが対応しているバージョン(" + SYSTEM_VER + ")と異なります。<br>");
			sb.append("ソフトウェアを更新してください。");
			sb.append("</html>");
			showErrorMessage(sb.toString());
			return;
		}

		// 一時フォルダが既に存在していたら確認の上削除
		final File tempDir = new File(textFieldTempDirDest.getText(), "temp");
		if (tempDir.exists()) {
			if (!showWarningConfirm("<html>一時フォルダが残っていました。削除してよいですか？<br>※一時フォルダ：\"" + tempDir.getPath() + "\"</html>")) {
				// YESを押す以外の応答があったらエラー
				showErrorMessage("一時フォルダ(" + tempDir.getPath() + ")の削除を拒否されました。");
				return;
			}

			try {
				FileUtil.deleteFilesRecursively(tempDir);
			} catch (Exception exception) {
				Logger.put(exception.toString());
				exception.printStackTrace();
				showErrorMessage("一時フォルダ(" + tempDir.getPath() + ")の削除に失敗しました。");
				return;
			}
		}

		// 一時フォルダを(再)作成
		try {
			Files.createDirectory(tempDir.toPath());
		} catch (IOException exception) {
			Logger.put(exception.toString());
			exception.printStackTrace();
			showErrorMessage("一時フォルダ(" + tempDir.getPath() + ")の作成に失敗しました。");
			return;
		}

		// モデル除外データのダウンロード
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			public Void doInBackground() {
				boolean succeeded = true;
				textFieldResourcesUrl.setEnabled(false);
				textFieldTempDirDest.setEnabled(false);
				textFieldSpeakerInfoDir.setEnabled(false);
				buttonAutoTempDirDest.setEnabled(false);
				buttonAutoSpeakerInfoDir.setEnabled(false);

				final String tempDirPathString = Paths.get(textFieldTempDirDest.getText(), "temp").toString();
				String zipPathString = null;
				try (HttpDownloadManager httpDownloadManager = HttpDownloadManager.connect(resourcesSetting.get("except_model"), Paths.get(tempDirPathString)); JDownloadDialog dialog = new JDownloadDialog(httpDownloadManager);) {
					zipPathString = httpDownloadManager.getOutputFilepathString();
					while (httpDownloadManager.next()) {
						dialog.updateDialog(httpDownloadManager);
					}
				} catch (Exception exception) {
					Logger.put(exception.toString());
					exception.printStackTrace();
					showErrorMessage("ZIPファイル(" + zipPathString + ")の解凍に失敗しました。");
					succeeded = false;
				}

				// モデル除外データの解凍
				if (succeeded) {
					try {
						FileUtil.unzipFile(Paths.get(zipPathString), tempDirPathString);
					} catch (IOException exception) {
						Logger.put(exception.toString());
						exception.printStackTrace();
						showErrorMessage("ZIPファイル(" + zipPathString + ")の解凍に失敗しました。");
						succeeded = false;
					}
				}

				if (succeeded) {
					final String metasJsonPathString = Paths.get(FileUtil.getFirstDirectory(new File(tempDirPathString)), "metas.json").toString();
					metasJson = new MetasJson(metasJsonPathString);
				}

				centerScrollPane.setViewportView(initializeCenterPane());
				textFieldResourcesUrl.setEnabled(!succeeded);
				textFieldTempDirDest.setEnabled(!succeeded);
				textFieldSpeakerInfoDir.setEnabled(!succeeded);
				buttonAutoTempDirDest.setEnabled(!succeeded);
				buttonAutoSpeakerInfoDir.setEnabled(!succeeded);
				buttonStart.setEnabled(!succeeded);
				buttonCommit.setEnabled(succeeded);
				buttonRollback.setEnabled(succeeded);

				return null;
			}

			public void done() {
			}
		};
		worker.execute();

	}

	/**
	 * 変更確定ボタン押下時の処理
	 */
	private void commit() {
		Logger.put("●変更確定ボタン押下");

		// 一時フォルダ内のの話者フォルダ名を取得する。唯一あるディレクトリが話者フォルダであることを前提とする。
		final String tempSpeakerDirPathString = FileUtil.getFirstDirectory(Paths.get(textFieldTempDirDest.getText(), "temp").toFile());
		final String tempSpeakerDirName = Paths.get(tempSpeakerDirPathString).getFileName().toString();
		final String prodSpeakerDirPathString = Paths.get(textFieldSpeakerInfoDir.getText(), tempSpeakerDirName).toString();

		// 最終確認
		final StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("変更を確定するため、<font color='green'>一時フォルダ</font>から<font color='red'>本番フォルダ</font>へ上書きします。<br>");
		sb.append("※<font color='green'>一時フォルダ</font>……\"" + tempSpeakerDirPathString + "\"<br>");
		sb.append("※<font color='red'>本番フォルダ</font>……\"" + prodSpeakerDirPathString + "\"<br>");
		sb.append("<br>");
		sb.append("(詳細)<br>");
		sb.append("①<font color='red'>本番</font>⇒<font color='green'>一時</font>に既存のモデル(\"model\"フォルダの中身)を移動<br>");
		sb.append("②<font color='red'>本番</font>から話者フォルダ(\"" + tempSpeakerDirName + "\"フォルダ)を削除<br>");
		sb.append("③<font color='green'>一時</font>⇒<font color='red'>本番</font>に話者フォルダ(\"" + tempSpeakerDirName + "\"フォルダ)を移動<br>");
		sb.append("④<font color='red'>本番</font>の不要なボイスサンプル(\"voice_samples\"フォルダの中身)の削除<br>");
		sb.append("⑤<font color='red'>本番</font>の不要なアイコン(\"icons\"フォルダの中身)の削除<br>");
		sb.append("⑥<font color='red'>本番</font>のmetas.jsonを存在するスタイルのみで再作成して上書き<br>");
		sb.append("<br>");
		sb.append("変更を確定しますか？");
		sb.append("</html>");
		if (!showQuestionConfirm(sb.toString())) {
			// YESを押す以外の応答があったら中断
			return;
		}

		try {
			// 本番に存在するモデルを取得
			final List<String> prodModelList = MycoeLogic.searchModels(Paths.get(prodSpeakerDirPathString, "model").toString());
			Logger.put("prodModelList : " + prodModelList);

			// 本番⇒一時にモデルを移動
			for (int i = 0; i < prodModelList.size(); i++) {
				final File srcFile = Paths.get(prodSpeakerDirPathString, "model", prodModelList.get(i)).toFile();
				final File dstFile = Paths.get(tempSpeakerDirPathString, "model", prodModelList.get(i)).toFile();
				FileUtil.moveFilesRecursively(srcFile, dstFile);
			}

			// この時点で一次側にモデルが存在しているスタイルのスタイルIDを列挙。
			// 最終的に残すスタイルとして確定する。
			final List<String> tempModelList = MycoeLogic.searchModels(Paths.get(tempSpeakerDirPathString, "model").toString());
			Logger.put("tempModelList : " + tempModelList);

			// 本番から話者フォルダを丸ごと削除
			FileUtil.deleteFilesRecursively(new File(prodSpeakerDirPathString));

			// 一時⇒本番に話者フォルダを丸ごと移動
			FileUtil.moveFilesRecursively(new File(tempSpeakerDirPathString), new File(prodSpeakerDirPathString));

			// 不要なアイコンとボイスサンプルを削除する。
			MycoeLogic.deleteUnnecessaryIconsAndVoiceSamples(metasJson, prodSpeakerDirPathString, tempModelList);

			// metas.jsonを、存在するスタイルだけで再作成
			final String newMetasJsonString = MetasJson.makeMetasJsonString(metasJson, tempModelList);
			final String newMetasJsonPath = Paths.get(prodSpeakerDirPathString, "metas.json").toString();
			Files.write(Paths.get(newMetasJsonPath), newMetasJsonString.getBytes(StandardCharsets.UTF_8));

			// 完了メッセージの表示
			showInformationMessage("<html>処理が完了しました。COEIROINKを起動してスタイルを確認してください。<br>問題があれば、本ソフトウェアの起動時に表示される連絡先に問い合わせてください。</html>");

			// 一時フォルダを削除
			final File tempDir = new File(textFieldTempDirDest.getText(), "temp");
			if (showQuestionConfirm("<html>一時フォルダはこのソフトウェアからはもう使用しません。削除しますか？<br>※一時フォルダ：\"" + tempDir.getPath() + "\"</html>")) {
				try {
					FileUtil.deleteFilesRecursively(tempDir);
				} catch (Exception exception) {
					Logger.put(exception.toString());
					exception.printStackTrace();
					showErrorMessage("一時フォルダ(" + tempDir.getPath() + ")の削除に失敗しました。");
					return;
				}
			}

			// 設定を破棄
			resourcesSetting = null;
			metasJson = null;

			// センターペインの初期化
			centerScrollPane.setViewportView(initializeCenterPane());

			// 入力部品の属性設定
			textFieldResourcesUrl.setEnabled(true);
			textFieldTempDirDest.setEnabled(true);
			textFieldSpeakerInfoDir.setEnabled(true);
			buttonAutoTempDirDest.setEnabled(true);
			buttonAutoSpeakerInfoDir.setEnabled(true);
			buttonStart.setEnabled(true);
			buttonCommit.setEnabled(false);
			buttonRollback.setEnabled(false);

		} catch (IOException exception) {
			exception.printStackTrace();
			Logger.put(exception.toString());

			final StringBuilder sbError = new StringBuilder();
			sbError.append("<html>");
			sbError.append("ファイルの移動に失敗しました。<br>");
			sbError.append("\"speaker_info\"フォルダの中身等が破壊された可能性があります。<br>");
			sbError.append("問題があれば、本ソフトウェアの起動時に表示される連絡先に問い合わせてください。<br>");
			sbError.append("その際、ログファイルを提出頂く場合があります。");
			sbError.append("</html>");
			showErrorMessage(sbError.toString());
		}
	}

	/**
	 * 変更破棄ボタン押下時の処理
	 */
	private void rollback() {
		Logger.put("●変更破棄ボタン押下");

		if (!showQuestionConfirm("「変更を適用して終了」ボタンを押すまで変更は適用されません。本当に終了しますか？")) {
			return;
		}

		// 一時フォルダを削除
		final File tempDir = new File(textFieldTempDirDest.getText(), "temp");
		if (showQuestionConfirm("<html>一時フォルダはこのソフトウェアからはもう使用しません。削除しますか？<br>※一時フォルダ：\"" + tempDir.getPath() + "\"</html>")) {
			try {
				FileUtil.deleteFilesRecursively(tempDir);
			} catch (Exception exception) {
				Logger.put(exception.toString());
				exception.printStackTrace();
				showErrorMessage("一時フォルダ(" + tempDir.getPath() + ")の削除に失敗しました。");
				return;
			}
		}

		// 設定を破棄
		resourcesSetting = null;
		metasJson = null;

		// センターペインの初期化
		centerScrollPane.setViewportView(initializeCenterPane());

		// 入力部品の属性設定
		textFieldResourcesUrl.setEnabled(true);
		textFieldTempDirDest.setEnabled(true);
		textFieldSpeakerInfoDir.setEnabled(true);
		buttonAutoTempDirDest.setEnabled(true);
		buttonAutoSpeakerInfoDir.setEnabled(true);
		buttonStart.setEnabled(true);
		buttonCommit.setEnabled(false);
		buttonRollback.setEnabled(false);

	}

	/**
	 * ウィンドウ関連イベント
	 */
	class MycoeWindowAdapter extends WindowAdapter {
		final JMycoeDownloaderFrame parentComponent;

		MycoeWindowAdapter(JMycoeDownloaderFrame parentComponent) {
			this.parentComponent = parentComponent;
		}

		/**
		 * ウィンドウ終了時の処理(確認ウィンドウを表示)
		 */
		public void windowClosing(WindowEvent e) {
			Logger.put("●閉じるボタン押下");
			if (buttonCommit.isEnabled()) {
				if (!parentComponent.showWarningConfirm("「変更を適用して終了」ボタンを押すまで変更は適用されません。本当に終了しますか？")) {
					return;
				}
			}
			Logger.put("～おわり～");
			System.exit(0);
		}
	}

	/**
	 * 警告メッセージを表示する。
	 * @param message メッセージとして表示したい文言。
	 * @return OKが押されたらtrue、それ以外の応答があればfalse。
	 */
	private void showInformationMessage(String message) {
		Logger.put("メッセージを表示：" + System.lineSeparator() + message);
		JOptionPane.showMessageDialog(this, message, TITLE, JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * 警告メッセージを表示する。
	 * @param message メッセージとして表示したい文言。
	 * @return OKが押されたらtrue、それ以外の応答があればfalse。
	 */
	private void showWarningMessage(String message) {
		Logger.put("メッセージを表示：" + System.lineSeparator() + message);
		JOptionPane.showMessageDialog(this, message, TITLE, JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * エラーメッセージを表示する。
	 * @param message メッセージとして表示したい文言。
	 * @return OKが押されたらtrue、それ以外の応答があればfalse。
	 */
	private void showErrorMessage(String message) {
		Logger.put("メッセージを表示：" + System.lineSeparator() + message);
		JOptionPane.showMessageDialog(this, message, TITLE, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * 確認メッセージ(警告)を表示する。
	 * @param message メッセージとして表示したい文言。
	 * @return OKが押されたらtrue、それ以外の応答があればfalse。
	 */
	private boolean showWarningConfirm(String message) {
		Logger.put("確認メッセージ(警告)を表示：" + System.lineSeparator() + message);
		final int ret = JOptionPane.showConfirmDialog(this, message, TITLE, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

		// OKを押す以外の応答があったらエラー
		if (ret == JOptionPane.OK_OPTION) {
			Logger.put("●OK押下");
			return true;
		} else {
			Logger.put("●OK以外押下");
			return false;
		}
	}

	/**
	 * 確認メッセージ(質問)を表示する。
	 * @param message メッセージとして表示したい文言。
	 * @return OKが押されたらtrue、それ以外の応答があればfalse。
	 */
	private boolean showQuestionConfirm(String message) {
		Logger.put("確認メッセージ(質問)を表示：" + System.lineSeparator() + message);
		final int ret = JOptionPane.showConfirmDialog(this, message, TITLE, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

		// OKを押す以外の応答があったらエラー
		if (ret == JOptionPane.OK_OPTION) {
			Logger.put("●OK押下");
			return true;
		} else {
			Logger.put("●OK以外押下");
			return false;
		}
	}
}