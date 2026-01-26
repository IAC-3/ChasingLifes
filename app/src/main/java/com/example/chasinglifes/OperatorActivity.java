package com.example.chasinglifes;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OperatorActivity extends AppCompatActivity
        implements JoinSessionDialogFragment.JoinSessionDialogListener,
        AddPatientDialogFragment.AddPatientDialogListener,
        PatientAdapter.PatientAdapterListener,
        MatchFoundDialogFragment.MatchFoundListener {

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private LinearLayout preSessionContainer;
    private RecyclerView patientsRecyclerView;
    private PatientAdapter patientAdapter;
    private final List<Patient> patientList = new ArrayList<>();

    private boolean inSession = false;
    private String currentSessionCode;
    private String operatorUsername;

    // English: Initializes the activity, sets up the UI, and retrieves the operator's username.
    // Italiano: Inizializza l'attività, imposta l'interfaccia utente e recupera il nome utente dell'operatore.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        operatorUsername = getIntent().getStringExtra("USERNAME");
        setTitle(getString(R.string.title_operator, operatorUsername));

        db = AppDatabase.getDatabase(this);

        preSessionContainer = findViewById(R.id.pre_session_container);
        patientsRecyclerView = findViewById(R.id.patients_recycler_view);

        findViewById(R.id.generate_session_button).setOnClickListener(v -> generateAndSaveSession());
        findViewById(R.id.join_session_button).setOnClickListener(v -> showJoinSessionDialog());
    }

    // English: Sets up the UI for an active session, including the patient list.
    // Italiano: Imposta l'interfaccia utente per una sessione attiva, inclusa la lista dei pazienti.
    private void enterSession(String sessionCode) {
        inSession = true;
        currentSessionCode = sessionCode;
        setTitle(getString(R.string.session_room_title, sessionCode));

        patientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        patientAdapter = new PatientAdapter(patientList, this, true, this);
        patientsRecyclerView.setAdapter(patientAdapter);

        preSessionContainer.setVisibility(View.GONE);
        patientsRecyclerView.setVisibility(View.VISIBLE);
        invalidateOptionsMenu();
        loadPatients();
    }

    // English: Loads the list of patients for the current session from the database.
    // Italiano: Carica la lista dei pazienti per la sessione corrente dal database.
    private void loadPatients() {
        if (currentSessionCode == null) return;
        executor.execute(() -> {
            List<Patient> patientsFromDb = db.patientDao().getOperatorPatientsForSession(currentSessionCode, operatorUsername);
            runOnUiThread(() -> {
                patientList.clear();
                patientList.addAll(patientsFromDb);
                if (patientAdapter != null) {
                    patientAdapter.notifyDataSetChanged();
                }
            });
        });
    }

    // English: Creates the options menu in the toolbar.
    // Italiano: Crea il menu delle opzioni nella barra degli strumenti.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_operator, menu);
        menu.findItem(R.id.action_add_patient).setVisible(inSession);
        return true;
    }

    // English: Handles the selection of items from the options menu.
    // Italiano: Gestisce la selezione degli elementi dal menu delle opzioni.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add_patient) {
            AddPatientDialogFragment.newInstance(currentSessionCode, operatorUsername, null)
                    .show(getSupportFragmentManager(), "AddPatientDialog");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // English: Handles the data collected from the AddPatientDialogFragment, either adding a new patient or finding matches for an existing one.
    // Italiano: Gestisce i dati raccolti dall'AddPatientDialogFragment, aggiungendo un nuovo paziente o cercando corrispondenze per uno esistente.
    @Override
    public void onPatientDataCollected(Patient patientData, boolean isEditMode) {
        if (isEditMode) {
            insertNewPatient(patientData, true);
        } else {
            executor.execute(() -> {
                List<Patient> matches = findMatches(patientData);
                runOnUiThread(() -> {
                    if (matches.isEmpty()) {
                        insertNewPatient(patientData, false);
                    } else {
                        MatchFoundDialogFragment.newInstance(matches, patientData)
                                .show(getSupportFragmentManager(), "MatchFoundDialog");
                    }
                });
            });
        }
    }

    // English: Finds potential matches for a new patient based on name or distinguishing marks.
    // Italiano: Trova potenziali corrispondenze per un nuovo paziente in base al nome o ai segni particolari.
    private List<Patient> findMatches(Patient patientData) {
        String name = patientData.getName() != null ? patientData.getName() : "";
        String surname = patientData.getSurname() != null ? patientData.getSurname() : "";
        String marks = patientData.getDistinguishingMarks() != null ? patientData.getDistinguishingMarks() : "";

        if ("UNIDENTIFIED".equals(patientData.getStatus()) && !marks.isEmpty()) {
            return db.patientDao().findMissingByExactMarks(marks);
        } else if (!name.isEmpty() && !surname.isEmpty()){
            return db.patientDao().findMissingByExactName(name, surname);
        } else {
            return new ArrayList<>();
        }
    }

    // English: Updates an existing patient's data when a match is selected.
    // Italiano: Aggiorna i dati di un paziente esistente quando viene selezionata una corrispondenza.
    @Override
    public void onMatchSelected(Patient existingPatient, Patient newPatientData) {
        existingPatient.setStatus(newPatientData.getStatus());
        existingPatient.setConditions(newPatientData.getConditions());
        existingPatient.setDeceased(newPatientData.isDeceased());
        existingPatient.setOperatorUsername(operatorUsername);
        insertNewPatient(existingPatient, true);
    }

    // English: Inserts a new patient when no match is selected.
    // Italiano: Inserisce un nuovo paziente quando non viene selezionata alcuna corrispondenza.
    @Override
    public void onNoneSelected(Patient newPatientData) {
        insertNewPatient(newPatientData, false);
    }

    // English: Inserts or updates a patient in the database.
    // Italiano: Inserisce o aggiorna un paziente nel database.
    private void insertNewPatient(Patient patient, boolean isUpdate) {
        if (!isUpdate) {
            patient.setSessionCode(currentSessionCode);
            patient.setOperatorUsername(operatorUsername);
        }
        executor.execute(() -> {
            if(isUpdate) db.patientDao().update(patient);
            else db.patientDao().insert(patient);

            runOnUiThread(() -> {
                Toast.makeText(this, isUpdate ? getString(R.string.patient_updated) : getString(R.string.patient_added), Toast.LENGTH_SHORT).show();
                loadPatients();
            });
        });
    }

    // English: Generates and saves a new session code.
    // Italiano: Genera e salva un nuovo codice di sessione.
    private void generateAndSaveSession() {
        String sessionCode = UUID.randomUUID().toString().substring(0, 8);
        executor.execute(() -> {
            db.sessionDao().insert(new Session(sessionCode));
            runOnUiThread(() -> {
                Toast.makeText(this, getString(R.string.session_created, sessionCode), Toast.LENGTH_LONG).show();
                enterSession(sessionCode);
            });
        });
    }

    // English: Shows the dialog to join a session.
    // Italiano: Mostra il dialogo per unirsi a una sessione.
    private void showJoinSessionDialog() {
        new JoinSessionDialogFragment().show(getSupportFragmentManager(), "JoinSessionDialog");
    }

    // English: Handles the attempt to join a session with the given code.
    // Italiano: Gestisce il tentativo di unirsi a una sessione con il codice fornito.
    @Override
    public void onJoinAttempt(String code) {
        executor.execute(() -> {
            Session session = db.sessionDao().findByCode(code);
            runOnUiThread(() -> {
                if (session != null) {
                    Toast.makeText(this, getString(R.string.joined_session, code), Toast.LENGTH_SHORT).show();
                    enterSession(code);
                } else {
                    Toast.makeText(this, R.string.invalid_session_code, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // English: Shows the dialog to edit an existing patient.
    // Italiano: Mostra il dialogo per modificare un paziente esistente.
    @Override
    public void onEditPatient(Patient patient) {
        AddPatientDialogFragment.newInstance(currentSessionCode, operatorUsername, patient).show(getSupportFragmentManager(), "EditPatientDialog");
    }

    // English: Shows a confirmation dialog to delete a patient.
    // Italiano: Mostra un dialogo di conferma per eliminare un paziente.
    @Override
    public void onDeletePatient(Patient patient) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_patient_title)
                .setMessage(R.string.confirm_delete_patient_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> executor.execute(() -> {
                    db.patientDao().delete(patient);
                    runOnUiThread(this::loadPatients);
                }))
                .setNegativeButton(R.string.no, null)
                .show();
    }

    // English: Handles the identification of a patient (not implemented for operator).
    // Italiano: Gestisce l'identificazione di un paziente (non implementato per l'operatore).
    @Override
    public void onIdentifyPatient(Patient patient) {}

    // English: Handles adding a contact to a patient (not implemented for operator).
    // Italiano: Gestisce l'aggiunta di un contatto a un paziente (non implementato per l'operatore).
    @Override
    public void onAddContact(Patient patient) {}

    // English: Shows a dialog with the list of contacts for a patient.
    // Italiano: Mostra un dialogo con l'elenco dei contatti per un paziente.
    @Override
    public void onViewContacts(Patient patient) {
        if (patient.getContacts() == null || patient.getContacts().trim().isEmpty()) {
            Toast.makeText(this, R.string.no_contacts_available, Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.contacts_for, patient.getName() != null ? patient.getName() : getString(R.string.patient_fallback)));
        builder.setMessage(patient.getContacts());
        builder.setPositiveButton(R.string.ok, null);
        builder.show();
    }
}
