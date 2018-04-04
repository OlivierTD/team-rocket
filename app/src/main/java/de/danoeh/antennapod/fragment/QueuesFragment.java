package de.danoeh.antennapod.fragment;

import android.support.v4.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.adapter.EpisodesAdapter;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.feed.Queue;
import de.danoeh.antennapod.core.storage.DBReader;

/**
 * Created by olitr on 2018-03-28.
 */


public class QueuesFragment extends Fragment {

    public static final String TAG = "QueuesFragment";

    // List view for the list of episodes
    ListView lvEpisodes;

    // Adapter for the list of episodes
    private EpisodesAdapter episodesAdapter;

    // Each fragment has a queue object to display
    private Queue queue;

    // Called to do initial creation of fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Called to have fragment instantiate its user interface view
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(queue.getName());
        View root = inflater.inflate(R.layout.fragment_queue, container, false);

        List<FeedItem> feedItems;
        feedItems = generateEpisodeList(queue.getEpisodesIDList());

        episodesAdapter = new EpisodesAdapter(getActivity(), feedItems);

        lvEpisodes = (ListView) root.findViewById(R.id.list_view_episodes);
        lvEpisodes.setAdapter(episodesAdapter);

        return root;
    }

    // Called when fragment is visible to the user
    @Override
    public void onStart() {
        super.onStart();
    }

    // Called when fragment is visible to the user and actively running
    @Override
    public void onResume() {
        super.onResume();
    }

    // Called when the fragment is no longer resumed
    @Override
    public void onPause() {
        super.onPause();
    }

    /*
    * Called when the view previously created by onCreateView has been detached from the fragment
    * Allows the fragment to clean up resources associated with its View
    */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    public Queue getQueue() { return this.queue; }

    /**
     * Generates a list of episodes with a list of ids
     * @param idList List of ids of each episodes in the queue
     * @return a list of FeedItem (episodes)
     */
    public List<FeedItem> generateEpisodeList(List<Long> idList) {
        List<FeedItem> feedItems = new ArrayList<FeedItem>();
        for (long id: idList) {
            feedItems.add(DBReader.getFeedItem(id));
        }
        return feedItems;
    }
}
