package de.danoeh.antennapod.fragment;

import android.support.v4.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.adapter.EpisodesAdapter;
import de.danoeh.antennapod.core.event.DownloadEvent;
import de.danoeh.antennapod.core.event.DownloaderUpdate;
import de.danoeh.antennapod.core.event.FeedItemEvent;
import de.danoeh.antennapod.core.event.QueueEvent;
import de.danoeh.antennapod.core.feed.EventDistributor;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.feed.Queue;
import de.danoeh.antennapod.core.service.download.DownloadService;
import de.danoeh.antennapod.core.service.download.Downloader;
import de.danoeh.antennapod.core.storage.DBReader;
import de.danoeh.antennapod.core.storage.DownloadRequester;
import de.danoeh.antennapod.core.util.FeedItemUtil;
import de.danoeh.antennapod.menuhandler.MenuItemUtils;
import de.greenrobot.event.EventBus;
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
    private RecyclerView rvEpisodes;
    private RecyclerView.LayoutManager rvLayoutManager;

    // Adapter for the list of episodes
    private EpisodesAdapter episodesAdapter;

    private List<Downloader> downloaderList;

    // Each fragment has a queue object to display
    public Queue queue;

    // List of feed items
    private List<FeedItem> feedItems;

    private boolean isUpdatingFeeds = false;

    // Called to do initial creation of fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    // Called to have fragment instantiate its user interface view
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Set title name to queue name
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(queue.getName());
        // Inflate the view
        View root = inflater.inflate(R.layout.fragment_queue, container, false);
        // Set the RecyclerView
        rvEpisodes = (RecyclerView) root.findViewById(R.id.recyclerView);
        RecyclerView.ItemAnimator animator = rvEpisodes.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        // Give the RecyclerView a linear layout
        rvLayoutManager = new LinearLayoutManager(this.getActivity());
        rvEpisodes.setLayoutManager(rvLayoutManager);
        rvEpisodes.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).build());
        rvEpisodes.setHasFixedSize(true);
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
        rvEpisodes.setAdapter(episodesAdapter);
        this.loadItems();
        // Register an EventListener observer to the EventDistributor
        EventDistributor.getInstance().register(contentUpdate);
        // Register to the bus, will handle threads
        EventBus.getDefault().registerSticky(this);
    }

    // Called when the fragment is no longer resumed
    @Override
    public void onPause() {
        super.onPause();
        EventDistributor.getInstance().unregister(contentUpdate);
        EventBus.getDefault().unregister(this);
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    private void resetViewState() {
        episodesAdapter = null;
    }

    /*
    * Called when the view previously created by onCreateView has been detached from the fragment
    * Allows the fragment to clean up resources associated with its View
    */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        resetViewState();
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    public Queue getQueue() { return this.queue; }

    private void onFragmentLoaded() {
        if (episodesAdapter == null) {
            MainActivity activity = (MainActivity) getActivity();
            episodesAdapter = new EpisodesAdapter(activity, this.feedItems);
            episodesAdapter.setHasStableIds(true);
            rvEpisodes.setAdapter(episodesAdapter);
        }
        if(feedItems == null || feedItems.size() == 0) {
            rvEpisodes.setVisibility(View.GONE);
        } else {
            rvEpisodes.setVisibility(View.VISIBLE);
        }

    }

    private EventDistributor.EventListener contentUpdate = new EventDistributor.EventListener() {
        @Override
        public void update(EventDistributor eventDistributor, Integer arg) {
            if ((arg & EVENTS) != 0) {
                Log.d(TAG, "arg: " + arg);
                loadItems();
                if (isUpdatingFeeds != updateRefreshMenuItemChecker.isRefreshing()) {
                    getActivity().supportInvalidateOptionsMenu();
                }
            }
        }
    };

    /**
     * Called in Android UI's main thread, will update corresponding FeedItem whenever
     * there is an EventBus post call
     * @param event
     */
    public void onEventMainThread(FeedItemEvent event) {
        Log.d(TAG, "onEventMainThread() called with: " + "event = [" + event + "]");
        if(queue == null || episodesAdapter == null) {
            return;
        }
        for(int i=0, size = event.items.size(); i < size; i++) {
            FeedItem item = event.items.get(i);
            int pos = FeedItemUtil.indexOfItemWithId(feedItems, item.getId());
            if(pos >= 0) {
                feedItems.remove(pos);
                feedItems.add(pos, item);
                episodesAdapter.notifyItemChanged(pos);
            }
        }
    }

    /**
     * Called in Android UI's main thread
     * @param event
     */
    public void onEventMainThread(DownloadEvent event) {
        Log.d(TAG, "onEventMainThread() called with: " + "event = [" + event + "]");
        DownloaderUpdate update = event.update;
        downloaderList = update.downloaders;
        if (isUpdatingFeeds != update.feedIds.length > 0) {
            getActivity().supportInvalidateOptionsMenu();
        }
        if (episodesAdapter != null && update.mediaIds.length > 0) {
            for (long mediaId : update.mediaIds) {
                int pos = FeedItemUtil.indexOfItemWithMediaId(feedItems, mediaId);
                if (pos >= 0) {
                    episodesAdapter.notifyItemChanged(pos);
                }
            }
        }
    }

    /**
     * Loads items for the adapter from the Database asynchronously.
     */
    private void loadItems() {
        Log.d(TAG, "loadItems()");
        if(subscription != null) {
            subscription.unsubscribe();
        }

        if (feedItems == null) {
            rvEpisodes.setVisibility(View.GONE);
        }

        // Uses RxAndroid to fetch data asynchronously
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
                }, error -> Log.e(TAG, Log.getStackTraceString(error)));
    }


    private final MenuItemUtils.UpdateRefreshMenuItemChecker updateRefreshMenuItemChecker =
            () -> DownloadService.isRunning && DownloadRequester.getInstance().isDownloadingFeeds();

}
