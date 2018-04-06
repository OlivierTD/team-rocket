package de.danoeh.antennapod.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.adapter.QueuesAdapter;
import de.danoeh.antennapod.core.feed.Queue;

import java.lang.reflect.Type;
import java.util.ArrayList;

import de.danoeh.antennapod.R;

public class QueueListFragment extends Fragment {

    public static final String TAG = "QueueListFragment";

    //List of queue fragments
    private ArrayList<Queue> queueList = new ArrayList<>();

    // List view for the list of queues
    ListView lvQueue;
    // Adapter for the ListView
    private QueuesAdapter queuesAdapter;

    // Called to do initial creation of fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true); // Retains fragment instance across Activity re-creation
        setHasOptionsMenu(true); // Fragment would like to participate in populating the options menu

        //attempts to load from local storage
        this.loadList();

    }

    // Called to have fragment instantiate its user interface view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.queues);

        View root = inflater.inflate(R.layout.fragment_queue_list, container, false);

        // Adapter to convert the ArrayList to views
        queuesAdapter = new QueuesAdapter(getActivity(), queueList, this);
        // Attach the adapter to the ListView
        lvQueue = (ListView) root.findViewById(R.id.queueList);
        lvQueue.setAdapter(queuesAdapter);

        return root;
    }

    //Shows the menu on top
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.queue_toolbar, menu);
    }

    //Allows to click the button on the menu on top
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addQueue:
                Log.d("Queue", "Adding queue");
                addFromMenu();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //Method called in the onOptionsItemSelected in order add queues from the top menu
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void addFromMenu(){
        final EditText enterName = new EditText(getActivity());
        enterName.setInputType(InputType.TYPE_CLASS_TEXT);
        enterName.setHint(R.string.enter_queue_name);

        //Create the dialog to be shown to the user
        AlertDialog enterNameDialog = new AlertDialog.Builder(getActivity())
                .setView(enterName)
                .setTitle(R.string.enter_queue_name)
                .setPositiveButton(R.string.create, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        //Pops out the keyboard
        enterNameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        //Display the dialog
        enterNameDialog.show();

        enterNameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enterName.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), R.string.enter_valid_name, Toast.LENGTH_SHORT).show();
                }
                else if (nameExists(enterName.getText().toString())) {
                    Toast.makeText(getActivity(), R.string.name_already_exists, Toast.LENGTH_SHORT).show();
                }
                else {
                    createNewQueue(enterName.getText().toString());
                    enterNameDialog.dismiss();
                }
            }
        });


    }
    // Called when fragment is visible to the user
    @Override
    public void onStart() {
        super.onStart();
        //attempts to load from local storage
        this.loadList();
    }

    // Called when fragment is visible to the user and actively running
    @Override
    public void onResume() {
        super.onResume();
        //attempts to load from local storage
        this.loadList();
    }

    // Called when the fragment is no longer resumed
    @Override
    public void onPause() {
        //attempts to store in local storage
        this.storeList();
        super.onPause();
    }

    /*
    * Called when the view previously created by onCreateView has been detached from the fragment
    * Allows the fragment to clean up resources associated with its View
    */
    @Override
    public void onDestroyView() {
        //attempts to store in local storage
        this.storeList();
        super.onDestroyView();
    }

    public ArrayList<Queue> getQueuesList() {
        return this.queueList;
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void createNewQueue(String name) {
        Queue toAdd = new Queue(name);
        this.queueList.add(toAdd);
        // Update adapter
        queuesAdapter.updateQueueList(this.queueList);

    }

    // Removes an element in the list according to its position and stores new queue in storage
    public void removeWithPos(int position) {
        this.queueList.remove(position);
    }

    // Attempts to load data from local storage
    private void loadList() {
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("queue list", null);
        Type type = new TypeToken<ArrayList<Queue>>() {}.getType();
        queueList = gson.fromJson(json, type);

        if (queueList == null) {
            queueList = new ArrayList<>();
        }
    }

    // Attempts to persist data to local storage
    private void storeList() {
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(queueList);
        editor.putString("queue list", json);
        editor.apply();

    }

    public void setQueuesAdapter(QueuesAdapter queuesAdapter){
        this.queuesAdapter = queuesAdapter;
    }

    //Will verify if there is a QueueObject in the list that already has the name
    public boolean nameExists(String testName) {
        boolean flag = false;
        for (Queue queueObject : queueList) {
            if (queueObject.getName().equals(testName)) {
                flag = true;
            }
        }
        return flag;
    }


}