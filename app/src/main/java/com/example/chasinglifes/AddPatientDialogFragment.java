package com.example.chasinglifes;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.io.Serializable;

// Questo dialogo ha UNA SOLA RESPONSABILITÀ: raccogliere i dati e passarli all'activity.
public class AddPatientDialogFragment extends DialogFragment {

    public interface AddPatientDialogListener {
        // Il listener ora passa l'oggetto Patient costruito
        void onPatientDataCollected(Patient patientData, boolean isEditMode);
    }

    private AddPatientDialogListener listener;
    private Patient patientToEdit;
    private View view;

    public static AddPatientDialogFragment newInstance(String sessionCode, String operatorUsername, Patient patient) {
        AddPatientDialogFragment fragment = new AddPatientDialogFragment();
        Bundle args = new Bundle();
        args.putString("session_code", sessionCode);
        args.putString("operator_username", operatorUsername);
        if (patient != null) {
            args.putSerializable("patient_to_edit", patient);
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
        if (getArguments() != null && getArguments().containsKey("patient_to_edit")) {
            patientToEdit = (Patient) getArguments().getSerializable("patient_to_edit");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        boolean isEditMode = (patientToEdit != null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        this.view = inflater.inflate(R.layout.dialog_add_patient, null);

        setupCheckboxListeners();
        if (isEditMode) {
            populateFields();
        }

        builder.setView(view)
                .setTitle(isEditMode ? "Modifica Paziente" : "Aggiungi Paziente")
                .setPositiveButton(isEditMode ? "Salva" : "Avanti", (dialog, id) -> {
                    // Costruisce l'oggetto e lo passa all'activity
                    Patient patientData = buildPatientFromInput(isEditMode);
                    listener.onPatientDataCollected(patientData, isEditMode);
                })
                .setNegativeButton("Annulla", null);

        return builder.create();
    }

    private Patient buildPatientFromInput(boolean isEditMode) {
        final Patient p = isEditMode ? patientToEdit : new Patient();
        if (!isEditMode) {
            p.setSessionCode(getArguments().getString("session_code"));
            p.setOperatorUsername(getArguments().getString("operator_username"));
        }
        p.setDeceased(((CheckBox) view.findViewById(R.id.deceased_checkbox)).isChecked());
        p.setConditions(p.isDeceased() ? null : ((EditText) view.findViewById(R.id.conditions_input)).getText().toString().trim());
        if (((CheckBox) view.findViewById(R.id.unidentified_checkbox)).isChecked()) {
            p.setStatus("UNIDENTIFIED");
            p.setDistinguishingMarks(((EditText) view.findViewById(R.id.distinguishing_marks_input)).getText().toString().trim());
            p.setName(null); p.setSurname(null);
        } else {
            p.setStatus("IDENTIFIED");
            p.setName(((EditText) view.findViewById(R.id.patient_name_input)).getText().toString().trim());
            p.setSurname(((EditText) view.findViewById(R.id.patient_surname_input)).getText().toString().trim());
        }
        p.setDistinguishingMarks(((EditText) view.findViewById(R.id.distinguishing_marks_input)).getText().toString().trim());
        return p;
    }

    private void populateFields() {
        ((EditText) view.findViewById(R.id.patient_name_input)).setText(patientToEdit.getName());
        ((EditText) view.findViewById(R.id.patient_surname_input)).setText(patientToEdit.getSurname());
        ((EditText) view.findViewById(R.id.distinguishing_marks_input)).setText(patientToEdit.getDistinguishingMarks());
        ((EditText) view.findViewById(R.id.conditions_input)).setText(patientToEdit.getConditions());
        CheckBox unidentCheckbox = view.findViewById(R.id.unidentified_checkbox);
        unidentCheckbox.setChecked("UNIDENTIFIED".equals(patientToEdit.getStatus()));
        CheckBox decCheckbox = view.findViewById(R.id.deceased_checkbox);
        decCheckbox.setChecked(patientToEdit.isDeceased());
        view.findViewById(R.id.identified_patient_container).setVisibility(unidentCheckbox.isChecked() ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.conditions_container).setVisibility(decCheckbox.isChecked() ? View.GONE : View.VISIBLE);
    }

    private void setupCheckboxListeners() {
        final LinearLayout identifiedContainer = view.findViewById(R.id.identified_patient_container);
        final CheckBox unidentCheckbox = view.findViewById(R.id.unidentified_checkbox);
        final LinearLayout conditionsContainer = view.findViewById(R.id.conditions_container);
        final CheckBox decCheckbox = view.findViewById(R.id.deceased_checkbox);
        unidentCheckbox.setOnCheckedChangeListener((b, isChecked) -> identifiedContainer.setVisibility(isChecked ? View.GONE : View.VISIBLE));
        decCheckbox.setOnCheckedChangeListener((b, isChecked) -> conditionsContainer.setVisibility(isChecked ? View.GONE : View.VISIBLE));
    }
}
