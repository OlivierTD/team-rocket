package de.danoeh.antennapod.service;

/**
 * Created by James on 2018-04-12.
 */
import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

import de.danoeh.antennapod.core.feed.Queue;

public final class QueueStoreService {

    private QueueStoreService(){}

    // Attempts to load data from local storage
    public static ArrayList<Queue> loadList(Context context, ArrayList<Queue> queueList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("queue list", null);
        Type type = new TypeToken<ArrayList<Queue>>() {
        }.getType();
        queueList = gson.fromJson(json, type);

        if (queueList == null) {
            queueList = new ArrayList<>();
        }
        return queueList;
    }

    // Attempts to persist data to local storage
    public static void storeList(Context context, ArrayList<Queue> queueList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(queueList);
        editor.putString("queue list", json);
        editor.apply();
    }

}
