package org.twoflies.enhanceyourcalm;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class TimerFragment extends Fragment {

    private static final String ARG_TOTAL_TIME_SECONDS = "totalTimeSeconds";
    private static final float ALPHA_HIDDEN = 0.1f;
    private static final long ANIMATION_TIME_MILLIS = 1000;

    private int mTotalTimeSeconds;
    private TextView mRemainingTime;
    private ProgressIndicatorView mIndicator;
    private String mRemainingTimeFormatString;

    public TimerFragment() {
    }

    public static TimerFragment newInstance(int totalTimeSeconds) {
        TimerFragment fragment = new TimerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TOTAL_TIME_SECONDS, totalTimeSeconds);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTotalTimeSeconds = getArguments().getInt(ARG_TOTAL_TIME_SECONDS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);

        mRemainingTime = (TextView)view.findViewById(R.id.remainingtime);
        mIndicator = (ProgressIndicatorView)view.findViewById(R.id.indicator);

        mRemainingTimeFormatString = getString(R.string.timer_fragment_remaining_time_format);

        resetTimer();

        return view;
    }

    public int getTotalTimeSeconds() {
        return mTotalTimeSeconds;
    }

    public void setRemainingTime(int timeSeconds) {
        int minutes = timeSeconds / 60;
        int seconds = timeSeconds % 60;
        mRemainingTime.setText(String.format(mRemainingTimeFormatString, minutes, seconds));
        mIndicator.setProgress((mTotalTimeSeconds - timeSeconds) / (float)mTotalTimeSeconds);
    }

    public void startTimer() {
        cancelAnimateRemainingTime();
    }

    public void pauseTimer() {
        animateHideRemainingTime();
    }

    public void expireTimer() {
        mRemainingTime.setTextColor(getResources().getColor(R.color.colorAccent));
        mRemainingTime.setText(String.format(mRemainingTimeFormatString, 0, 0));
        mIndicator.setComplete(true);

        animateHideIndicator();
    }

    public void resetTimer() {
        mRemainingTime.setTextColor(getResources().getColor(R.color.colorText));
        setRemainingTime(mTotalTimeSeconds);
        mIndicator.setComplete(false);

        cancelAnimateRemainingTime();
        cancelAnimateIndicator();
    }

    private void animateHideRemainingTime() {
        mRemainingTime.animate().alpha(ALPHA_HIDDEN).setDuration(ANIMATION_TIME_MILLIS).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                animateShowRemainingTime();
            }
        });
    }

    private void animateShowRemainingTime() {
        mRemainingTime.animate().alpha(1.0f).setDuration(ANIMATION_TIME_MILLIS).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animateHideRemainingTime();
            }
        });
    }

    private void cancelAnimateRemainingTime() {
        mRemainingTime.animate().setListener(null).cancel();
        mRemainingTime.setAlpha(1.0f);
    }

    private void animateHideIndicator() {
        mIndicator.animate().alpha(ALPHA_HIDDEN).setDuration(ANIMATION_TIME_MILLIS).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                animateShowIndicator();
            }
        });
    }

    private void animateShowIndicator() {
        mIndicator.animate().alpha(1.0f).setDuration(ANIMATION_TIME_MILLIS).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animateHideIndicator();
            }
        });
    }

    private void cancelAnimateIndicator() {
        mIndicator.animate().setListener(null).cancel();
        mIndicator.setAlpha(1.0f);
    }
}
