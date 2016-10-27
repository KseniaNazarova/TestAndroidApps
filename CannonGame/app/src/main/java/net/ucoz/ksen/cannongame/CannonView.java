package net.ucoz.ksen.cannongame;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

public class CannonView extends SurfaceView {

    public static final String TAG = "CannonView";

    public static final int MISS_PENALTY = 2; // Штраф при промахе
    public static final int HIT_REWARD = 3; // Прибавка при попадании

    // Пушка
    public static final double CANNON_BASE_RADIUS_PERCENT = 3.0 / 40;
    public static final double CANNON_BARREL_WIDTH_PERCENT = 3.0 / 40;
    public static final double CANNON_BARREL_LENGTH_PERCENT = 1.0 / 10;

    // Ядро
    public static final double CANNONBALL_RADIUS_PERCENT = 3.0 / 80;
    public static final double CANNONBALL_SPEED_PERCENT = 3.0 / 2;

    // Мишень
    public static final double TARGET_WIDTH_PERCENT = 1.0 / 40;
    public static final double TARGET_LENGTH_PERCENT = 3.0 / 20;
    public static final double TARGET_FIRST_X_PERCENT = 3.0 / 5;
    public static final double TARGET_SPACING_PERCENT = 1.0 / 60;
    public static final double TARGET_PIECES = 9;
    public static final double TARGET_MIN_SPEED_PERCENT = 3.0 / 4;
    public static final double TARGET_MAX_SPEED_PERCENT = 6.0 /4;

    // Блок
    public static final double BLOCKER_WIDTH_PERCENT = 1.0 / 40;
    public static final double BLOCKER_LENGTH_PERCENT = 1.0 / 4;
    public static final double BLOCKER_X_PERCENT = 1.0 / 2;
    public static final double BLOCKER_SPEED_PERCENT = 1.0;

    // Текст
    public static final double TEXT_SIZE_PERCERNT = 1.0 / 18;

    // Звуки
    public static final int TARGET_SOUND_ID = 0;
    public static final int CANNON_SOUND_ID = 1;
    public static final int BLOCKER_SOUND_ID = 2;
    private SoundPool soundPool;
    private SparseIntArray soundMap;

    private CannonThread cannonThread;
    private Activity activity;
    private boolean dialogIsDisplayed = false;

    private Cannon cannon;
    private Blocker blocker;
    private ArrayList<Target> targets;

    private int screenWidth;
    private int screenHeight;

    private boolean gameOver;
    private double timeLeft;
    private int shotsFired;
    private double totalElapsedTime;

    private Paint textPaint;
    private Paint backgroundPaint;

    public CannonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        activity = (Activity) context;

        getHolder().addCallback((SurfaceHolder.Callback) this);

        AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
        attrBuilder.setUsage(AudioAttributes.USAGE_GAME);

        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setMaxStreams(1);
        builder.setAudioAttributes(attrBuilder.build());
        soundPool = builder.build();

        soundMap = new SparseIntArray(3);
        soundMap.put(TARGET_SOUND_ID, soundPool.load(context, R.raw.target_hit, 1));
        soundMap.put(CANNON_SOUND_ID, soundPool.load(context, R.raw.cannon_fire, 1));
        soundMap.put(BLOCKER_SOUND_ID, soundPool.load(context, R.raw.blocker_hit, 1));

        textPaint = new Paint();
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        screenHeight = h;
        screenWidth = w;

        textPaint.setTextSize((float) (TEXT_SIZE_PERCERNT * screenHeight));
        textPaint.setAntiAlias(true);
    }

    public void playSound(int soundId){
        soundPool.play(soundMap.get(soundId), 1, 1, 1, 0, 1f);
    }

    public int getScreenHeight(){
        return screenHeight;
    }

    public int getScreenWidth(){
        return screenWidth;
    }

    public void newGame(){
        cannon = new Cannon(this, (int)(CANNON_BASE_RADIUS_PERCENT * screenHeight),
                (int)(CANNON_BARREL_LENGTH_PERCENT * screenWidth),
                (int)(CANNON_BARREL_WIDTH_PERCENT * screenHeight));

        Random random = new Random();
        targets = new ArrayList<>();

        int targetX = (int)(TARGET_FIRST_X_PERCENT * screenWidth);
        int targetY = (int)((0.5 - TARGET_LENGTH_PERCENT / 2) * screenHeight);

        for (int i = 0; i < TARGET_PIECES; i++) {
            double velocity = screenHeight * (random.nextDouble() * (TARGET_MAX_SPEED_PERCENT - TARGET_MIN_SPEED_PERCENT) + TARGET_MIN_SPEED_PERCENT);
            int color = (i % 2 == 0) ? getResources().getColor(R.color.dark, getContext().getTheme()) : getResources().getColor(R.color.light, getContext().getTheme());
            velocity *= -1;

            targets.add(new Target(this, color, HIT_REWARD, targetX, targetY, (int)(TARGET_WIDTH_PERCENT * screenWidth), (int)(TARGET_LENGTH_PERCENT * screenHeight), (int)velocity));
            targetX += (TARGET_WIDTH_PERCENT + TARGET_SPACING_PERCENT) * screenWidth;
        }

        blocker = new Blocker(this, Color.BLACK, MISS_PENALTY, (int)(BLOCKER_X_PERCENT * screenWidth), (int)((0.5 - BLOCKER_LENGTH_PERCENT / 2) * screenHeight), (int)(BLOCKER_WIDTH_PERCENT * screenWidth), (int)(BLOCKER_LENGTH_PERCENT * screenHeight), (float)(BLOCKER_SPEED_PERCENT * screenHeight));
        timeLeft = 10;
        shotsFired = 0;
        totalElapsedTime = 0.0;

        if (gameOver){
            gameOver = false;
            cannonThread = new CannonThread(getHolder());
            cannonThread.start();
        }

        hideSystemBars();
    }

}
