package com.example.chasinglifes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {

    public interface OnMatchClickListener {
        void onMatchClicked(Patient patient);
    }

    private final List<Patient> matchList;
    private final OnMatchClickListener listener;

    public MatchAdapter(List<Patient> matchList, OnMatchClickListener listener) {
        this.matchList = matchList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Uso il nuovo layout per il "rettangolino"
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.match_list_item, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        Patient patient = matchList.get(position);
        String displayText = patient.getName() + " " + patient.getSurname() + "\nSegni: " + patient.getDistinguishingMarks();
        holder.matchInfo.setText(displayText);
        // Collego il click del pulsante al listener
        holder.chooseButton.setOnClickListener(v -> listener.onMatchClicked(patient));
    }

    @Override
    public int getItemCount() {
        return matchList.size();
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView matchInfo;
        Button chooseButton;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            matchInfo = itemView.findViewById(R.id.match_info_text);
            chooseButton = itemView.findViewById(R.id.choose_match_button);
        }
    }
}
