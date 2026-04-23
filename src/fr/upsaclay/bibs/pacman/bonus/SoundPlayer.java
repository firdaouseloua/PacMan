package fr.upsaclay.bibs.pacman.bonus;

import javax.sound.sampled.*;
import java.io.File;

public class SoundPlayer {
    private Thread musicThread;
    private volatile boolean stopMusic = false;
    private volatile SourceDataLine musicLine; // Référence directe pour couper le son

    public void playLoop(String filePath) {
        // 1. On demande l'arrêt
        stop(); 

        // 2. On attend un tout petit peu que l'ancien thread lâche la ligne audio
        try { Thread.sleep(100); } catch (InterruptedException e) {}

        stopMusic = false;
        musicThread = new Thread(() -> {
            try {
                File file = new File(filePath);
                while (!stopMusic) {
                    AudioInputStream in = AudioSystem.getAudioInputStream(file);
                    AudioFormat baseFormat = in.getFormat();
                    AudioFormat targetFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED, 
                        baseFormat.getSampleRate(), 16, 2, 4, baseFormat.getSampleRate(), false);
                    
                    AudioInputStream din = AudioSystem.getAudioInputStream(targetFormat, in);
                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, targetFormat);
                    
                    musicLine = (SourceDataLine) AudioSystem.getLine(info);
                    musicLine.open(targetFormat);
                    musicLine.start();
                    
                    byte[] buffer = new byte[4096];
                    int nRead;
                    // On vérifie stopMusic à CHAQUE écriture de buffer
                    while ((nRead = din.read(buffer, 0, buffer.length)) != -1 && !stopMusic) {
                        musicLine.write(buffer, 0, nRead);
                    }
                    
                    // Nettoyage propre
                    if (musicLine != null) {
                        musicLine.drain();
                        musicLine.stop();
                        musicLine.close();
                    }
                    din.close();
                    if (stopMusic) break; 
                }
            } catch (Exception e) {
                // Erreur silencieuse lors de l'arrêt
            }
        });
        musicThread.setDaemon(true);
        musicThread.start();
    }

    public void stop() {
        stopMusic = true;
        // ACTION CRITIQUE : On coupe la ligne matériellement
        if (musicLine != null) {
            try {
                musicLine.stop();
                musicLine.flush(); // Vide le buffer immédiatement
            } catch (Exception e) {}
        }
        if (musicThread != null) {
            musicThread.interrupt();
        }
    }

    public void play(String filePath) {
        new Thread(() -> {
            try {
                File file = new File(filePath);
                AudioInputStream in = AudioSystem.getAudioInputStream(file);
                AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 
                    in.getFormat().getSampleRate(), 16, 2, 4, in.getFormat().getSampleRate(), false);
                AudioInputStream din = AudioSystem.getAudioInputStream(targetFormat, in);
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, targetFormat));
                line.open(targetFormat);
                line.start();
                byte[] buffer = new byte[4096];
                int n;
                while ((n = din.read(buffer, 0, buffer.length)) != -1) line.write(buffer, 0, n);
                line.drain(); line.close();
            } catch (Exception e) {}
        }).start();
    }
}