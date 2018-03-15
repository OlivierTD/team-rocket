package de.danoeh.antennapod.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
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
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.OnlineFeedViewActivity;
import de.danoeh.antennapod.adapter.SubscriptionsAdapter;
import de.danoeh.antennapod.adapter.itunes.ItunesAdapter;
import de.danoeh.antennapod.core.ClientConfig;
import de.danoeh.antennapod.core.service.download.AntennapodHttpClient;
import de.danoeh.antennapod.core.storage.DBReader;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Sai Shan on 2018-03-08.
 */

public class suggestedPodcastsFragment extends Fragment{

    private static final String TAG = "sugPodcastsFragment";
    private static final String API_URL = "https://itunes.apple.com/search?media=podcast&term=%s";

    private DBReader.NavDrawerData subsList;  //subscription list
    private ArrayList<String> subsTitles; //the keywords in titles


    /**
     * needed attributes
     */
    private List<ItunesAdapter.Podcast> searchResults;
    private List<ItunesAdapter.Podcast> suggestedPod;
    private Subscription subscription;
    private SubscriptionsAdapter subscriptionAdapter;

    /**
     * Adapter responsible with the search results
     */
    private ItunesAdapter adapter;
    private GridView gridView;
    private ProgressBar progressBar;
    private TextView txtvError;
    private Button butRetry;
    private TextView txtvEmpty;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(subscription != null) {
            subscription.unsubscribe();
        }
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
            if(podcast.feedUrl == null) {
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

        loadSubscriptions();
        searchPodcasts(getKeywords(subsList));
        //loadPdcasts(); */

        return view;
    }


    // load the subscription

    private void loadSubscriptions() {
        if(subscription != null) {
            subscription.unsubscribe();
        }
        subscription = Observable.fromCallable(DBReader::getNavDrawerData)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    subsList = result;
                    subscriptionAdapter.notifyDataSetChanged();
                }, error -> Log.e(TAG, Log.getStackTraceString(error)));
    }


    //method that loads the suggestedPodcasts
   /* private void loadPdcasts() {
        if(subscription != null) {
            subscription.unsubscribe();
        }

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
                if(!response.isSuccessful()) {
                    // toplist for language does not exist, fall back to united states
                    url = "https://itunes.apple.com/us/rss/toppodcasts/limit=25/explicit=true/json";
                    httpReq = new Request.Builder()
                            .url(url)
                            .header("User-Agent", ClientConfig.USER_AGENT);
                    response = client.newCall(httpReq.build()).execute();
                }
                if(response.isSuccessful()) {
                    String resultString = response.body().string();
                    JSONObject result = new JSONObject(resultString);
                    JSONObject feed = result.getJSONObject("feed");
                    JSONArray entries = feed.getJSONArray("entry");

                    for(int i=0; i < entries.length(); i++) {
                        JSONObject json = entries.getJSONObject(i);
                        ItunesAdapter.Podcast podcast = ItunesAdapter.Podcast.fromToplist(json);
                        results.add(podcast);
                    }
                }
                else {
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
                    suggestedPod = podcasts;
                    updateData(suggestedPod);
                }, error -> {
                    Log.e(TAG, Log.getStackTraceString(error));
                });



    }
*/
    /**
     * Replace adapter data with provided search results from SearchTask.
     * @param result List of Podcast objects containing search results
     */
    void updateData(List<ItunesAdapter.Podcast> result) {
        this.searchResults = result;
        adapter.clear();
        if (result != null && result.size() > 0) {
            gridView.setVisibility(View.VISIBLE);
            txtvEmpty.setVisibility(View.GONE);
            for (ItunesAdapter.Podcast p : result) {
                adapter.add(p);
            }
            adapter.notifyDataSetInvalidated();
        } else {
            gridView.setVisibility(View.GONE);
            txtvEmpty.setVisibility(View.VISIBLE);
        }
    }

    /**
     *
     * @param subsList
     * Takes the list of podcasts that the user is subscribed to as a parameter
     * Output: Returns a list of keywords of all the the subscriptions
     * @return keywords
     */
    public ArrayList<String> getKeywords(DBReader.NavDrawerData subsList){
        subsTitles = new ArrayList<String>();

        //Variable x iterates through each title
        //Variable i iterates through each title's characters
        //variable j is an indicator of the last space that had a white space
        for(int x=0;x<subsList.feeds.size();x++){

            String temp;
            temp = subsList.feeds.get(x).getTitle();
            String keyword;
            int j=0;

            for(int i=0; i<temp.length();i++){
                if((i-j) > 3){
                    keyword = temp.substring(j,i);
                    subsTitles.add(keyword);
                    break;
                }
                else{
                    if((i-j)<=3){
                        j = i;
                    }
                }
            }
        }

        return subsTitles;
    }

    //method that search podcasts for a given query in the iTunes library
    //insert the podcasts in the arrayList podcasts

    private void search (String query){
        if (subscription != null) {
            subscription.unsubscribe();
        }
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
            List<ItunesAdapter.Podcast> podcasts = new ArrayList<>();
            try {
                Response response = client.newCall(httpReq.build()).execute();

                if (response.isSuccessful()) {
                    String resultString = response.body().string();
                    JSONObject result = new JSONObject(resultString);
                    JSONArray j = result.getJSONArray("results");

                    //missing: max number of podcasts per search //ok done! podcasts = 5

                    for (int i = 0; i < 5; i++) {
                        JSONObject podcastJson = j.getJSONObject(i);
                        ItunesAdapter.Podcast podcast = ItunesAdapter.Podcast.fromSearch(podcastJson);
                        podcasts.add(podcast);
                    }
                } else {
                    String prefix = getString(R.string.error_msg_prefix);
                    subscriber.onError(new IOException(prefix + response));
                }
            } catch (IOException | JSONException e) {
                subscriber.onError(e);
            }
            subscriber.onNext(podcasts);
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

    //search method that looks for the keywords in the titles of subscribed podcasts

    private void searchPodcasts (ArrayList<String> subsTitles){

        for(int i=0; i <subsTitles.size(); i++){
            String query = subsTitles.get(i);
            search(query);
        }
    }




}