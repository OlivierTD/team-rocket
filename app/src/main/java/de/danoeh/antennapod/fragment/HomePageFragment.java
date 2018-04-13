package de.danoeh.antennapod.fragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

/**
 * Created by Sai Shan on 2018-02-02.
 */

public class HomePageFragment extends Fragment{

    private TextView txtHome;
    public static final String TAG = "HomePageFragment";
    private static View root;

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
            ((MainActivity) getActivity()).getSupportActionBar().setTitle("Home");
        try {
            root = inflater.inflate(R.layout.home_page, container, false);
            return root;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return root;
    }

    //inflates the top menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.home_toolbar, menu);
    }

    //Handles the options selected
    //Redirects to the search page with the random episode button
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.search_home:
                Log.d("Home Search","Redirecting to centralized search results...");
                FragmentManager manager = getFragmentManager();
                PodSearchFragment fragment = new PodSearchFragment();
                manager.beginTransaction()
                        .replace(R.id.main_view,fragment,fragment.getTag())
                        .addToBackStack(null)
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