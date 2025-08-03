package util;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpDownloadManager implements AutoCloseable {
	private DataInputStream dataInputStream = null;
	private DataOutputStream outputStream = null;
	private HttpURLConnection connection = null;
	private int availableByteNumber;
	private byte[] buffers = new byte[1 * 1024 * 1024];
	private long outputFileSize = 0L;
	private long downloadedFileSize = 0L;
	private String outputFilepathString;
	private String filename = "";

	/**
	 * 出力先ファイルパスを取得する。
	 * @return ファイルパス
	 */
	public String getOutputFilepathString() {
		return outputFilepathString;
	}

	/**
	 * 出力先ファイル名を取得する。
	 * @return ファイル名
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * 今までにダウンロードしたサイズを取得する。
	 * @return 今までにダウンロードしたサイズ
	 */
	public long getDownloadedFileSize() {
		return downloadedFileSize;
	}

	/**
	 * ダウンロードされる合計サイズを取得する。
	 * @return ダウンロードされる合計サイズ
	 */
	public long getOutputFileSize() {
		return outputFileSize;
	}

	/**
	 * 今までにダウンロードしたサイズを百分率で表した文字列で取得する。
	 * @return 今までにダウンロードしたサイズを百分率で表した文字列
	 */
	public String getPercentageString() {
		return String.format("%1$.2f", (float) downloadedFileSize / (float) outputFileSize * 100) + "%";
	}

	/** newさせないためのプライベートコンストラクタ */
	private HttpDownloadManager() {
	}

	/**
	 * ファイルダウンロード準備済みのHttpDownloadManagerインスタンスを作成して返す。後で「next()」メソッドの呼び出しが必要。
	 * @param urlString ダウンロード用リンク
	 * @param saveDest 保存先ディレクトリ
	 * @return ファイルダウンロード準備済みのHttpDownloadManagerインスタンス
	 * @throws Exception
	 */
	public static HttpDownloadManager connect(String urlString, Path saveDest) throws Exception {
		final HttpDownloadManager httpDownloadManager = new HttpDownloadManager();
		httpDownloadManager.prepareDownloadFileFromUrl(urlString, saveDest);

		return httpDownloadManager;
	}

	/**
	 * コネクションを確立し、ファイルダウンロードの準備をする。
	 * @param urlString ダウンロード用リンク
	 * @param saveDest 保存先ディレクトリ
	 * @return 保存したファイルのパス
	 * @throws Exception
	 */
	private String prepareDownloadFileFromUrl(String urlString, Path saveDest) throws Exception {

		try {
			urlString = urlString.replaceAll("&dl=0", "&dl=1"); // Dropbox向けに、DL設定に変更
			final URL url = new URL(urlString);

			Logger.put("DL開始 urlString : " + urlString);

			connection = (HttpURLConnection) url.openConnection();
			connection.setAllowUserInteraction(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestMethod("GET");
			connection.connect();

			// ヘッダー情報を取得＆表示
			final Map<String, List<String>> headerFields = connection.getHeaderFields();
			final StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
				sb.append(entry.getKey());
				sb.append(" : ");
				sb.append(entry.getValue());
				sb.append(System.lineSeparator());
			}
			Logger.put(sb.toString());

			// レスポンスコードのチェック。OKでなければ終了する。
			final int responseCode = connection.getResponseCode();
			Logger.put("responseCode : " + responseCode);
			if (responseCode != HttpURLConnection.HTTP_OK) {
				Logger.put("接続失敗。responseCode : " + responseCode);
				return null;
			}

			// ヘッダー情報からコンテンツサイズを取得する
			outputFileSize = 0L;
			final String contentLength = headerFields.get("Content-Length").get(0);
			final String originalContentLength = headerFields.get("Original-Content-Length").get(0);
			if (contentLength != null && contentLength.trim().length() > 0 && Long.parseLong(contentLength) > 0) {
				outputFileSize = Long.parseLong(contentLength);
			} else if (originalContentLength != null && originalContentLength.trim().length() > 0 && Long.parseLong(originalContentLength) > 0) {
				outputFileSize = Long.parseLong(originalContentLength);
			}
			Logger.put("outputFileSize : " + outputFileSize);
			if (outputFileSize <= 0) {
				Logger.put("Content-Lengthを取得できません。");
				return null;
			}

			// ヘッダー情報からファイル名を取得する
			final String disposition = headerFields.get("Content-Disposition").get(0);
			Logger.put("disposition : " + disposition);
			if (disposition == null || disposition.trim().length() <= 0) {
				Logger.put("dispositionを取得できません。");
				return null;
			}
			final Pattern pattern = Pattern.compile("filename=\"(.+)\"");
			final Matcher matcher = pattern.matcher(disposition);
			if (!matcher.find()) {
				Logger.put("filenameが見つかりません。");
				return null;
			}
			filename = matcher.group(1);
			if (filename == null || filename.trim().isEmpty()) {
				Logger.put("filenameが空です。");
				return null;
			}

			outputFilepathString = saveDest.resolve(filename).toString();

			dataInputStream = new DataInputStream(connection.getInputStream());
			outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFilepathString)));

			Logger.put("DL完了 urlString : " + urlString);
		} catch (Exception exception) {
			close();
			throw exception;
		}

		return outputFilepathString;
	}

	/**
	 * ファイルを一部ダウンロードする。
	 * @return 読み込み中ならtrue、読み込み完了したらfalse
	 * @throws IOException
	 */
	public boolean next() throws IOException {
		availableByteNumber = dataInputStream.read(buffers);
		if (availableByteNumber > 0) {
			downloadedFileSize += availableByteNumber;
			outputStream.write(buffers, 0, availableByteNumber);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 各種リソースをクローズする。
	 */
	@Override
	public void close() {

		try {
			if (dataInputStream != null) {
				dataInputStream.close();
				dataInputStream = null;
			}
		} catch (IOException exception) {
			Logger.put(exception.toString());
		}

		try {
			if (outputStream != null) {
				outputStream.close();
				outputStream = null;
			}
		} catch (IOException exception) {
			Logger.put(exception.toString());
		}

		if (connection != null) {
			connection.disconnect();
			connection = null;
		}
	}
}
