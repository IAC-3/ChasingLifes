package com.example.chasinglifes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    private final List<Patient> patientList;

    public PatientAdapter(List<Patient> patientList) {
        this.patientList = patientList;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.patient_list_item, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patientList.get(position);

        // Gestione Nomi/Segni particolari
        if (patient.isUnidentified()) {
            // Se il paziente non è identificato, mostra i segni particolari.
            holder.patientNameOrMarks.setText(patient.getDistinguishingMarks());
        } else {
            // Altrimenti, mostra nome e cognome.
            String fullName = patient.getName() + " " + patient.getSurname();
            holder.patientNameOrMarks.setText(fullName);
        }

        // Gestione Condizioni
        holder.patientConditions.setText(patient.getConditions());

        // Gestione Foto (Semplificata)
        // Per ora, non carichiamo nessuna immagine da URI per evitare crash.
        // Mostriamo solo un'icona di default.
        holder.patientPhoto.setImageResource(R.mipmap.ic_launcher);
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        ImageView patientPhoto;
        TextView patientNameOrMarks;
        TextView patientConditions;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            patientPhoto = itemView.findViewById(R.id.patient_photo);
            patientNameOrMarks = itemView.findViewById(R.id.patient_name_or_marks);
            patientConditions = itemView.findViewById(R.id.patient_conditions);
        }
    }
}
