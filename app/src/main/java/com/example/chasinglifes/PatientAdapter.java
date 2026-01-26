package com.example.chasinglifes;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    public interface PatientAdapterListener {
        void onEditPatient(Patient patient);
        void onDeletePatient(Patient patient);
        void onIdentifyPatient(Patient patient);
        void onAddContact(Patient patient);
        void onViewContacts(Patient patient);
    }

    private final List<Patient> patientList;
    private final PatientAdapterListener listener;
    private final boolean isOperatorView;

    public PatientAdapter(List<Patient> patientList, PatientAdapterListener listener, boolean isOperatorView) {
        this.patientList = patientList;
        this.listener = listener;
        this.isOperatorView = isOperatorView;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_list_item, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patientList.get(position);

        // Logica di visualizzazione
        StringBuilder topLineBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(patient.getName()) || !TextUtils.isEmpty(patient.getSurname())) {
            topLineBuilder.append(patient.getName()).append(" ").append(patient.getSurname());
        }
        if (!TextUtils.isEmpty(patient.getDistinguishingMarks())) {
            if (topLineBuilder.length() > 0) topLineBuilder.append("\n");
            topLineBuilder.append("Segni: ").append(patient.getDistinguishingMarks());
        }
        holder.patientNameOrMarks.setText(topLineBuilder.toString());

        if (patient.isDeceased()) {
            holder.patientConditions.setText("Deceduto");
        } else {
            holder.patientConditions.setText(patient.getConditions());
        }

        if (!isOperatorView && patient.getOperatorUsername() != null) {
            holder.operatorUsername.setText("Inserito da: " + patient.getOperatorUsername());
            holder.operatorUsername.setVisibility(View.VISIBLE);
        } else {
            holder.operatorUsername.setVisibility(View.GONE);
        }

        holder.patientPhoto.setImageResource(R.mipmap.ic_launcher);

        // Logica dei pulsanti
        if (listener != null) {
            holder.editButton.setVisibility(isOperatorView ? View.VISIBLE : View.GONE);
            holder.deleteButton.setVisibility(isOperatorView ? View.VISIBLE : View.GONE);
            holder.viewContactsButton.setVisibility(isOperatorView ? View.VISIBLE : View.GONE);
            holder.addContactButton.setVisibility(!isOperatorView ? View.VISIBLE : View.GONE);

            holder.editButton.setOnClickListener(v -> listener.onEditPatient(patient));
            holder.deleteButton.setOnClickListener(v -> listener.onDeletePatient(patient));
            holder.viewContactsButton.setOnClickListener(v -> listener.onViewContacts(patient));
            holder.addContactButton.setOnClickListener(v -> listener.onAddContact(patient));

            if (!isOperatorView && "UNIDENTIFIED".equals(patient.getStatus())) {
                holder.identifyButton.setVisibility(View.VISIBLE);
                holder.identifyButton.setOnClickListener(v -> listener.onIdentifyPatient(patient));
            } else {
                holder.identifyButton.setVisibility(View.GONE);
            }
        } else {
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
            holder.identifyButton.setVisibility(View.GONE);
            holder.addContactButton.setVisibility(View.GONE);
            holder.viewContactsButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return patientList.size(); }

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        ImageView patientPhoto, editButton, deleteButton, identifyButton, addContactButton, viewContactsButton;
        TextView patientNameOrMarks, patientConditions, operatorUsername;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            patientPhoto = itemView.findViewById(R.id.patient_photo);
            patientNameOrMarks = itemView.findViewById(R.id.patient_name_or_marks);
            patientConditions = itemView.findViewById(R.id.patient_conditions);
            operatorUsername = itemView.findViewById(R.id.patient_operator_username);
            editButton = itemView.findViewById(R.id.edit_patient_button);
            deleteButton = itemView.findViewById(R.id.delete_patient_button);
            identifyButton = itemView.findViewById(R.id.identify_patient_button);
            addContactButton = itemView.findViewById(R.id.add_contact_button);
            viewContactsButton = itemView.findViewById(R.id.view_contacts_button);
        }
    }
}
