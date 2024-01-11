package com.team01.thememorygame.activity;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.team01.thememorygame.R;
import com.team01.thememorygame.Utils.DelayAction;
import com.team01.thememorygame.Utils.GameUtils;
import com.team01.thememorygame.game.GameOverDialogManager;
import com.team01.thememorygame.game.GameTimer;
import com.team01.thememorygame.model.CardBean;
import com.team01.thememorygame.model.CardViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class CardMatchActivity extends AppCompatActivity implements GameTimer.TimerListener, GameOverDialogManager.GameOverDialogListener, Handler.Callback {

    private GameOverDialogManager gameOverDialogManager;
    @Override
    public void onRestartGame() {
        restartGame();
    }
    @Override
    public void onFinishActivity() {
        finish();
    }

    private Handler handler;// handle message


    private void initHandler() {
        if (handler ==null) {
            handler = new Handler(Looper.getMainLooper(),this);
        }
    }
    // send message to Handler to execute delayed operations
    private void commitAction(int id,int arg,Object extra,int ms) {
        if (handler ==null) {
            initHandler();
        }
        handler.sendMessageDelayed(Message.obtain(handler,id,arg,0,extra),ms);
    }
    // remove specified type messages from handler
    private void removeActionById() {
        if (handler !=null) {
            handler.removeMessages(CardMatchActivity.IA_FLOP_BACK);
        }
    }
    // Game monitoring thread, check whether the game is over
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
                                gameTimer.stopTimer();
                                gameOver();
                            }
                        });
                        break;
                    }
                    try {
                        Thread.sleep(1000); // check every 1s
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
    private static final int ONCE_DONE = 3; // check for card match completion
    private static final int SOUND_CORRECT = 4;
    private static final int SOUND_WRONG = 5;
    private static final int CARD_MEMBER_NUM = 2;

    private boolean isHardMode = false;
    private int currentMatchCount = 0;
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

    private GameTimer gameTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        loadAnim();
        startGameMonitor();
        initTimer();
        setupHardModeSwitcher();
        gameOverDialogManager = new GameOverDialogManager(this, this);
    }

    private void initTimer() {
        TextView tvTimer = findViewById(R.id.tvTimer);
        gameTimer =  new GameTimer(this, tvTimer);
    }

    @Override
    public void onTimerFinish() {
        gameOver();
    }

    private void setupHardModeSwitcher() {
        Switch hardModeSwitch = findViewById(R.id.hard_mode_switch);

        if (hardModeSwitch != null) {
            hardModeSwitch.setChecked(isHardMode);
        }
        hardModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isHardMode = isChecked;
                if (isHardMode) {
                    hardModeSwitch.setEnabled(false);
                }
            }
        });
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
        // execute flipping all cards
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
        //Loop through creating card views and setting layout parameters
        for (int i = 0; i < rows * columns; i++) {
            View cardView = LayoutInflater.from(this).inflate(R.layout.card_view_item, mRlCardView, false);
            RelativeLayout.LayoutParams cardLp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);

            // Set the position relationship and margin of the card
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

    private void bindCardViewHolder(final CardViewHolder cardViewHolder, final int position) {
        if (cardList == null || position >= cardList.size() * CARD_MEMBER_NUM) {
            return;
        }

        final int currPosition = indexList.get(position); // from 0 to 11
        cardViewHolder.position = position;
        cardViewHolder.realIndex = currPosition / CARD_MEMBER_NUM; // 0, 1, 2, 3, 4, 5
        cardViewHolder.isFirst = currPosition % CARD_MEMBER_NUM != 0;
        CardBean realCardBean = cardList.get(cardViewHolder.realIndex);
        //Load the image into the card view based on the card data
        String imagePath = cardViewHolder.isFirst ? realCardBean.getLocalImagePathFirst() : realCardBean.getLocalImagePathSecond();
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        cardViewHolder.mIvImage.setImageBitmap(bitmap);
        //Set the label and click event of the card view
        cardViewHolder.mRlBg.setTag(cardViewHolder);
        cardViewHolder.mIvImage.setTag(cardViewHolder);
        //Set the 3D effect of the card
        GameUtils.setCameraDistance(cardViewHolder.mRlBg, cardViewHolder.mIvImage, this);
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
        // Load flip animation for matching success and failure processing

        for (int pos = 0; pos < cardNum; pos++) {
            //Load the animation of successful flip matching
            mAnimOutSet[pos] = AnimatorInflater.loadAnimator(this, R.animator.flop_match_out);
            //Load the animation that failed to flip the match
            mAnimInSet[pos] = AnimatorInflater.loadAnimator(this, R.animator.flop_match_in);
            final int finalPos = pos;
            //Add a listener for the successfully matched flip animation,
            // and handle the clickability and matching logic of the card
            // at the end of the animation
            mAnimInSet[pos].addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    setCardViewClickable(finalPos);//Set the clickability of the card
                    if (isStart) {
                        matchCard(finalPos);// If the game has started, execute matching logic
                    }
                }
            });
        }

        //The animation of card flashing when loading fails
        mAnimFailed = (AlphaAnimation) AnimationUtils.loadAnimation(this, R.anim.twinkle_rect_failed);
        mAnimFailed.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //Operation at the beginning of animation
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Actions at the end of the animation,
                // including flipping the card and initializing the selected card view
                if (selCardView1 == null || selCardView2 == null) return;

                flipCard(selCardView1.mIvImage, selCardView1.mRlBg, selCardView1.position);
                flipCard(selCardView2.mIvImage, selCardView2.mRlBg, selCardView2.position);
                selCardView1.isShowing = false;
                selCardView2.isShowing = false;
                initSelCardView();//Initialize the selected card view
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Operation when animation repeats
            }

        });
        //The animation of the card flashing when loading is successful
        mAnimSuccess = AnimationUtils.loadAnimation(this, R.anim.twinkle_rect_success);
        mAnimSuccess.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //Operation at the beginning of animation
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Operation at the end of the animation, initialize the selected card view
                initSelCardView();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }
    //Set the clickability of the card view
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

    // Get the card view holder based on position
    // Save references to individual subviews within each card view
    // By saving these elements in a Holder object,
    // you can avoid repeatedly calling the findViewById() method
    // when frequently operating the view, thereby reducing resource consumption
    // and improving application performance.
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

    // Match cards and handle the success or failure of matching
    private void matchCard(int currPosition) {

        if (selCardView1 == null || selCardView2 == null || selCardView2.position != currPosition) {
            if (selCardView2 == null && selCardView1 != null) {
                commitAction(IA_FLOP_BACK, FLOP_BACK_ONE, currPosition, 3000);
            }
            return;
        }
        boolean isMatched = selCardView1.realIndex == selCardView2.realIndex;
        // If there is a match, update the game status and the number of matches
        if (isMatched) {
            currentMatchCount++;
            updateMatchCount(currentMatchCount);
        }

        if (!isMatched && isHardMode){
            gameTimer.timeLeftInMillis -= 5000; // -5s
            gameTimer.timeLeftInMillis = Math.max(gameTimer.timeLeftInMillis, 0);
        }

        playMatchResAnim(selCardView1.mIvRect, isMatched);
        playMatchResAnim(selCardView2.mIvRect, isMatched);

        gameTimer.startTimer(gameTimer.timeLeftInMillis);

    }

    public void updateMatchCount(int count) {
        final int totalPairs = cardNum>>1 ;
        TextView tvMatchCount = findViewById(R.id.tvMatchCount);
        String matchText = "Match: " + count + "/" + totalPairs;
        tvMatchCount.setText(matchText);
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

    // checking if the game has reached its end
    private boolean checkEnd() {
        return currentMatchCount == cardList.size();
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
        removeActionById();
        view1.setClickable(false);
        view2.setClickable(false);

        mAnimOutSet[pos].setTarget(view1);
        mAnimInSet[pos].setTarget(view2);
        mAnimOutSet[pos].start();
        mAnimInSet[pos].start();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseAudio();
        //stop timer
        gameTimer.stopTimer();
        //stop game monitor
        if (gameMonitorThread != null) {
            gameMonitorThread.interrupt();
        }

    }

    private void gameOver() {
        gameOverDialogManager.showGameOverDialog(checkEnd());
        Switch hardModeSwitch = findViewById(R.id.hard_mode_switch);

        if (hardModeSwitch != null) {
            hardModeSwitch.setEnabled(true);
        }
    }

    private void restartGame() {
        isStart = false;
        isHardMode = false;
/*      initCardList();
        Collections.shuffle(indexList);
        initCardViewLayout();*/
        releaseAudio();
        //stop monitor
        if (gameMonitorThread != null) {
            gameMonitorThread.interrupt();
        }
        currentMatchCount = 0;
        updateMatchCount(currentMatchCount);

        initView();
        initData();
        loadAnim();
        startGameMonitor();
        Switch hardModeSwitch = findViewById(R.id.hard_mode_switch);
        if (hardModeSwitch != null) {
            hardModeSwitch.setEnabled(true);
            hardModeSwitch.setChecked(false);
            hardModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    isHardMode = isChecked;
                    if (isHardMode) {
                        hardModeSwitch.setEnabled(false);
                    }
                }
            });
        }
        initTimer();
    }
}