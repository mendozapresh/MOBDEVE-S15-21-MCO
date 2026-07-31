package com.steadyme.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.*;
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
 private String pendingEmotion;
 private int pendingScore;

 @Override
 public void onCreate(Bundle b) {
  super.onCreate(b);
  binding = ActivityCameraLogBinding.inflate(getLayoutInflater());
  setContentView(binding.getRoot());

  cameraExecutor = Executors.newSingleThreadExecutor();

  binding.btnCloseCamera.setOnClickListener(v -> finish());
  binding.btnScan.setOnClickListener(v -> scan());
  binding.btnRetry.setOnClickListener(v -> showScan());
  binding.btnUseEmotion.setOnClickListener(v -> proceed(pendingEmotion, pendingScore));

  // finish() handles returning to the previous screen safely
  binding.btnSwitchManual.setOnClickListener(v -> finish());

  binding.btnCamHappy.setOnClickListener(v -> proceed("Happy", 8));
  binding.btnCamElated.setOnClickListener(v -> proceed("Elated", 10));
  binding.btnCamCalm.setOnClickListener(v -> proceed("Calm", 7));
  binding.btnCamTired.setOnClickListener(v -> proceed("Tired", 4));
  binding.btnCamSad.setOnClickListener(v -> proceed("Sad", 2));
  binding.btnCamAnxious.setOnClickListener(v -> proceed("Anxious", 3));
  binding.btnCamFrustrated.setOnClickListener(v -> proceed("Frustrated", 3));
  binding.btnCamAngry.setOnClickListener(v -> proceed("Angry", 2));

  if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
   startCamera();
  } else {
   requestPermissions(new String[]{Manifest.permission.CAMERA}, 5);
  }
 }

 @Override
 public void onRequestPermissionsResult(int r, @NonNull String[] p, @NonNull int[] g) {
  super.onRequestPermissionsResult(r, p, g);
  if (r == 5 && g.length > 0 && g[0] == PackageManager.PERMISSION_GRANTED) {
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
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
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

  float smiling = latestSmiling == null ? 0.5f : latestSmiling;
  float leftOpen = latestLeftEyeOpen == null ? 1f : latestLeftEyeOpen;
  float rightOpen = latestRightEyeOpen == null ? 1f : latestRightEyeOpen;
  float avgEyesOpen = (leftOpen + rightOpen) / 2f;
  String emotion;

  int score;
  int confidence;

  if (smiling >= 0.6f) {
   emotion = "Happy";
   score = 8;
   confidence = Math.round(smiling * 100);
  } else if (avgEyesOpen <= 0.4f) {
   emotion = "Tired";
   score = 4;
   confidence = Math.round((1 - avgEyesOpen) * 100);
  } else if (smiling <= 0.15f) {
   emotion = "Sad";
   score = 2;
   confidence = Math.round((1 - smiling) * 100);
  } else {
   emotion = "Calm";
   score = 7;
   confidence = Math.round((1 - Math.abs(smiling - 0.5f) * 2) * 100);
  }

  pendingEmotion = emotion;
  pendingScore = score;
  binding.tvDetected.setText("Detected: " + emotion + " (" + confidence + "%)");
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
  Intent intent = new Intent(this, AddNoteActivity.class);
  intent.putExtra(AddNoteActivity.EXTRA_EMOTION, emotion);
  intent.putExtra(AddNoteActivity.EXTRA_SCORE, score);
  intent.putExtra(AddNoteActivity.EXTRA_SOURCE, "camera");
  startActivity(intent);
 }

 @Override
 protected void onDestroy() {
  cameraExecutor.shutdown();
  super.onDestroy();
 }
}
