package com.steadyme.app.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.ListenerRegistration;
import com.steadyme.app.data.FirebaseRepository;
import com.steadyme.app.databinding.FragmentInsightsBinding;
import com.steadyme.app.model.MoodLog;

import java.text.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class InsightsFragment extends Fragment {

    private FragmentInsightsBinding binding;
    private ListenerRegistration registration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        binding = FragmentInsightsBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        // The repository handles the background threading, delivering the result back to the Main Thread safely
        registration = new FirebaseRepository().observeMoodLogs(new FirebaseRepository.LogsCallback() {
            public void onLoaded(List<MoodLog> logs) {
                render(logs);
            }

            public void onError(Exception e) {
                Snackbar.make(binding.getRoot(), e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (registration != null) registration.remove();
        binding = null; // Prevent memory leaks as taught in ViewBinding lessons
        super.onDestroyView();
    }

    private void render(List<MoodLog> logs) {
        if (logs == null || logs.isEmpty()) {
            binding.tvInsightSummary.setText("Log a mood to see your trends.");
            return;
        }

        // --- 1. POPULATE EXISTING LINE CHART ---
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int shown = Math.min(14, logs.size());

        for (int i = 0; i < shown; i++) {
            MoodLog log = logs.get(shown - 1 - i);
            entries.add(new Entry(i, log.getScore()));
            labels.add(log.getCreatedAt() == null ? "Now" : new SimpleDateFormat("MMM d", Locale.getDefault()).format(log.getCreatedAt().toDate()));
        }

        LineDataSet data = new LineDataSet(entries, "Mood score");
        data.setColor(Color.rgb(142, 185, 90));
        data.setCircleColor(Color.rgb(142, 185, 90));
        data.setLineWidth(3f);
        binding.chartMood.setData(new LineData(data));
        binding.chartMood.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        binding.chartMood.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.chartMood.getAxisRight().setEnabled(false);
        binding.chartMood.getDescription().setEnabled(false);
        binding.chartMood.invalidate();

        binding.tvInsightSummary.setText("Based on your last " + shown + " check-ins.");

        // --- 2. CALCULATE TOP METRICS & DISTRIBUTIONS ---
        binding.tvTotalLogs.setText(String.valueOf(logs.size()));

        int logsThisWeek = 0;
        int currentStreak = 0;
        long currentTime = System.currentTimeMillis();
        long oneWeekAgo = currentTime - TimeUnit.DAYS.toMillis(7);

        Map<String, Integer> emotionDistribution = new HashMap<>();

        // Assuming logs are sorted newest to oldest
        Date previousLogDate = null;
        for (MoodLog log : logs) {
            Date logDate = log.getCreatedAt() != null ? log.getCreatedAt().toDate() : new Date();

            // Weekly count
            if (logDate.getTime() >= oneWeekAgo) {
                logsThisWeek++;
            }

            // Distribution Map for Pie Chart
            // Note: Update "getEmotion()" to whatever your actual getter is called in MoodLog!
            String emotion = log.getEmotion() != null ? log.getEmotion() : "Unknown";
            emotionDistribution.put(emotion, emotionDistribution.getOrDefault(emotion, 0) + 1);

            // Basic Streak Calculation
            if (previousLogDate == null) {
                currentStreak = 1;
            } else {
                long diffInMillies = Math.abs(previousLogDate.getTime() - logDate.getTime());
                long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                if (diffInDays <= 1) {
                    currentStreak++;
                }
            }
            previousLogDate = logDate;
        }

        binding.tvThisWeek.setText(Math.min(logsThisWeek, 7) + "/7");
        binding.tvDayStreak.setText(currentStreak + "d");

        // --- 3. DYNAMIC DESCRIPTION ---
        generateDynamicDescription(emotionDistribution, logsThisWeek);

        // --- 4. POPULATE PIE CHART ---
        setupPieChart(emotionDistribution);

        // --- 5. CONSISTENCY METRICS ---
        binding.tvAvgThisWeek.setText("AVG THIS WEEK\n" + (logsThisWeek > 0 ? logsThisWeek + " logs" : "N/A"));

        // Basic mock logic for stability and consistency score
        int consistencyScore = Math.min((logsThisWeek * 100) / 7, 100);
        binding.pbConsistency.setProgress(consistencyScore);
        binding.tvScorePercent.setText(consistencyScore + "%");

        if (consistencyScore >= 80) {
            binding.tvStability.setText("STABILITY\nHighly Stable");
            binding.tvStability.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            binding.tvStability.setText("STABILITY\nVariable");
            binding.tvStability.setTextColor(Color.parseColor("#FF9800")); // Orange
        }
    }

    private void generateDynamicDescription(Map<String, Integer> emotionCounts, int logsThisWeek) {
        if (logsThisWeek == 0) {
            binding.tvDynamicTitle.setText("Quiet Week");
            binding.tvDynamicDesc.setText("You haven't logged any emotions this week. Check in to keep your streak alive!");
            return;
        }

        int negativeEmotions = emotionCounts.getOrDefault("Anxious", 0)
                + emotionCounts.getOrDefault("Sad", 0)
                + emotionCounts.getOrDefault("Angry", 0)
                + emotionCounts.getOrDefault("Frustrated", 0)
                + emotionCounts.getOrDefault("Overwhelmed", 0);

        int total = 0;
        for (int count : emotionCounts.values()) total += count;

        if (total > 0 && (negativeEmotions * 100 / total) > 50) {
            binding.tvDynamicTitle.setText("Significant Mood Swing");
            binding.tvDynamicTitle.setTextColor(Color.parseColor("#FF9800")); // Orange
            binding.tvDynamicDesc.setText("A large mood shift was detected in recent entries. High variability may signal an oncoming episode.");
        } else {
            binding.tvDynamicTitle.setText("Stable Mood");
            binding.tvDynamicTitle.setTextColor(Color.parseColor("#4CAF50")); // Green
            binding.tvDynamicDesc.setText("Your emotions have been mostly stable and positive. Keep up the great work!");
        }
    }

    private void setupPieChart(Map<String, Integer> emotionDistribution) {
        List<PieEntry> pieEntries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : emotionDistribution.entrySet()) {
            pieEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        // ColorTemplate.MATERIAL_COLORS works well for light themes
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setValueTextSize(14f);

        PieData pieData = new PieData(pieDataSet);

        binding.chartMoodDistribution.setData(pieData);
        binding.chartMoodDistribution.getDescription().setEnabled(false);
        binding.chartMoodDistribution.setDrawHoleEnabled(true);
        binding.chartMoodDistribution.setHoleColor(Color.WHITE);
        binding.chartMoodDistribution.setTransparentCircleRadius(0f);
        binding.chartMoodDistribution.getLegend().setTextColor(Color.BLACK);
        binding.chartMoodDistribution.invalidate();
    }
}
