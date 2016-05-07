package org.twoflies.enhanceyourcalm;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class TimerPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private List<Fragment> mFragments = new ArrayList<>();
    private List<String> mTitles = new ArrayList<>();

    public TimerPagerAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager);

        mContext = context;
    }

    public void addTimer(int time) {
        mFragments.add(TimerFragment.newInstance(time * 60));
        mTitles.add(String.format(mContext.getResources().getString(R.string.timer_fragment_title_format), time));
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }
}
