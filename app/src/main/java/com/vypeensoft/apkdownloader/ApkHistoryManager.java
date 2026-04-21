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
    private static final String KEY_LAST_CLEARED_SIZE = "last_cleared_size";

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

    public static List<String> getVisibleHistory(Context context) {
        List<String> fullHistory = getHistory(context);
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int clearedSize = prefs.getInt(KEY_LAST_CLEARED_SIZE, 0);
        
        if (clearedSize >= fullHistory.size()) {
            return new ArrayList<>();
        }
        return fullHistory.subList(clearedSize, fullHistory.size());
    }

    public static void clearVisibleHistory(Context context) {
        List<String> history = getHistory(context);
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_LAST_CLEARED_SIZE, history.size()).apply();
    }

    public static void clearHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_HISTORY).remove(KEY_LAST_CLEARED_SIZE).apply();
    }

    private static void saveHistory(Context context, List<String> history) {
        JSONArray array = new JSONArray();
        for (String url : history) {
            array.put(url);
        }
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply();
    }

    public static void writeToHistoryFile(String dirString, String fileName) {
        if (dirString == null || dirString.isEmpty()) return;
        try {
            java.io.File dir = new java.io.File(dirString);
            if (!dir.exists()) dir.mkdirs();
            java.io.File historyFile = new java.io.File(dir, "history.txt");
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(historyFile, true)) {
                String entry = fileName + "\n";
                fos.write(entry.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isFilenameInHistoryFile(String dirString, String fileName) {
        if (dirString == null || dirString.isEmpty()) return false;
        java.io.File historyFile = new java.io.File(dirString, "history.txt");
        if (!historyFile.exists()) return false;
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(historyFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equals(fileName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
