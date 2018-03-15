package de.danoeh.antennapod.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.core.feed.QueueObject;
import de.danoeh.antennapod.fragment.QueueListFragment;

/**
 * Created by Olivier Trépanier-Desfossés on 2018-03-14.
 * See https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView for guide
 */

public class QueuesAdapter extends ArrayAdapter<QueueObject> {

    private ArrayList<QueueObject> queueList;
    private QueueListFragment queueListFragment;

    public QueuesAdapter (Context context,  ArrayList<QueueObject> queueList, QueueListFragment queueListFragment){
        super(context, 0, queueList);
        this.queueList = queueList;
        this.queueListFragment = queueListFragment;
    }

    // Method that describes the translation between the data item and the View to display
    public View getView(int position, View convertView, ViewGroup parent){
        // Get data item for this position
        QueueObject queue = getItem(position);
        // Check if existing view to reuse, otherwise inflate view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.queue_layout, parent, false);
        }
        // Lookup view for data population
        TextView queueName = (TextView) convertView.findViewById(R.id.queue_name);
        Button deleteBtn = (Button) convertView.findViewById(R.id.queue_delete_button);
        // Populate the data into the template view using the data object
        queueName.setText(queue.name);
        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                removeQueue(position);
            }
        });
        // Return the completed view to render on screen
        return convertView;
    }

    // Used to update the ArrayList with a new ArrayList and make it display properly
    public void updateQueueList(ArrayList<QueueObject> queueList){
        this.queueList.clear();
        this.queueList.addAll(queueList);
        this.notifyDataSetChanged();
    }

    // Removes a queue in the list and updates the adapter
    public void removeQueue(int position){
        // Remove the element in the ArrayList
        this.queueListFragment.removeWithPos(position);
        // Update the adapter to display the correct list
        this.updateQueueList(this.queueListFragment.getQueuesList());
        this.notifyDataSetChanged();
    }

    public ArrayList<QueueObject> getQueueList(){
        return this.queueList;
    }

}
