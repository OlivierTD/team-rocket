package de.danoeh.antennapod.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Random;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.OnlineFeedViewActivity;
import de.danoeh.antennapod.adapter.itunes.ItunesAdapter;
import de.danoeh.antennapod.core.ClientConfig;
import de.danoeh.antennapod.core.feed.Feed;
import de.danoeh.antennapod.core.service.download.AntennapodHttpClient;
import de.danoeh.antennapod.core.storage.DBReader;
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
 * Created by Sai Shan on 2018-03-08.
 */


public class suggestedPodcastsFragment extends Fragment {

    private static final String TAG = "sugPodcastsFragment";


    private List<ItunesAdapter.Podcast> FYYDSearchResult;
    private List<ItunesAdapter.Podcast> iTunesSearchResult;

    private FyydClient client = new FyydClient(AntennapodHttpClient.getHttpClient());

    private List<ItunesAdapter.Podcast> searchResults;
    private Subscription subscription;

    private ItunesAdapter suggestedAdapter;
    private GridView gridView;
    private ProgressBar progressBar;
    private TextView noSubbedPodcast;

    //array holding categories of the user
    private ArrayList<String> userCategories = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.suggested_podcasts, container, false);
        gridView = (GridView) view.findViewById(R.id.gridViewHome);
        suggestedAdapter = new ItunesAdapter(getActivity(), new ArrayList<>());
        gridView.setAdapter(suggestedAdapter);
        noSubbedPodcast = (TextView) view.findViewById(R.id.noSubbedPodcast);
        noSubbedPodcast.setVisibility(View.GONE);

        initializeGridview(gridView);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        suggestedPodcastSearch();

        return view;
    }

    /**
     * Show information about the podcast when the list item is clicked
     * */
    private void initializeGridview(GridView gridview){
        gridview.setOnItemClickListener((parent, view1, position, id) -> {
            ItunesAdapter.Podcast podcast = searchResults.get(position);

            if (podcast.feedUrl != null) {
                if (!podcast.feedUrl.contains("itunes.apple.com")) {
                    initializeIntent(podcast.feedUrl);
                } else {
                    subscription = initializeSubscriptionSearch(gridview, podcast);
                }
            }
        });
    }

    private void initializeIntent(String feedUrl){
        Intent intent = new Intent(getActivity(), OnlineFeedViewActivity.class);
        intent.putExtra(OnlineFeedViewActivity.ARG_FEEDURL, feedUrl);
        intent.putExtra(OnlineFeedViewActivity.ARG_TITLE, "iTunes");
        startActivity(intent);
    }

    private Subscription initializeSubscriptionSearch(GridView gridview, ItunesAdapter.Podcast podcast){
        Subscription tempSubscription = null;

        tempSubscription = Observable.create((Observable.OnSubscribe<String>) subscriber -> {
            OkHttpClient client = AntennapodHttpClient.getHttpClient();
            Request.Builder httpReq = new Request.Builder()
                    .url(podcast.feedUrl)
                    .header("User-Agent", ClientConfig.USER_AGENT);

            establishConnection(subscriber, client, httpReq);
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(feedUrl -> {
                    gridview.setVisibility(View.VISIBLE);
                    initializeIntent(feedUrl);
                }, error -> {
                    Log.e(TAG, Log.getStackTraceString(error));
                    gridview.setVisibility(View.VISIBLE);
                    String prefix = getString(R.string.error_msg_prefix);
                    new MaterialDialog.Builder(getActivity())
                            .content(prefix + " " + error.getMessage())
                            .neutralText(android.R.string.ok)
                            .show();
                });
        return tempSubscription;
    }

    private void establishConnection(rx.Subscriber subscriber, OkHttpClient client, Request.Builder httpReq){
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
    }

    //method that perform the search for all categories based on the users podcasts
    public void suggestedPodcastSearch(){
        //fetching the list of subscribed podcast by the user
        List<Feed> feed  = DBReader.getFeedList();//works

        if (!feed.isEmpty()) {
            int min = 0;
            int max = feed.size() - 1;

            Random rand = new Random();
            int num = (rand.nextInt((max - min) + 1) + 0);

            //Suggest podcasts related to randomly selected subscribed podcast
            categorySearch(feed.get(num).getTitle());
        }else{
            noSubbedPodcast.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Replace adapter data with provided search results from SearchTask.
     *
     * @param result List of Podcast objects containing search results
     */
    private void updateData(List<ItunesAdapter.Podcast> result) {
        this.searchResults = result;
        if (result != null) {
            for (ItunesAdapter.Podcast p : result) {
                suggestedAdapter.add(p);
            }
            gridView.setVisibility(View.VISIBLE);
            suggestedAdapter.notifyDataSetInvalidated();
        } else
            gridView.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    private Subscription getSubscriptionItunes(String query){
        Subscription tempSubscription;

        tempSubscription = rx.Observable.create((Observable.OnSubscribe<List<ItunesAdapter.Podcast>>) subscriber -> {
            String API_URL = "https://itunes.apple.com/search?term="+query+"&media=podcast&attibute=genreIndex";

            //Spaces in the query need to be replaced with '+' character.
            String formattedUrl = String.format(API_URL).replace(' ', '+');

            OkHttpClient client = AntennapodHttpClient.getHttpClient();
            Request.Builder httpReq = new Request.Builder()
                    .url(formattedUrl)
                    .header("User-Agent", ClientConfig.USER_AGENT);
            iTunesSearchResult = new ArrayList<>();

            //search iTunes, add to list
            initializeList(client, httpReq, subscriber);
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(podcasts -> {
                    progressBar.setVisibility(View.GONE);
                    updateData(podcasts);
                }, error -> {
                    Log.e(TAG, Log.getStackTraceString(error));
                    progressBar.setVisibility(View.GONE);
                });
        return tempSubscription;
    }

    private Subscription getSubscriptionFYYD(String query){
        Subscription tempSubscription;

        tempSubscription = client.searchPodcasts(query)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    progressBar.setVisibility(View.GONE);
                    FYYDSearchResult = new ArrayList<>();
                    processSearchResult(result);
                    gridView.setVisibility(!searchResults.isEmpty() ? View.VISIBLE : View.GONE);
                }, error -> {
                    Log.e(TAG, Log.getStackTraceString(error));
                    progressBar.setVisibility(View.GONE);
                });
        return tempSubscription;
    }

    private void search(String query) {
        if (subscription != null) {
            subscription.unsubscribe();
        }

        searchResults = new ArrayList<>();
        subscription = getSubscriptionItunes(query);

        //search FYYD, add to list
        subscription = getSubscriptionFYYD(query);
    }

    private void initializeList(OkHttpClient client, Request.Builder httpReq, rx.Subscriber subscriber){
        try {
            Response response = client.newCall(httpReq.build()).execute();

            if (response.isSuccessful()) {
                String resultString = response.body().string();
                JSONObject result = new JSONObject(resultString);
                JSONArray j = result.getJSONArray("results");

                //Add iTunes result to list
                for (int i = 0; i < 3; i++) {
                    JSONObject podcastJson = j.getJSONObject(i);
                    ItunesAdapter.Podcast podcastiTunes = ItunesAdapter.Podcast.fromSearch(podcastJson);
                    iTunesSearchResult.add(podcastiTunes);
                }
            } else {
                String prefix = getString(R.string.error_msg_prefix);
                subscriber.onError(new IOException(prefix + response));
            }
        } catch (IOException | JSONException e) {
            subscriber.onError(e);
        }
        subscriber.onNext(iTunesSearchResult);
        subscriber.onCompleted();
    }

    //Add FYYD search to result list and remove the ones that are duplicated
    private void processSearchResult(FyydResponse response) {
        ItunesAdapter tempAdapter = new ItunesAdapter(getActivity(), new ArrayList<>());

        boolean duplicate = false;

        for (int i = 0; i < suggestedAdapter.getCount(); i++)
            tempAdapter.add(suggestedAdapter.getItem(i));

        suggestedAdapter.clear();

        if (!response.getData().isEmpty()) {
            for (SearchHit searchHit : response.getData().values()) {
                ItunesAdapter.Podcast podcastFYYD = ItunesAdapter.Podcast.fromSearch(searchHit);

                //Add podcast if not already in result list from iTunes
                for (int i = 0; i < tempAdapter.getCount(); i++) {
                    if (tempAdapter.getItem(i).title.toString().equals(podcastFYYD.title.toString()) || tempAdapter.getItem(i).feedUrl.toString().equals(podcastFYYD.feedUrl.toString()))
                        duplicate = true;
                }
                if (!duplicate)
                    searchResults.add(podcastFYYD);
                duplicate = false;
            }
        } else {
            searchResults = emptyList();
        }

        //Add search result podcast to view list
        for (ItunesAdapter.Podcast podcastFYYD : searchResults) {
            FYYDSearchResult.add(podcastFYYD);
            suggestedAdapter.add(podcastFYYD);
        }
        suggestedAdapter.notifyDataSetInvalidated();
    }

    //search category
    private void categorySearch(String categoryTitle) {
        if (subscription != null)
            subscription.unsubscribe();

        suggestedAdapter.clear();
        subscription = rx.Observable.create((Observable.OnSubscribe<List<ItunesAdapter.Podcast>>) subscriber -> {
            String API_URL = "https://itunes.apple.com/search?media=podcast&term=" + categoryTitle;

            //Spaces in the query need to be replaced with '+' character.
            String formattedUrl = String.format(API_URL).replace(' ', '+');

            OkHttpClient client = AntennapodHttpClient.getHttpClient();
            Request.Builder httpReq = new Request.Builder()
                    .url(formattedUrl)
                    .header("User-Agent", ClientConfig.USER_AGENT);
            try {
                Response response = client.newCall(httpReq.build()).execute();

                if (response.isSuccessful()) {
                    String resultString = response.body().string();
                    JSONObject result = new JSONObject(resultString);
                    JSONArray resultArray = result.getJSONArray("results");

                    //Perform search on random subbed podcast category
                    getPodcastCategory(categoryTitle, resultArray);
                }
            } catch (IOException | JSONException e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        })
            .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(podcasts -> {}, error -> {Log.e(TAG, Log.getStackTraceString(error));
            });
    }

    /**
     * Return podcast category given JSONArray result list
     */
    private void getPodcastCategory(String categoryTitle, JSONArray podcastJSONArray){
        String category = "";

        try {
            for (int i = 0; i < podcastJSONArray.length(); i++) {
                JSONObject podcastJson = podcastJSONArray.getJSONObject(i);
                String JSONTitle = podcastJson.optString("collectionName", "");
                if (JSONTitle.equalsIgnoreCase(categoryTitle)) {
                    category = podcastJson.optString("primaryGenreName", "");

                    //Perform search on random subbed podcast category
                    search(category);
                    break;
                }
            }
        }catch(JSONException e){
            System.out.println(System.err);
        }
    }
}