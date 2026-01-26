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
import androidx.appcompat.widget.SearchView;
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
    private FilterType currentFilter = FilterType.UNIDENTIFIED;
    private SearchView searchView;

    // English: Initializes the activity, sets up the UI, and retrieves the user's username.
    // Italiano: Inizializza l'attività, imposta l'interfaccia utente e recupera il nome utente dell'utente.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentUserUsername = getIntent().getStringExtra("USERNAME");
        setTitle(getString(R.string.title_user, currentUserUsername));

        db = AppDatabase.getDatabase(this);

        preSessionContainer = findViewById(R.id.pre_session_container);
        patientsRecyclerView = findViewById(R.id.patients_recycler_view);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        findViewById(R.id.join_session_button).setOnClickListener(v -> {
            new JoinSessionDialogFragment().show(getSupportFragmentManager(), "JoinSessionDialog");
        });

        setupPostSessionUI();
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

    // English: Sets up the UI for an active session, including the patient list.
    // Italiano: Imposta l'interfaccia utente per una sessione attiva, inclusa la lista dei pazienti.
    private void enterSession(String sessionCode) {
        currentSessionCode = sessionCode;
        setTitle(getString(R.string.session_room_title, sessionCode));
        preSessionContainer.setVisibility(View.GONE);
        patientsRecyclerView.setVisibility(View.VISIBLE);
        bottomNavigation.setVisibility(View.VISIBLE);
        invalidateOptionsMenu();
        loadPatients();
        bottomNavigation.setSelectedItemId(R.id.navigation_unidentified);
    }

    // English: Sets up the post-session UI, including the RecyclerView and BottomNavigationView.
    // Italiano: Imposta l'interfaccia utente post-sessione, inclusi RecyclerView e BottomNavigationView.
    private void setupPostSessionUI() {
        patientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        patientAdapter = new PatientAdapter(displayedPatients, this, false, this);
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

    // English: Loads the list of patients for the current session from the database.
    // Italiano: Carica la lista dei pazienti per la sessione corrente dal database.
    private void loadPatients() {
        if (currentSessionCode == null) return;
        executor.execute(() -> {
            List<Patient> patientsFromDb = db.patientDao().getPatientsBySession(currentSessionCode);
            runOnUiThread(() -> {
                allPatients.clear();
                allPatients.addAll(patientsFromDb);
                filterPatients(currentFilter);
            });
        });
    }

    // English: Filters the displayed patient list based on the selected filter type.
    // Italiano: Filtra l'elenco dei pazienti visualizzati in base al tipo di filtro selezionato.
    private void filterPatients(FilterType filter) {
        this.currentFilter = filter;
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

    // English: Filters the patient list based on a search query.
    // Italiano: Filtra l'elenco dei pazienti in base a una query di ricerca.
    private void filterBySearchQuery(String query) {
        List<Patient> results = new ArrayList<>();
        if (query.isEmpty()) {
            filterPatients(this.currentFilter);
            return;
        }

        String lowerCaseQuery = query.toLowerCase();
        for (Patient patient : allPatients) {
            if ((patient.getName() != null && patient.getName().toLowerCase().contains(lowerCaseQuery)) ||
                    (patient.getSurname() != null && patient.getSurname().toLowerCase().contains(lowerCaseQuery)) ||
                    (patient.getDistinguishingMarks() != null && patient.getDistinguishingMarks().toLowerCase().contains(lowerCaseQuery))) {
                results.add(patient);
            }
        }

        displayedPatients.clear();
        displayedPatients.addAll(results);
        patientAdapter.notifyDataSetChanged();
    }

    // English: Creates the options menu in the toolbar, including the search view.
    // Italiano: Crea il menu delle opzioni nella barra degli strumenti, inclusa la vista di ricerca.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.optionsMenu = menu;
        menu.findItem(R.id.action_add_missing_person).setVisible(false);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        this.searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterBySearchQuery(newText);
                return true;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                filterPatients(currentFilter);
                return true;
            }
        });

        return true;
    }

    // English: Handles the selection of items from the options menu.
    // Italiano: Gestisce la selezione degli elementi dal menu delle opzioni.
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

    // English: Shows a confirmation dialog to delete the user's account.
    // Italiano: Mostra un dialogo di conferma per eliminare l'account dell'utente.
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_account_title)
                .setMessage(R.string.confirm_delete_account_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteAccount())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    // English: Deletes the current user's account from the database.
    // Italiano: Elimina l'account dell'utente corrente dal database.
    private void deleteAccount() {
        executor.execute(() -> {
            User user = db.userDao().findByUsername(currentUserUsername);
            if (user != null) {
                db.userDao().delete(user);
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.account_deleted_successfully, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }

    // English: Updates the visibility of the menu items based on the selected navigation tab.
    // Italiano: Aggiorna la visibilità degli elementi del menu in base alla scheda di navigazione selezionata.
    private void updateMenuVisibility(int selectedItemId) {
        if (optionsMenu != null) {
            MenuItem addItem = optionsMenu.findItem(R.id.action_add_missing_person);
            addItem.setVisible(selectedItemId == R.id.navigation_missing);
        }
    }

    // English: Handles the addition of a new missing person.
    // Italiano: Gestisce l'aggiunta di una nuova persona scomparsa.
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

        Toast.makeText(this, R.string.missing_person_added, Toast.LENGTH_SHORT).show();
    }

    // English: Shows a dialog to identify an unidentified patient.
    // Italiano: Mostra un dialogo per identificare un paziente non identificato.
    @Override
    public void onIdentifyPatient(Patient patient) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.identify_patient_title);
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        final EditText nameInput = new EditText(this);
        nameInput.setHint(R.string.name_hint);
        layout.addView(nameInput);

        final EditText surnameInput = new EditText(this);
        surnameInput.setHint(R.string.surname_hint);
        layout.addView(surnameInput);

        builder.setView(layout);

        builder.setPositiveButton(R.string.identify_button, (dialog, which) -> {
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
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    // English: Shows a dialog to add a new contact to a patient.
    // Italiano: Mostra un dialogo per aggiungere un nuovo contatto a un paziente.
    @Override
    public void onAddContact(Patient patient) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_contact);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText roleInput = new EditText(this);
        roleInput.setHint(R.string.add_contact_role_hint);
        layout.addView(roleInput);

        final EditText nameInput = new EditText(this);
        nameInput.setHint(R.string.add_contact_name_hint);
        layout.addView(nameInput);

        final EditText contactInput = new EditText(this);
        contactInput.setHint(R.string.add_contact_contact_hint);
        contactInput.setInputType(InputType.TYPE_CLASS_PHONE);
        layout.addView(contactInput);

        builder.setView(layout);

        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            String role = roleInput.getText().toString().trim();
            String name = nameInput.getText().toString().trim();
            String contact = contactInput.getText().toString().trim();

            if (!role.isEmpty() && !name.isEmpty() && !contact.isEmpty()) {
                String newContactInfo = getString(R.string.add_contact_role_hint) + ": " + role + "\n" + getString(R.string.add_contact_name_hint) + ": " + name + "\n" + getString(R.string.add_contact_contact_hint) + ": " + contact;
                String existingContacts = patient.getContacts() != null ? patient.getContacts() : "";
                String newContacts = existingContacts.isEmpty() ? newContactInfo : existingContacts + "\n\n" + newContactInfo;
                patient.setContacts(newContacts);

                executor.execute(() -> {
                    db.patientDao().update(patient);
                    runOnUiThread(this::loadPatients);
                });
                Toast.makeText(this, R.string.contact_added, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

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

    // English: Handles editing a patient (not implemented for user).
    // Italiano: Gestisce la modifica di un paziente (non implementato per l'utente).
    @Override public void onEditPatient(Patient p) {}

    // English: Handles deleting a patient (not implemented for user).
    // Italiano: Gestisce l'eliminazione di un paziente (non implementato per l'utente).
    @Override public void onDeletePatient(Patient p) {}

    private enum FilterType { IDENTIFIED, UNIDENTIFIED, MISSING, DECEASED }
}
