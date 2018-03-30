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
   // private MainActivity activity;


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



    //creates search menu on top
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){

        final MainActivity activity = (MainActivity) getActivity();
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.itunes_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final android.support.v7.widget.SearchView sv = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(searchItem);
        MenuItemUtils.adjustTextColor(getActivity(), sv);
        sv.setQueryHint(getString(R.string.home_search));
        sv.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                sv.clearFocus();
                goToResults(activity, s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

    }

    public void goToResults(MainActivity activity, String query){
        CentralizedSearchFragment centralizedSearchFragment = new CentralizedSearchFragment();
        centralizedSearchFragment.setQuery(query);
        activity.loadChildFragment(centralizedSearchFragment);
    }

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_home:
                Log.d("Home search", "Redirecting to new page to centralized search fragment...");
                //reDirect();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }*/

    //method that allows a redirect from the homepage to the centralized search page
    public void reDirect(){
        CentralizedSearchFragment centralizedSearchFragment = new CentralizedSearchFragment();
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction().replace(R.id.main_view,centralizedSearchFragment,centralizedSearchFragment.getTag())
                .commit();

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