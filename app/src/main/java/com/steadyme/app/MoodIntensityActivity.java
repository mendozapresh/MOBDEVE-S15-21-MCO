package com.steadyme.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.steadyme.app.databinding.ActivityMoodIntensityBinding;
import com.steadyme.app.ui.MoodPalette;

public class MoodIntensityActivity extends AppCompatActivity {
    public static final String EXTRA_EMOTION = "extra_emotion";
    public static final String EXTRA_SOURCE = "extra_source";

    private ActivityMoodIntensityBinding binding;
    private String emotion;
    private String source;
    private int score = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMoodIntensityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        emotion = getIntent().getStringExtra(EXTRA_EMOTION);
        source = getIntent().getStringExtra(EXTRA_SOURCE);

        if (emotion == null) emotion = "Happy";

        binding.tvIntensityQuestion.setText("How " + emotion.toLowerCase() + " are you?");
        binding.ivIntensityEmoji.setImageResource(MoodPalette.iconRes(emotion));
        binding.ivIntensityEmoji.getBackground().setTint(MoodPalette.paleColor(this, emotion));

        binding.sliderIntensity.addOnChangeListener((slider, value, fromUser) -> {
            score = (int) value;
            binding.tvScoreValue.setText(score + " / 5");
        });

        binding.btnBackIntensity.setOnClickListener(v -> finish());
        binding.btnNextIntensity.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReasonsActivity.class);
            intent.putExtra(ReasonsActivity.EXTRA_EMOTION, emotion);
            intent.putExtra(ReasonsActivity.EXTRA_SCORE, score);
            intent.putExtra(ReasonsActivity.EXTRA_SOURCE, source);
            startActivity(intent);
        });
    }
}
