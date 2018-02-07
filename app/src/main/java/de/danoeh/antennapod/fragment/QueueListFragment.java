package de.danoeh.antennapod.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;


public class QueueListFragment extends Fragment {

    public static final String TAG = "QueueListFragment";

    // Called to do initial creation of fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // Retains fragment instance across Activity re-creation
        setHasOptionsMenu(true); // Fragment would like to participate in populating the options menu
    }

    // Called to have fragment instantiate its user interface view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.fragment_queue_list, container, false);

        return root;
    }

    // Called when fragment is visible to the user
    @Override
    public void onStart(){
        super.onStart();
    }

    // Called when fragment is visible to the user and actively running
    @Override
    public void onResume(){
        super.onResume();
    }

    // Called when the fragment is no longer resumed
    @Override
    public void onPause(){
        super.onPause();
    }

    /*
    * Called when the view previously created by onCreateView has been detached from the fragment
    * Allows the fragment to clean up resources associated with its View
    */
    @Override
    public void onDestroyView(){
        super.onDestroyView();
    }
}