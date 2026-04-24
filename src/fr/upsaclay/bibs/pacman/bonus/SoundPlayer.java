package fr.upsaclay.bibs.pacman.bonus;

import javax.sound.sampled.*;
import java.io.File;

public class SoundPlayer {
    private Thread musicThread;
    private volatile boolean stopMusic = false;
    private volatile SourceDataLine musicLine;
    private volatile SourceDataLine sfxLine; 

    public void playLoop(String filePath) {
        stop(); 
        try { 
            Thread.sleep(50); 
        } catch (InterruptedException e) {}

        stopMusic = false; 
        musicThread = new Thread(() -> {
            try {
                File file = new File(filePath);

                while (!stopMusic) {
                    AudioInputStream in = AudioSystem.getAudioInputStream(file);
                    AudioFormat baseFormat = in.getFormat();

                    AudioFormat targetFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED, 
                        baseFormat.getSampleRate(),
                        16,
                        2,
                        4,
                        baseFormat.getSampleRate(),
                        false
                    );

                    AudioInputStream din = AudioSystem.getAudioInputStream(targetFormat, in);

                    musicLine = (SourceDataLine) AudioSystem.getLine(
                        new DataLine.Info(SourceDataLine.class, targetFormat)
                    );

                    musicLine.open(targetFormat);
                    musicLine.start();

                    byte[] buffer = new byte[4096];
                    int nRead;

                    while ((nRead = din.read(buffer, 0, buffer.length)) != -1 && !stopMusic) {
                        musicLine.write(buffer, 0, nRead);
                    }

                    SourceDataLine line = musicLine;
                    if (line != null) {
                        try {
                            line.stop();
                            line.flush();
                            line.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            musicLine = null;
                        }
                    }

                    din.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        musicThread.setDaemon(true);
        musicThread.start();
    }

    public void play(String filePath) {
        new Thread(() -> {
            try {
                File file = new File(filePath);

                AudioInputStream in = AudioSystem.getAudioInputStream(file);
                AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    in.getFormat().getSampleRate(),
                    16,
                    2,
                    4,
                    in.getFormat().getSampleRate(),
                    false
                );

                AudioInputStream din = AudioSystem.getAudioInputStream(targetFormat, in);

                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(
                    new DataLine.Info(SourceDataLine.class, targetFormat)
                );

                sfxLine = line;

                line.open(targetFormat);
                line.start();

                byte[] buffer = new byte[4096];
                int n;

                while ((n = din.read(buffer, 0, buffer.length)) != -1) {
                    line.write(buffer, 0, n);
                }

                line.drain();
                line.close();
                din.close();
                in.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stop() {
        stopMusic = true; 

        if (musicLine != null) {
            try {
                musicLine.stop();
                musicLine.flush();
                musicLine.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            musicLine = null;
        }

        if (sfxLine != null) {
            try {
                sfxLine.stop();
                sfxLine.flush();
                sfxLine.close();
            } catch (Exception e) {}
            sfxLine = null;
        }

        if (musicThread != null) {
            musicThread.interrupt();
        }
    }

    public void stopAll() {
        stop();
    }
}