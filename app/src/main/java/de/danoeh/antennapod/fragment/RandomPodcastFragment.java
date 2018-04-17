package de.danoeh.antennapod.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by David on 2018-03-27.
 */

public class RandomPodcastFragment extends Fragment {

    private static final String TAG = "RandomPodcastFragment";
    private Subscription subscription;
    HomeSearchFragment homeSearchFrag = new HomeSearchFragment();
    String topic;
    ItunesAdapter.Podcast podcast;
    List<ItunesAdapter.Podcast> resultList = new ArrayList<>();
    AlertDialog enterNameDialog;
    String[] random_topic_list;

    public RandomPodcastFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.random_podcast, container, false);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.btnRandomPodcast:
                clickRandom();
        }

        return super.onOptionsItemSelected(item);
    }
    public void clickRandom() {
        topic = getRandomTopic();
        homeSearchFrag = (HomeSearchFragment) getFragmentManager().findFragmentById(R.id.homeSearchFragment);

        resultList = homeSearchFrag.searchItunes(topic);   //Get search result from random topic

        //Create the dialog to be shown to the user
        enterNameDialog = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.random_category) + topic.toString())
                .setPositiveButton(R.string.confirm, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        //Display the dialog
        enterNameDialog.show();

        enterNameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                podcast = getRandomPodcast(resultList);  //Get random podcast from result list
                playEpisode(podcast);  //Play episode
                enterNameDialog.hide();
            }
        });
    }

    //Play episode
    private void playEpisode(ItunesAdapter.Podcast episode){
        if (subscription != null) {
            subscription.unsubscribe();
        }

        if (episode.feedUrl == null) {
            return;
        }
        if (!episode.feedUrl.contains("itunes.apple.com")) {
            Intent intent = new Intent(getActivity(), OnlineFeedViewActivity.class);
            intent.putExtra(OnlineFeedViewActivity.ARG_FEEDURL, episode.feedUrl);
            intent.putExtra(OnlineFeedViewActivity.ARG_TITLE, "iTunes");
            startActivity(intent);
        } else {
            subscription = Observable.create((Observable.OnSubscribe<String>) subscriber -> {
                OkHttpClient client = AntennapodHttpClient.getHttpClient();
                Request.Builder httpReq = new Request.Builder()
                        .url(episode.feedUrl)
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
                        Intent intent = new Intent(getActivity(), OnlineFeedViewActivity.class);
                        intent.putExtra(OnlineFeedViewActivity.ARG_FEEDURL, feedUrl);
                        intent.putExtra(OnlineFeedViewActivity.ARG_TITLE, "iTunes");
                        startActivity(intent);
                    }, error -> {
                        Log.e(TAG, Log.getStackTraceString(error));
                        String prefix = getString(R.string.error_msg_prefix);
                        new MaterialDialog.Builder(getActivity())
                                .content(prefix + " " + error.getMessage())
                                .neutralText(android.R.string.ok)
                                .show();
                    });
        }
    }

    //Return random search topic
    public String getRandomTopic(){
        if (random_topic_list == null)
            random_topic_list = getTopicList();

        int randomNum = getRandomNum(0, random_topic_list.length-1);

        return random_topic_list[randomNum];
    }

    //Returns random episode
    public ItunesAdapter.Podcast getRandomPodcast(List<ItunesAdapter.Podcast> list){
        return list.get(getRandomNum(0, list.size()-1));
    }

    //Returns random number
    public int getRandomNum(int min, int max){
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + 0;
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
    }

    public String[] getTopicList(){
        Resources res = getResources();
        random_topic_list = res.getStringArray(R.array.random_dictionnary);

        return random_topic_list;
    }

    public void setTopicList(String[] random_topic_list){
        this.random_topic_list = random_topic_list;
    }
}
