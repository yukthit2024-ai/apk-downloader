package com.vypeensoft.apkdownloader;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private TextView textStatus;
    private ListView listHistory;
    private ArrayAdapter<String> adapter;

    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshHistory();
            textStatus.setText("Check complete. Last scan finished.");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.menu_help, R.string.menu_about);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        textStatus = findViewById(R.id.text_status);
        listHistory = findViewById(R.id.list_history);
        Button btnCheck = findViewById(R.id.btn_check);
        Button btnClear = findViewById(R.id.btn_clear);

        btnCheck.setOnClickListener(v -> startCheck());
        btnClear.setOnClickListener(v -> clearApks());

        checkPermissions();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s", getPackageName())));
                    startActivity(intent);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        1001);
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1002);
            }
        }
    }

    private void startCheck() {
        textStatus.setText("Checking for APKs...");
        Intent serviceIntent = new Intent(this, MonitorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void clearApks() {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String defaultDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/APKs";
        String dirString = prefs.getString(SettingsActivity.KEY_DOWNLOAD_DIR, defaultDir);
        
        if (dirString == null || dirString.isEmpty()) {
            textStatus.setText("Error: Download directory not set.");
            return;
        }

        File dir = new File(dirString);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                int deletedCount = 0;
                for (File file : files) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".apk")) {
                        if (file.delete()) {
                            deletedCount++;
                        }
                    }
                }
                textStatus.setText("Deleted " + deletedCount + " APKs.");
            } else {
                textStatus.setText("No APKs found to delete.");
            }
        } else {
            textStatus.setText("Directory does not exist.");
        }

        ApkHistoryManager.clearVisibleHistory(this);
        refreshHistory();
    }

    private void refreshHistory() {
        List<String> history = ApkHistoryManager.getVisibleHistory(this);
        List<String> displayNames = new java.util.ArrayList<>();
        if (!history.isEmpty()) {
            String url = history.get(history.size() - 1);
            displayNames.add(url.substring(url.lastIndexOf('/') + 1));
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayNames);
        listHistory.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHistory();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(updateReceiver, new IntentFilter("com.vypeensoft.apkdownloader.UPDATE_UI"), Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(updateReceiver, new IntentFilter("com.vypeensoft.apkdownloader.UPDATE_UI"));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(updateReceiver);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_help) {
            startActivity(new Intent(this, HelpActivity.class));
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
