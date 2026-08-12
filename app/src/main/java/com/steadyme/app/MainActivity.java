package com.steadyme.app;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.steadyme.app.databinding.ActivityMainBinding;
import com.steadyme.app.ui.HistoryFragment;
import com.steadyme.app.ui.HomeFragment;
import com.steadyme.app.ui.InsightsFragment;
import com.steadyme.app.ui.LogOptionsFragment;
import com.steadyme.app.ui.ProfileFragment;
import com.steadyme.app.worker.ReminderWorker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            switchTab(id);
            return true;
        });

        if (savedInstanceState == null) {
            binding.bottomNav.setSelectedItemId(com.steadyme.app.R.id.nav_home);
        }

        scheduleReminder();

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 20);
        }
    }

    private void switchTab(int id) {
        Fragment fragment;
        if (id == R.id.nav_log) {
            fragment = new LogOptionsFragment();
        } else if (id == R.id.nav_history) {
            fragment = new HistoryFragment();
        } else if (id == R.id.nav_insights) {
            fragment = new InsightsFragment();
        } else if (id == R.id.nav_profile) {
            fragment = new ProfileFragment();
        } else {
            fragment = new HomeFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void scheduleReminder() {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(ReminderWorker.class, 1, TimeUnit.DAYS).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("dailyMoodReminder", ExistingPeriodicWorkPolicy.UPDATE, request);
    }
}
