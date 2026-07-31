package com.steadyme.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.ListenerRegistration;
import com.steadyme.app.data.FirebaseRepository;
import com.steadyme.app.databinding.FragmentHistoryBinding;
import com.steadyme.app.model.MoodLog;

import java.util.List;

public class HistoryFragment extends Fragment {
    private FragmentHistoryBinding binding;
    private ListenerRegistration registration;
    private final MoodLogAdapter adapter = new MoodLogAdapter();

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

        registration = new FirebaseRepository().observeMoodLogs(new FirebaseRepository.LogsCallback() {
            @Override
            public void onLoaded(List<MoodLog> logs) {
                adapter.submit(logs);
                binding.tvEmptyHistory.setVisibility(logs.isEmpty() ? View.VISIBLE : View.GONE);
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
}
