package com.tq.wechatnotifier.alert;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

public class AlertManager {
    private static final String TAG = "AlertManager";
    private static MediaPlayer mediaPlayer;
    private static Vibrator vibrator;

    public static synchronized void playAlert(Context context) {
        playRingtone(context);
        vibrate(context);
    }

    private static void playRingtone(Context context) {
        stopRingtone();

        // Get system alarm ringtone
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }

        if (alarmUri == null) {
            Log.e(TAG, "No system ringtone available");
            return;
        }

        mediaPlayer = new MediaPlayer();

        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        mediaPlayer.setAudioAttributes(attrs);

        try {
            mediaPlayer.setDataSource(context, alarmUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                if (mediaPlayer == mp) {
                    mediaPlayer = null;
                }
            });
            Log.i(TAG, "Playing alarm ringtone");
        } catch (Exception e) {
            Log.e(TAG, "Failed to play alarm ringtone", e);
            try {
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }
    }

    private static void stopRingtone() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping media player", e);
            }
            mediaPlayer = null;
        }
    }

    private static void vibrate(Context context) {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 500, 200, 500, 200, 500};
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        }
    }

    public static synchronized void stopAlert() {
        stopRingtone();
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
    }
}
