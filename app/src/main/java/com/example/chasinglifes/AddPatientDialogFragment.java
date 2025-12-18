package com.example.chasinglifes;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddPatientDialogFragment extends DialogFragment {

    public interface AddPatientDialogListener {
        void onPatientAddedOrUpdated();
    }

    private AddPatientDialogListener listener;
    private static final String ARG_SESSION_CODE = "session_code";
    private static final String ARG_OPERATOR_USERNAME = "operator_username";
    private static final String ARG_PATIENT_TO_EDIT = "patient_to_edit";

    private Patient patientToEdit;

    public static AddPatientDialogFragment newInstance(String sessionCode, String operatorUsername) {
        return newInstance(sessionCode, operatorUsername, null);
    }

    public static AddPatientDialogFragment newInstance(String sessionCode, String operatorUsername, Patient patient) {
        AddPatientDialogFragment fragment = new AddPatientDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SESSION_CODE, sessionCode);
        args.putString(ARG_OPERATOR_USERNAME, operatorUsername);
        if (patient != null) {
            args.putSerializable(ARG_PATIENT_TO_EDIT, patient);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (AddPatientDialogListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_PATIENT_TO_EDIT)) {
            patientToEdit = (Patient) getArguments().getSerializable(ARG_PATIENT_TO_EDIT);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        boolean isEditMode = (patientToEdit != null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_patient, null);

        if (isEditMode) {
            populateFields(view, patientToEdit);
        }

        setupCheckboxListeners(view);

        builder.setView(view)
                .setTitle(isEditMode ? "Modifica Paziente" : "Aggiungi Paziente")
                .setPositiveButton(isEditMode ? "Salva Modifiche" : "Aggiungi", (dialog, id) -> {
                    if (isEditMode) {
                        saveOrUpdatePatient(view, true);
                    } else {
                        checkForMatches(view);
                    }
                })
                .setNegativeButton("Annulla", null);

        return builder.create();
    }

    private void setupCheckboxListeners(View view) {
        final LinearLayout identifiedContainer = view.findViewById(R.id.identified_patient_container);
        final EditText marksInput = view.findViewById(R.id.distinguishing_marks_input);
        final CheckBox unidentCheckbox = view.findViewById(R.id.unidentified_checkbox);
        final LinearLayout conditionsContainer = view.findViewById(R.id.conditions_container);
        final CheckBox decCheckbox = view.findViewById(R.id.deceased_checkbox);

        unidentCheckbox.setOnCheckedChangeListener((b, isChecked) -> {
            identifiedContainer.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            marksInput.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        decCheckbox.setOnCheckedChangeListener((b, isChecked) -> {
            conditionsContainer.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        });
    }

    private void populateFields(View view, Patient p) {
        ((EditText) view.findViewById(R.id.patient_name_input)).setText(p.getName());
        ((EditText) view.findViewById(R.id.patient_surname_input)).setText(p.getSurname());
        ((EditText) view.findViewById(R.id.distinguishing_marks_input)).setText(p.getDistinguishingMarks());
        ((EditText) view.findViewById(R.id.conditions_input)).setText(p.getConditions());
        ((CheckBox) view.findViewById(R.id.unidentified_checkbox)).setChecked("UNIDENTIFIED".equals(p.getStatus()));
        ((CheckBox) view.findViewById(R.id.deceased_checkbox)).setChecked(p.isDeceased());
    }

    private void checkForMatches(View view) {
        final CheckBox unidentCheckbox = view.findViewById(R.id.unidentified_checkbox);
        final EditText nameInput = view.findViewById(R.id.patient_name_input);
        final EditText surnameInput = view.findViewById(R.id.patient_surname_input);
        final EditText marksInput = view.findViewById(R.id.distinguishing_marks_input);

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            List<Patient> matches;

            if (unidentCheckbox.isChecked()) {
                String marks = "%" + marksInput.getText().toString().trim() + "%";
                matches = db.patientDao().findMissingByMarks(marks);
            } else {
                String name = "%" + nameInput.getText().toString().trim() + "%";
                String surname = "%" + surnameInput.getText().toString().trim() + "%";
                matches = db.patientDao().findMissingByName(name, surname);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (matches.isEmpty()) {
                        Toast.makeText(getContext(), "Nessun riscontro con i dispersi. Inserimento diretto.", Toast.LENGTH_SHORT).show();
                        saveOrUpdatePatient(view, false);
                    } else {
                        Toast.makeText(getContext(), "Trovati " + matches.size() + " possibili riscontri tra i dispersi!", Toast.LENGTH_LONG).show();
                        // Per ora, anche se troviamo riscontri, procediamo comunque con l'inserimento come nuovo paziente.
                        // In futuro qui mostreremo il dialogo di selezione.
                        saveOrUpdatePatient(view, false);
                    }
                });
            }
        });
    }

    private void saveOrUpdatePatient(View view, boolean isEditMode) {
        final Patient patient = isEditMode ? patientToEdit : new Patient();
        if (!isEditMode) {
            patient.setSessionCode(getArguments().getString(ARG_SESSION_CODE));
            patient.setOperatorUsername(getArguments().getString(ARG_OPERATOR_USERNAME));
        }
        
        final EditText nameInput = view.findViewById(R.id.patient_name_input);
        final EditText surnameInput = view.findViewById(R.id.patient_surname_input);
        final EditText marksInput = view.findViewById(R.id.distinguishing_marks_input);
        final EditText condInput = view.findViewById(R.id.conditions_input);
        final CheckBox unidentCheckbox = view.findViewById(R.id.unidentified_checkbox);
        final CheckBox decCheckbox = view.findViewById(R.id.deceased_checkbox);

        patient.setDeceased(decCheckbox.isChecked());
        patient.setConditions(decCheckbox.isChecked() ? null : condInput.getText().toString().trim());

        if (unidentCheckbox.isChecked()) {
            patient.setStatus("UNIDENTIFIED");
            patient.setDistinguishingMarks(marksInput.getText().toString().trim());
            patient.setName(null); patient.setSurname(null);
        } else {
            patient.setStatus("IDENTIFIED");
            patient.setName(nameInput.getText().toString().trim());
            patient.setSurname(surnameInput.getText().toString().trim());
            patient.setDistinguishingMarks(marksInput.getText().toString().trim());
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            if (isEditMode) {
                db.patientDao().update(patient);
            } else {
                db.patientDao().insert(patient);
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    listener.onPatientAddedOrUpdated();
                });
            }
        });
    }
}
