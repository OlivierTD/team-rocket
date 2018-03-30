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

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

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

    private ItunesAdapter adapter;
    private GridView gridView;
    private ProgressBar progressBar;

    //array holding categories of the user
    private ArrayList<String > userCategories = new ArrayList<String>();


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


        suggestedPodcastSearch(userCategories);


        return view;

    }


    //method that perform the search for all categories based on the users podcasts
    public void suggestedPodcastSearch(ArrayList<String> userCat){

        //fetching the list of subscribed podcast by the user
        List<Feed> feed  = DBReader.getFeedList();//works

        //add category of each feed to the ArrayList<String> userCategories
        for(int i = 0; i < feed.size(); i++){
            categorySearch(feed.get(i).getTitle());
        }

        //search for podcasts for each categories
        for(int i = 0; i<userCat.size(); i++){
            search(userCat.get(i));
        }
    }



    /**
     * Replace adapter data with provided search results from SearchTask.
     *
     * @param result List of Podcast objects containing search results
     */
    void updateData(List<ItunesAdapter.Podcast> result) {
        Log.d("update", "entering te update data");
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }


    public void search(String query) {
        Log.d("testing", "going in search");
        adapter.clear();
        searchResults = new ArrayList<>();

        if (subscription != null) {
            subscription.unsubscribe();
        }

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

            String API_URL = "https://itunes.apple.com/search?term="+query+"&media=podcast&attibute=genreIndex";


            //Spaces in the query need to be replaced with '+' character.
            String formattedUrl = String.format(API_URL).replace(' ', '+');

            OkHttpClient client = AntennapodHttpClient.getHttpClient();
            Request.Builder httpReq = new Request.Builder()
                    .url(formattedUrl)
                    .header("User-Agent", ClientConfig.USER_AGENT);
            iTunesSearchResult = new ArrayList<>();
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

        //FYYD search results
        subscription = client.searchPodcasts(query)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    progressBar.setVisibility(View.GONE);
                    processSearchResult(result);
                }, error -> {
                    Log.e(TAG, Log.getStackTraceString(error));
                    progressBar.setVisibility(View.GONE);
                });
    }

    //Add FYYD search to result list and remove the ones that are duplicated
    void processSearchResult(FyydResponse response) {
        ItunesAdapter tempAdapter = new ItunesAdapter(getActivity(), new ArrayList<>());
        FYYDSearchResult = new ArrayList<>();
        boolean duplicate = false;

        for (int i = 0; i < adapter.getCount(); i++)
            tempAdapter.add(adapter.getItem(i));

        adapter.clear();

        try {
            //Add search results podcast to data list
            if (!response.getData().isEmpty()) {
                for (SearchHit searchHit : response.getData().values()) {
                    ItunesAdapter.Podcast podcastFYYD = ItunesAdapter.Podcast.fromSearch(searchHit);

                    //Add podcast if not already in result list from iTunes
                    for (int i = 0; i < tempAdapter.getCount(); i++) {
                        if (tempAdapter.getItem(i).title.toString().compareTo(podcastFYYD.title.toString()) == 0 || tempAdapter.getItem(i).feedUrl.toString().compareTo(podcastFYYD.feedUrl.toString()) == 0)
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
                adapter.add(podcastFYYD);
            }
            adapter.notifyDataSetInvalidated();
            gridView.setVisibility(!searchResults.isEmpty() ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    //search category
    public void categorySearch(String title123) {
        Log.d("feed","never going here");
        if (subscription != null) {
            subscription.unsubscribe();
        }

        subscription = rx.Observable.create((Observable.OnSubscribe<List<ItunesAdapter.Podcast>>) subscriber -> {

            String API_URL = "https://itunes.apple.com/search?media=podcast&term="+title123;

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
                    JSONArray j = result.getJSONArray("results");

                    //get primaryGenreName from the given category
                    for (int i = 0; i < j.length(); i++) {
                        JSONObject podcastJson = j.getJSONObject(i);
                        String titre = podcastJson.optString("collectionName", "");
                        Log.d("feed","ok??");
                        if(titre.equalsIgnoreCase(title123)){
                            Log.d("feed","is it going here ");
                            String cate = podcastJson.optString("primaryGenreName", "");
                            userCategories.add(cate);
                            Log.d("feed","is cate a variable " +cate);
                            break;
                        }
                    }
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
                .subscribe(podcasts -> {
                }, error -> {
                    Log.e(TAG, Log.getStackTraceString(error));
                });
    }

}



