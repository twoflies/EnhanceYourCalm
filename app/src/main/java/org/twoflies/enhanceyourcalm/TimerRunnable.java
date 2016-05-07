package org.twoflies.enhanceyourcalm;

import android.os.Handler;

public class TimerRunnable implements Runnable {

    private static final int TIMER_TICK_MS = 100;

    private int mTotalTimeSeconds;
    private Callback mCallback;
    private int mStartTimeSeconds;
    private int mRemainingTimeSeconds;
    private long mStartTimeMillis;
    private Handler mHandler;

    public TimerRunnable(int totalTimeSeconds, Callback callback) {
        mTotalTimeSeconds = totalTimeSeconds;
        mCallback = callback;

        reset();
    }

    @Override
    public void run() {
        mRemainingTimeSeconds = mStartTimeSeconds - (int)((System.currentTimeMillis() - mStartTimeMillis) / 1000);
        if (mRemainingTimeSeconds > 0) {
            mCallback.onTimerTick(mRemainingTimeSeconds);
            mHandler.postDelayed(this, TIMER_TICK_MS);
        } else {
            mCallback.onTimerExpired();
            mHandler.removeCallbacks(this);
        }
    }

    public int getRemainingTimeSeconds() {
        return mRemainingTimeSeconds;
    }

    public synchronized void start() {
        if (mHandler != null) {
            mHandler.removeCallbacks(this);
        }

        mStartTimeSeconds = mRemainingTimeSeconds;
        mStartTimeMillis = System.currentTimeMillis();

        mHandler = new Handler();
        mHandler.postDelayed(this, TIMER_TICK_MS);
    }

    public synchronized void pause() {
        if (mHandler != null) {
            mHandler.removeCallbacks(this);
        }
    }

    public synchronized void reset() {
        mRemainingTimeSeconds = mTotalTimeSeconds;
    }

    interface Callback {
        void onTimerTick(int remainingTimeSeconds);
        void onTimerExpired();
    }
}
