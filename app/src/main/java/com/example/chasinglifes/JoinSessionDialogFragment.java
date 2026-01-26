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

    public interface JoinSessionDialogListener {
        void onJoinAttempt(String code);
    }

    private JoinSessionDialogListener listener;

    // English: Attaches the listener to the fragment.
    // Italiano: Collega il listener al frammento.
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (JoinSessionDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement JoinSessionDialogListener");
        }
    }

    // English: Creates the dialog for joining a session.
    // Italiano: Crea il dialogo per unirsi a una sessione.
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_join_session, null);

        final EditText codeInput = view.findViewById(R.id.session_code_input);

        builder.setView(view)
                .setTitle(R.string.join_a_session)
                .setPositiveButton(R.string.join, (dialog, id) -> {
                    String code = codeInput.getText().toString().trim();
                    listener.onJoinAttempt(code);
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    JoinSessionDialogFragment.this.getDialog().cancel();
                });

        return builder.create();
    }
}
