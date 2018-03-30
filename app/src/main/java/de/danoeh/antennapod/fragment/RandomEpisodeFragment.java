package de.danoeh.antennapod.fragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.adapter.DefaultActionButtonCallback;
import de.danoeh.antennapod.adapter.itunes.ItunesAdapter;
import de.danoeh.antennapod.core.feed.FeedImage;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.feed.FeedMedia;
import de.danoeh.antennapod.core.storage.DBTasks;
import de.danoeh.antennapod.core.util.FeedItemUtil;
import de.danoeh.antennapod.core.util.LongList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by David on 2018-03-27.
 */

public class RandomEpisodeFragment extends Fragment {

    private Button randomButton;

    private static final String TAG = "RandomEpisodeFragment";

    public RandomEpisodeFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.random_episode, container, false);

        randomButton = (Button) view.findViewById(R.id.btnPlayRandom);
        randomButton.setOnClickListener(this::onClick);
        return view;
    }

    private void onClick(View v) {
        String topic = getRandomTopic();
        List<ItunesAdapter.Podcast> resultList = new ArrayList<>();

        CentralizedSearchFragment centralFrag = (CentralizedSearchFragment) getFragmentManager().findFragmentById(R.id.centralizedFragment);

        resultList = centralFrag.searchItunes(topic);   //Get search result from random topic
        ItunesAdapter.Podcast episode = getRandomEpisode(resultList);  //Get random episode from result list
        playEpisode(episode);  //Play episode
    }

    //Play episode
    private void playEpisode(ItunesAdapter.Podcast episode){
        MainActivity activity = (MainActivity) getActivity();
        FeedItem feedItem = new FeedItem();
        FeedImage feedImg = new FeedImage();

        feedImg.setFile_url(episode.imageUrl);
        feedImg.setTitle(episode.title);

        feedItem.setTitle(episode.title);
        feedItem.setLink(episode.feedUrl);
        feedItem.setImage(feedImg);

        activity.getSupportActionBar().setTitle(feedItem.getTitle());
        DefaultActionButtonCallback actionButtonCallback = new DefaultActionButtonCallback(getActivity());

        actionButtonCallback.onActionButtonPressed(feedItem, feedItem.isTagged(FeedItem.TAG_QUEUE) ?
                LongList.of(feedItem.getId()) : new LongList(0));

        FeedMedia media = feedItem.getMedia();

        if (media != null && media.isDownloaded()) {
            // playback was started, dialog should close itself
            ((MainActivity) getActivity()).dismissChildFragment();
        }
        // if media isn't downloaded
        else if (media != null) {
            DBTasks.playMedia(getActivity(), media, true, true, true);
            ((MainActivity) getActivity()).dismissChildFragment();
        }
    }

    //Return random search topic
    private String getRandomTopic(){
        Resources res = getResources();
        String[] random_dictionnary = res.getStringArray(R.array.random_dictionnary);
        int randomNum = getRandomNum(0, random_dictionnary.length-1);

        return random_dictionnary[randomNum];
    }

    //Returns random episode
    private ItunesAdapter.Podcast getRandomEpisode(List<ItunesAdapter.Podcast> list){
        return list.get(getRandomNum(0, list.size()-1));
    }

    //Returns random number
    private int getRandomNum(int min, int max){
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
}
