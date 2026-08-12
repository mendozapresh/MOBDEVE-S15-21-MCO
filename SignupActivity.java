package com.steadyme.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.steadyme.app.data.FirebaseRepository;
import com.steadyme.app.databinding.ActivitySignupBinding;

public class SignupActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;
    private FirebaseAuth auth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        binding.btnCreateAccount.setOnClickListener(v -> signup());
        binding.tvLogin.setOnClickListener(v -> finish());
    }

    private void signup() {
        String name = String.valueOf(binding.etName.getText()).trim();
        String email = String.valueOf(binding.etEmail.getText()).trim();
        String pass = String.valueOf(binding.etPassword.getText());

        if (name.isEmpty()) {
            binding.tilName.setError("Name is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Enter a valid email address");
            return;
        }

        if (pass.length() < 6) {
            binding.tilPassword.setError("Password must be at least 6 characters");
            return;
        }

        binding.progressSignup.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(x -> {
                    new FirebaseRepository().createProfile(name);
                    startActivity(new Intent(this, MainActivity.class));
                    finishAffinity();
                })
                .addOnFailureListener(e -> {
                    binding.progressSignup.setVisibility(View.GONE);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
