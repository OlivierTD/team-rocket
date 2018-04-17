package de.danoeh.antennapod.fragment;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.fragment.CentralizedSearchFragment;
import de.danoeh.antennapod.menuhandler.MenuItemUtils;

/**
 * Created by Sai Shan on 2018-02-02.
 */

public class HomePageFragment extends Fragment{

    private TextView txtHome;
    public static final String TAG = "HomePageFragment";
    private View root;

    public HomePageFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        try {
            root = inflater.inflate(R.layout.home_page, container, false);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return root;
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
    }
}