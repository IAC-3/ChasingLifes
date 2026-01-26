package com.example.chasinglifes;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.io.Serializable;
import java.util.List;

public class MatchFoundDialogFragment extends DialogFragment {

    public interface MatchFoundListener {
        void onMatchSelected(Patient existingPatient, Patient newPatientData);
        void onNoneSelected(Patient newPatientData);
    }

    private MatchFoundListener listener;
    private List<Patient> matches;
    private Patient newPatientData;

    // English: Creates a new instance of the fragment, passing the list of matches and the new patient data.
    // Italiano: Crea una nuova istanza del frammento, passando l'elenco delle corrispondenze e i dati del nuovo paziente.
    public static MatchFoundDialogFragment newInstance(List<Patient> matches, Patient newPatientData) {
        MatchFoundDialogFragment fragment = new MatchFoundDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("matches", (Serializable) matches);
        args.putSerializable("new_patient_data", newPatientData);
        fragment.setArguments(args);
        return fragment;
    }

    // English: Attaches the listener to the fragment.
    // Italiano: Collega il listener al frammento.
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (MatchFoundListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement MatchFoundListener");
        }
    }

    // English: Initializes the fragment, retrieving the list of matches and the new patient data.
    // Italiano: Inizializza il frammento, recuperando l'elenco delle corrispondenze e i dati del nuovo paziente.
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            matches = (List<Patient>) getArguments().getSerializable("matches");
            newPatientData = (Patient) getArguments().getSerializable("new_patient_data");
        }
    }

    // English: Creates the dialog to show the list of potential matches.
    // Italiano: Crea il dialogo per mostrare l'elenco delle potenziali corrispondenze.
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        CharSequence[] items = new CharSequence[matches.size()];
        for (int i = 0; i < matches.size(); i++) {
            items[i] = matches.get(i).getName() + " " + matches.get(i).getSurname();
        }

        builder.setTitle("Corrispondenza Trovata")
                .setItems(items, (dialog, which) -> {
                    listener.onMatchSelected(matches.get(which), newPatientData);
                })
                .setNegativeButton("Nessuna di queste", (dialog, id) -> {
                    listener.onNoneSelected(newPatientData);
                });

        return builder.create();
    }
}
