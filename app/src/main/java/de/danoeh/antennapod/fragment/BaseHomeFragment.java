package de.danoeh.antennapod.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;

//This fragment is the base fragment of the Home Page. The HomePageFragment and CategoriesFragment will be
//loaded in the tabs contained in the BaseHomeFragment
public class BaseHomeFragment extends Fragment{

    public static final String TAG = "BaseHomeFragment";

    private static final String LAST_TAB = "tab_position";

    private SharedPreferences prefs;

    //positions of the tabs
    public static final int POS_FEATURED = 0;
    public static final int POS_CATEGORIES = 1;
    public static final int TOTAL = 2;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    public BaseHomeFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Home Page");

        View rootView = inflater.inflate(R.layout.base_home_fragment, container, false);
        viewPager = (ViewPager)rootView.findViewById(R.id.home_viewpager);
        viewPager.setAdapter(new HomePagerAdapter(getChildFragmentManager(), getResources()));

        // Give the tabs layout the viewpager
        tabLayout = (TabLayout) rootView.findViewById(R.id.home_tabs);
        tabLayout.setupWithViewPager(viewPager);

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        // save the tab selection
        prefs = getActivity().getSharedPreferences(TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(LAST_TAB, tabLayout.getSelectedTabPosition());
        editor.apply();
    }

    @Override
    public void onStart() {
        super.onStart();
        // restore our last position
        prefs = getActivity().getSharedPreferences(TAG, Context.MODE_PRIVATE);
        int lastPosition = prefs.getInt(LAST_TAB, 0);
        viewPager.setCurrentItem(lastPosition);
    }


    //HomePagerAdapter is a static inner class of BaseHomeFragment
    public static class HomePagerAdapter extends FragmentPagerAdapter {

        private final Resources resources; //it is required in order to have the constructor
        private Fragment[] fragments = {
                new HomePageFragment(),
                new CategoriesFragment(),
        };

        //constructor
        public HomePagerAdapter(FragmentManager fm, Resources resources) {
            super(fm);
            this.resources = resources;
        }

        //get a certain fragment from the array of Fragment
        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        //get the numbers of tabs
        @Override
        public int getCount() {
            return TOTAL;
        }

        //get title of each tabs
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case POS_FEATURED:
                    return "FEATURED";
                case POS_CATEGORIES:
                    return "CATEGORIES";
                default:
                    return super.getPageTitle(position);
            }
        }
    }
}
