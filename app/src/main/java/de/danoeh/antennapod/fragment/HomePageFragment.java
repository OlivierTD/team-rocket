package de.danoeh.antennapod.fragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.menuhandler.MenuItemUtils;


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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.home_page, container, false);

        return root;
    }

    //inflates the top menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        final MainActivity activity = (MainActivity)getActivity();

        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.home_toolbar, menu);
    }

    //Handles the options selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.search_home:
                Log.d("Home Search","Redirecting to centralized search results...");
                FragmentManager manager = getFragmentManager();
                PodSearchFragment fragment = new PodSearchFragment();
                manager.beginTransaction()
                        .replace(R.id.main_view,fragment,fragment.getTag())
                        .commit();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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