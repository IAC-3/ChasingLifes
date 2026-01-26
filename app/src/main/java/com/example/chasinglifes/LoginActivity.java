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

    // English: Initializes the activity, sets up the database, and displays the login fragment.
    // Italiano: Inizializza l'attività, imposta il database e visualizza il frammento di login.
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

    // English: Navigates to the registration fragment.
    // Italiano: Naviga al frammento di registrazione.
    @Override
    public void onNavigateToRegister() {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, new RegisterFragment())
            .addToBackStack(null)
            .commit();
    }

    // English: Navigates back to the login fragment.
    // Italiano: Torna al frammento di login.
    @Override
    public void onNavigateToLogin() {
        getSupportFragmentManager().popBackStack();
    }

    // English: Handles the login attempt, verifying the user's credentials.
    // Italiano: Gestisce il tentativo di login, verificando le credenziali dell'utente.
    @Override
    public void onLoginAttempt(String username, String password) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, getString(R.string.username) + " e " + getString(R.string.password) + " non possono essere vuoti", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            User user = db.userDao().findByUsername(username);
            runOnUiThread(() -> {
                if (user == null || !user.getPassword().equals(password)) {
                    Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                } else {
                    launchNextActivity(user.isOperator(), user.getUsername());
                }
            });
        });
    }

    // English: Handles the registration attempt, creating a new user if the username is not already taken.
    // Italiano: Gestisce il tentativo di registrazione, creando un nuovo utente se il nome utente non è già in uso.
    @Override
    public void onRegisterAttempt(String username, String password, boolean isOperator) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, getString(R.string.username) + " e " + getString(R.string.password) + " non possono essere vuoti", Toast.LENGTH_SHORT).show();
            return;
        }

        User newUser = new User(username, password, isOperator);
        executor.execute(() -> {
            User existingUser = db.userDao().findByUsername(username);
            if (existingUser != null) {
                runOnUiThread(() -> Toast.makeText(this, R.string.user_already_exists, Toast.LENGTH_SHORT).show());
                return;
            }
            
            db.userDao().insert(newUser);
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.registration_successful, Toast.LENGTH_LONG).show();
                onNavigateToLogin();
            });
        });
    }

    // English: Launches the next activity based on the user type (User or Operator).
    // Italiano: Avvia l'attività successiva in base al tipo di utente (Utente o Operatore).
    private void launchNextActivity(boolean isOperator, String username) {
        Intent intent;
        if (isOperator) {
            intent = new Intent(this, OperatorActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        intent.putExtra("USERNAME", username);
        startActivity(intent);
        finish();
    }
}
