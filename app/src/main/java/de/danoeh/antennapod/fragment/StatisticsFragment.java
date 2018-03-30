package de.danoeh.antennapod.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.List;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.adapter.StatisticsListAdapter;

import de.danoeh.antennapod.core.storage.DBReader;
import de.danoeh.antennapod.core.util.Converter;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Vartan on 2018-03-26.
 */

public class StatisticsFragment extends Fragment implements AdapterView.OnItemClickListener {

    public static final String TAG = "StatisticsFragment";
    private static final String PREF_NAME = "StatisticsFragmentPrefs";
    private static final String PREF_COUNT_ALL = "countAll";

    private Subscription subscription;
    private TextView numberOfPodcasts;
    private TextView totalTimeTextView;
    private ListView feedStatisticsList;
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
        View root = inflater.inflate(R.layout.statistics_layout, container, false);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        prefs = getContext().getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        countAll = prefs.getBoolean(PREF_COUNT_ALL, false);

        numberOfPodcasts = (TextView) getView().findViewById(R.id.number_of_subscriptions);
        totalTimeTextView = (TextView) getView().findViewById(R.id.total_time);
        feedStatisticsList = (ListView) getView().findViewById(R.id.statistics_list);
        listAdapter = new StatisticsListAdapter(getActivity());
        listAdapter.setCountAll(countAll);
        feedStatisticsList.setAdapter(listAdapter);
        feedStatisticsList.setOnItemClickListener(this);
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshStatistics();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().finish();
            return true;
        } else if (item.getItemId() == R.id.statistics_mode) {
            selectStatisticsMode();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void selectStatisticsMode() {
        View contentView = View.inflate(getActivity(), R.layout.statistics_mode_select_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(contentView);
        builder.setTitle(R.string.statistics_mode);

        if (countAll) {
            ((RadioButton) contentView.findViewById(R.id.statistics_mode_count_all)).setChecked(true);
        } else {
            ((RadioButton) contentView.findViewById(R.id.statistics_mode_normal)).setChecked(true);
        }

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            countAll = ((RadioButton) contentView.findViewById(R.id.statistics_mode_count_all)).isChecked();
            listAdapter.setCountAll(countAll);
            prefs.edit().putBoolean(PREF_COUNT_ALL, countAll).apply();
            refreshStatistics();
        });

        builder.show();
    }

    private void refreshStatistics() {
        numberOfPodcasts.setVisibility(View.GONE);
        totalTimeTextView.setVisibility(View.GONE);
        feedStatisticsList.setVisibility(View.GONE);
        loadStatistics();
    }

    private void loadStatistics() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
        subscription = Observable.fromCallable(() -> DBReader.getStatistics(countAll))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result != null) {
                        totalTimeTextView.setText(getString(R.string.total_time_listened_to_podcasts) + " ");
                        totalTimeTextView.append(Converter
                                .shortLocalizedDuration(getActivity(), countAll ? result.totalTimeCountAll : result.totalTime));
                        listAdapter.update(result.feedTime);
                        numberOfPodcasts.setVisibility(View.VISIBLE);
                        totalTimeTextView.setVisibility(View.VISIBLE);
                        feedStatisticsList.setVisibility(View.VISIBLE);
                        int numberOfSubscriptions = feedStatisticsList.getAdapter().getCount();
                        numberOfPodcasts.setText(getString(R.string.statistics_number_of_subscriptions) + " " + numberOfSubscriptions);
                    }
                }, error -> Log.e(TAG, Log.getStackTraceString(error)));
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