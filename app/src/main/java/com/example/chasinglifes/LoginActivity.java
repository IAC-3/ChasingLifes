package com.example.chasinglifes;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity 
    implements LoginFragment.OnLoginFragmentListener, RegisterFragment.OnRegisterFragmentListener {

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = AppDatabase.getDatabase(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();
        }
    }

    @Override
    public void onNavigateToRegister() {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, new RegisterFragment())
            .addToBackStack(null) // Permette di tornare al login con il tasto back
            .commit();
    }

    @Override
    public void onNavigateToLogin() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onLoginAttempt(String username, String password) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Inserisci Username e Password", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            User user = db.userDao().findByUsername(username);
            runOnUiThread(() -> {
                if (user == null || !user.getPassword().equals(password)) {
                    Toast.makeText(this, "Credenziali non valide", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Accesso effettuato!", Toast.LENGTH_SHORT).show();
                    launchNextActivity(user.isOperator());
                }
            });
        });
    }

    @Override
    public void onRegisterAttempt(String username, String password, boolean isOperator) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Username e Password non possono essere vuoti", Toast.LENGTH_SHORT).show();
            return;
        }

        User newUser = new User(username, password, isOperator);
        executor.execute(() -> {
            User existingUser = db.userDao().findByUsername(username);
            if (existingUser != null) {
                runOnUiThread(() -> Toast.makeText(this, "Username già in uso", Toast.LENGTH_SHORT).show());
                return;
            }
            
            db.userDao().insert(newUser);
            runOnUiThread(() -> {
                Toast.makeText(this, "Registrazione completata! Effettua il login.", Toast.LENGTH_LONG).show();
                onNavigateToLogin();
            });
        });
    }

    private void launchNextActivity(boolean isOperator) {
        Intent intent;
        if (isOperator) {
            intent = new Intent(this, OperatorActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
