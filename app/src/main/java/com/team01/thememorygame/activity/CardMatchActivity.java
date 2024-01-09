package com.team01.thememorygame.activity;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.team01.thememorygame.R;
import com.team01.thememorygame.Utils.DelayAction;
import com.team01.thememorygame.bean.CardBean;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class CardMatchActivity extends AppCompatActivity implements  Handler.Callback {
    private Handler mHdler;


    private void initHandler() {
        if (mHdler==null) {
            mHdler = new Handler(Looper.getMainLooper(),this);
        }
    }

    private void commitAction(int id,int arg,Object extra,int ms) {
        if (mHdler==null) {
            initHandler();
        }
        mHdler.sendMessageDelayed(Message.obtain(mHdler,id,arg,0,extra),ms);
    }

    private void removeActionById(int id) {
        if (mHdler!=null) {
            mHdler.removeMessages(id);
        }
    }
    private Thread gameMonitorThread;
    private void startGameMonitor() {
        gameMonitorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {

                    if (checkEnd()) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //stop timer
                                if (countDownTimer != null) {
                                    countDownTimer.cancel();
                                }
                                gameOver();
                            }
                        });
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        gameMonitorThread.start();
    }
    private static final int IA_FLOP_BACK = 0;
    private static final int FLOP_BACK_ALL = 1;
    private static final int FLOP_BACK_ONE = 2;
    private static final int ONCE_DONE = 3;
    private static final int SOUND_CORRECT = 4;
    private static final int SOUND_WRONG = 5;
    public static final int CARD_MEMBER_NUM = 2;

    private int mClickFlag = 0x0;
    // view
    private RelativeLayout mRlCardView;

    private Animator[] mAnimOutSet;
    private Animator[] mAnimInSet;

    private AlphaAnimation mAnimFailed;
    private Animation mAnimSuccess;

    private int cardNum = 0;
    private boolean isStart = false;
    private CardViewHolder selCardView1 = null, selCardView2 = null;
    private ArrayList<Integer> indexList = new ArrayList<>();

    private ArrayList<CardBean> cardList = new ArrayList<>();
    private MediaPlayer mediaPlayer;
    private DelayAction delayAction = new DelayAction();

    private CountDownTimer countDownTimer;
    private TextView tvTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        loadAnim();
        startGameMonitor();
        startTimer();
    }

    private void initView() {
        View mRootView = View.inflate(this, R.layout.card_match_view, null);
        mRlCardView = (RelativeLayout) mRootView.findViewById(R.id.card_list);
        setContentView(mRootView);
    }


    private void initData() {
        initCardList();
        delayAction.setInner(600);
        cardNum = cardList.size() * CARD_MEMBER_NUM;
        mAnimOutSet = new Animator[cardNum];
        mAnimInSet = new Animator[cardNum];

        isStart = false;

        initIndexList();

        initCardViewLayout();

        commitAction(IA_FLOP_BACK, FLOP_BACK_ALL, null, 2000);
    }

    private void initCardList() {
        cardList.clear();
        File imagesDir = new File(getFilesDir(), "images");
        for (int i=0; i<6; i++) {
            CardBean cardBean = new CardBean();
            String suffix = ".png";
            cardBean.imgName_first = "img"+i+ suffix;
            cardBean.imgName_second = "img"+i+ suffix;
            String imagePathFirst = new File(imagesDir, cardBean.imgName_first).getAbsolutePath();
            String imagePathSecond = new File(imagesDir, cardBean.imgName_second).getAbsolutePath();

            cardBean.setLocalImagePathFirst(imagePathFirst);
            cardBean.setLocalImagePathSecond(imagePathSecond);

            cardList.add(cardBean);
        }
    }

    private void initCardViewLayout() {
        mRlCardView.removeAllViews();
        int rows = 4;
        int columns = 3;
        int margin = getResources().getDimensionPixelSize(R.dimen.card_margin);

        for (int i = 0; i < rows * columns; i++) {
            View cardView = LayoutInflater.from(this).inflate(R.layout.card_view_item, mRlCardView, false);
            RelativeLayout.LayoutParams cardLp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);


            if (i % columns != 0) {
                cardLp.addRule(RelativeLayout.RIGHT_OF, mRlCardView.getChildAt(i - 1).getId());
                cardLp.leftMargin = margin;
            }
            if (i >= columns) {
                cardLp.addRule(RelativeLayout.BELOW, mRlCardView.getChildAt(i - columns).getId());
                cardLp.topMargin = margin;
            }

            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                cardView.setId(View.generateViewId());
            //}
            cardView.setLayoutParams(cardLp);
            mRlCardView.addView(cardView);

            CardViewHolder holder = new CardViewHolder(cardView);
            bindCardViewHolder(holder, i);
            cardView.setTag(holder);
            setCardViewClickable(i);
        }
    }

/*
    private void bindCardViewHolder(final CardViewHolder holder, final int position) {
        if (cardList == null || position >= cardList.size() * CARD_MEMBER_NUM) {
            return;
        }

        final int currPosition = indexList.get(position);
        holder.position = position;
        holder.realIndex = currPosition / CARD_MEMBER_NUM;
        holder.isFirst = currPosition % CARD_MEMBER_NUM != 0;
        CardBean realCardBean = cardList.get(holder.realIndex);
        String imgStr = holder.isFirst ? realCardBean.imgName_first : realCardBean.imgName_second;
        int cardViewId = this.getResources().getIdentifier(imgStr, "drawable", this.getPackageName());
        holder.mIvImage.setImageResource(cardViewId);

        holder.mRlBg.setTag(holder);
        holder.mIvImage.setTag(holder);
        setCameraDistance(holder.mRlBg, holder.mIvImage);
    }

*/

    private void bindCardViewHolder(final CardViewHolder holder, final int position) {
        if (cardList == null || position >= cardList.size() * CARD_MEMBER_NUM) {
            return;
        }

        final int currPosition = indexList.get(position);
        holder.position = position;
        holder.realIndex = currPosition / CARD_MEMBER_NUM;
        holder.isFirst = currPosition % CARD_MEMBER_NUM != 0;
        CardBean realCardBean = cardList.get(holder.realIndex);

        String imagePath = holder.isFirst ? realCardBean.getLocalImagePathFirst() : realCardBean.getLocalImagePathSecond();
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        holder.mIvImage.setImageBitmap(bitmap);

        holder.mRlBg.setTag(holder);
        holder.mIvImage.setTag(holder);
        setCameraDistance(holder.mRlBg, holder.mIvImage);
    }

    private void setCameraDistance(View view1, View view2) {
        int distance = 16000;
        float scale = px(distance);
        view1.setCameraDistance(scale);
        view2.setCameraDistance(scale);
    }


    private void initIndexList() {
        if (cardNum <= 0)
            return;
        this.indexList.clear();
        for (int i = 0; i < cardNum; i++) {
            this.indexList.add(i);
        }
        Collections.shuffle(this.indexList);
    }


    private void loadAnim() {

        for (int pos = 0; pos < cardNum; pos++) {
            mAnimOutSet[pos] = AnimatorInflater.loadAnimator(this, R.animator.flop_match_out);
            mAnimInSet[pos] = AnimatorInflater.loadAnimator(this, R.animator.flop_match_in);
            final int finalPos = pos;
            mAnimInSet[pos].addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    setCardViewClickable(finalPos);
                    if (isStart) {
                        matchCard(finalPos);
                    }
                }
            });
        }

        mAnimFailed = (AlphaAnimation) AnimationUtils.loadAnimation(this, R.anim.twinkle_rect_failed);
        mAnimFailed.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (selCardView1 == null || selCardView2 == null) return;

                flipCard(selCardView1.mIvImage, selCardView1.mRlBg, selCardView1.position);
                flipCard(selCardView2.mIvImage, selCardView2.mRlBg, selCardView2.position);
                selCardView1.isShowing = false;
                selCardView2.isShowing = false;
                initSelCardView();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

        });

        mAnimSuccess = AnimationUtils.loadAnimation(this, R.anim.twinkle_rect_success);
        mAnimSuccess.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                initSelCardView();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void setCardViewClickable(int position) {
        CardViewHolder cardViewHolder = getCardView(position);
        if (cardViewHolder == null) {
            return;
        }
        if (cardViewHolder.isShowing) {
            cardViewHolder.mIvImage.setClickable(true);
            cardViewHolder.mRlBg.setClickable(false);
        } else {
            cardViewHolder.mIvImage.setClickable(false);
            cardViewHolder.mRlBg.setClickable(true);
        }
    }


    private CardViewHolder getCardView(int position) {
        CardViewHolder cardViewHolder = null;
        View view = mRlCardView.getChildAt(position);
        if (null != view) {
            cardViewHolder = (CardViewHolder) view.getTag();
        }
        return cardViewHolder;
    }

    private void initSelCardView() {
        selCardView1 = null;
        selCardView2 = null;
    }


    private void matchCard(int currPosition) {

        if (selCardView1 == null || selCardView2 == null || selCardView2.position != currPosition) {
            if (selCardView2 == null && selCardView1 != null) {
                commitAction(IA_FLOP_BACK, FLOP_BACK_ONE, currPosition, 3000);
            }
            return;
        }
        boolean isMatched = selCardView1.realIndex == selCardView2.realIndex;
        if (isMatched) {
            mClickFlag = mClickFlag | (0x1 << selCardView1.realIndex);
        }

        playMatchResAnim(selCardView1.mIvRect, isMatched);
        playMatchResAnim(selCardView2.mIvRect, isMatched);
    }


    private void playMatchResAnim(ImageView view, boolean isMatched) {
        view.setVisibility(View.VISIBLE);
        if (isMatched) {
            view.setImageResource(R.drawable.btn_rectangle_green);
            view.startAnimation(mAnimSuccess);
            partlyDone(true);
        } else {
            view.setImageResource(R.drawable.btn_rectangle_red);
            view.startAnimation(mAnimFailed);
            partlyDone(false);
        }
    }

    private boolean checkEnd() {
        return mClickFlag == (0x1 << cardList.size()) - 1;
    }


    private void partlyDone(boolean isCorrect) {
        if (isCorrect) {
            commitAction(ONCE_DONE, SOUND_CORRECT, null, 0);
        } else {
            commitAction(ONCE_DONE, SOUND_WRONG, null, 0);
        }
    }


    private void flipCard(View view1, View view2, int pos) {
        if (pos < 0) {
            return;
        }
        delayAction.valid();
        removeActionById(IA_FLOP_BACK);
        view1.setClickable(false);
        view2.setClickable(false);

        mAnimOutSet[pos].setTarget(view1);
        mAnimInSet[pos].setTarget(view2);
        mAnimOutSet[pos].start();
        mAnimInSet[pos].start();

    }

    private static class CardViewHolder {
        private ImageView mIvImage;
        private RelativeLayout mRlBg;
        private ImageView mIvRect;
        private int realIndex;
        private boolean isFirst;
        private int position = -1;
        private boolean isShowing = true;

        private CardViewHolder(View itemView) {
            mIvImage = (ImageView) itemView.findViewById(R.id.mIvImage);
            mRlBg = (RelativeLayout) itemView.findViewById(R.id.mRlBg);
            mIvRect = (ImageView) itemView.findViewById(R.id.rect_iv);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case IA_FLOP_BACK:
                if (msg.arg1 == FLOP_BACK_ALL) {
                    delayAction.valid();
                    flipBackAllCards();
                } else if (msg.arg1 == FLOP_BACK_ONE) {
                    if (msg.obj instanceof Integer) {
                        CardViewHolder cardView = getCardView((Integer) msg.obj);
                        cardView.isShowing = false;
                        flipCard(cardView.mIvImage, cardView.mRlBg, (Integer) msg.obj);
                        initSelCardView();
                    }
                }
                break;
            case ONCE_DONE:
                playAnswerVoice(msg.arg1 == SOUND_CORRECT);
                break;
            default:
                break;
        }
        return true;
    }

    public void onClick(View view) {
        CardViewHolder cardViewHolder = (CardViewHolder) view.getTag();
        isStart = true;

        if (!isAddAllowed() || delayAction.invalid()) {
            return;
        }
        selCard(cardViewHolder);
        cardViewHolder.isShowing = true;
        flipCard(cardViewHolder.mRlBg, cardViewHolder.mIvImage, cardViewHolder.position);
    }

    private void playAnswerVoice(boolean correct) {
        releaseAudio();
        if (checkEnd()) {
            mediaPlayer= MediaPlayer.create(this, R.raw.win_sound);
        } else if (correct) {
            mediaPlayer= MediaPlayer.create(this, R.raw.correct_sound);
        } else {
            mediaPlayer= MediaPlayer.create(this, R.raw.wrong_sound);
        }
        mediaPlayer.start();

    }

    private void releaseAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void flipBackAllCards() {
        if (cardNum <= 0)
            return;
        for (int pos = 0; pos < cardNum; pos++) {
            CardViewHolder cardView = getCardView(pos);
            cardView.isShowing = false;
            flipCard(cardView.mIvImage, cardView.mRlBg, pos);
        }
        initSelCardView();
    }


    private void selCard(CardViewHolder cardViewHolder) {
        if (selCardView1 == null) {
            selCardView1 = cardViewHolder;
        } else if (selCardView2 == null) {
            selCardView2 = cardViewHolder;
        }
    }


    private boolean isAddAllowed() {
        return !(selCardView1 != null && selCardView2 != null);
    }

    public int px(float dp) {
        int result = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
        return result > 0 ? result : 1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseAudio();
        //stop timer
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        //stop game monitor
        if (gameMonitorThread != null) {
            gameMonitorThread.interrupt();
        }

    }

    private void startTimer() {
        final long totalTime = 60000; // 60 seconds in milliseconds
        final long interval = 1000; // 1 second in milliseconds
        final long last15Seconds = 15000; // 15 seconds in milliseconds

        tvTimer = findViewById(R.id.tvTimer);

        countDownTimer = new CountDownTimer(totalTime, interval) {
            public void onTick(long millisUntilFinished) {

                tvTimer.setText(formatTime(millisUntilFinished));
                if (millisUntilFinished <= last15Seconds) {
                    tvTimer.setTextColor(Color.RED);
                }
            }

            public void onFinish() {
                tvTimer.setText("00:00");
                gameOver();
            }
        }.start();
    }

    private String formatTime(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private void showGameOverDialog(boolean isWin) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(isWin ? "WIN" : "LOSE");
        dialogBuilder.setMessage(isWin ? "Congratulations! You've won!" : "Game Over! Try again?");

        int titleColor = isWin ? 0xFFD700 : Color.RED;
        dialogBuilder.setTitle(Html.fromHtml("<font color='" + titleColor + "'>" + (isWin ? "WIN" : "LOSE") + "</font>"));

        dialogBuilder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                restartGame();
            }
        });

        dialogBuilder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Back to ?
                finish();
            }
        });

        AlertDialog gameOverDialog = dialogBuilder.create();
        gameOverDialog.show();
    }
    private void gameOver() {
        showGameOverDialog(checkEnd());
    }

    private void restartGame() {
        mClickFlag = 0x0;
        isStart = false;
/*      initCardList();
        Collections.shuffle(indexList);
        initCardViewLayout();*/
        releaseAudio();
        //stop monitor
        if (gameMonitorThread != null) {
            gameMonitorThread.interrupt();
        }

        tvTimer.setTextColor(Color.BLACK);
        tvTimer.setText(formatTime(60000));
        initView();
        initData();
        loadAnim();
        startGameMonitor();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        startTimer();
    }
}