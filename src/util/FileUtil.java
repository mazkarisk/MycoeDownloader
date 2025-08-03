package util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtil {

	/**
	 * ファイル・ディレクトリを再帰的に削除する
	 * @param targetFile 対象ファイル(またはディレクトリ)
	 * @throws Exception
	 */
	public static void deleteFilesRecursively(File targetFile) {
		Logger.put("ファイル削除開始 : " + targetFile.toString());

		if (targetFile == null || !targetFile.exists()) {
			return;
		}
		if (targetFile.isDirectory()) {
			// ディレクトリだった場合、中身について再帰的に呼び出して根絶する。
			for (File file : targetFile.listFiles()) {
				deleteFilesRecursively(file);
			}
		}
		targetFile.delete();

		Logger.put("ファイル削除完了 : " + targetFile.toString());
	}

	/**
	 * ファイル・ディレクトリを再帰的に移動する
	 * @param targetFile 対象ファイル(またはディレクトリ)
	 * @throws IOException 
	 * @throws Exception
	 */
	public static void moveFilesRecursively(File srcFile, File dstFile) throws IOException {
		Logger.put("ファイル移動開始 : " + srcFile.toString() + " to " + dstFile.toString());

		// 存在チェック
		if (srcFile == null || !srcFile.exists()) {
			return;
		}

		if (srcFile.isDirectory()) {
			// ディレクトリだった場合、中身について再帰的に呼び出して処理する。
			Files.createDirectories(dstFile.toPath());
			for (File file : srcFile.listFiles()) {
				moveFilesRecursively(file, new File(dstFile, file.getName()));
			}
		} else {
			// ディレクトリでない場合は移動する。
			Files.move(srcFile.toPath(), dstFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		Logger.put("ファイル移動完了 : " + srcFile.toString() + " to " + dstFile.toString());
	}

	/**
	 * ディレクトリ内の、正規表現にマッチする名前を持つファイルを削除する。
	 * @param targetDirectory 削除したいファイルが入っているディレクトリ
	 * @param pattern 削除したいファイルのファイル名。例えば逆に「1.pngと3.png<strong>以外</strong>を削除したい」場合は"^(?!(1|3)\.png$)"とする。
	 * @return 成功ならtrue、そうでなければfalse。
	 */
	public static boolean deleteMatchedFiles(File targetDirectory, Pattern pattern) {
		Logger.put("指定ファイルの削除開始 : targetDirectory=" + targetDirectory.toString() + ", pattern=" + pattern.toString());

		// 対象ディレクトリの存在チェックする。
		if (targetDirectory == null || !targetDirectory.exists() || !targetDirectory.isDirectory()) {
			return false;
		}

		// ディレクトリの中身を1ファイルずつループする。
		boolean allSucceeded = true;
		for (File file : targetDirectory.listFiles()) {
			final String filename = file.getName();

			// 引数で指定された正規表現と照合する。
			final Matcher matcher = pattern.matcher(filename);
			if (matcher.find()) {
				// ファイル名が正規表現とマッチしたら削除する。
				if (!file.delete()) {
					allSucceeded = false;
				}
			}
		}

		Logger.put("指定ファイルの削除完了");
		return allSucceeded;
	}

	/**
	 * ZIPファイルを解凍する
	 * @param zipFilePath 解凍したいZIPファイルのパス
	 * @param outputDir 解凍先ディレクトリ
	 * @throws IOException 
	 */
	public static void unzipFile(Path zipFilePath, String outputDir) throws IOException {
		Logger.put("ZIP解凍開始 : " + zipFilePath + " to " + outputDir);

		if (Files.notExists(zipFilePath)) {
			Logger.put("ZIP解凍失敗(ファイルが存在しません)");
			return;
		}

		try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
			final Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				final ZipEntry entry = entries.nextElement();
				final Path outName = Paths.get(outputDir, entry.getName());

				// ディレクトリならスキップする。
				if (entry.isDirectory()) {
					continue;
				}

				// 親ディレクトリを作成する。
				Files.createDirectories(outName.getParent());

				// 各エントリ(ファイル)の解凍する。
				try (InputStream is = zipFile.getInputStream(entry); BufferedOutputStream os = new BufferedOutputStream(Files.newOutputStream(outName))) {
					byte[] buf = new byte[1024];
					int len;
					while ((len = is.read(buf)) >= 0) {
						os.write(buf, 0, len);
					}
				}
			}
		}

		Logger.put("ZIP解凍完了 : " + zipFilePath);
	}

	/**
	 * あるディレクトリの中で見つかった最初のディレクトリを返す。
	 * @param paths 検索対象のディレクトリのパス文字列
	 * @return 見つけたディレクトリのパス文字列
	 */
	static public String getFirstDirectory(File file) {
		// 存在チェック
		if (file != null && file.exists() && !file.isDirectory()) {
			Logger.put("getFirstDirectory 失敗 (" + file.getPath() + ")");
			return null;
		}

		// ディレクトリを探す。
		final File[] list = file.listFiles();
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				if (list[i].isDirectory()) {
					return list[i].getPath();
				}
			}
		}

		return null;
	}
}
