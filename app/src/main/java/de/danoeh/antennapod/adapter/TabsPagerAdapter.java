package de.danoeh.antennapod.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.fragment.HomePageFragment;
import de.danoeh.antennapod.fragment.ToplistFragment;

/**
 * Created by Sai Shan on 2018-04-10.
 */

public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter (FragmentManager fm){
        super(fm);
    }

    public Fragment getItem(int index){
        switch (index){
            case 0:
                return new HomePageFragment();
            case 2:
               // return new ToplistFragment();
        }
    return null;
    }

    public int getCount (){
        return 2;
    }

    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "FEATURED";
            case 1:
                return "CATEGORIES";
            default:
                return super.getPageTitle(position);
        }
    }
}
