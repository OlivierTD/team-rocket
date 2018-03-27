package de.danoeh.antennapod.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.danoeh.antennapod.R;

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
        View root = inflater.inflate(R.layout.random_episode, container, false);

      //  randomButton = (Button) getView().findViewById(R.id.btnPlayRandom);

        return root;
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
