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
               PatientAdapter.PatientAdapterListener {

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

        setupPostSessionUI();
    }

    private void setupPostSessionUI() {
        patientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        patientAdapter = new PatientAdapter(patientList, this, true);
        patientsRecyclerView.setAdapter(patientAdapter);
    }

    private void enterSession(String sessionCode) {
        inSession = true;
        currentSessionCode = sessionCode;
        setTitle("Stanza: " + sessionCode);
        preSessionContainer.setVisibility(View.GONE);
        patientsRecyclerView.setVisibility(View.VISIBLE);
        invalidateOptionsMenu();
        loadPatients();
    }

    // METODO AGGIORNATO CON LA QUERY CORRETTA
    private void loadPatients() {
        if (currentSessionCode == null || operatorUsername == null) return;
        executor.execute(() -> {
            // Uso il nuovo metodo del DAO per caricare solo i pazienti corretti
            List<Patient> patientsFromDb = db.patientDao().getOperatorPatientsForSession(currentSessionCode, operatorUsername);
            runOnUiThread(() -> {
                patientList.clear();
                patientList.addAll(patientsFromDb);
                patientAdapter.notifyDataSetChanged();
            });
        });
    }

    // ... (tutti gli altri metodi restano invariati) ...
    private void showJoinSessionDialog() { new JoinSessionDialogFragment().show(getSupportFragmentManager(), "JoinSessionDialog"); }
    private void generateAndSaveSession() { String s=UUID.randomUUID().toString().substring(0,8);executor.execute(()->{db.sessionDao().insert(new Session(s));runOnUiThread(()->{Toast.makeText(this,"Sessione "+s+" creata!",Toast.LENGTH_LONG).show();enterSession(s);});});}
    @Override public void onJoinAttempt(String c) {executor.execute(()->{Session s=db.sessionDao().findByCode(c);runOnUiThread(()->{if(s!=null){Toast.makeText(this,"Unito alla sessione "+c,Toast.LENGTH_SHORT).show();enterSession(c);}else{Toast.makeText(this,"Codice sessione non valido.",Toast.LENGTH_SHORT).show();}});});}
    @Override public boolean onCreateOptionsMenu(Menu m) {getMenuInflater().inflate(R.menu.menu_operator,m);MenuItem i=m.findItem(R.id.action_add_patient);if(i!=null){i.setVisible(inSession);}return true;}
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem i) {if(i.getItemId()==R.id.action_add_patient){AddPatientDialogFragment d=AddPatientDialogFragment.newInstance(currentSessionCode,operatorUsername);d.show(getSupportFragmentManager(),"AddPatientDialog");return true;}return super.onOptionsItemSelected(i);}
    @Override public void onPatientAddedOrUpdated() {loadPatients();}
    @Override public void onEditPatient(Patient p) {AddPatientDialogFragment d=AddPatientDialogFragment.newInstance(currentSessionCode,operatorUsername,p);d.show(getSupportFragmentManager(),"EditPatientDialog");}
    @Override public void onDeletePatient(Patient p) {new AlertDialog.Builder(this).setTitle("Conferma Cancellazione").setMessage("Sei sicuro?").setPositiveButton("Sì",(di,wh)->{executor.execute(()->{db.patientDao().delete(p);runOnUiThread(this::loadPatients);});Toast.makeText(this,"Paziente cancellato.",Toast.LENGTH_SHORT).show();}).setNegativeButton("No",null).show();}
    @Override public void onIdentifyPatient(Patient p) {}
}
