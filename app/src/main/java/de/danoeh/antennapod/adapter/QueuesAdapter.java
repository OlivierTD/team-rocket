package de.danoeh.antennapod.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.core.feed.Queue;
import de.danoeh.antennapod.fragment.QueueFragment;
import de.danoeh.antennapod.fragment.QueueListFragment;
import de.danoeh.antennapod.fragment.QueuesFragment;

import static de.danoeh.antennapod.service.QueueStoreService.loadList;
import static de.danoeh.antennapod.service.QueueStoreService.storeList;

/**
 * Created by Olivier Trépanier-Desfossés on 2018-03-14.
 * See https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView for guide
 */

public class QueuesAdapter extends ArrayAdapter<Queue> {

    private ArrayList<Queue> queueList;
    private QueueListFragment queueListFragment;
    private Queue changeName;

    public QueuesAdapter(Context context, ArrayList<Queue> queueList, QueueListFragment queueListFragment) {
        super(context, 0, queueList);
        this.queueList = queueList;
        this.queueListFragment = queueListFragment;
    }

    // Method that describes the translation between the data item and the View to display
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get data item for this position
        Queue queue = getItem(position);
        // Check if existing view to reuse, otherwise inflate view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.queue_layout, parent, false);
        }
        // Lookup view for data population
        TextView queueName = (TextView) convertView.findViewById(R.id.queue_name);
        Button deleteBtn = (Button) convertView.findViewById(R.id.queue_delete_button);
        // Populate the data into the template view using the data object
        queueName.setText(queue.getName());
        //set clickable, transfer to queueFragment on click
        queueName.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onClick(View view) {
                //Create queue fragment
                QueuesFragment queuesFragment = new QueuesFragment();
                queuesFragment.setQueue(queue);
                //gets ID of the queueListFragment in order to properly repl
                // ace it
                int id = queueListFragment.getId();
                FragmentTransaction fragmentTransaction = queueListFragment.getFragmentManager().beginTransaction();
                fragmentTransaction.replace(id, queuesFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        });
        queueName.setOnLongClickListener(new View.OnLongClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public boolean onLongClick(View view) {
                changeName = queue;
                renameQueue(position);
                return true;
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog confirmDialog = new AlertDialog.Builder(view.getContext())
                        .setTitle(R.string.confirmation_title)
                        .setMessage(R.string.delete_queue_confirmation_message)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeQueue(position);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create();
                confirmDialog.show();
            }
        });
        // Return the completed view to render on screen
        return convertView;
    }

    // Used to update the ArrayList with a new ArrayList and make it display properly
    public void updateQueueList(ArrayList<Queue> queueList) {
        this.queueList.clear();
        this.queueList.addAll(queueList);
        this.notifyDataSetChanged();
    }

    // Removes a queue in the list and updates the adapter
    public void removeQueue(int position) {
        // Remove the element in the ArrayList
        this.queueListFragment.removeWithPos(position);
        // Update the adapter to display the correct list
        this.updateQueueList(this.queueListFragment.getQueuesList());
        this.notifyDataSetChanged();
    }

    public void rename(int position){
        //update the element in the ArrayList
        this.queueListFragment.renameWithPos(position, changeName);

        //update the adapter to properly display the correct list
        this.updateQueueList(this.queueListFragment.getQueuesList());
        this.notifyDataSetChanged();
    }

    public ArrayList<Queue> getQueueList() {
        return this.queueList;
    }

    //Method called from long click listener in order add queues from the top menu
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void renameQueue(int position) {
        final EditText enterName = new EditText(queueListFragment.getActivity());
        enterName.setInputType(InputType.TYPE_CLASS_TEXT);
        enterName.setHint(R.string.enter_queue_name);

        //Create the dialog to be shown to the user
        AlertDialog enterNameDialog = new AlertDialog.Builder(queueListFragment.getActivity())
                .setView(enterName)
                .setTitle(R.string.rename_queue)
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
                    Toast.makeText(queueListFragment.getActivity(), R.string.enter_valid_name, Toast.LENGTH_SHORT).show();
                } else if (queueListFragment.nameExists(enterName.getText().toString())) {
                    Toast.makeText(queueListFragment.getActivity(), R.string.name_already_exists, Toast.LENGTH_SHORT).show();
                } else {
                    changeName.setName(enterName.getText().toString());
                    enterNameDialog.dismiss();
                    rename(position);
                }
            }
        });



    }


}