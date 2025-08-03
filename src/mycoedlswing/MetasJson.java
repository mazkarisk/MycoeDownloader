package mycoedlswing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.Logger;

public class MetasJson {

	// メンバ変数
	boolean valid;
	String speakerName;
	String speakerUuid;
	List<String> styleNames;
	List<String> styleIds;

	// ゲッター
	public String getSpeakerName() {
		return speakerName;
	}

	public String getSpeakerUuid() {
		return speakerUuid;
	}

	public List<String> getStyleNames() {
		return styleNames;
	}

	public List<String> getStyleIds() {
		return styleIds;
	}

	public boolean isValid() {
		return valid;
	}

	// コンストラクタ
	public MetasJson(String metasJsonFilePath) {
		valid = false;

		Logger.put("JSON読み込み開始 : " + metasJsonFilePath);

		speakerName = null;
		speakerUuid = null;
		styleNames = new ArrayList<String>();
		styleIds = new ArrayList<String>();

		final Path path = Paths.get(metasJsonFilePath);
		List<String> lines = null;
		try {
			lines = Files.readAllLines(path, StandardCharsets.UTF_8);
		} catch (IOException e) {
			Logger.put("JSON読み込み失敗 : " + metasJsonFilePath);
			Logger.put(e.toString());
			return;
		}

		final Pattern patternSpeakerName = Pattern.compile("\"speakerName\"\\s*:\\s*\"(.*)\"");
		final Pattern patternSpeakerUuid = Pattern.compile("\"speakerUuid\"\\s*:\\s*\"(.*)\"");
		final Pattern patternStyleName = Pattern.compile("\"styleName\"\\s*:\\s*\"(.*)\"");
		final Pattern patternStyleId = Pattern.compile("\"styleId\"\\s*:\\s*(\\d+)");

		for (int i = 0; i < lines.size(); i++) {
			final String line = lines.get(i);

			final Matcher matcherSpeakerName = patternSpeakerName.matcher(line);
			if (matcherSpeakerName.find()) {
				speakerName = matcherSpeakerName.group(1);
				Logger.put("JSONから読み込んだspeakerName : " + matcherSpeakerName.group(1));
			}

			final Matcher matcherSpeakerUuid = patternSpeakerUuid.matcher(line);
			if (matcherSpeakerUuid.find()) {
				speakerUuid = matcherSpeakerUuid.group(1);
				Logger.put("JSONから読み込んだspeakerUuid : " + matcherSpeakerUuid.group(1));
			}

			final Matcher matcherStyleName = patternStyleName.matcher(line);
			if (matcherStyleName.find()) {

				styleNames.add(matcherStyleName.group(1));
				Logger.put("JSONから読み込んだstyleName : " + matcherStyleName.group(1));
			}

			final Matcher matcherStyleId = patternStyleId.matcher(line);
			if (matcherStyleId.find()) {
				styleIds.add(matcherStyleId.group(1));
				Logger.put("JSONから読み込んだstyleId : " + matcherStyleId.group(1));
			}
		}

		Logger.put("JSONから読み込んだstyleName数 : " + styleIds.size());
		Logger.put("JSONから読み込んだstyleId数 : " + styleNames.size());
		Logger.put("JSON読み込み終了 : " + metasJsonFilePath);

		valid = true;
	}

	/**
	 * metas.jsonに書き出すための文字列を作成する。
	 * @param originalMetasJson 元のmetas.jsonの解析結果
	 * @param excludeStyleIds 除外するスタイルのスタイルID
	 * @return metas.jsonに書き出すための文字列
	 */
	public static String makeMetasJsonString(MetasJson originalMetasJson, List<String> includeStyleIds) {

		// 不要なスタイルを除外する
		final List<String> remainStyleIds = new ArrayList<String>();
		final List<String> remainStyleNames = new ArrayList<String>();
		final List<String> excludeStyleIds = new ArrayList<String>();
		for (int i = 0; i < originalMetasJson.getStyleIds().size(); i++) {
			if (includeStyleIds.contains(originalMetasJson.getStyleIds().get(i))) {
				remainStyleIds.add(originalMetasJson.getStyleIds().get(i));
				remainStyleNames.add(originalMetasJson.getStyleNames().get(i));
			} else {
				excludeStyleIds.add(originalMetasJson.getStyleIds().get(i));
			}
		}
		Logger.put("JSON作成 含む : " + remainStyleIds.toString());
		Logger.put("JSON作成 含まない : " + excludeStyleIds.toString());

		// 文字列作成
		final StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		sb.append("    \"speakerName\": \"" + originalMetasJson.speakerName + "\",\n");
		sb.append("    \"speakerUuid\": \"" + originalMetasJson.speakerUuid + "\",\n");
		sb.append("    \"styles\": [\n");
		for (int i = 0; i < remainStyleIds.size(); i++) {
			sb.append("        {\n");
			sb.append("            \"styleName\": \"" + remainStyleNames.get(i) + "\",\n");
			sb.append("            \"styleId\": " + remainStyleIds.get(i) + "\n");
			if (i < remainStyleIds.size() - 1) {
				sb.append("        },\n");
			} else {
				sb.append("        }\n");
			}
		}
		sb.append("    ]\n");
		sb.append("}");

		Logger.put("JSON作成完了");
		Logger.put(sb.toString());

		return sb.toString();
	}

}
