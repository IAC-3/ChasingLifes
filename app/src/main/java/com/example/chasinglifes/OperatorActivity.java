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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        operatorUsername = getIntent().getStringExtra("USERNAME");
        setTitle("Operatore: " + operatorUsername);

        db = AppDatabase.getDatabase(this);

        preSessionContainer = findViewById(R.id.pre_session_container);
        patientsRecyclerView = findViewById(R.id.patients_recycler_view);

        findViewById(R.id.generate_session_button).setOnClickListener(v -> generateAndSaveSession());
        findViewById(R.id.join_session_button).setOnClickListener(v -> showJoinSessionDialog());
    }

    private void enterSession(String sessionCode) {
        inSession = true;
        currentSessionCode = sessionCode;
        setTitle("Stanza: " + sessionCode);

        patientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        patientAdapter = new PatientAdapter(patientList, this, true);
        patientsRecyclerView.setAdapter(patientAdapter);

        preSessionContainer.setVisibility(View.GONE);
        patientsRecyclerView.setVisibility(View.VISIBLE);
        invalidateOptionsMenu();
        loadPatients();
    }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_operator, menu);
        menu.findItem(R.id.action_add_patient).setVisible(inSession);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add_patient) {
            AddPatientDialogFragment.newInstance(currentSessionCode, operatorUsername, null)
                    .show(getSupportFragmentManager(), "AddPatientDialog");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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

    @Override
    public void onMatchSelected(Patient existingPatient, Patient newPatientData) {
        existingPatient.setStatus(newPatientData.getStatus());
        existingPatient.setConditions(newPatientData.getConditions());
        existingPatient.setDeceased(newPatientData.isDeceased());
        existingPatient.setOperatorUsername(operatorUsername);
        insertNewPatient(existingPatient, true); // Esegue un UPDATE
    }

    @Override
    public void onNoneSelected(Patient newPatientData) {
        insertNewPatient(newPatientData, false); // Esegue un INSERT
    }

    private void insertNewPatient(Patient patient, boolean isUpdate) {
        if (!isUpdate) {
            patient.setSessionCode(currentSessionCode);
            patient.setOperatorUsername(operatorUsername);
        }
        executor.execute(() -> {
            if(isUpdate) db.patientDao().update(patient);
            else db.patientDao().insert(patient);

            runOnUiThread(() -> {
                Toast.makeText(this, isUpdate ? "Paziente aggiornato!" : "Paziente aggiunto!", Toast.LENGTH_SHORT).show();
                loadPatients();
            });
        });
    }

    private void generateAndSaveSession() {
        String sessionCode = UUID.randomUUID().toString().substring(0, 8);
        executor.execute(() -> {
            db.sessionDao().insert(new Session(sessionCode));
            runOnUiThread(() -> {
                Toast.makeText(this, "Sessione " + sessionCode + " creata!", Toast.LENGTH_LONG).show();
                enterSession(sessionCode);
            });
        });
    }

    private void showJoinSessionDialog() {
        new JoinSessionDialogFragment().show(getSupportFragmentManager(), "JoinSessionDialog");
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

    @Override
    public void onEditPatient(Patient patient) {
        AddPatientDialogFragment.newInstance(currentSessionCode, operatorUsername, patient).show(getSupportFragmentManager(), "EditPatientDialog");
    }

    @Override
    public void onDeletePatient(Patient patient) {
        new AlertDialog.Builder(this)
                .setTitle("Conferma Cancellazione")
                .setMessage("Sei sicuro?")
                .setPositiveButton("Sì", (dialog, which) -> executor.execute(() -> {
                    db.patientDao().delete(patient);
                    runOnUiThread(this::loadPatients);
                }))
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onIdentifyPatient(Patient patient) {}

    @Override
    public void onAddContact(Patient patient) {}

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
}
