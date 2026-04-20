package com.vypeensoft.apkdownloader;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MonitorService extends Service {
    private static final String CHANNEL_ID = "monitor_channel";
    private Thread monitorThread;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("APK Monitor")
                .setContentText("Checking for new APKs...")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .build();
        startForeground(1, notification);

        if (monitorThread == null || !monitorThread.isAlive()) {
            monitorThread = new Thread(this::checkAndDownloadApks);
            monitorThread.start();
        }

        return START_NOT_STICKY;
    }

    private void checkAndDownloadApks() {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String urlString = prefs.getString(SettingsActivity.KEY_URL, "");
        String dirString = prefs.getString(SettingsActivity.KEY_DOWNLOAD_DIR, "");

        if (urlString.isEmpty() || dirString.isEmpty()) {
            Intent updateIntent = new Intent("com.vypeensoft.apkdownloader.UPDATE_UI");
            sendBroadcast(updateIntent);
            stopSelf();
            return;
        }

        try {
            Document doc = Jsoup.connect(urlString).get();
            Elements links = doc.select("a[href]");

            File dir = new File(dirString);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            OkHttpClient client = new OkHttpClient();

            for (Element link : links) {
                String href = link.attr("abs:href");
                if (href.toLowerCase().endsWith(".apk") && !ApkHistoryManager.hasBeenDownloaded(this, href)) {
                    // Download
                    Request request = new Request.Builder().url(href).build();
                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful() && response.body() != null) {
                            String fileName = href.substring(href.lastIndexOf('/') + 1);
                            
                            // Prevent empty filename
                            if(fileName.isEmpty()) fileName = "downloaded.apk";

                            File outFile = new File(dir, fileName);
                            
                            try (InputStream is = response.body().byteStream();
                                 FileOutputStream fos = new FileOutputStream(outFile)) {
                                byte[] buffer = new byte[8192];
                                int bytesRead;
                                while ((bytesRead = is.read(buffer)) != -1) {
                                    fos.write(buffer, 0, bytesRead);
                                }
                            }
                            ApkHistoryManager.addDownload(this, href);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Intent updateIntent = new Intent("com.vypeensoft.apkdownloader.UPDATE_UI");
            sendBroadcast(updateIntent);
            stopSelf();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Monitor Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
