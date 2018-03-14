package de.danoeh.antennapod.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.OnlineFeedViewActivity;
import de.danoeh.antennapod.adapter.itunes.ItunesAdapter;
import de.danoeh.antennapod.core.ClientConfig;
import de.danoeh.antennapod.core.service.download.AntennapodHttpClient;
import de.danoeh.antennapod.menuhandler.MenuItemUtils;
import de.mfietz.fyydlin.FyydClient;
import de.mfietz.fyydlin.FyydResponse;
import de.mfietz.fyydlin.SearchHit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static java.util.Collections.emptyList;

/**
 * Created by David on 2018-02-12.
 */

public class ToplistFragment extends Fragment {

    private static final String TAG = "ItunesSearchFragment";

    private static final String API_URL = "https://itunes.apple.com/search?media=podcast&term=%s";

    private FyydClient client = new FyydClient(AntennapodHttpClient.getHttpClient());

    private TextView txtvError;
    private Button butRetry;
    private TextView txtvEmpty;
    private ProgressBar progressBar;

    /**
     * Adapter responsible with the search results
     */
    private ItunesAdapter adapter;
    private GridView gridView;

    /**
     * List of podcasts retreived from the search
     */
    private List<ItunesAdapter.Podcast> searchResults;
    private List<ItunesAdapter.Podcast> topList;
    private Subscription subscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.toplist_itunes, container, false);
        gridView = (GridView) view.findViewById(R.id.gridViewHome);
        adapter = new ItunesAdapter(getActivity(), new ArrayList<>());
        gridView.setAdapter(adapter);

        //Show information about the podcast when the list item is clicked
        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            ItunesAdapter.Podcast podcast = searchResults.get(position);
            if (podcast.feedUrl == null) {
                return;
            }
            if (!podcast.feedUrl.contains("itunes.apple.com")) {
                Intent intent = new Intent(getActivity(), OnlineFeedViewActivity.class);
                intent.putExtra(OnlineFeedViewActivity.ARG_FEEDURL, podcast.feedUrl);
                intent.putExtra(OnlineFeedViewActivity.ARG_TITLE, "iTunes");
                startActivity(intent);
            } else {
                gridView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                subscription = Observable.create((Observable.OnSubscribe<String>) subscriber -> {
                    OkHttpClient client = AntennapodHttpClient.getHttpClient();
                    Request.Builder httpReq = new Request.Builder()
                            .url(podcast.feedUrl)
                            .header("User-Agent", ClientConfig.USER_AGENT);
                    try {
                        Response response = client.newCall(httpReq.build()).execute();
                        if (response.isSuccessful()) {
                            String resultString = response.body().string();
                            JSONObject result = new JSONObject(resultString);
                            JSONObject results = result.getJSONArray("results").getJSONObject(0);
                            String feedUrl = results.getString("feedUrl");
                            subscriber.onNext(feedUrl);
                        } else {
                            String prefix = getString(R.string.error_msg_prefix);
                            subscriber.onError(new IOException(prefix + response));
                        }
                    } catch (IOException | JSONException e) {
                        subscriber.onError(e);
                    }
                    subscriber.onCompleted();
                })
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(feedUrl -> {
                            gridView.setVisibility(View.VISIBLE);
                            Intent intent = new Intent(getActivity(), OnlineFeedViewActivity.class);
                            intent.putExtra(OnlineFeedViewActivity.ARG_FEEDURL, feedUrl);
                            intent.putExtra(OnlineFeedViewActivity.ARG_TITLE, "iTunes");
                            startActivity(intent);
                        }, error -> {
                            Log.e(TAG, Log.getStackTraceString(error));
                            gridView.setVisibility(View.VISIBLE);
                            String prefix = getString(R.string.error_msg_prefix);
                            new MaterialDialog.Builder(getActivity())
                                    .content(prefix + " " + error.getMessage())
                                    .neutralText(android.R.string.ok)
                                    .show();
                        });
            }
        });
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        txtvError = (TextView) view.findViewById(R.id.txtvError);
        butRetry = (Button) view.findViewById(R.id.butRetry);
        txtvEmpty = (TextView) view.findViewById(android.R.id.empty);

        loadToplist();

        return view;
    }

    private void loadToplist() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
        gridView.setVisibility(View.GONE);
        subscription = Observable.create((Observable.OnSubscribe<List<ItunesAdapter.Podcast>>) subscriber -> {
            String lang = Locale.getDefault().getLanguage();
            String url = "https://itunes.apple.com/" + lang + "/rss/toppodcasts/limit=25/explicit=true/json";
            OkHttpClient client = AntennapodHttpClient.getHttpClient();
            Request.Builder httpReq = new Request.Builder()
                    .url(url)
                    .header("User-Agent", ClientConfig.USER_AGENT);
            List<ItunesAdapter.Podcast> results = new ArrayList<>();
            try {
                Response response = client.newCall(httpReq.build()).execute();
                if (!response.isSuccessful()) {
                    // toplist for language does not exist, fall back to united states
                    url = "https://itunes.apple.com/us/rss/toppodcasts/limit=25/explicit=true/json";
                    httpReq = new Request.Builder()
                            .url(url)
                            .header("User-Agent", ClientConfig.USER_AGENT);
                    response = client.newCall(httpReq.build()).execute();
                }
                if (response.isSuccessful()) {
                    String resultString = response.body().string();
                    JSONObject result = new JSONObject(resultString);
                    JSONObject feed = result.getJSONObject("feed");
                    JSONArray entries = feed.getJSONArray("entry");

                    for (int i = 0; i < entries.length(); i++) {
                        JSONObject json = entries.getJSONObject(i);
                        ItunesAdapter.Podcast podcast = ItunesAdapter.Podcast.fromToplist(json);
                        results.add(podcast);
                    }
                } else {
                    String prefix = getString(R.string.error_msg_prefix);
                    subscriber.onError(new IOException(prefix + response));
                }
            } catch (IOException | JSONException e) {
                subscriber.onError(e);
            }
            subscriber.onNext(results);
            subscriber.onCompleted();
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(podcasts -> {
                    topList = podcasts;
                    updateData(topList);
                }, error -> {
                    Log.e(TAG, Log.getStackTraceString(error));
                });
    }

    void updateData(List<ItunesAdapter.Podcast> result) {
        this.searchResults = result;
        if (result != null && result.size() > 0) {
            gridView.setVisibility(View.VISIBLE);
            for (ItunesAdapter.Podcast p : result) {
                adapter.add(p);
            }
            adapter.notifyDataSetInvalidated();
        } else {
            gridView.setVisibility(View.GONE);
        }
    }

    //Search bar event handler
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.itunes_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView sv = (SearchView) MenuItemCompat.getActionView(searchItem);
        MenuItemUtils.adjustTextColor(getActivity(), sv);
        sv.setQueryHint(getString(R.string.search_itunes_label));
        sv.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                sv.clearFocus();
                search(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (searchResults != null) {
                    searchResults = null;
                    updateData(topList);
                }
                return true;
            }
        });
    }

    //Search iTunes and add to result list
    private void search(String query) {
        adapter.clear();

        if (subscription != null) {
            subscription.unsubscribe();
        }

        showOnlyProgressBar();

        //FYYD search results
        subscription =  client.searchPodcasts(query)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    progressBar.setVisibility(View.GONE);
                    processSearchResult(result);
                }, error -> {
                    Log.e(TAG, Log.getStackTraceString(error));
                    progressBar.setVisibility(View.GONE);
                    txtvError.setText(error.toString());
                    txtvError.setVisibility(View.VISIBLE);
                    butRetry.setOnClickListener(v -> search(query));
                    butRetry.setVisibility(View.VISIBLE);
                });

        gridView.setVisibility(View.GONE);
        txtvError.setVisibility(View.GONE);
        butRetry.setVisibility(View.GONE);
        txtvEmpty.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        subscription = rx.Observable.create((Observable.OnSubscribe<List<ItunesAdapter.Podcast>>) subscriber -> {
            String encodedQuery = null;
            try {
                encodedQuery = URLEncoder.encode(query, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // this won't ever be thrown
            }
            if (encodedQuery == null) {
                encodedQuery = query; // failsafe
            }

            //Spaces in the query need to be replaced with '+' character.
            String formattedUrl = String.format(API_URL, query).replace(' ', '+');

            OkHttpClient client = AntennapodHttpClient.getHttpClient();
            Request.Builder httpReq = new Request.Builder()
                    .url(formattedUrl)
                    .header("User-Agent", ClientConfig.USER_AGENT);
            List<ItunesAdapter.Podcast> podcastsiTunes = new ArrayList<>();
            try {
                Response response = client.newCall(httpReq.build()).execute();

                if(response.isSuccessful()) {
                    String resultString = response.body().string();
                    JSONObject result = new JSONObject(resultString);
                    JSONArray j = result.getJSONArray("results");

                    //Add iTunes result to list
                    for (int i = 0; i < j.length(); i++) {
                        JSONObject podcastJson = j.getJSONObject(i);
                        ItunesAdapter.Podcast podcastiTunes = ItunesAdapter.Podcast.fromSearch(podcastJson);
                        podcastsiTunes.add(podcastiTunes);
                    }
                }
                else {
                    String prefix = getString(R.string.error_msg_prefix);
                    subscriber.onError(new IOException(prefix + response));
                }
            } catch (IOException | JSONException e) {
                subscriber.onError(e);
            }
            subscriber.onNext(podcastsiTunes);
            subscriber.onCompleted();
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(podcasts -> {
                    progressBar.setVisibility(View.GONE);
                    updateData(podcasts);
                }, error -> {
                    Log.e(TAG, Log.getStackTraceString(error));
                    progressBar.setVisibility(View.GONE);
                    txtvError.setText(error.toString());
                    txtvError.setVisibility(View.VISIBLE);
                    butRetry.setOnClickListener(v -> search(query));
                    butRetry.setVisibility(View.VISIBLE);
                });
    }
/*

private void search(String query) {
    if (subscription != null) {
        subscription.unsubscribe();
    }
    showOnlyProgressBar();
    subscription =  client.searchPodcasts(query)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(result -> {
                progressBar.setVisibility(View.GONE);
                processSearchResult(result);
            }, error -> {
                Log.e(TAG, Log.getStackTraceString(error));
                progressBar.setVisibility(View.GONE);
                txtvError.setText(error.toString());
                txtvError.setVisibility(View.VISIBLE);
                butRetry.setOnClickListener(v -> search(query));
                butRetry.setVisibility(View.VISIBLE);
            });
}
*/
    private void showOnlyProgressBar() {
        gridView.setVisibility(View.GONE);
        txtvError.setVisibility(View.GONE);
        butRetry.setVisibility(View.GONE);
        txtvEmpty.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    //Add FYYD search to result list
    void processSearchResult(FyydResponse response) {
        if (!response.getData().isEmpty()) {
            searchResults = new ArrayList<>();
            for (SearchHit searchHit : response.getData().values()) {
                ItunesAdapter.Podcast podcastFYYD = ItunesAdapter.Podcast.fromSearch(searchHit);
                searchResults.add(podcastFYYD);
            }
        } else {
            searchResults = emptyList();
        }
        for(ItunesAdapter.Podcast podcastFYYD : searchResults) {
            adapter.add(podcastFYYD);
        }
//            adapter.notifyDataSetInvalidated();
//        gridView.setVisibility(!searchResults.isEmpty() ? View.VISIBLE : View.GONE);
 //       txtvEmpty.setVisibility(searchResults.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
