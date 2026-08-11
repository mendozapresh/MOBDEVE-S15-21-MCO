package com.steadyme.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.steadyme.app.data.FirebaseRepository;
import com.steadyme.app.databinding.ActivityAddNoteBinding;
import com.steadyme.app.model.MoodLog;

import java.util.List;

public class AddNoteActivity extends AppCompatActivity {
    public static final String EXTRA_EMOTION = "extra_emotion";
    public static final String EXTRA_SCORE = "extra_score";
    public static final String EXTRA_SOURCE = "extra_source";
    public static final String EXTRA_REASONS = "extra_reasons";

    private ActivityAddNoteBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBackNote.setOnClickListener(v -> finish());
        binding.btnBackNote.bringToFront();
        binding.btnSaveNote.setOnClickListener(v -> save());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void save() {
        String emotion = getIntent().getStringExtra(EXTRA_EMOTION);
        int score = getIntent().getIntExtra(EXTRA_SCORE, 3);
        String source = getIntent().getStringExtra(EXTRA_SOURCE);
        List<String> reasons = getIntent().getStringArrayListExtra(EXTRA_REASONS);
        String notes = binding.etNote.getText() == null ? "" : binding.etNote.getText().toString().trim();

        if (emotion == null || emotion.isEmpty()) {
            Toast.makeText(this, "Emotion data missing. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.btnSaveNote.setEnabled(false);

        try {
            if (score <= 2) {
                NotificationHelper.showNotification(this, "We're here for you", "You logged a low mood. Remember to reach out if you need support.");
            }

            MoodLog log = new MoodLog(emotion, notes, source, score, reasons);
            new FirebaseRepository().saveMood(log, new FirebaseRepository.SaveCallback() {
                @Override
                public void onSaved() {
                }

                @Override
                public void onError(Exception e) {
                }
            });

            Intent intent = new Intent(AddNoteActivity.this, EntrySavedActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            binding.btnSaveNote.setEnabled(true);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
