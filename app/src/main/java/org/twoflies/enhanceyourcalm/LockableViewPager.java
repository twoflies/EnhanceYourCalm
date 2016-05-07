package org.twoflies.enhanceyourcalm;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class LockableViewPager extends ViewPager {

    private boolean mLocked;

    public LockableViewPager(Context context) {
        super(context);
    }

    public LockableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return !mLocked && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return !mLocked && super.onInterceptTouchEvent(event);
    }

    public void setLocked(boolean locked) {
        mLocked = locked;
    }

    public boolean isLocked() {
        return mLocked;
    }
}
