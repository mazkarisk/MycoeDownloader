package mycoedlswing;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import util.Logger;

public class Settings {

	// メンバ変数
	private String resourcesUrl;
	private String tempDirDest;
	private String speakerInfoDir;

	// getterとsetter
	/**	@return resourcesUrl */
	public String getResourcesUrl() {
		return resourcesUrl;
	}

	/** @return tempDirDest */
	public String getTempDirDest() {
		return tempDirDest;
	}

	/** @return speakerInfoDir	 */
	public String getSpeakerInfoDir() {
		return speakerInfoDir;
	}

	/** @param resourcesUrl セットしたい resourcesUrl */
	public void setResourcesUrl(String resourcesUrl) {
		this.resourcesUrl = resourcesUrl;
	}

	/** @param tempDirDest セットしたい tempDirDest */
	public void setTempDirDest(String tempDirDest) {
		this.tempDirDest = tempDirDest;
	}

	/** @param speakerInfoDir セットしたい speakerInfoDir	 */
	public void setSpeakerInfoDir(String speakerInfoDir) {
		this.speakerInfoDir = speakerInfoDir;
	}

	// 各種メソッド

	/** newさせないためのプライベートコンストラクタ */
	private Settings() {
	}

	/**
	 * settings.csvの中身を解釈してオブジェクトを作成する
	 * @return 解釈結果
	 */
	static public Settings readSettings() {
		Logger.put("設定の解析開始");

		// デフォルト設定で作成しておく
		final Settings result = new Settings();
		result.setResourcesUrl("");
		result.setTempDirDest(getDefaultTempDirDest());
		result.setSpeakerInfoDir(getDefaultSpeakerInfoDir());

		// 設定ファイルの存在チェック
		if (!getSettingsFilePath().toFile().exists()) {
			return result;
		}

		List<String> lines = null;
		try {
			lines = Files.readAllLines(getSettingsFilePath(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Logger.put("設定ファイル読み込み失敗 : ");
			Logger.put(e.toString());
			return result;
		}

		// 1行ごとに処理
		for (int i = 0; i < lines.size(); i++) {
			final String line = lines.get(i);
			final String[] splitLine = line.split(",", 2);
			if (splitLine.length < 2) {
				Logger.put("2列に分割できないため無視 : " + line);
				continue;
			}
			if (splitLine[0] == null || splitLine[0].trim().isEmpty()) {
				Logger.put("1列目が空のため無視 : " + line);
				continue;
			}
			if (splitLine[1] == null || splitLine[1].trim().isEmpty()) {
				Logger.put("2列目が空のため無視 : " + line);
				continue;
			}

			if ("resourcesUrl".equals(splitLine[0])) {
				Logger.put("resourcesUrl : " + splitLine[1]);
				result.setResourcesUrl(splitLine[1].trim());
			} else if ("tempDirDest".equals(splitLine[0])) {
				Logger.put("tempDirDest : " + splitLine[1]);
				result.setTempDirDest(splitLine[1].trim());
			} else if ("speakerInfoDir".equals(splitLine[0])) {
				Logger.put("speakerInfoDir : " + splitLine[1]);
				result.setSpeakerInfoDir(splitLine[1].trim());
			} else {
				Logger.put("未対応のプロパティなので無視 : " + line);
			}

		}
		Logger.put("設定の解析完了");
		return result;
	}

	/**
	 * settings.csvに設定を書き込む
	 */
	static public void saveSettings(String resourcesUrl, String tempDirDest, String speakerInfoDir) {
		final StringBuilder sb = new StringBuilder();
		sb.append("resourcesUrl,");
		sb.append(resourcesUrl);
		sb.append(System.lineSeparator());
		sb.append("tempDirDest,");
		sb.append(tempDirDest);
		sb.append(System.lineSeparator());
		sb.append("speakerInfoDir,");
		sb.append(speakerInfoDir);
		sb.append(System.lineSeparator());

		// ログ出力フォルダを作成
		final Path settingsFilePath = getSettingsFilePath();
		if (!settingsFilePath.getParent().toFile().exists()) {
			try {
				Files.createDirectories(settingsFilePath.getParent());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// ファイルへ出力(上書き)
		final File file = getSettingsFilePath().toFile();
		if (!file.exists() || (file.exists() && file.isFile() && file.canWrite())) {
			try (final FileWriter filewriter = new FileWriter(file, false)) {
				filewriter.write(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	static private Path getSettingsFilePath() {
		final Path currentDir = FileSystems.getDefault().getPath("").toAbsolutePath();
		final Path settingFilePath = currentDir.resolve("mycoedl").resolve("settings.csv");
		Logger.put("getSettingsFilePath returns \"" + settingFilePath + "\"");
		return settingFilePath;
	};

	/**
	 * @return 自動取得した作業用一時フォルダの作成先。
	 */
	static public String getDefaultTempDirDest() {
		final Path currentDir = FileSystems.getDefault().getPath("").toAbsolutePath();
		final Path tempDirDest = currentDir.resolve("mycoedl");
		return tempDirDest.toString();
	};

	/**
	 * @return 自動取得した"speaker_info"フォルダのパス。取得できない場合は長さ0の文字列。
	 */
	static public String getDefaultSpeakerInfoDir() {
		final Path currentDir = FileSystems.getDefault().getPath("").toAbsolutePath();

		// 同じ階層に「speaker_info」フォルダがあるか検索
		File speakerInfoFile = null;
		for (File file : currentDir.toFile().listFiles()) {
			if ("speaker_info".equals(file.getName())) {
				return file.toString();
			}
		}

		// 同じ階層に無ければ、一つ上の階層に「speaker_info」フォルダがあるか検索
		if (speakerInfoFile == null) {
			for (File file : currentDir.getParent().toFile().listFiles()) {
				if ("speaker_info".equals(file.getName())) {
					return file.toString();
				}
			}
		}

		// それでも無ければ諦めて空文字を返す。
		return "";
	};
}
