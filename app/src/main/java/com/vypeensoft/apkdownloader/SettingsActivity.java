package com.vypeensoft.apkdownloader;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

public class SettingsActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "apk_downloader_prefs";
    public static final String KEY_URL = "monitor_url";
    public static final String KEY_DOWNLOAD_DIR = "download_dir";

    private TextInputEditText editUrl;
    private TextInputEditText editDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        editUrl = findViewById(R.id.edit_url);
        editDir = findViewById(R.id.edit_dir);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String url = prefs.getString(KEY_URL, "http://192.168.1.68:8000/");
        String dir = prefs.getString(KEY_DOWNLOAD_DIR, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/APKs");

        editUrl.setText(url);
        editDir.setText(dir);

        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> saveSettings());
    }

    private void saveSettings() {
        String url = editUrl.getText() != null ? editUrl.getText().toString().trim() : "";
        String dir = editDir.getText() != null ? editDir.getText().toString().trim() : "";

        if (url.isEmpty() || dir.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_URL, url)
                .putString(KEY_DOWNLOAD_DIR, dir)
                .apply();

        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
