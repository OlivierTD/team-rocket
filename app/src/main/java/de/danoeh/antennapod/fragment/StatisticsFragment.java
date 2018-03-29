package de.danoeh.antennapod.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.adapter.StatisticsListAdapter;

import de.danoeh.antennapod.core.storage.DBReader;
import de.danoeh.antennapod.core.util.Converter;
import rx.Subscription;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Vartan on 2018-03-26.
 */

public class StatisticsFragment extends Fragment implements AdapterView.OnItemClickListener {

    public static final String TAG = "StatisticsFragment";
    private static final String PREF_NAME = "StatisticsFragmentPrefs";
    private static final String PREF_COUNT_ALL = "countAll";

    private Subscription subscription;
    private TextView totalTimeTextView;
    private LinearLayout feedStatisticsList;
    private ProgressBar progressBar;
    private StatisticsListAdapter listAdapter;
    private boolean countAll = false;
    private SharedPreferences prefs;

    public StatisticsFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.statistics_list, container, false);

        feedStatisticsList = (LinearLayout) root.findViewById(R.id.list);
        listAdapter = new StatisticsListAdapter(getActivity());
        listAdapter.setCountAll(countAll);
//        feedStatisticsList.setAdapter(listAdapter);
//        feedStatisticsList.setOnItemClickListener(this);



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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DBReader.StatisticsItem stats = listAdapter.getItem(position);

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(stats.feed.getTitle());
        dialog.setMessage(getString(R.string.statistics_details_dialog,
                countAll ? stats.episodesStartedIncludingMarked : stats.episodesStarted,
                stats.episodes,
                Converter.shortLocalizedDuration(getActivity(), countAll ?
                        stats.timePlayedCountAll : stats.timePlayed),
                Converter.shortLocalizedDuration(getActivity(), stats.time)));
        dialog.setPositiveButton(android.R.string.ok, null);
        dialog.show();
    }
}