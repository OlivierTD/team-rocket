package de.danoeh.antennapod.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
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
import android.widget.ArrayAdapter;
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
import java.lang.reflect.Array;
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
 * Created by Batoul on 2018-04-16.
 * A version of the centralized search fragment but without an options menu.
 * Its purpose is to be loaded on the homepage in order to make the Random Episode button work. 
 */

public class CentralSearchFragment extends Fragment {

    public static final String TAG = "CentralFragment";

    private FyydClient client = new FyydClient(AntennapodHttpClient.getHttpClient());

    private TextView txtvError;
    private Button butRetry;
    private TextView txtvEmpty;
    private ProgressBar progressBar;
    private TextView titleMessage;
    private String query;

    /**
     * Adapter responsible with the search results
     */
    private List<ItunesAdapter.Podcast> FYYDSearchResult;
    private List<ItunesAdapter.Podcast> iTunesSearchResult;

    private ItunesAdapter adapter;  //search result view
    private List<ItunesAdapter.Podcast> searchResults;  //search result data

    private List<ItunesAdapter.Podcast> topList; //holds toplist podcasts
    private Subscription subscription;

    private GridView gridView;

    public CentralSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.centralized_search, container, false);
        gridView = (GridView) view.findViewById(R.id.gridSearchResult);
        titleMessage = (TextView) view.findViewById(R.id.textSearchResult);
        adapter = new ItunesAdapter(getActivity(), new ArrayList<>());
        gridView.setAdapter(adapter);

        titleMessage.setVisibility(View.GONE);
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

        search(query);

        return view;
    }

    public  CentralizedSearchFragment newInstance(){
        Bundle bundle = new Bundle();

        CentralizedSearchFragment fragment = new CentralizedSearchFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private void showOnlyProgressBar() {
        gridView.setVisibility(View.GONE);
        txtvError.setVisibility(View.GONE);
        butRetry.setVisibility(View.GONE);
        txtvEmpty.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    //Search libraries
    public void search(String query) {
        adapter.clear();
        searchResults = new ArrayList<>();

        if (subscription != null) {
            subscription.unsubscribe();
        }

        iTunesSearchResult = searchItunes(query);
        updateData(iTunesSearchResult);

        searchFYYD(query);
    }

    //Search iTunes
    public List<ItunesAdapter.Podcast> searchItunes(String query){
        String API_URL = getString(R.string.itunes_search_api);
        List<ItunesAdapter.Podcast> resultList = new ArrayList<>();

        showOnlyProgressBar();

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

                        //Only add podcasts with active connections
                        if (podcastiTunes.feedUrl != null)
                            resultList.add(podcastiTunes);
                    }
                }
                else {
                    String prefix = getString(R.string.error_msg_prefix);
                    subscriber.onError(new IOException(prefix + response));
                }
            } catch (IOException | JSONException e) {
                subscriber.onError(e);
            }
            subscriber.onNext(resultList);
            subscriber.onCompleted();
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(podcasts -> {
                    progressBar.setVisibility(View.GONE);
                    titleMessage.setText("Search results");
                }, error -> {
                    Log.e(TAG, Log.getStackTraceString(error));
                    progressBar.setVisibility(View.GONE);
                    txtvError.setText(error.toString());
                    txtvError.setVisibility(View.VISIBLE);
                    butRetry.setOnClickListener(v -> search(query));
                    butRetry.setVisibility(View.VISIBLE);
                });
        return resultList;
    }

    public void searchFYYD(String query){
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

    //Update adapter with iTunes search results
    void updateData(List<ItunesAdapter.Podcast> result) {
        this.searchResults = result;
        if (result != null && result.size() > 0) {
            titleMessage.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.VISIBLE);
            for (ItunesAdapter.Podcast p : result) {
                adapter.add(p);
            }
            adapter.notifyDataSetInvalidated();
        } else {
            titleMessage.setVisibility(View.GONE);
            gridView.setVisibility(View.GONE);
        }
    }

    //Update adapter with FYYD search results
    void processSearchResult(FyydResponse response) {
        ItunesAdapter tempAdapter = new ItunesAdapter(getActivity(), new ArrayList<>());
        FYYDSearchResult = new ArrayList<>();
        boolean duplicate = false;

        for (int i = 0; i < adapter.getCount(); i++)
            tempAdapter.add(adapter.getItem(i));

        adapter.clear();

        try{
            //Add search results podcast to data list
            if (!response.getData().isEmpty()) {
                for (SearchHit searchHit : response.getData().values()) {

                    ItunesAdapter.Podcast podcastFYYD = ItunesAdapter.Podcast.fromSearch(searchHit);

                    //Add podcast if not already in result list from iTunes
                    for (int i = 0; i < tempAdapter.getCount(); i++){
                        if (tempAdapter.getItem(i).feedUrl != null) {
                            if (tempAdapter.getItem(i).title.toString().compareTo(podcastFYYD.title.toString()) == 0 || tempAdapter.getItem(i).feedUrl.toString().compareTo(podcastFYYD.feedUrl.toString()) == 0)
                                duplicate = true;
                        }
                    }
                    if (!duplicate && podcastFYYD.feedUrl != null)
                        searchResults.add(podcastFYYD);

                    if (tempAdapter.getCount() > 0)
                        titleMessage.setVisibility(View.VISIBLE);
                    else
                        titleMessage.setVisibility(View.GONE);

                    duplicate = false;
                }
            } else {
                searchResults = emptyList();
            }

            //Add search result podcast to view list
            for(ItunesAdapter.Podcast podcastFYYD : searchResults) {
                FYYDSearchResult.add(podcastFYYD);
                adapter.add(podcastFYYD);
            }
            adapter.notifyDataSetInvalidated();
            gridView.setVisibility(!searchResults.isEmpty() ? View.VISIBLE : View.GONE);
            txtvEmpty.setVisibility(searchResults.isEmpty() ? View.VISIBLE : View.GONE);
        }catch (Exception e){
            System.out.println("Error: " + e);
        }
    }

    public int getSearchRestultSize(){
        return adapter.getCount();
    }

    public List<ItunesAdapter.Podcast> getiTunesResultSize(){
        return iTunesSearchResult;
    }

    public List<ItunesAdapter.Podcast> getFYYDResultSize(){
        return FYYDSearchResult;
    }

    public void setSearchAdapter(ItunesAdapter searchAdapter){
        adapter = searchAdapter;
    }

    public void setFYYDClient(FyydClient client){
        this.client = client;
    }

    public void setQuery(String query){
        this.query = query;
    }


}
