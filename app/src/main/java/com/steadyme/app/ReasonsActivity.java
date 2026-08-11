package com.steadyme.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.Chip;
import com.steadyme.app.databinding.ActivityReasonsBinding;
import java.util.ArrayList;

public class ReasonsActivity extends AppCompatActivity {
    public static final String EXTRA_EMOTION = "extra_emotion";
    public static final String EXTRA_SCORE = "extra_score";
    public static final String EXTRA_SOURCE = "extra_source";
    public static final String EXTRA_REASONS = "extra_reasons";

    private ActivityReasonsBinding binding;
    private String emotion;
    private String source;
    private int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReasonsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        emotion = getIntent().getStringExtra(EXTRA_EMOTION);
        source = getIntent().getStringExtra(EXTRA_SOURCE);
        score = getIntent().getIntExtra(EXTRA_SCORE, 3);

        binding.btnBackReasons.setOnClickListener(v -> finish());
        
        setupReasonsFilter();

        binding.btnNextReasons.setOnClickListener(v -> {
            ArrayList<String> selectedReasons = new ArrayList<>();
            for (int i = 0; i < binding.chipGroupReasons.getChildCount(); i++) {
                View child = binding.chipGroupReasons.getChildAt(i);
                if (child instanceof Chip) {
                    Chip chip = (Chip) child;
                    if (chip.isChecked()) {
                        selectedReasons.add(chip.getText().toString());
                    }
                }
            }

            Intent intent = new Intent(this, AddNoteActivity.class);
            intent.putExtra(AddNoteActivity.EXTRA_EMOTION, emotion);
            intent.putExtra(AddNoteActivity.EXTRA_SCORE, score);
            intent.putExtra(AddNoteActivity.EXTRA_SOURCE, source);
            intent.putStringArrayListExtra(AddNoteActivity.EXTRA_REASONS, selectedReasons);
            startActivity(intent);
        });
    }

    private void setupReasonsFilter() {
        boolean isPositive = "Happy".equals(emotion) || "Elated".equals(emotion) || "Calm".equals(emotion);

        for (int i = 0; i < binding.chipGroupReasons.getChildCount(); i++) {
            View child = binding.chipGroupReasons.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                String text = chip.getText().toString();

                boolean shouldShow = true;

                if (isPositive) {
                    // Hide purely negative reasons
                    if (text.equals("Lack of sleep") || text.equals("Social withdrawal") || 
                        text.equals("Low motivation") || text.equals("Ruminating thoughts") || 
                        text.equals("Financial stress") || text.equals("Missed medication")) {
                        shouldShow = false;
                    }
                } else {
                    // Hide purely positive reasons for negative moods
                    if (text.equals("Productive day") || text.equals("Good social time") || 
                        text.equals("Personal achievement") || text.equals("Restful sleep") ||
                        text.equals("Meaningful hobby") || text.equals("Outdoor time")) {
                        shouldShow = false;
                    }
                }

                chip.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
            }
        }
    }
}
