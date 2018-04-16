package de.danoeh.antennapod.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.core.preferences.UserPreferences;
import de.danoeh.antennapod.preferences.PreferenceController;

/**
 * PreferenceActivity for API 11+. In order to change the behavior of the preference UI, see
 * PreferenceController.
 */
public class PreferenceActivity extends AppCompatActivity {

    private static WeakReference<PreferenceActivity> instance;
    private PreferenceController preferenceController;
    private MainFragment prefFragment;
    private final PreferenceController.PreferenceUI preferenceUI = new PreferenceController.PreferenceUI() {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public Preference findPreference(CharSequence key) {
            return prefFragment.findPreference(key);
        }

        @Override
        public Activity getActivity() {
            return PreferenceActivity.this;
        }
    };

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // This must be the FIRST thing we do, otherwise other code may not have the
        // reference it needs
        instance = new WeakReference<>(this);

        setTheme(UserPreferences.getTheme());
        super.onCreate(savedInstanceState);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        // set up layout
        FrameLayout root = new FrameLayout(this);
        root.setId(R.id.content);
        root.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);

        // we need to create the PreferenceController before the MainFragment
        // since the MainFragment depends on the preferenceController already being created
        preferenceController = new PreferenceController(preferenceUI);

        prefFragment = new MainFragment();
        getFragmentManager().beginTransaction().replace(R.id.content, prefFragment).commit();

        //get saved color scheme if there is
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);


        //int color1 = getResources().getInteger(R.integer.saved_high_score_default_key);
        int color1 = sharedPref.getInt("color1", 1);
        int color2 = sharedPref.getInt("color2", 1);
        int color3 = sharedPref.getInt("color3", 1);

        if (color1==1 || color1==0) {


        } else {
            this.setActivityBackgroundColor(color1);
        }

        if (color2==1 || color2==0) {


        } else {

            View navList = this.findViewById(R.id.nav_list);
            navList.setBackgroundColor((Integer) color2);

        }

        if(color3==1 || color3==0) {


        } else {
            View navTitle = this.findViewById(R.id.nav_layout);
            navTitle.setBackgroundColor(color3);
            View navSettings = this.findViewById(R.id.nav_settings);
            navSettings.setBackgroundColor(color3);

            String hexColor = String.format("#%06X", (0xFFFFFF &color3));
            ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(hexColor));
            ActionBar bar = this.getSupportActionBar();
            bar.setBackgroundDrawable(colorDrawable);
        }
    }

    public void setActivityBackgroundColor(Integer color){
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(color);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        preferenceController.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class MainFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            addPreferencesFromResource(R.xml.preferences);
            PreferenceActivity activity = instance.get();
            if(activity != null && activity.preferenceController != null) {
                activity.preferenceController.onCreate();
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            PreferenceActivity activity = instance.get();
            if(activity != null && activity.preferenceController != null) {
                activity.preferenceController.onResume();
            }
        }

        @Override
        public void onPause() {
            PreferenceActivity activity = instance.get();
            if(activity != null && activity.preferenceController != null) {
                activity.preferenceController.onPause();
            }
            super.onPause();
        }

        @Override
        public void onStop() {
            PreferenceActivity activity = instance.get();
            if(activity != null && activity.preferenceController != null) {
                activity.preferenceController.onStop();
            }
            super.onStop();
        }
    }
}
