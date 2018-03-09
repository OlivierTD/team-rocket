package de.danoeh.antennapod.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;


public class QueueListFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "QueueListFragment";

    //List of queue fragments

    private List<QueueFragment> queueList = new ArrayList<>();
    //button to add queues to list
    private Button addButton;


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

        addButton = (Button) root.findViewById(R.id.addQueue);
        addButton.setOnClickListener(this);

        return root;
    }


    //adds a queue to the list of queues
    @Override
    public void onClick(View v){
        createNewQueue();

        //this is for testing purposes primarily, prints a message on screen displaying the current size of the queuesList
        String testMessage = Integer.toString(this.getQueuesList().size());
        Toast.makeText(getActivity(), testMessage, Toast.LENGTH_SHORT).show();
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

    public List<QueueFragment> getQueuesList() {
        return this.queueList;
    }

    public void createNewQueue(){
        QueueFragment toAdd = new QueueFragment();
        this.getQueuesList().add(toAdd);
    }
}
