package ptee;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SSEliteGuard extends Thread {

	public SSEliteGuard() {
		
	}

	public void run() {

		AudioInputStream audioIn = null;
		Clip clip = null;

		try {
			audioIn = AudioSystem.getAudioInputStream(SSEliteGuard.class
					.getResource("ss.wav"));
			clip = AudioSystem.getClip();
			clip.open(audioIn);
		} catch (UnsupportedAudioFileException | LineUnavailableException
				| IOException e) {
			e.printStackTrace();
		}

		//clip.start();
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}
}
