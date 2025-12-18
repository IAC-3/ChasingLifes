package com.example.chasinglifes;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Implementa l'interfaccia del dialogo
public class OperatorActivity extends AppCompatActivity implements JoinSessionDialogFragment.JoinSessionDialogListener {

    private Toolbar toolbar;
    private Button generateRoomButton;
    private Button joinSessionButton;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Pannello Operatore");

        generateRoomButton = findViewById(R.id.generate_room_button);
        joinSessionButton = findViewById(R.id.join_session_button);

        db = AppDatabase.getDatabase(this);

        generateRoomButton.setOnClickListener(v -> generateAndSaveSession());
        joinSessionButton.setOnClickListener(v -> showJoinSessionDialog());
    }

    private void showJoinSessionDialog() {
        JoinSessionDialogFragment dialog = new JoinSessionDialogFragment();
        dialog.show(getSupportFragmentManager(), "JoinSessionDialogFragment");
    }

    private void generateAndSaveSession() {
        String sessionCode = UUID.randomUUID().toString().substring(0, 8);
        Session newSession = new Session(sessionCode);

        executor.execute(() -> {
            db.sessionDao().insert(newSession);
            runOnUiThread(() -> {
                setTitle("Stanza: " + sessionCode);
                generateRoomButton.setVisibility(View.GONE);
                joinSessionButton.setVisibility(View.GONE); // Nasconde anche il join button
                Toast.makeText(OperatorActivity.this, "Sessione " + sessionCode + " creata!", Toast.LENGTH_LONG).show();
            });
        });
    }

    // Metodo dell'interfaccia che riceve il codice dal dialogo
    @Override
    public void onJoinAttempt(String code) {
        executor.execute(() -> {
            Session session = db.sessionDao().findByCode(code);
            runOnUiThread(() -> {
                if (session != null) {
                    setTitle("Stanza: " + code);
                    generateRoomButton.setVisibility(View.GONE);
                    joinSessionButton.setVisibility(View.GONE);
                    Toast.makeText(this, "Unito alla sessione " + code, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Codice sessione non valido.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
