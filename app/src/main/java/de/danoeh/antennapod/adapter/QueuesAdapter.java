package de.danoeh.antennapod.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.core.feed.QueueObject;

/**
 * Created by Olivier Trépanier-Desfossés on 2018-03-14.
 * See https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView for guide
 */

public class QueuesAdapter extends ArrayAdapter<QueueObject> {

    private ArrayList<QueueObject> queueList;

    public QueuesAdapter (Context context,  ArrayList<QueueObject> queueList){
        super(context, 0, queueList);
        this.queueList = queueList;
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
        TextView queueName = (TextView) convertView.findViewById(R.id.queueName);
        // Populate the data into the template view using the data object
        queueName.setText(queue.name);
        // Return the completed view to render on screen
        return convertView;
    }

    public void updateQueueList(ArrayList<QueueObject> queueList){
        this.queueList.clear();
        this.queueList.addAll(queueList);
        this.notifyDataSetChanged();
    }

}
