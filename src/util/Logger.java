package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	private static String filename = null;

	public static void put(String string) {

		if (string == null) {
			string = "";
		}

		// 出力文字列を作成
		final StringBuilder sb = new StringBuilder();
		final SimpleDateFormat sdf = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss.SSS] ");
		final String dateString = sdf.format(new Date());
		final String[] splitString = string.split("\\r?\\n");
		for (int i = 0; i < splitString.length; i++) {
			if (i == 0) {
				sb.append(dateString + splitString[i]);
				sb.append(System.lineSeparator());
			} else {
				String padding = String.format("%" + dateString.length() + "s", "");
				sb.append(padding + splitString[i]);
				sb.append(System.lineSeparator());
			}
		}

		// コンソールへ出力
		System.out.print(sb.toString());

		// ログ出力フォルダを作成
		final Path logFilePath = getLogFilePath();
		if (!logFilePath.getParent().toFile().exists()) {
			try {
				Files.createDirectories(logFilePath.getParent());
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		// ファイルへ出力
		final File file = getLogFilePath().toFile();
		if (!file.exists() || (file.exists() && file.isFile() && file.canWrite())) {
			try (final FileWriter filewriter = new FileWriter(file, true)) {
				filewriter.write(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * ログファイルのパスを取得
	 * @return ログファイルのパス(Pathのインスタンス)
	 */
	public static Path getLogFilePath() {

		// 初回呼び出し時点の日時からファイル名を取得する
		if (filename == null) {
			final SimpleDateFormat sdfForFilename = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
			filename = "mycoedl-" + sdfForFilename.format(new Date()) + ".log";
		}

		final Path currentDir = FileSystems.getDefault().getPath("").toAbsolutePath();
		final Path logFilePath = currentDir.resolve("mycoedl").resolve("logs").resolve(filename);

		return logFilePath;
	}
}
