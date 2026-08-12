package com.steadyme.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.steadyme.app.data.FirebaseRepository;
import com.steadyme.app.databinding.ActivityAddNoteBinding;
import com.steadyme.app.model.MoodLog;

public class AddNoteActivity extends AppCompatActivity {
    public static final String EXTRA_EMOTION = "extra_emotion";
    public static final String EXTRA_SCORE = "extra_score";
    public static final String EXTRA_SOURCE = "extra_source";

    private ActivityAddNoteBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBackNote.setOnClickListener(v -> finish());
        binding.btnSaveNote.setOnClickListener(v -> save());
    }

    private void save() {
        String emotion = getIntent().getStringExtra(EXTRA_EMOTION);
        int score = getIntent().getIntExtra(EXTRA_SCORE, 5);
        String source = getIntent().getStringExtra(EXTRA_SOURCE);
        String notes = binding.etNote.getText() == null ? "" : binding.etNote.getText().toString().trim();

        binding.btnSaveNote.setEnabled(false);

        new FirebaseRepository().saveMood(new MoodLog(emotion, notes, source, score), new FirebaseRepository.SaveCallback() {
            @Override
            public void onSaved() {
                startActivity(new Intent(AddNoteActivity.this, EntrySavedActivity.class));
                finish();
            }

            @Override
            public void onError(Exception e) {
                binding.btnSaveNote.setEnabled(true);
                Toast.makeText(AddNoteActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
