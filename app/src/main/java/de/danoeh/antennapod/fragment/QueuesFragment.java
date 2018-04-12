package de.danoeh.antennapod.fragment;

import android.content.Context;
import android.content.SharedPreferences;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.adapter.EpisodesAdapter;
import de.danoeh.antennapod.core.event.DownloadEvent;
import de.danoeh.antennapod.core.event.DownloaderUpdate;
import de.danoeh.antennapod.core.feed.EventDistributor;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.feed.FeedMedia;
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

    // Each fragment has a queue object to display
    public Queue queue;

    // List of feed items
    private List<FeedItem> feedItems;
    //Object where we will store the list of queues, important for context menu
    private static ArrayList<Queue> queueList = new ArrayList<>();

    private List<Downloader> downloaderList;

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
        this.loadItems();
    }

    // Called when fragment is visible to the user and actively running
    @Override
    public void onResume() {
        super.onResume();
        rvEpisodes.setAdapter(episodesAdapter);
        this.fetchItems();
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

    private void resetViewState() {
        episodesAdapter = null;
    }

    private void onFragmentLoaded() {
        if (episodesAdapter == null) {
            MainActivity activity = (MainActivity) getActivity();
            episodesAdapter = new EpisodesAdapter(activity, this.feedItems, this.itemAccess);
            episodesAdapter.setHasStableIds(true);
            rvEpisodes.setAdapter(episodesAdapter);
        }
        if(feedItems == null || feedItems.size() == 0) {
            rvEpisodes.setVisibility(View.GONE);
        } else {
            rvEpisodes.setVisibility(View.VISIBLE);
        }

    }


    /**
     * Fetch items for the adapter from the Database asynchronously.
     */
    private void fetchItems() {
        if(subscription != null) {
            subscription.unsubscribe();
        }
        // Uses RxAndroid to fetch data asynchronously
        subscription = Observable.fromCallable(() -> {
            List<FeedItem> items = new ArrayList<>();
            for (long id: queue.getEpisodesIDList()) {
                items.add(DBReader.getFeedItem(id));
            }
            return items;
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result != null) {
                        loadData(result);
                    }
                }, error -> Log.e(TAG, Log.getStackTraceString(error)));
    }

    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo adapterInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.inner_queue_menu, menu);
    }


    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();

        switch (item.getItemId()) {
            case R.id.remove_from_inner_queue:
                long id = feedItems.get(info.position).getId();
                feedItems.remove(info.position);
                removeId(id);
                episodesAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    // Attempts to load data from local storage
    private ArrayList<Queue> retrieveList() {
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("queue list", null);
        Type type = new TypeToken<ArrayList<Queue>>() {
        }.getType();
        ArrayList<Queue> list;
        list = gson.fromJson(json, type);

        if (list == null) {
            list = new ArrayList<>();
        }

        return list;

    }

    private void removeId(long Id){
        queueList = retrieveList();

        if (queueList == null) {
            queueList = new ArrayList<>();
        }
        for (Queue queues : queueList) {
            if (queues.getName().equalsIgnoreCase(this.queue.getName())) {
                queues.getEpisodesIDList().remove(Id);
                queue.getEpisodesIDList().remove(Id);
            }
        }
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(queueList);
        editor.putString("queue list", json);
        editor.apply();
    }


    /**
     * Loads items that were fetch asynchronously by fetchItems
     * @param items items to load in the adapter
     */
    private void loadData(List<FeedItem> items) {
        if (this.feedItems == null) {
            this.feedItems = items;
        } else {
            this.feedItems.clear();
            this.feedItems.addAll(items);
        }
        this.onFragmentLoaded();
        episodesAdapter.notifyDataSetChanged();
    }


    /**
     * Whenever a user presses play or pause, it will update the episode's view
     */
    private EventDistributor.EventListener contentUpdate = new EventDistributor.EventListener() {
        @Override
        public void update(EventDistributor eventDistributor, Integer arg) {
            if ((arg & EVENTS) != 0) {
                Log.d(TAG, "arg: " + arg);
                fetchItems();
                if (isUpdatingFeeds != updateRefreshMenuItemChecker.isRefreshing()) {
                    getActivity().supportInvalidateOptionsMenu();
                }
            }
        }
    };

    /**
     * EventBus: whenever there is a DownloadEvent posted in the code.
     * @param event
     */
    public void onEventMainThread(DownloadEvent event) {
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

    private final MenuItemUtils.UpdateRefreshMenuItemChecker updateRefreshMenuItemChecker =
            () -> DownloadService.isRunning && DownloadRequester.getInstance().isDownloadingFeeds();

    /**
     * Interface is used to update an episode whenever it is downloaded.
     * The following methods will retrieve needed information that are used
     * in the EpisodesAdapter
     */
    private EpisodesAdapter.ItemAccess itemAccess = new EpisodesAdapter.ItemAccess() {
        @Override
        public long getItemDownloadedBytes(FeedItem item) {
            if (downloaderList != null) {
                for (Downloader downloader : downloaderList) {
                    if (downloader.getDownloadRequest().getFeedfileType() == FeedMedia.FEEDFILETYPE_FEEDMEDIA
                            && downloader.getDownloadRequest().getFeedfileId() == item.getMedia().getId()) {
                        return downloader.getDownloadRequest().getSoFar();
                    }
                }
            }
            return 0;
        }

        @Override
        public long getItemDownloadSize(FeedItem item) {
            if (downloaderList != null) {
                for (Downloader downloader : downloaderList) {
                    if (downloader.getDownloadRequest().getFeedfileType() == FeedMedia.FEEDFILETYPE_FEEDMEDIA
                            && downloader.getDownloadRequest().getFeedfileId() == item.getMedia().getId()) {
                        return downloader.getDownloadRequest().getSize();
                    }
                }
            }
            return 0;
        }
        @Override
        public int getItemDownloadProgressPercent(FeedItem item) {
            if (downloaderList != null) {
                for (Downloader downloader : downloaderList) {
                    if (downloader.getDownloadRequest().getFeedfileType() == FeedMedia.FEEDFILETYPE_FEEDMEDIA
                            && downloader.getDownloadRequest().getFeedfileId() == item.getMedia().getId()) {
                        return downloader.getDownloadRequest().getProgressPercent();
                    }
                }
            }
            return 0;
        }

    };

}
