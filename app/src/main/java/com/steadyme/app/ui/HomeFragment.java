package com.steadyme.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;
import com.steadyme.app.LoginActivity;
import com.steadyme.app.R;
import com.steadyme.app.data.FirebaseRepository;
import com.steadyme.app.databinding.FragmentHomeBinding;
import com.steadyme.app.model.MoodLog;

import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String HOTLINE_NUMBER = "1553";

    private FragmentHomeBinding binding;
    private ListenerRegistration registration;
    private final FirebaseRepository repo = new FirebaseRepository();
    private final MoodLogAdapter adapter = new MoodLogAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.rvRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecent.setAdapter(adapter);

        String email = FirebaseAuth.getInstance().getCurrentUser() == null ? "there" : FirebaseAuth.getInstance().getCurrentUser().getEmail();
        binding.tvGreetingName.setText(email == null ? "there 👋" : email.split("@")[0] + " 👋");
        binding.ivLogout.setOnClickListener(x -> logout());
        binding.ivBell.setOnClickListener(x -> getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new NotificationsFragment())
                .addToBackStack(null)
                .commit());
        binding.tvSeeAll.setOnClickListener(x ->
                ((BottomNavigationView) requireActivity().findViewById(R.id.bottomNav)).setSelectedItemId(R.id.nav_history));
        binding.tvHotline.setOnClickListener(x -> {
            try {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + HOTLINE_NUMBER)));
            } catch (Exception e) {
                Snackbar.make(binding.getRoot(), "Unable to open dialer", Snackbar.LENGTH_SHORT).show();
            }
        });

        registration = repo.observeMoodLogs(new FirebaseRepository.LogsCallback() {
            @Override
            public void onLoaded(List<MoodLog> logs) {
                adapter.submit(logs);
                int streak = calculateStreak(logs);
                binding.tvStreakValue.setText(String.valueOf(streak));
                binding.tvFlame.setText(streak == 0 ? "💧" : "🔥");
                updateStreakSegments(streak);
                bindToday(logs);
            }

            @Override
            public void onError(Exception e) {
                Snackbar.make(binding.getRoot(), e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void updateStreakSegments(int streak) {
        int activeColor = ContextCompat.getColor(requireContext(), R.color.primary);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.gray_300);
        int displayStreak = Math.min(7, streak);

        for (int i = 0; i < binding.llStreakSegments.getChildCount(); i++) {
            View segment = binding.llStreakSegments.getChildAt(i);
            if (segment.getBackground() != null) {
                segment.getBackground().setTint(i < displayStreak ? activeColor : inactiveColor);
            }
        }
    }


    private void bindToday(List<MoodLog> logs) {
        MoodLog today = null;
        Calendar now = Calendar.getInstance();
        for (MoodLog log : logs) {
            if (log.getCreatedAt() == null) continue;
            Calendar logCal = Calendar.getInstance();
            logCal.setTime(log.getCreatedAt().toDate());
            if (logCal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                    && logCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
                today = log;
                break;
            }
        }

        if (today == null) {
            binding.cardToday.setVisibility(View.GONE);
            return;
        }

        binding.cardToday.setVisibility(View.VISIBLE);
        binding.cardToday.setCardBackgroundColor(MoodPalette.palestColor(requireContext(), today.getEmotion()));
        binding.tvTodayEmoji.setImageResource(MoodPalette.iconRes(today.getEmotion()));
        binding.tvTodayEmoji.getBackground().setTint(MoodPalette.paleColor(requireContext(), today.getEmotion()));

        binding.tvTodayEmotion.setText(today.getEmotion());
        binding.tvTodayEmotion.setTextColor(MoodPalette.color(requireContext(), today.getEmotion()));


        String notes = today.getNotes();
        binding.tvTodayNote.setText(notes == null || notes.isEmpty() ? "No notes added" : notes);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private int calculateStreak(List<MoodLog> logs) {
        if (logs == null || logs.isEmpty()) return 0;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
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
            logCal.set(Calendar.HOUR_OF_DAY, 0);
            logCal.set(Calendar.MINUTE, 0);
            logCal.set(Calendar.SECOND, 0);
            logCal.set(Calendar.MILLISECOND, 0);
            long logStart = logCal.getTimeInMillis();

            if (logStart == todayStart || logStart == yesterdayStart) {
                hasCurrentActivity = true;
            }

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
                } else {
                    break;
                }
            }
        }

        return hasCurrentActivity ? streak : 0;
    }

    @Override
    public void onDestroyView() {
        if (registration != null) {
            registration.remove();
        }
        binding = null;
        super.onDestroyView();
    }
}
