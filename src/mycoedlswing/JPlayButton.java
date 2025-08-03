package mycoedlswing;

import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.swing.JButton;

import util.Logger;

public class JPlayButton extends JButton {
	private static String DISPLAY_PLAY = "再生";
	private static String DISPLAY_STOP = "停止";

	private String filepath;
	private Clip clip;
	private LineListener listener;

	/**
	 * コンストラクタ
	 * @param filepath このボタンを押したときに再生させたい音声ファイルのパス
	 */
	public JPlayButton(String filepath) {
		super(DISPLAY_PLAY);
		this.filepath = filepath;
		addActionListener(e -> onClick(e));
	}

	/**
	 * ボタン押下時の処理
	 */
	private void onClick(ActionEvent e) {
		Logger.put("●サンプル再生ボタン押下");
		Logger.put("サンプル再生ボタン(" + filepath + ")押下処理 開始");
		if (clip != null && clip.isRunning()) {
			// 押下時に再生中なら停止する
			Logger.put("サンプル再生ボタン(" + filepath + ")押下処理 音声再生中断");
			clip.stop();
			if (listener != null) {
				clip.removeLineListener(listener);
				listener = null;
			}
			clip.close();
			clip = null;
			setText(DISPLAY_PLAY);

		} else {

			// 押下時に再生中でないならロードして再生
			if (clip == null) {
				Logger.put("サンプル再生ボタン(" + filepath + ")押下処理 音声読み込み開始");
				final File file = new File(filepath);
				try (InputStream is = new FileInputStream(file); BufferedInputStream bis = new BufferedInputStream(is); AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bis)) { // 「AudioSystem.getAudioInputStream」に直接Fileオブジェクトを与えたところ、正常にリソース開放してくれなくなった。回避のためこんな長くなっちゃった。
					clip = AudioSystem.getClip();
					clip.open(audioInputStream);
				} catch (Exception ex) {
					Logger.put("サンプル再生ボタン(" + filepath + ")押下処理 音声読み込み失敗");
					Logger.put(ex.toString());
					ex.printStackTrace();
					return;
				}
				Logger.put("サンプル再生ボタン(" + filepath + ")押下処理 音声読み込み完了");
			}

			if (clip != null) {

				// 停止時のイベントを追加
				listener = eventClip -> {
					if (eventClip.getType() == LineEvent.Type.STOP) {
						Logger.put("サンプル再生ボタン(" + filepath + ")押下処理 音声再生完了");
						clip.stop();
						if (listener != null) {
							clip.removeLineListener(listener);
							listener = null;
						}
						clip.close();
						clip = null;
						setText(DISPLAY_PLAY);
					}
				};
				clip.addLineListener(listener);

				Logger.put("サンプル再生ボタン(" + filepath + ")押下処理 音声再生開始");
				clip.setMicrosecondPosition(0); // 頭出し
				clip.start();
				setText(DISPLAY_STOP);
			}
		}
		Logger.put("サンプル再生ボタン(" + filepath + ")押下処理 完了");
	}
}
