package com.example.chasinglifes;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity
        implements JoinSessionDialogFragment.JoinSessionDialogListener,
        PatientAdapter.PatientAdapterListener,
        AddMissingPersonDialogFragment.AddMissingPersonListener {

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private LinearLayout preSessionContainer;
    private RecyclerView patientsRecyclerView;
    private BottomNavigationView bottomNavigation;

    private PatientAdapter patientAdapter;
    private final List<Patient> allPatients = new ArrayList<>();
    private final List<Patient> displayedPatients = new ArrayList<>();

    private String currentSessionCode;
    private String currentUserUsername;
    private Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentUserUsername = getIntent().getStringExtra("USERNAME");
        setTitle("Utente: " + currentUserUsername);

        db = AppDatabase.getDatabase(this);

        preSessionContainer = findViewById(R.id.pre_session_container);
        patientsRecyclerView = findViewById(R.id.patients_recycler_view);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        findViewById(R.id.join_session_button).setOnClickListener(v -> {
            new JoinSessionDialogFragment().show(getSupportFragmentManager(), "JoinSessionDialog");
        });

        setupPostSessionUI();
    }

    @Override
    public void onJoinAttempt(String code) {
        executor.execute(() -> {
            Session session = db.sessionDao().findByCode(code);
            runOnUiThread(() -> {
                if (session != null) {
                    Toast.makeText(this, "Unito alla sessione " + code, Toast.LENGTH_SHORT).show();
                    enterSession(code);
                } else {
                    Toast.makeText(this, "Codice sessione non valido.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void enterSession(String sessionCode) {
        currentSessionCode = sessionCode;
        setTitle("Stanza: " + sessionCode);
        preSessionContainer.setVisibility(View.GONE);
        patientsRecyclerView.setVisibility(View.VISIBLE);
        bottomNavigation.setVisibility(View.VISIBLE);
        invalidateOptionsMenu();
        loadPatients();
    }

    private void setupPostSessionUI() {
        patientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        patientAdapter = new PatientAdapter(displayedPatients, this, false);
        patientsRecyclerView.setAdapter(patientAdapter);

        bottomNavigation.setOnItemSelectedListener(item -> {
            updateMenuVisibility(item.getItemId());
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_identified) filterPatients(FilterType.IDENTIFIED);
            else if (itemId == R.id.navigation_unidentified) filterPatients(FilterType.UNIDENTIFIED);
            else if (itemId == R.id.navigation_missing) filterPatients(FilterType.MISSING);
            else if (itemId == R.id.navigation_deceased) filterPatients(FilterType.DECEASED);
            return true;
        });
    }

    private void loadPatients() {
        if (currentSessionCode == null) return;
        executor.execute(() -> {
            List<Patient> patientsFromDb = db.patientDao().getPatientsBySession(currentSessionCode);
            runOnUiThread(() -> {
                allPatients.clear();
                allPatients.addAll(patientsFromDb);
                filterPatients(FilterType.UNIDENTIFIED);
            });
        });
    }

    private void filterPatients(FilterType filter) {
        displayedPatients.clear();
        switch (filter) {
            case IDENTIFIED:
                displayedPatients.addAll(allPatients.stream().filter(p -> "IDENTIFIED".equals(p.getStatus()) && !p.isDeceased()).collect(Collectors.toList()));
                break;
            case UNIDENTIFIED:
                displayedPatients.addAll(allPatients.stream().filter(p -> "UNIDENTIFIED".equals(p.getStatus())).collect(Collectors.toList()));
                break;
            case DECEASED:
                displayedPatients.addAll(allPatients.stream().filter(p -> p.isDeceased() && !"UNIDENTIFIED".equals(p.getStatus())).collect(Collectors.toList()));
                break;
            case MISSING:
                displayedPatients.addAll(allPatients.stream().filter(p -> "MISSING".equals(p.getStatus())).collect(Collectors.toList()));
                break;
        }
        patientAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.optionsMenu = menu;
        menu.findItem(R.id.action_add_missing_person).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_add_missing_person) {
            new AddMissingPersonDialogFragment().show(getSupportFragmentManager(), "AddMissingPersonDialog");
            return true;
        } else if (itemId == R.id.action_delete_account) {
            showDeleteConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Conferma Cancellazione")
                .setMessage("Sei sicuro di voler cancellare il tuo account? Questa azione è irreversibile.")
                .setPositiveButton("Sì", (dialog, which) -> deleteAccount())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteAccount() {
        executor.execute(() -> {
            User user = db.userDao().findByUsername(currentUserUsername);
            if (user != null) {
                db.userDao().delete(user);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Account cancellato con successo.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }


    private void updateMenuVisibility(int selectedItemId) {
        if (optionsMenu != null) {
            MenuItem addItem = optionsMenu.findItem(R.id.action_add_missing_person);
            addItem.setVisible(selectedItemId == R.id.navigation_missing);
        }
    }

    @Override
    public void onMissingPersonAdded(String name, String surname, String marks) {
        Patient missingPerson = new Patient();
        missingPerson.setSessionCode(currentSessionCode);
        missingPerson.setName(name);
        missingPerson.setSurname(surname);
        missingPerson.setDistinguishingMarks(marks);
        missingPerson.setStatus("MISSING");
        missingPerson.setDeceased(false);
        missingPerson.setReporterUsername(currentUserUsername);

        executor.execute(() -> {
            db.patientDao().insert(missingPerson);
            runOnUiThread(this::loadPatients);
        });

        Toast.makeText(this, "Persona scomparsa aggiunta.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onIdentifyPatient(Patient patient) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Identifica Paziente");
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        final EditText nameInput = new EditText(this);
        nameInput.setHint("Nome");
        layout.addView(nameInput);

        final EditText surnameInput = new EditText(this);
        surnameInput.setHint("Cognome");
        layout.addView(surnameInput);

        builder.setView(layout);

        builder.setPositiveButton("Identifica", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String surname = surnameInput.getText().toString().trim();
            if (!name.isEmpty() && !surname.isEmpty()) {
                patient.setName(name);
                patient.setSurname(surname);
                if ("UNIDENTIFIED".equals(patient.getStatus())) {
                    patient.setStatus("IDENTIFIED");
                }
                executor.execute(() -> {
                    db.patientDao().update(patient);
                    runOnUiThread(this::loadPatients);
                });
            }
        });
        builder.setNegativeButton("Annulla", null);
        builder.show();
    }

    @Override
    public void onAddContact(Patient patient) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Aggiungi Contatto");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText roleInput = new EditText(this);
        roleInput.setHint("Ruolo (es: Moglie)");
        layout.addView(roleInput);

        final EditText nameInput = new EditText(this);
        nameInput.setHint("Nome Cognome (es: Gina Rossi)");
        layout.addView(nameInput);

        final EditText contactInput = new EditText(this);
        contactInput.setHint("Contatto (es: 3456789001)");
        contactInput.setInputType(InputType.TYPE_CLASS_PHONE);
        layout.addView(contactInput);

        builder.setView(layout);

        builder.setPositiveButton("Salva", (dialog, which) -> {
            String role = roleInput.getText().toString().trim();
            String name = nameInput.getText().toString().trim();
            String contact = contactInput.getText().toString().trim();

            if (!role.isEmpty() && !name.isEmpty() && !contact.isEmpty()) {
                String newContactInfo = "Ruolo: " + role + "\nNome Cognome: " + name + "\nContatto: " + contact;
                String existingContacts = patient.getContacts() != null ? patient.getContacts() : "";
                String newContacts = existingContacts.isEmpty() ? newContactInfo : existingContacts + "\n\n" + newContactInfo;
                patient.setContacts(newContacts);

                executor.execute(() -> {
                    db.patientDao().update(patient);
                    runOnUiThread(this::loadPatients);
                });
                Toast.makeText(this, "Contatto aggiunto.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Annulla", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onViewContacts(Patient patient) {
        if (patient.getContacts() == null || patient.getContacts().trim().isEmpty()) {
            Toast.makeText(this, "Nessun contatto disponibile.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Contatti per " + (patient.getName() != null ? patient.getName() : "paziente"));
        builder.setMessage(patient.getContacts());
        builder.setPositiveButton("OK", null);
        builder.show();
    }


    @Override public void onEditPatient(Patient p) {}
    @Override public void onDeletePatient(Patient p) {}

    private enum FilterType { IDENTIFIED, UNIDENTIFIED, MISSING, DECEASED }
}
