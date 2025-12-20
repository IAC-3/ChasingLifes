package com.example.chasinglifes;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.List;

public class MatchFoundDialogFragment extends DialogFragment implements MatchAdapter.OnMatchClickListener {

    public interface MatchFoundListener {
        void onMatchSelected(Patient existingPatient, Patient newPatientData);
        void onNoneSelected(Patient newPatientData);
    }

    private MatchFoundListener listener;
    private List<Patient> matchList;
    private Patient newPatientData;

    public static MatchFoundDialogFragment newInstance(List<Patient> matches, Patient newPatient) {
        MatchFoundDialogFragment fragment = new MatchFoundDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("matches", (Serializable) matches);
        args.putSerializable("new_patient", newPatient);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // L'activity che ospita il dialogo deve implementare il listener
        if (context instanceof MatchFoundListener) {
            listener = (MatchFoundListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement MatchFoundListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            matchList = (List<Patient>) getArguments().getSerializable("matches");
            newPatientData = (Patient) getArguments().getSerializable("new_patient");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_match_found, null);

        RecyclerView recyclerView = view.findViewById(R.id.matches_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        MatchAdapter adapter = new MatchAdapter(matchList, this);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.none_of_these_button).setOnClickListener(v -> {
            listener.onNoneSelected(newPatientData);
            dismiss();
        });

        builder.setView(view).setTitle("Riscontri Trovati");

        return builder.create();
    }

    // Click su un elemento della lista
    @Override
    public void onMatchClicked(Patient patient) {
        listener.onMatchSelected(patient, newPatientData);
        dismiss();
    }
}
