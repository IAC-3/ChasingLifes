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

public class JoinSessionDialogFragment extends DialogFragment {

    // Interfaccia per comunicare il codice inserito all'activity
    public interface JoinSessionDialogListener {
        void onJoinAttempt(String code);
    }

    private JoinSessionDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (JoinSessionDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement JoinSessionDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_join_session, null);

        final EditText codeInput = view.findViewById(R.id.session_code_input);

        builder.setView(view)
                .setPositiveButton("Unisciti", (dialog, id) -> {
                    String code = codeInput.getText().toString().trim();
                    listener.onJoinAttempt(code);
                })
                .setNegativeButton("Annulla", (dialog, id) -> {
                    // L'utente ha annullato il dialogo
                    JoinSessionDialogFragment.this.getDialog().cancel();
                });

        return builder.create();
    }
}
