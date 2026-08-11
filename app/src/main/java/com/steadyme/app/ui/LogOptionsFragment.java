package com.steadyme.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.steadyme.app.CameraLogActivity;
import com.steadyme.app.R;
import com.steadyme.app.databinding.FragmentLogOptionsBinding;

public class LogOptionsFragment extends Fragment {
    private FragmentLogOptionsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLogOptionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.cardManual.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new ManualLogFragment())
                .addToBackStack(null)
                .commit());

        binding.cardCamera.setOnClickListener(v -> startActivity(new Intent(requireContext(), CameraLogActivity.class)));
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
