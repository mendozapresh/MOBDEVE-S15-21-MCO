package com.steadyme.app.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.ListenerRegistration;
import com.steadyme.app.R;
import com.steadyme.app.data.FirebaseRepository;
import com.steadyme.app.databinding.FragmentInsightsBinding;
import com.steadyme.app.model.MoodLog;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class InsightsFragment extends Fragment {

    private FragmentInsightsBinding binding;
    private ListenerRegistration registration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentInsightsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        registration = new FirebaseRepository().observeMoodLogs(new FirebaseRepository.LogsCallback() {
            @Override
            public void onLoaded(List<MoodLog> logs) {
                render(logs);
            }

            @Override
            public void onError(Exception e) {
                Snackbar.make(binding.getRoot(), e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (registration != null) {
            registration.remove();
        }
        binding = null;
        super.onDestroyView();
    }

    private void render(List<MoodLog> logs) {
        if (logs == null || logs.isEmpty()) {
            binding.tvInsightSummary.setText("Log a mood to see your clinical trends.");
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

        int brandGreen = ContextCompat.getColor(requireContext(), R.color.primary);
        LineDataSet lineDataSet = new LineDataSet(entries, "Mood score");
        lineDataSet.setColor(brandGreen);
        lineDataSet.setCircleColor(brandGreen);
        lineDataSet.setLineWidth(3f);
        lineDataSet.setDrawValues(false); // Clean up UI

        binding.chartMood.setData(new LineData(lineDataSet));
        binding.chartMood.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        binding.chartMood.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.chartMood.getAxisRight().setEnabled(false);
        binding.chartMood.getDescription().setEnabled(false);
        binding.chartMood.getLegend().setEnabled(false); // Remove legend per user request

        binding.chartMood.getXAxis().setTextColor(Color.BLACK);
        binding.chartMood.getAxisLeft().setTextColor(Color.BLACK);
        binding.chartMood.invalidate();

        // --- 2. REASON DETECTION & TOP REASONS ---
        Map<String, Integer> reasonCounts = new HashMap<>();
        for (MoodLog log : logs) {
            if (log.getReasons() != null) {
                for (String r : log.getReasons()) {
                    Integer current = reasonCounts.get(r);
                    reasonCounts.put(r, (current == null ? 0 : current) + 1);
                }
            }
        }

        List<Map.Entry<String, Integer>> sortedReasons = new ArrayList<>(reasonCounts.entrySet());
        sortedReasons.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        StringBuilder topReasonsText = new StringBuilder();
        if (sortedReasons.isEmpty()) {
            topReasonsText.append("No common reasons logged yet.");
        } else {
            int count = 0;
            for (Map.Entry<String, Integer> entry : sortedReasons) {
                if (count >= 5) break;
                topReasonsText.append(count + 1).append(". ").append(entry.getKey())
                        .append(" (").append(entry.getValue()).append(" times)\n");
                count++;
            }
        }
        binding.tvTopReasons.setText(topReasonsText.toString().trim());

        binding.tvInsightSummary.setText("Based on last " + shown + " check-ins.");

        // --- 3. CALCULATE METRICS ---
        binding.tvTotalLogs.setText(String.valueOf(logs.size()));

        Set<String> uniqueDaysThisWeek = new HashSet<>();
        Map<String, Integer> emotionDistribution = new HashMap<>();
        double totalScoreThisWeek = 0;
        int logsThisWeek = 0;

        Calendar cal = Calendar.getInstance();
        long oneWeekAgoMillis = cal.getTimeInMillis() - TimeUnit.DAYS.toMillis(7);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

        for (MoodLog log : logs) {
            if (log.getCreatedAt() == null) continue;

            Date logDate = log.getCreatedAt().toDate();
            String dateString = fmt.format(logDate);

            if (logDate.getTime() >= oneWeekAgoMillis) {
                uniqueDaysThisWeek.add(dateString);
                totalScoreThisWeek += log.getScore();
                logsThisWeek++;
            }

            String emotion = log.getEmotion() != null ? log.getEmotion() : "Unknown";
            emotionDistribution.put(emotion, emotionDistribution.getOrDefault(emotion, 0) + 1);
        }

        int streak = calculateStreak(logs);
        binding.tvDayStreak.setText(streak + "d");
        binding.tvThisWeek.setText(uniqueDaysThisWeek.size() + "/7");

        double avgScore = logsThisWeek == 0 ? 0 : totalScoreThisWeek / logsThisWeek;
        binding.tvAvgThisWeek.setText(String.format(Locale.getDefault(), "%.1f/5", avgScore));

        double totalScoreAll = 0;
        for (MoodLog log : logs) totalScoreAll += log.getScore();
        double overallAvg = logs.isEmpty() ? 0 : totalScoreAll / logs.size();

        // --- 4. DYNAMIC DESCRIPTION ---
        generateDynamicDescription(emotionDistribution, uniqueDaysThisWeek.size(), overallAvg);

        // --- 5. POPULATE PIE CHART ---
        setupPieChart(emotionDistribution);

        // --- 6. STABILITY ---
        double stabilityPercent = calculateStability(logs);

        if (stabilityPercent == -1) {
            binding.tvStability.setText("N/A");
            binding.tvStability.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_600));
        } else if (stabilityPercent >= 70) {
            if (overallAvg >= 3.5) {
                binding.tvStability.setText("Highly Stable");
                binding.tvStability.setTextColor(ContextCompat.getColor(requireContext(), R.color.stable_green));
            } else {
                binding.tvStability.setText("Persistent Low");
                binding.tvStability.setTextColor(Color.BLACK);
            }
        } else {
            binding.tvStability.setText("Unstable");
            binding.tvStability.setTextColor(Color.BLACK);
        }
    }

    private int calculateStreak(List<MoodLog> logs) {
        if (logs == null || logs.isEmpty()) return 0;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
        long todayStart = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        long yesterdayStart = cal.getTimeInMillis();

        int streak = 0;
        long lastDateStart = -1;
        boolean hasCurrentActivity = false;

        for (MoodLog log : logs) {
            if (log.getCreatedAt() == null) continue;
            Calendar logCal = Calendar.getInstance();
            logCal.setTime(log.getCreatedAt().toDate());
            logCal.set(Calendar.HOUR_OF_DAY, 0); logCal.set(Calendar.MINUTE, 0); logCal.set(Calendar.SECOND, 0); logCal.set(Calendar.MILLISECOND, 0);
            long logStart = logCal.getTimeInMillis();

            if (logStart == todayStart || logStart == yesterdayStart) hasCurrentActivity = true;

            if (lastDateStart == -1) {
                streak = 1;
                lastDateStart = logStart;
            } else if (logStart == lastDateStart) {
                continue;
            } else {
                long diffDays = (lastDateStart - logStart) / (24 * 60 * 60 * 1000);
                if (diffDays == 1) {
                    streak++;
                    lastDateStart = logStart;
                } else break;
            }
        }
        return hasCurrentActivity ? streak : 0;
    }

    private double calculateStability(List<MoodLog> logs) {
        if (logs == null || logs.size() < 2) return -1;
        double sumDiff = 0;
        int count = 0;
        for (int i = 0; i < logs.size() - 1; i++) {
            sumDiff += Math.abs(logs.get(i).getScore() - logs.get(i + 1).getScore());
            count++;
        }
        double avgDiff = sumDiff / count;
        return Math.max(0, 100 - (avgDiff * 30));
    }

    private void generateDynamicDescription(Map<String, Integer> emotionCounts, int uniqueDaysThisWeek, double overallAvg) {
        if (uniqueDaysThisWeek == 0) {
            binding.tvDynamicTitle.setText("Awaiting Data");
            binding.tvDynamicDesc.setText("Check in daily to build your clinical history. This helps identify reasons and mood cycles.");
            return;
        }

        int elevatedCount = getSafeCount(emotionCounts, "Elated") + getSafeCount(emotionCounts, "Happy");
        int total = 0;
        for (Integer count : emotionCounts.values()) {
            if (count != null) total += count;
        }

        double elevatedRatio = total == 0 ? 0 : (double) elevatedCount / total;

        if (binding.tvStability.getText().toString().contains("Unstable")) {
            binding.tvDynamicTitle.setText("High Mood Variability");
            binding.tvDynamicTitle.setTextColor(Color.BLACK);
            binding.tvDynamicDesc.setText("Frequent shifts between mood extremes were detected. This variability can be a clinical marker for rapid cycling in affective disorders.");
        } else if (overallAvg < 3.5) {
            binding.tvDynamicTitle.setText("Persistent Low Mood");
            binding.tvDynamicTitle.setTextColor(Color.BLACK);
            binding.tvDynamicDesc.setText("Your emotions are consistently in the lower range. While stable (low variability), a persistent low baseline is a key clinical indicator of a depressive episode.");
        } else if (elevatedRatio > 0.7 || overallAvg > 4.2) {
            binding.tvDynamicTitle.setText("Elevated Phase Detected");
            binding.tvDynamicTitle.setTextColor(Color.BLACK);
            binding.tvDynamicDesc.setText("Your logs show a sustained pattern of elevated mood. This may indicate a hypomanic or manic phase. Monitor for lack of sleep or impulsivity.");
        } else {
            binding.tvDynamicTitle.setText("Mood is Stabilizing");
            binding.tvDynamicTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.stable_green));
            binding.tvDynamicDesc.setText("Your emotional baseline is showing healthy consistency in the 'up' range. Maintain your routine to support long-term mood stability.");
        }
    }

    private int getSafeCount(Map<String, Integer> map, String key) {
        Integer val = map.get(key);
        return val == null ? 0 : val;
    }

    private void setupPieChart(Map<String, Integer> emotionDistribution) {
        List<PieEntry> pieEntries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : emotionDistribution.entrySet()) {
            pieEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
            colors.add(MoodPalette.paleColor(requireContext(), entry.getKey()));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(colors);
        pieDataSet.setValueTextColor(Color.BLACK);
        pieDataSet.setValueTextSize(14f);
        // Custom formatter to remove .00 decimals
        pieDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        PieData pieData = new PieData(pieDataSet);

        binding.chartMoodDistribution.setData(pieData);
        binding.chartMoodDistribution.getDescription().setEnabled(false);
        binding.chartMoodDistribution.setDrawHoleEnabled(true);
        binding.chartMoodDistribution.setHoleColor(Color.WHITE);
        binding.chartMoodDistribution.setTransparentCircleRadius(0f);
        binding.chartMoodDistribution.getLegend().setTextColor(Color.BLACK);
        binding.chartMoodDistribution.setDrawEntryLabels(false);
        binding.chartMoodDistribution.invalidate();
    }
}
