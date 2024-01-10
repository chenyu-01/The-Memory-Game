package com.team01.thememorygame.managers;

import android.graphics.Color;
import android.os.CountDownTimer;
import android.widget.TextView;



import java.util.Locale;

public class GameTimer {

    private CountDownTimer countDownTimer;
    public long timeLeftInMillis;
    private final TextView tvTimer;

    public interface TimerListener {
        void onTimerFinish();
    }

    private final TimerListener timerListener;

    public GameTimer(TimerListener timerListener, TextView tvTimer) {
        this.timerListener = timerListener;
        this.tvTimer = tvTimer;
        initializeTimer();
    }



    public void initializeTimer() {
        tvTimer.setTextColor(Color.BLACK);
        timeLeftInMillis = 60000;
        startTimer(timeLeftInMillis);
    }
    public void startTimer(long timeInMillis) {
        if(countDownTimer != null)
            countDownTimer.cancel(); // Cancel existing timer
        countDownTimer = new CountDownTimer(timeInMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished; // Update remaining time
                tvTimer.setText(formatTime(millisUntilFinished));
                if (millisUntilFinished <= 15000) { // Less than 15 seconds
                    tvTimer.setTextColor(Color.RED);
                }
            }

            public void onFinish() {
                tvTimer.setText("00:00");
                timerListener.onTimerFinish();
            }
        }.start();
    }

    public String formatTime(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
}
