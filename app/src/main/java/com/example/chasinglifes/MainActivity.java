package com.example.chasinglifes;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chasinglifes.databinding.ActivityMainBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements JoinSessionDialogFragment.JoinSessionDialogListener {

    private ActivityMainBinding binding;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        setTitle("Utente");

        db = AppDatabase.getDatabase(this);

        Button joinSessionButton = findViewById(R.id.join_session_button);
        joinSessionButton.setOnClickListener(v -> {
            JoinSessionDialogFragment dialog = new JoinSessionDialogFragment();
            dialog.show(getSupportFragmentManager(), "JoinSessionDialogFragment");
        });
    }

    @Override
    public void onJoinAttempt(String code) {
        executor.execute(() -> {
            Session session = db.sessionDao().findByCode(code);
            runOnUiThread(() -> {
                if (session != null) {
                    // Per ora, nascondiamo il pulsante e mostriamo un messaggio.
                    // In futuro qui mostreremo la lista dei pazienti.
                    findViewById(R.id.join_session_button).setVisibility(View.GONE);
                    setTitle("Stanza: " + code);
                    Toast.makeText(this, "Unito alla sessione " + code, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Codice sessione non valido.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
