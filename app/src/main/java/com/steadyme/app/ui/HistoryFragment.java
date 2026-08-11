package com.steadyme.app.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.ListenerRegistration;
import com.steadyme.app.R;
import com.steadyme.app.data.FirebaseRepository;
import com.steadyme.app.databinding.FragmentHistoryBinding;
import com.steadyme.app.model.MoodLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {
    private FragmentHistoryBinding binding;
    private ListenerRegistration registration;
    private final MoodLogAdapter adapter = new MoodLogAdapter();
    private List<MoodLog> allLogs = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.rvMoodHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMoodHistory.setAdapter(adapter);

        setupFilters();

        registration = new FirebaseRepository().observeMoodLogs(new FirebaseRepository.LogsCallback() {
            @Override
            public void onLoaded(List<MoodLog> logs) {
                allLogs = logs;
                applyFilters();
            }

            @Override
            public void onError(Exception e) {
                Snackbar.make(binding.getRoot(), e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void setupFilters() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilters());

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                applyFilters();
            }
        });
    }

    private void applyFilters() {
        if (allLogs == null) return;

        String query = binding.etSearch.getText().toString().toLowerCase(Locale.getDefault()).trim();
        int checkedId = binding.chipGroupFilters.getCheckedChipId();
        String selectedEmotion = "";

        if (checkedId != View.NO_ID && checkedId != R.id.chipAll) {
            Chip chip = binding.getRoot().findViewById(checkedId);
            if (chip != null) {
                selectedEmotion = chip.getText().toString();
            }
        }

        List<MoodLog> filtered = new ArrayList<>();
        for (MoodLog log : allLogs) {
            boolean matchesQuery = query.isEmpty() ||
                    (log.getEmotion() != null && log.getEmotion().toLowerCase().contains(query)) ||
                    (log.getNotes() != null && log.getNotes().toLowerCase().contains(query));

            boolean matchesChip = selectedEmotion.isEmpty() ||
                    (log.getEmotion() != null && log.getEmotion().equalsIgnoreCase(selectedEmotion));

            if (matchesQuery && matchesChip) {
                filtered.add(log);
            }
        }

        adapter.submit(filtered);
        binding.tvEmptyHistory.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        binding.tvEntryCount.setText(filtered.size() + " ENTRIES");
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
