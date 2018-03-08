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
    private List<String> subsTitles; //the keywords in titles
    private List<ItunesAdapter.Podcast> podcasts = new ArrayList<>(); //arrayList holding the suggested podcasts


    /**
     * needed attributes
     */
    private List<ItunesAdapter.Podcast> searchResults;
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



    //method that loads the user subscriptions
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

    //method missing: Getting keywords in the subscription titles

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

                try {
                    Response response = client.newCall(httpReq.build()).execute();

                    if (response.isSuccessful()) {
                        String resultString = response.body().string();
                        JSONObject result = new JSONObject(resultString);
                        JSONArray j = result.getJSONArray("results");

                        for (int i = 0; i < j.length(); i++) {
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

        private void searchPodcasts (List<String> subsTitles){

            for(int i=0; i <subsTitles.size(); i++){
                String query = subsTitles.get(i);
                search(query);

            }


        }


    //method missing: displaying the suggested podcasts on the HomePage

}
