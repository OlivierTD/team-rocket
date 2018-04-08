package de.danoeh.antennapod.fragment;

import android.support.v4.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.adapter.EpisodesAdapter;
import de.danoeh.antennapod.core.feed.EventDistributor;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.feed.Queue;
import de.danoeh.antennapod.core.storage.DBReader;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by olitr on 2018-03-28.
 */
public class QueuesFragment extends Fragment {

    public static final String TAG = "QueuesFragment";

    private static final int EVENTS = EventDistributor.DOWNLOAD_HANDLED |
            EventDistributor.UNREAD_ITEMS_UPDATE | // sent when playback position is reset
            EventDistributor.PLAYER_STATUS_UPDATE;
    private Subscription subscription;

    // List view for the list of episodes
    ListView lvEpisodes;

    // Adapter for the list of episodes
    private EpisodesAdapter episodesAdapter;

    // Each fragment has a queue object to display
    public Queue queue;

    // List of feed items
    private List<FeedItem> feedItems;

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
        lvEpisodes = (ListView) root.findViewById(R.id.list_view_episodes);
        return root;
    }

    // Called when fragment is visible to the user
    @Override
    public void onStart() {
        super.onStart();
        this.loadItems();
    }

    // Called when fragment is visible to the user and actively running
    @Override
    public void onResume() {
        super.onResume();
        EventDistributor.getInstance().register(contentUpdate);
    }

    // Called when the fragment is no longer resumed
    @Override
    public void onPause() {
        super.onPause();
        EventDistributor.getInstance().unregister(contentUpdate);
        if (subscription != null) {
            subscription.unsubscribe();
        }
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

    private void onFragmentLoaded() {
        if (episodesAdapter == null) {
            MainActivity activity = (MainActivity) getActivity();
            episodesAdapter = new EpisodesAdapter(activity, this.feedItems);
            lvEpisodes.setAdapter(episodesAdapter);
        }
        if(feedItems == null || feedItems.size() == 0) {
            lvEpisodes.setVisibility(View.GONE);
        } else {
            lvEpisodes.setVisibility(View.VISIBLE);
        }

    }

    private EventDistributor.EventListener contentUpdate = new EventDistributor.EventListener() {
        @Override
        public void update(EventDistributor eventDistributor, Integer arg) {
            if ((arg & EVENTS) != 0) {
                Log.d(TAG, "arg: " + arg);
                loadItems();
            }
        }
    };

    private void loadItems() {
        Log.d(TAG, "loadItems()");
        if(subscription != null) {
            subscription.unsubscribe();
        }

        subscription = Observable.fromCallable(() -> {
            List<FeedItem> items = new ArrayList<>();
            for (long id: queue.getEpisodesIDList()) {
                Log.d("generateEpisodeList:", "This is in generateEpisodeList");
                items.add(DBReader.getFeedItem(id));
            }
            return items;
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result != null) {
                        this.feedItems = result;
                        this.onFragmentLoaded();
                        if(episodesAdapter != null) {
                            episodesAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

}
