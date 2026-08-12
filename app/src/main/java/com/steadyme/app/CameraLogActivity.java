package com.steadyme.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.steadyme.app.databinding.ActivityCameraLogBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraLogActivity extends AppCompatActivity {

    // Added TAG for proper logging conventions as taught in Intro to Android Studio
    private static final String TAG = "CAMERA_LOG_ACT";

    private ActivityCameraLogBinding binding;
    private ExecutorService cameraExecutor;
    private volatile boolean faceDetected = false;
    private volatile Float latestSmiling, latestLeftEyeOpen, latestRightEyeOpen;
    private volatile Float latestBrowRatio; // Distance ratio (inner brow to inner eye corner / eye width)
    private String pendingEmotion;
    private int pendingScore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCameraLogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cameraExecutor = Executors.newSingleThreadExecutor();

        binding.btnBackCamera.setOnClickListener(v -> finish());
        binding.btnBackCamera.bringToFront();
        binding.btnScan.setOnClickListener(v -> scan());

        binding.btnRetry.setOnClickListener(v -> showScan());
        binding.btnUseEmotion.setOnClickListener(v -> proceed(pendingEmotion, pendingScore));

        // finish() handles returning to the previous screen safely
        binding.btnSwitchManual.setOnClickListener(v -> finish());

        binding.btnCamHappy.setOnClickListener(v -> proceed("Happy", 4));
        binding.btnCamElated.setOnClickListener(v -> proceed("Elated", 5));
        binding.btnCamCalm.setOnClickListener(v -> proceed("Calm", 3));
        binding.btnCamTired.setOnClickListener(v -> proceed("Tired", 2));
        binding.btnCamSad.setOnClickListener(v -> proceed("Sad", 1));
        binding.btnCamAnxious.setOnClickListener(v -> proceed("Anxious", 2));
        binding.btnCamFrustrated.setOnClickListener(v -> proceed("Frustrated", 2));
        binding.btnCamAngry.setOnClickListener(v -> proceed("Angry", 1));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 5);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 5 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Camera permission is needed for camera logging.", Toast.LENGTH_LONG).show();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.previewCamera.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                FaceDetector detector = FaceDetection.getClient(
                        new FaceDetectorOptions.Builder()
                                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                                .build()
                );

                analysis.setAnalyzer(cameraExecutor, image -> {
                    if (image.getImage() == null) {
                        image.close();
                        return;
                    }
                    InputImage input = InputImage.fromMediaImage(image.getImage(), image.getImageInfo().getRotationDegrees());
                    detector.process(input)
                            .addOnSuccessListener(faces -> {
                                faceDetected = !faces.isEmpty();
                                if (!faces.isEmpty()) {
                                    Face face = faces.get(0);
                                    latestSmiling = face.getSmilingProbability();
                                    latestLeftEyeOpen = face.getLeftEyeOpenProbability();
                                    latestRightEyeOpen = face.getRightEyeOpenProbability();

                                    // Advanced Brow Tracking: Inner brow to inner eye corner ratio
                                    com.google.mlkit.vision.face.FaceContour leftBrow = face.getContour(com.google.mlkit.vision.face.FaceContour.LEFT_EYEBROW_TOP);
                                    com.google.mlkit.vision.face.FaceContour leftEye = face.getContour(com.google.mlkit.vision.face.FaceContour.LEFT_EYE);

                                    if (leftBrow != null && leftEye != null && leftBrow.getPoints().size() >= 5 && leftEye.getPoints().size() >= 9) {
                                        android.graphics.PointF browInner = leftBrow.getPoints().get(0); // Near nose
                                        android.graphics.PointF eyeInner = leftEye.getPoints().get(0);   // Near nose
                                        android.graphics.PointF eyeOuter = leftEye.getPoints().get(8);   // Near temple

                                        // Distance formula: sqrt((x2-x1)^2 + (y2-y1)^2)
                                        float distBrowEye = (float) Math.hypot(browInner.x - eyeInner.x, browInner.y - eyeInner.y);
                                        float eyeWidth = (float) Math.hypot(eyeOuter.x - eyeInner.x, eyeOuter.y - eyeInner.y);

                                        if (eyeWidth > 0) {
                                            latestBrowRatio = distBrowEye / eyeWidth;
                                        }
                                    }
                                }

                                // Safely updating UI from a background thread using View.post()
                                binding.tvCameraStatus.post(() ->
                                        binding.tvCameraStatus.setText(faceDetected ? "Face detected — tap Scan" : "Position your face in the frame")
                                );
                            })
                            .addOnCompleteListener(t -> image.close());
                });

                provider.unbindAll();

                // --- THIS IS THE FIX ---
                // We check if a front camera exists. If not, we fall back to the back camera.
                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                if (!provider.hasCamera(cameraSelector)) {
                    Log.w(TAG, "Front camera not found, falling back to back camera.");
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                }

                provider.bindToLifecycle(this, cameraSelector, preview, analysis);

            } catch (Exception e) {
                // Using Log.e for error output as taught in Intro to Android Studio
                Log.e(TAG, "Error starting camera: ", e);
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void scan() {
        if (!faceDetected) {
            Toast.makeText(this, "No face detected yet — hold still and try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        float smiling = latestSmiling == null ? 0.2f : latestSmiling;
        float leftOpen = latestLeftEyeOpen == null ? 0.9f : latestLeftEyeOpen;
        float rightOpen = latestRightEyeOpen == null ? 0.9f : latestRightEyeOpen;
        float avgEyesOpen = (leftOpen + rightOpen) / 2f;
        float browRatio = latestBrowRatio == null ? 0.50f : latestBrowRatio;

        Log.d(TAG, String.format("Scan Stats: Smile=%.2f, Eyes=%.2f, BrowRatio=%.2f", smiling, avgEyesOpen, browRatio));
        
        String emotion;
        int score;

        // Refined Clinical Emotion Detection Logic using Accurate Mode Data
        if (smiling >= 0.95f) {
            emotion = "Elated";
            score = 5;
        } else if (smiling >= 0.30f) {
            emotion = "Happy";
            score = 4;
        } else if (avgEyesOpen <= 0.30f) {
            emotion = "Tired";
            score = 2;
        } else if (browRatio <= 0.42f && smiling <= 0.15f) {
            // ANGRY: Low brow (furrowed) + No smile
            emotion = "Angry";
            score = 1;
        } else if (browRatio <= 0.42f) {
            // FRUSTRATED: Low brow with some facial movement
            emotion = "Frustrated";
            score = 2;
        } else if (browRatio >= 0.58f && smiling <= 0.15f) {
            // ANXIOUS: High inner brow (raised) + No smile
            emotion = "Anxious";
            score = 2;
        } else if (smiling <= 0.10f || (browRatio >= 0.56f && smiling <= 0.20f)) {
            // SAD: Flat affect or slightly raised brow with no smile
            emotion = "Sad";
            score = 1;
        } else {
            // CALM: Neutral baseline
            emotion = "Calm";
            score = 3;
        }

        pendingEmotion = emotion;
        pendingScore = score;
        binding.tvDetected.setText("Detected: " + emotion);
        showResult();
    }

    private void showResult() {
        binding.groupScan.setVisibility(android.view.View.GONE);
        binding.groupResult.setVisibility(android.view.View.VISIBLE);
        binding.tvCameraStatus.setVisibility(android.view.View.GONE);
    }

    private void showScan() {
        binding.groupResult.setVisibility(android.view.View.GONE);
        binding.groupScan.setVisibility(android.view.View.VISIBLE);
        binding.tvCameraStatus.setVisibility(android.view.View.VISIBLE);
    }

    private void proceed(String emotion, int score) {
        Intent intent = new Intent(this, MoodIntensityActivity.class);
        intent.putExtra(MoodIntensityActivity.EXTRA_EMOTION, emotion);
        intent.putExtra(MoodIntensityActivity.EXTRA_SOURCE, "camera");
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        cameraExecutor.shutdown();
        super.onDestroy();
    }
}
