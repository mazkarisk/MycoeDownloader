package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpUtil {

	/**
	 * HTTP通信でURLからファイルをダウンロードし、ファイル内容を文字列として取得する
	 * @param urlString ダウンロード元URL
	 * @return ダウンロードした文字列
	 * @throws IOException
	 */
	static public String downloadFileFromUrlAsString(String urlString) throws IOException {
		urlString = urlString.replaceAll("&dl=0", "&dl=1"); // Dropbox向けに、DL設定に変更

		final URL url = new URL(urlString);

		// コネクションを作成し実行する
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.connect();

		// レスポンスコードがOKでなければ終了
		final int responseCode = connection.getResponseCode();
		if (responseCode != HttpURLConnection.HTTP_OK) {
			connection.disconnect();
			return null;
		}

		// テキストを取得する
		StringBuilder stringBuilder = new StringBuilder();
		try (InputStream is = connection.getInputStream(); InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8); BufferedReader bufReader = new BufferedReader(isr)) {
			String line = null;
			// 1行ずつテキストを読み込む
			while ((line = bufReader.readLine()) != null) {
				stringBuilder.append(line).append(System.lineSeparator());
			}
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return stringBuilder.toString();
	}
}
