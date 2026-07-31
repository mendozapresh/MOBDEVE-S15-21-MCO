package com.steadyme.app;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.work.*;
import com.steadyme.app.databinding.ActivityMainBinding;
import com.steadyme.app.ui.*;
import com.steadyme.app.worker.ReminderWorker;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

 private ActivityMainBinding binding;

 @Override
 public void onCreate(Bundle b) {
  super.onCreate(b);
  binding = ActivityMainBinding.inflate(getLayoutInflater());
  setContentView(binding.getRoot());

  binding.bottomNav.setOnItemSelectedListener(item -> {
   int id = item.getItemId();

   // Check if the Profile button was clicked
   if (id == R.id.nav_profile) {
    // Launch Profile Activity via Intent as taught in "Activity Overview"
    Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
    startActivity(intent);
    return false; // Don't highlight the profile icon since we are leaving this screen
   } else {
    // Otherwise, swap the fragments normally
    switchTab(id);
    return true;
   }
  });

  if (b == null) {
   binding.bottomNav.setSelectedItemId(com.steadyme.app.R.id.nav_home);
  }

  scheduleReminder();

  if (Build.VERSION.SDK_INT >= 33) {
   requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 20);
  }
 }

 private void switchTab(int id) {
  Fragment f = id == R.id.nav_log ? new LogOptionsFragment() :
          id == R.id.nav_history ? new HistoryFragment() :
          id == R.id.nav_insights ? new InsightsFragment() :
          new HomeFragment();

  getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, f).commit();
 }

 private void scheduleReminder() {
  PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(ReminderWorker.class, 1, TimeUnit.DAYS).build();
  WorkManager.getInstance(this).enqueueUniquePeriodicWork("dailyMoodReminder", ExistingPeriodicWorkPolicy.UPDATE, req);
 }
}
