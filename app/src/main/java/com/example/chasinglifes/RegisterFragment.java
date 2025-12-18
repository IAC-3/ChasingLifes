package com.example.chasinglifes;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RegisterFragment extends Fragment {

    public interface OnRegisterFragmentListener {
        void onRegisterAttempt(String username, String password, boolean isOperator);
        void onNavigateToLogin();
    }

    private OnRegisterFragmentListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnRegisterFragmentListener) {
            listener = (OnRegisterFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnRegisterFragmentListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        EditText usernameInput = view.findViewById(R.id.username_input);
        EditText passwordInput = view.findViewById(R.id.password_input);
        CheckBox isOperatorCheckbox = view.findViewById(R.id.is_operator_checkbox);
        Button registerButton = view.findViewById(R.id.register_button);
        TextView goToLoginText = view.findViewById(R.id.go_to_login_text);

        registerButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            boolean isOperator = isOperatorCheckbox.isChecked();
            listener.onRegisterAttempt(username, password, isOperator);
        });

        goToLoginText.setOnClickListener(v -> listener.onNavigateToLogin());

        return view;
    }
}
