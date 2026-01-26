package com.example.chasinglifes;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    // English: Initializes the activity, sets up the language selection, and handles the continue button.
    // Italiano: Inizializza l'attività, imposta la selezione della lingua e gestisce il pulsante per continuare.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView flagIt = findViewById(R.id.flag_it);
        ImageView flagUs = findViewById(R.id.flag_us);

        updateFlagSelection();

        flagIt.setOnClickListener(v -> {
            if (!getCurrentLocale().getLanguage().equals("it")) {
                setLocale("it");
            }
        });

        flagUs.setOnClickListener(v -> {
            if (!getCurrentLocale().getLanguage().equals("en")) {
                setLocale("en");
            }
        });

        findViewById(R.id.continue_button).setOnClickListener(v -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        });
    }

    // English: Sets the application's locale to the selected language.
    // Italiano: Imposta la localizzazione dell'applicazione sulla lingua selezionata.
    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        recreate();
    }

    // English: Updates the visual selection of the language flags.
    // Italiano: Aggiorna la selezione visiva delle bandiere della lingua.
    private void updateFlagSelection() {
        ImageView flagIt = findViewById(R.id.flag_it);
        ImageView flagUs = findViewById(R.id.flag_us);
        String currentLanguage = getCurrentLocale().getLanguage();

        if ("it".equals(currentLanguage)) {
            flagIt.setAlpha(1.0f);
            flagUs.setAlpha(0.5f);
        } else {
            flagUs.setAlpha(1.0f);
            flagIt.setAlpha(0.5f);
        }
    }

    // English: Returns the current locale of the application.
    // Italiano: Restituisce la localizzazione corrente dell'applicazione.
    private Locale getCurrentLocale() {
        return getResources().getConfiguration().getLocales().get(0);
    }
}
