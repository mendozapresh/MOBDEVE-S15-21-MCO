package com.steadyme.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.steadyme.app.MoodIntensityActivity;
import com.steadyme.app.R;
import com.steadyme.app.databinding.FragmentManualLogBinding;

public class ManualLogFragment extends Fragment {
    private FragmentManualLogBinding binding;
    private String emotion;
    private int score;
    private View selectedCell;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManualLogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.btnBackManual.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        binding.btnHappy.setOnClickListener(v -> pick(binding.btnHappy, "Happy", 4));
        binding.btnElated.setOnClickListener(v -> pick(binding.btnElated, "Elated", 5));
        binding.btnCalm.setOnClickListener(v -> pick(binding.btnCalm, "Calm", 3));
        binding.btnTired.setOnClickListener(v -> pick(binding.btnTired, "Tired", 2));
        binding.btnSad.setOnClickListener(v -> pick(binding.btnSad, "Sad", 1));
        binding.btnAnxious.setOnClickListener(v -> pick(binding.btnAnxious, "Anxious", 2));
        binding.btnFrustrated.setOnClickListener(v -> pick(binding.btnFrustrated, "Frustrated", 2));
        binding.btnAngry.setOnClickListener(v -> pick(binding.btnAngry, "Angry", 1));

        binding.btnContinue.setOnClickListener(v -> continueToNote());
    }

    @Override
    public void onDestroyView() {
        binding = null;
        selectedCell = null;
        super.onDestroyView();
    }

    private void pick(View cell, String value, int valueScore) {
        emotion = value;
        score = valueScore;

        if (selectedCell != null) {
            selectedCell.setBackgroundResource(android.R.color.transparent);
        }
        cell.setBackgroundResource(R.drawable.bg_mood_cell_selected);
        cell.getBackground().setTint(MoodPalette.palestColor(requireContext(), value));
        selectedCell = cell;

        binding.btnContinue.setEnabled(true);
    }


    private void continueToNote() {
        if (emotion == null) {
            return;
        }
        Intent intent = new Intent(requireContext(), MoodIntensityActivity.class);
        intent.putExtra(MoodIntensityActivity.EXTRA_EMOTION, emotion);
        intent.putExtra(MoodIntensityActivity.EXTRA_SOURCE, "manual");
        startActivity(intent);
    }
}
