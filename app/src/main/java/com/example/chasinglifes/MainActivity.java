package com.example.chasinglifes;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chasinglifes.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            String message = "";
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_identified) {
                message = "Pazienti identificati";
            } else if (itemId == R.id.navigation_unidentified) {
                message = "Pazienti non identificati";
            } else if (itemId == R.id.navigation_missing) {
                message = "Dispersi";
            } else if (itemId == R.id.navigation_deceased) {
                message = "Deceduti";
            }
            Snackbar.make(binding.getRoot(), message + " selected", Snackbar.LENGTH_SHORT).show();
            return true;
        });
    }
}