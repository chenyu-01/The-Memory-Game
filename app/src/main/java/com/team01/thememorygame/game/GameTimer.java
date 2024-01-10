package com.team01.thememorygame.game;

import static com.team01.thememorygame.Utils.GameUtils.formatTime;

import android.graphics.Color;
import android.os.CountDownTimer;
import android.widget.TextView;




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



    private void initializeTimer() {
        tvTimer.setTextColor(Color.BLACK);
        timeLeftInMillis = 60000;
        startTimer(timeLeftInMillis);
    }
    public void startTimer(long timeInMillis) {
        stopTimer();
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

    public void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }


}
