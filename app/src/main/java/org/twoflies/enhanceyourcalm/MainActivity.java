package org.twoflies.enhanceyourcalm;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements TimerRunnable.Callback, AudioManager.OnAudioFocusChangeListener {

    public static final String LOG_TAG = "EnhanceYourCalm";
    public static final String INTENT_EXTRA_ALARM_EXPIRED = "org.twoflies.enhanceyourcalm.AlarmExpired";

    private static final int TIMER_STATE_NOT_STARTED = 0;
    private static final int TIMER_STATE_RUNNING = 1;
    private static final int TIMER_STATE_PAUSED = 2;
    private static final int TIMER_STATE_EXPIRED = 3;

    private static final int NOTIFICATION_ID = 100;
    private static final long NOTIFICATION_UPDATE_FREQUENCY_MILLIS = 30 * 1000;

    private TabLayout mTabLayout;
    private LockableViewPager mViewPager;
    private FloatingActionButton mFabStartPause;
    private ImageButton mFabReset;
    private int mTimerState = TIMER_STATE_NOT_STARTED;
    private PendingIntent mPendingAlarmIntent;
    private TimerRunnable mTimerRunnable;
    private boolean mIsVisible;
    private int mRemainingTimeSeconds;
    private long mLastNotificationMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabLayout = (TabLayout)findViewById(R.id.tablayout);
        mViewPager = (LockableViewPager)findViewById(R.id.viewpager);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mTimerRunnable = null;
            }
        });

        setupViewPager();
        mTabLayout.setupWithViewPager(mViewPager);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mFabStartPause = (FloatingActionButton)findViewById(R.id.fabstartpause);
        mFabStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTimerState == TIMER_STATE_RUNNING) {
                    pauseTimer();
                } else if (mTimerState == TIMER_STATE_EXPIRED) {
                    resetTimer();
                }
                else {
                    startTimer();
                }
            }
        });

        mFabReset = (ImageButton)findViewById(R.id.fabreset);
        mFabReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTimerState == TIMER_STATE_PAUSED) {
                    resetTimer();
                }
            }
        });

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    private void setupViewPager()
    {
        TimerPagerAdapter timerPagerAdapter = new TimerPagerAdapter(this, getSupportFragmentManager());
        timerPagerAdapter.addTimer(15);
        timerPagerAdapter.addTimer(30);
        timerPagerAdapter.addTimer(60);
        mViewPager.setAdapter(timerPagerAdapter);
    }

    private void handleIntent(Intent intent) {
        Log.d(LOG_TAG, "Handling intent: " + intent.toString() + ", IsVisible: " + mIsVisible);

        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }

        if (extras.getBoolean(INTENT_EXTRA_ALARM_EXPIRED, false)) {
            alertTimer(!mIsVisible);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mIsVisible = true;
    }

    @Override
    protected void onStop() {
        super.onStop();

        mIsVisible = false;
        if (mTimerState == TIMER_STATE_RUNNING) {
            sendNotification();
        }
    }

    private void startTimer() {
        mTabLayout.setVisibility(View.GONE);
        mViewPager.setLocked(true);

        mFabStartPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_white));
        mFabReset.setVisibility(View.INVISIBLE);

        TimerFragment timerFragment = getTimerFragment();
        if (mTimerState == TIMER_STATE_NOT_STARTED) {
            mTimerRunnable = new TimerRunnable(timerFragment.getTotalTimeSeconds(), this);
        }
        createAlarm(mTimerRunnable.getRemainingTimeSeconds());
        mTimerRunnable.start();
        timerFragment.startTimer();
        cancelNotification();

        mTimerState = TIMER_STATE_RUNNING;
    }

    private void pauseTimer() {
        mFabStartPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_white));
        mFabReset.setVisibility(View.VISIBLE);

        cancelAlarm();
        mTimerRunnable.pause();
        getTimerFragment().pauseTimer();
        cancelNotification();

        mTimerState = TIMER_STATE_PAUSED;
    }

    private void resetTimer() {
        mTabLayout.setVisibility(View.VISIBLE);
        mViewPager.setLocked(false);

        mFabStartPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_white));
        mFabReset.setVisibility(View.INVISIBLE);

        mTimerRunnable.reset();
        getTimerFragment().resetTimer();
        cancelNotification();

        mTimerState = TIMER_STATE_NOT_STARTED;
    }

    private void expireTimer() {
        mFabStartPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_white));

        getTimerFragment().expireTimer();

        mTimerState = TIMER_STATE_EXPIRED;
    }

    private void alertTimer(boolean sendNotification) {
        if (sendNotification) {
            sendNotification();
        }
        playAudio();
    }

    private void sendNotification() {
        String notificationText;
        if (mTimerState == TIMER_STATE_EXPIRED) {
            notificationText = getString(R.string.notification_text_expired);
        } else {
            int remainingMinutes = mRemainingTimeSeconds / 60;
            int remainingSeconds = mRemainingTimeSeconds % 60;
            if (remainingSeconds >= 30) {
                remainingMinutes++;
            }

            if (remainingMinutes > 1) {
                notificationText = String.format(getString(R.string.notification_text_remaining_time_format), remainingMinutes);
            } else {
                notificationText = getString(R.string.notification_text_less_than_one_minutes);
            }
        }

        NotificationCompat.Builder builder = (NotificationCompat.Builder)new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(notificationText)
                .setAutoCancel(true)
                .setVisibility(Notification.VISIBILITY_PUBLIC);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setColor(getColor(R.color.colorAccent));
        }

        Intent intent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Service.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        mLastNotificationMillis = System.currentTimeMillis();
    }

    private void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Service.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void playAudio() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            return;
        }

        try {
            int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                return;
            }

            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.bowl);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.release();
                }
            });
            mediaPlayer.start();

        } finally {
            audioManager.abandonAudioFocus(this);
        }
    }

    private void createAlarm(int seconds) {
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        mPendingAlarmIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        long triggerTime = SystemClock.elapsedRealtime() + (seconds * 1000);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, mPendingAlarmIntent);
    }

    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(mPendingAlarmIntent);
    }

    private TimerFragment getTimerFragment() {
        TimerPagerAdapter timerPagerAdapter = (TimerPagerAdapter)mViewPager.getAdapter();
        return (TimerFragment)timerPagerAdapter.getItem(mViewPager.getCurrentItem());
    }

    @Override
    public void onTimerTick(int remainingTimeSeconds) {
        mRemainingTimeSeconds = remainingTimeSeconds;
        getTimerFragment().setRemainingTime(remainingTimeSeconds);

        if (!mIsVisible && (System.currentTimeMillis() - mLastNotificationMillis) > NOTIFICATION_UPDATE_FREQUENCY_MILLIS) {
            sendNotification();
        }
    }

    @Override
    public void onTimerExpired() {
        expireTimer();
    }

    @Override
    public void onAudioFocusChange(int i) {

    }
}
