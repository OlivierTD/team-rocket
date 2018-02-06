package de.danoeh.antennapod.fragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.TextView;

/**
 * Created by Sai Shan on 2018-02-02.
 */

public class HomePageFragment extends Fragment{

    private TextView txtHome;
    public static final String TAG = "HomePageFragment";

    public HomePageFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
}
