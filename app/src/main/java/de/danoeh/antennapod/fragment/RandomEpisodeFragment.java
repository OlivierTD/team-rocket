package de.danoeh.antennapod.fragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.danoeh.antennapod.R;

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

        CentralizedSearchFragment test = (CentralizedSearchFragment) getFragmentManager().findFragmentById(R.id.centralizedFragment);
        test.search(topic);
    }

    //Return random search topic
    private String getRandomTopic(){
        Resources res = getResources();
        String[] random_dictionnary = res.getStringArray(R.array.random_dictionnary);

        Random rand = new Random();
        int randomNum = rand.nextInt((random_dictionnary.length-1 - 0) + 1) + 0;

        String topic = random_dictionnary[randomNum];

        return topic;
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
