package com.example.chasinglifes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chasinglifes.databinding.FragmentSecondBinding;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;

    // English: Creates and returns the view hierarchy associated with the fragment.
    // Italiano: Crea e restituisce la gerarchia delle viste associata al frammento.
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    // English: Called immediately after onCreateView() has returned, but before any saved state has been restored in to the view.
    // Italiano: Chiamato subito dopo che onCreateView() è tornato, ma prima che qualsiasi stato salvato sia stato ripristinato nella vista.
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
    }

    // English: Called when the view previously created by onCreateView() has been detached from the fragment.
    // Italiano: Chiamato quando la vista precedentemente creata da onCreateView() è stata scollegata dal frammento.
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
