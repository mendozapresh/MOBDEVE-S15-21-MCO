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
        // Inflating the ViewBinding class generated from your fragment_insights.xml
        binding = FragmentInsightsBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        // Observing logs off the main thread via FirebaseRepository
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
        binding = null;
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
        int shown = Math.min(14, logs.size()); // Showing up to 14 for better trend data

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

        // Light theme adjustments for the Line Chart
        binding.chartMood.getXAxis().setTextColor(Color.BLACK);
        binding.chartMood.getAxisLeft().setTextColor(Color.BLACK);
        binding.chartMood.getLegend().setTextColor(Color.BLACK);

        binding.chartMood.invalidate();
        binding.tvInsightSummary.setText("Based on your last " + shown + " check-ins.");

        // --- 2. CALCULATE METRICS (Total, Streak, Week, Distributions) ---
        binding.tvTotalLogs.setText(String.valueOf(logs.size()));

        Set<String> uniqueDaysThisWeek = new HashSet<>();
        Map<String, Integer> emotionDistribution = new HashMap<>();
        int currentStreak = 0;

        Calendar cal = Calendar.getInstance();
        long oneWeekAgoMillis = cal.getTimeInMillis() - TimeUnit.DAYS.toMillis(7);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

        Calendar lastDateProcessed = null;

        // Assuming logs list is ordered Newest to Oldest
        for (MoodLog log : logs) {
            if (log.getCreatedAt() == null) continue;

            Date logDate = log.getCreatedAt().toDate();
            String dateString = fmt.format(logDate);

            // Calculate Unique Days This Week (0/7 format)
            if (logDate.getTime() >= oneWeekAgoMillis) {
                uniqueDaysThisWeek.add(dateString);
            }

            // Pie Chart Distribution Count
            // NOTE: Make sure 'getEmotion()' exists in your MoodLog model!
            String emotion = log.getEmotion() != null ? log.getEmotion() : "Unknown";
            emotionDistribution.put(emotion, emotionDistribution.getOrDefault(emotion, 0) + 1);

            // Correct Day Streak Calculation (ignores multiple logs on the same day)
            Calendar logCal = Calendar.getInstance();
            logCal.setTime(logDate);
            logCal.set(Calendar.HOUR_OF_DAY, 0);
            logCal.set(Calendar.MINUTE, 0);
            logCal.set(Calendar.SECOND, 0);
            logCal.set(Calendar.MILLISECOND, 0);

            if (lastDateProcessed == null) {
                currentStreak = 1;
                lastDateProcessed = logCal;
            } else {
                long diffInMillis = lastDateProcessed.getTimeInMillis() - logCal.getTimeInMillis();
                long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

                if (diffInDays == 0) {
                    // Same day log, ignore for streak
                } else if (diffInDays == 1) {
                    // Consecutive day!
                    currentStreak++;
                    lastDateProcessed = logCal;
                } else {
                    // Gap found, stop calculating streak
                    // Note: If you want current streak to be 0 if they missed yesterday,
                    // you would compare the first log to "today" here as well.
                }
            }
        }

        binding.tvThisWeek.setText(uniqueDaysThisWeek.size() + "/7");
        binding.tvDayStreak.setText(currentStreak + "d");

        // --- 3. DYNAMIC DESCRIPTION ---
        generateDynamicDescription(emotionDistribution, uniqueDaysThisWeek.size());

        // --- 4. POPULATE PIE CHART ---
        setupPieChart(emotionDistribution);

        // --- 5. CONSISTENCY METRICS ---
        binding.tvAvgThisWeek.setText("AVG THIS WEEK\n" + (uniqueDaysThisWeek.isEmpty() ? "N/A" : uniqueDaysThisWeek.size() + " days"));

        // Mock consistency score (Based on 7 days)
        int consistencyScore = Math.min((uniqueDaysThisWeek.size() * 100) / 7, 100);
        binding.pbConsistency.setProgress(consistencyScore);
        binding.tvScorePercent.setText(consistencyScore + "%");

        if (consistencyScore >= 70) {
            binding.tvStability.setText("STABILITY\nHighly Stable");
            binding.tvStability.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            binding.tvStability.setText("STABILITY\nVariable");
            binding.tvStability.setTextColor(Color.parseColor("#FF9800")); // Orange
        }
    }

    private void generateDynamicDescription(Map<String, Integer> emotionCounts, int uniqueDaysThisWeek) {
        if (uniqueDaysThisWeek == 0) {
            binding.tvDynamicTitle.setText("Quiet Week");
            binding.tvDynamicDesc.setText("You haven't logged any emotions recently. Check in to keep your streak alive!");
            return;
        }

        // Tally negative emotions to determine the string
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
        // ColorTemplate generates vibrant colors suitable for light themes
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setValueTextSize(14f);

        PieData pieData = new PieData(pieDataSet);

        binding.chartMoodDistribution.setData(pieData);
        binding.chartMoodDistribution.getDescription().setEnabled(false);
        binding.chartMoodDistribution.setDrawHoleEnabled(true);
        binding.chartMoodDistribution.setHoleColor(Color.WHITE); // Light theme hole
        binding.chartMoodDistribution.setTransparentCircleRadius(0f);
        binding.chartMoodDistribution.getLegend().setTextColor(Color.BLACK); // Light theme legend
        binding.chartMoodDistribution.invalidate();
    }
}
