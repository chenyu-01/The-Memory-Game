package com.team01.thememorygame.game;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Html;

public class GameOverDialogManager {
    private final Context context;
    private final GameOverDialogListener listener;

    public interface GameOverDialogListener {
        void onRestartGame();

        void onFinishActivity();
    }

    public GameOverDialogManager(Context context, GameOverDialogListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void showGameOverDialog(boolean isWin) {
        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(context);
        dialogBuilder.setTitle(isWin ? "WIN" : "LOSE");
        dialogBuilder.setMessage(isWin ? "Congratulations! You've won!" : "Game Over! Try again?");

        int titleColor = isWin ? 0xFFD700 : Color.RED;
        dialogBuilder.setTitle(Html.fromHtml("<font color='" + titleColor + "'>" + (isWin ? "WIN" : "LOSE") + "</font>"));

        dialogBuilder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onRestartGame();
            }
        });

        dialogBuilder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onFinishActivity();
            }
        });

        AlertDialog gameOverDialog = dialogBuilder.create();

        gameOverDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                listener.onRestartGame();
            }
        });

        gameOverDialog.show();
    }
}
