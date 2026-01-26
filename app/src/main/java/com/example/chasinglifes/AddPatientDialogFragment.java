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

public class AddPatientDialogFragment extends DialogFragment {

    public interface AddPatientDialogListener {
        void onPatientDataCollected(Patient patientData, boolean isEditMode);
    }

    private AddPatientDialogListener listener;
    private Patient patientToEdit;
    private View view;

    // English: Creates a new instance of the fragment, passing patient data if in edit mode.
    // Italiano: Crea una nuova istanza del frammento, passando i dati del paziente se in modalità di modifica.
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

    // English: Attaches the listener to the fragment.
    // Italiano: Collega il listener al frammento.
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (AddPatientDialogListener) context;
    }

    // English: Initializes the fragment, retrieving patient data if in edit mode.
    // Italiano: Inizializza il frammento, recuperando i dati del paziente se in modalità di modifica.
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey("patient_to_edit")) {
            patientToEdit = (Patient) getArguments().getSerializable("patient_to_edit");
        }
    }

    // English: Creates the dialog for adding or editing a patient.
    // Italiano: Crea il dialogo per aggiungere o modificare un paziente.
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
                .setTitle(isEditMode ? R.string.edit_patient : R.string.add_patient)
                .setPositiveButton(isEditMode ? R.string.save : R.string.next, (dialog, id) -> {
                    Patient patientData = buildPatientFromInput(isEditMode);
                    listener.onPatientDataCollected(patientData, isEditMode);
                })
                .setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    // English: Builds a Patient object from the input fields.
    // Italiano: Costruisce un oggetto Patient dai campi di input.
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
        p.setHospital(((EditText) view.findViewById(R.id.hospital_input)).getText().toString().trim());
        return p;
    }

    // English: Populates the input fields with the patient's data when in edit mode.
    // Italiano: Popola i campi di input con i dati del paziente quando in modalità di modifica.
    private void populateFields() {
        ((EditText) view.findViewById(R.id.patient_name_input)).setText(patientToEdit.getName());
        ((EditText) view.findViewById(R.id.patient_surname_input)).setText(patientToEdit.getSurname());
        ((EditText) view.findViewById(R.id.distinguishing_marks_input)).setText(patientToEdit.getDistinguishingMarks());
        ((EditText) view.findViewById(R.id.conditions_input)).setText(patientToEdit.getConditions());
        ((EditText) view.findViewById(R.id.hospital_input)).setText(patientToEdit.getHospital());
        CheckBox unidentCheckbox = view.findViewById(R.id.unidentified_checkbox);
        unidentCheckbox.setChecked("UNIDENTIFIED".equals(patientToEdit.getStatus()));
        CheckBox decCheckbox = view.findViewById(R.id.deceased_checkbox);
        decCheckbox.setChecked(patientToEdit.isDeceased());
        view.findViewById(R.id.identified_patient_container).setVisibility(unidentCheckbox.isChecked() ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.conditions_container).setVisibility(decCheckbox.isChecked() ? View.GONE : View.VISIBLE);
    }

    // English: Sets up the listeners for the checkboxes to show or hide fields dynamically.
    // Italiano: Imposta i listener per le checkbox per mostrare o nascondere i campi dinamicamente.
    private void setupCheckboxListeners() {
        final LinearLayout identifiedContainer = view.findViewById(R.id.identified_patient_container);
        final CheckBox unidentCheckbox = view.findViewById(R.id.unidentified_checkbox);
        final LinearLayout conditionsContainer = view.findViewById(R.id.conditions_container);
        final CheckBox decCheckbox = view.findViewById(R.id.deceased_checkbox);
        unidentCheckbox.setOnCheckedChangeListener((b, isChecked) -> identifiedContainer.setVisibility(isChecked ? View.GONE : View.VISIBLE));
        decCheckbox.setOnCheckedChangeListener((b, isChecked) -> conditionsContainer.setVisibility(isChecked ? View.GONE : View.VISIBLE));
    }
}
