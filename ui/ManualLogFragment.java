package com.steadyme.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.steadyme.app.data.FirebaseRepository;
import com.steadyme.app.databinding.FragmentManualLogBinding;
import com.steadyme.app.model.MoodLog;

public class ManualLogFragment extends Fragment {
    private FragmentManualLogBinding binding;
    private String emotion;
    private int score;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManualLogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.btnHappy.setOnClickListener(v -> pick("Happy", 8));
        binding.btnCalm.setOnClickListener(v -> pick("Calm", 7));
        binding.btnElated.setOnClickListener(v -> pick("Elated", 10));
        binding.btnAnxious.setOnClickListener(v -> pick("Anxious", 3));
        binding.btnSad.setOnClickListener(v -> pick("Sad", 2));
        binding.btnTired.setOnClickListener(v -> pick("Tired", 4));
        binding.btnAngry.setOnClickListener(v -> pick("Angry", 2));
        binding.btnOverwhelmed.setOnClickListener(v -> pick("Overwhelmed", 3));
        binding.btnSaveMood.setOnClickListener(v -> save());
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    private void pick(String value, int valueScore) {
        emotion = value;
        score = valueScore;
        binding.tvSelectedEmotion.setText(value + " selected");
        binding.btnSaveMood.setEnabled(true);
    }

    private void save() {
        if (emotion == null) {
            Snackbar.make(binding.getRoot(), "Select an emotion first", Snackbar.LENGTH_SHORT).show();
            return;
        }

        new FirebaseRepository().saveMood(new MoodLog(emotion, String.valueOf(binding.etNotes.getText()).trim(), "manual", score), new FirebaseRepository.SaveCallback() {
            @Override
            public void onSaved() {
                Snackbar.make(binding.getRoot(), "Mood saved", Snackbar.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            }

            @Override
            public void onError(Exception e) {
                Snackbar.make(binding.getRoot(), e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
