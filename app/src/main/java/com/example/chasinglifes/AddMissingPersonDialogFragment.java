package com.example.chasinglifes;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class AddMissingPersonDialogFragment extends DialogFragment {

    public interface AddMissingPersonListener {
        void onMissingPersonAdded(String name, String surname, String marks);
    }

    private AddMissingPersonListener listener;

    // English: Attaches the listener to the fragment.
    // Italiano: Collega il listener al frammento.
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (AddMissingPersonListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement AddMissingPersonListener");
        }
    }

    // English: Creates the dialog for adding a missing person.
    // Italiano: Crea il dialogo per aggiungere una persona scomparsa.
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_missing_person, null);

        final EditText nameInput = view.findViewById(R.id.missing_person_name_input);
        final EditText surnameInput = view.findViewById(R.id.missing_person_surname_input);
        final EditText marksInput = view.findViewById(R.id.missing_person_marks_input);

        builder.setView(view)
                .setTitle(R.string.add_missing_person)
                .setPositiveButton(R.string.save, (dialog, id) -> {
                    String name = nameInput.getText().toString().trim();
                    String surname = surnameInput.getText().toString().trim();
                    String marks = marksInput.getText().toString().trim();
                    listener.onMissingPersonAdded(name, surname, marks);
                })
                .setNegativeButton(R.string.cancel, null);

        return builder.create();
    }
}
