package mycoedlswing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import util.FileUtil;
import util.HttpUtil;
import util.Logger;

public class MycoeLogic {

	/**
	 * resources.csvの中身を解釈してMapオブジェクトを作成する
	 * @param resourcesUrlString resources.csvをDLするためのURL
	 * @return resourcesSetting 解釈結果
	 */
	static public Map<String, String> downloadResourcesSetting(String resourcesUrlString) {
		final Map<String, String> result = new HashMap<String, String>();

		// リソース設定のダウンロード
		String resourcesSettingString = null;
		Logger.put("リソース設定を\"" + resourcesUrlString + "\"からダウンロード開始");
		try {
			resourcesSettingString = HttpUtil.downloadFileFromUrlAsString(resourcesUrlString);
		} catch (IOException e) {
			Logger.put("リソース設定を\"" + resourcesUrlString + "\"からダウンロード失敗");
			Logger.put(e.toString());
			return null;
		}
		Logger.put("リソース設定を\"" + resourcesUrlString + "\"からダウンロード完了");

		Logger.put("リソース設定の解析開始");

		// 改行で分割
		final String[] splitSetting = resourcesSettingString.split("\\r?\\n");
		Logger.put("リソース設定の有効行数 : " + splitSetting.length);

		// 1行ごとに処理
		for (int i = 0; i < splitSetting.length; i++) {
			final String line = splitSetting[i];
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
			if (splitLine[0].startsWith("/")) {
				Logger.put("1列目が「/」から始まっているため無視 : " + line);
				continue;
			}
			result.put(splitLine[0], splitLine[1]);
			Logger.put("正常に格納 : " + line);
		}

		Logger.put("リソース設定の解析完了");

		return result;
	}

	/**
	 * 音声スタイルの存在チェック
	 * @param modelDirPathString 存在をチェックしたい音声スタイルのモデルディレクトリ(があるはず)のパスを示す文字列
	 * @return 一式そろっていればtrue、一つでも要素が欠けていたらfalse。
	 */
	static public boolean isModelExists(String modelDirPathString) {
		boolean dirExists = false;
		boolean pthExists = false;
		boolean yamlExists = false;

		// 検索用正規表現を作成
		final Pattern patternPth = Pattern.compile(".*\\.pth");
		final Pattern patternYaml = Pattern.compile(".*\\.yaml");

		// ディレクトリそのものの存在チェック
		final File file = new File(modelDirPathString);
		dirExists = file != null && file.exists() && file.isDirectory();

		// ディレクトリが存在するなら、中に*.pthと*.yamlが存在するかチェック
		if (dirExists && file.listFiles() != null) {
			final File[] list = file.listFiles();
			for (int i = 0; i < list.length; i++) {
				// ファイルでなければスキップ(基本的に想定外)
				if (!list[i].isFile()) {
					continue;
				}
				// *.pthファイルを発見
				if (patternPth.matcher(list[i].getName()).find()) {
					pthExists = true;
				}
				// *.yamlファイルを発見
				if (patternYaml.matcher(list[i].getName()).find()) {
					yamlExists = true;
				}
			}
		}

		Logger.put("存在チェック : " + modelDirPathString + " (" + dirExists + ", " + pthExists + ", " + yamlExists + ")");

		return dirExists && pthExists && yamlExists;
	}

	/**
	 * 音声スタイルの存在チェック
	 * @param modelDirPathString 存在をチェックしたい音声スタイルのモデルディレクトリ(があるはず)のパスを示す文字列
	 * @return 一式そろっていればtrue、一つでも要素が欠けていたらfalse。
	 */
	static public List<String> searchModels(String modelDirPathString) {
		Logger.put("モデル列挙処理開始 検索対象 : " + modelDirPathString);

		final List<String> modelList = new ArrayList<String>();

		// modelディレクトリの存在チェック
		final File modelDirFile = new File(modelDirPathString);
		if (modelDirFile == null || !modelDirFile.exists() || !modelDirFile.isDirectory()) {
			Logger.put("モデル列挙処理完了(検索対象なし)");
			return modelList;
		}

		// modelディレクトリ内のファイル・ディレクトリのリストを取得
		final File[] modelDirFileList = modelDirFile.listFiles();
		if (modelDirFileList == null || modelDirFileList.length == 0) {
			Logger.put("モデル列挙処理完了(検索対象内にファイルなし)");
			return modelList;
		}

		// それぞれのディレクトリについて、音声モデルとして必要なファイルがそろっているかチェック
		for (int i = 0; i < modelDirFileList.length; i++) {
			final String dirName = modelDirFileList[i].getName();

			if (!modelDirFileList[i].isDirectory()) {
				Logger.put("modelディレクトリ内にファイルが含まれています。 : " + modelDirFileList[i]);
				continue;
			}
			isModelExists(modelDirFileList[i].getPath());
			modelList.add(dirName);
		}

		Logger.put("モデル列挙処理完了 検索結果 : " + modelList.toString());

		return modelList;
	}

	public static void deleteUnnecessaryIconsAndVoiceSamples(MetasJson metasJson, String prodSpeakerDirPathString, List<String> adoptedModelList) {
		// 不要なアイコンとボイスサンプルを削除するため、削除したいファイルを示す正規表現を作成
		// 例えば「1.pngと3.png以外を削除したい」なら、否定の表現を使い"^(?!(1|3)\.png$)"などとする
		final StringBuilder sbIcons = new StringBuilder();
		final StringBuilder sbVoiceSamples = new StringBuilder();
		sbIcons.append("^(?!(");
		sbVoiceSamples.append("^(?!(");
		for (int i = 0; i < metasJson.getStyleIds().size(); i++) {
			final String styleId = metasJson.getStyleIds().get(i);

			// 最終的に残すスタイルでなければ正規表現には追加しない
			if (!adoptedModelList.contains(styleId)) {
				continue;
			}

			// スタイルIDを正規表現に追加
			sbIcons.append(styleId);
			sbVoiceSamples.append(styleId);

			// パイプ区切りでスタイルIDをOR検索できるようにする
			if (i < metasJson.getStyleIds().size() - 1) {
				sbIcons.append("|");
				sbVoiceSamples.append("|");
			}
		}
		sbIcons.append(")\\.png$)");
		sbVoiceSamples.append(")_\\d+\\.wav$)");

		// 不要なアイコンとボイスサンプルを削除
		final File iconsDirectory = Paths.get(prodSpeakerDirPathString, "icons").toFile();
		final File voiceSamplesDirectory = Paths.get(prodSpeakerDirPathString, "voice_samples").toFile();
		final Pattern iconsPattern = Pattern.compile(sbIcons.toString());
		final Pattern voiceSamplesPattern = Pattern.compile(sbVoiceSamples.toString());
		FileUtil.deleteMatchedFiles(iconsDirectory, iconsPattern);
		FileUtil.deleteMatchedFiles(voiceSamplesDirectory, voiceSamplesPattern);
	}
}
