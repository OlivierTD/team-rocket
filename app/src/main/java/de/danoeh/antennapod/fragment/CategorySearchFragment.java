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
import java.util.ArrayList;
import java.util.List;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.OnlineFeedViewActivity;
import de.danoeh.antennapod.adapter.itunes.ItunesAdapter;
import de.danoeh.antennapod.core.ClientConfig;
import de.danoeh.antennapod.core.service.download.AntennapodHttpClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Sai Shan on 2018-03-26.
 */

public class CategorySearchFragment extends Fragment {

    public static final String TAG = "CategorySearchFragment";

    private static String API_URL = "http://itunes.apple.com/WebObjects/MZStoreServices.woa/ws/genres?id="; //need to add a id
    //genre mapping : https://affiliate.itunes.apple.com/resources/documentation/genre-mapping/

    //https://github.com/eazyliving/fyyd-api/blob/master/README.md

    private int id;

    //adapters for the view
    private ItunesAdapter adapter;
    private GridView gridView;
    private ProgressBar progressBar;
    private TextView txtvError;
    private Button butRetry;
    private TextView txtvEmpty;

    //list of podcasts retrieved by the the search
    private List<ItunesAdapter.Podcast> searchResults;
    private Subscription subscription;

    //setters and getters for the id attribute
    public void setId(int id){
        this.id = id;
    }

    //load the podcasts from the search in the adapter
    private void updateData(List<ItunesAdapter.Podcast> result) {
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

    //default constructor
    public CategorySearchFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.category_search, container, false);
        gridView = (GridView) root.findViewById(R.id.gridView);
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
                            progressBar.setVisibility(View.GONE);
                            gridView.setVisibility(View.VISIBLE);
                            Intent intent = new Intent(getActivity(), OnlineFeedViewActivity.class);
                            intent.putExtra(OnlineFeedViewActivity.ARG_FEEDURL, feedUrl);
                            intent.putExtra(OnlineFeedViewActivity.ARG_TITLE, "iTunes");
                            startActivity(intent);
                        }, error -> {
                            Log.e(TAG, Log.getStackTraceString(error));
                            progressBar.setVisibility(View.GONE);
                            gridView.setVisibility(View.VISIBLE);
                            String prefix = getString(R.string.error_msg_prefix);
                            new MaterialDialog.Builder(getActivity())
                                    .content(prefix + " " + error.getMessage())
                                    .neutralText(android.R.string.ok)
                                    .show();
                        });
            }
        });
        progressBar = (ProgressBar) root.findViewById(R.id.progressBar);
        txtvError = (TextView) root.findViewById(R.id.txtvError);
        butRetry = (Button) root.findViewById(R.id.butRetry);
        txtvEmpty = (TextView) root.findViewById(android.R.id.empty);

        search(id);

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (subscription != null) {
            subscription.unsubscribe();
        }
        adapter = null;
    }

    private void search(int id) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
        gridView.setVisibility(View.GONE);
        txtvError.setVisibility(View.GONE);
        butRetry.setVisibility(View.GONE);
        txtvEmpty.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        subscription = rx.Observable.create((Observable.OnSubscribe<List<ItunesAdapter.Podcast>>) subscriber -> {

            API_URL = "http://itunes.apple.com/WebObjects/MZStoreServices.woa/ws/genres?id=" +id;

            //Spaces in the query need to be replaced with '+' character.
            String formattedUrl = String.format(API_URL).replace(' ', '+');

            OkHttpClient client = AntennapodHttpClient.getHttpClient();
            Request.Builder httpReq = new Request.Builder()
                    .url(formattedUrl)
                    .header("User-Agent", ClientConfig.USER_AGENT);
            List<ItunesAdapter.Podcast> podcasts = new ArrayList<>();
            try {
                Response response = client.newCall(httpReq.build()).execute();

                if(response.isSuccessful()) {
                    String resultString = response.body().string();
                    JSONObject result = new JSONObject(resultString);
                    JSONArray j = result.getJSONArray("results");

                    for (int i = 0; i < j.length(); i++) {
                        JSONObject podcastJson = j.getJSONObject(i);
                        ItunesAdapter.Podcast podcast = ItunesAdapter.Podcast.fromSearch(podcastJson);
                        podcasts.add(podcast);
                    }
                }
                else {
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
                    butRetry.setOnClickListener(v -> search(id));
                    butRetry.setVisibility(View.VISIBLE);
                });
    }



}
