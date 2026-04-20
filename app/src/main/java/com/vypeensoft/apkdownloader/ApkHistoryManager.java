package com.vypeensoft.apkdownloader;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class ApkHistoryManager {

    private static final String PREFS_NAME = "apk_history_prefs";
    private static final String KEY_HISTORY = "history_list";

    public static List<String> getHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_HISTORY, "[]");
        List<String> list = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean hasBeenDownloaded(Context context, String apkUrl) {
        return getHistory(context).contains(apkUrl);
    }

    public static void addDownload(Context context, String apkUrl) {
        List<String> history = getHistory(context);
        if (!history.contains(apkUrl)) {
            history.add(apkUrl);
            saveHistory(context, history);
        }
    }

    private static void saveHistory(Context context, List<String> history) {
        JSONArray array = new JSONArray();
        for (String url : history) {
            array.put(url);
        }
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply();
    }
}
