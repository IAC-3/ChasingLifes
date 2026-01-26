package com.example.chasinglifes;

import android.content.Context;
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
    private final Context context;

    // English: Constructor for the adapter.
    // Italiano: Costruttore per l'adattatore.
    public PatientAdapter(List<Patient> patientList, PatientAdapterListener listener, boolean isOperatorView, Context context) {
        this.patientList = patientList;
        this.listener = listener;
        this.isOperatorView = isOperatorView;
        this.context = context;
    }

    // English: Creates a new ViewHolder when the RecyclerView needs one.
    // Italiano: Crea un nuovo ViewHolder quando il RecyclerView ne ha bisogno.
    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_list_item, parent, false);
        return new PatientViewHolder(view);
    }

    // English: Binds the data to the ViewHolder for a specific item.
    // Italiano: Collega i dati al ViewHolder for a specific item.
    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patientList.get(position);

        StringBuilder topLineBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(patient.getName()) || !TextUtils.isEmpty(patient.getSurname())) {
            topLineBuilder.append(patient.getName()).append(" ").append(patient.getSurname());
        }
        if (!TextUtils.isEmpty(patient.getDistinguishingMarks())) {
            if (topLineBuilder.length() > 0) topLineBuilder.append("\n");
            topLineBuilder.append(context.getString(R.string.signs_label)).append(": ").append(patient.getDistinguishingMarks());
        }
        holder.patientNameOrMarks.setText(topLineBuilder.toString());

        if (patient.isDeceased()) {
            holder.patientConditions.setText(R.string.deceased);
        } else {
            holder.patientConditions.setText(patient.getConditions());
        }

        if (!isOperatorView && patient.getOperatorUsername() != null) {
            holder.operatorUsername.setText(context.getString(R.string.inserted_by_label) + ": " + patient.getOperatorUsername());
            holder.operatorUsername.setVisibility(View.VISIBLE);
        } else {
            holder.operatorUsername.setVisibility(View.GONE);
        }

        holder.patientPhoto.setImageResource(R.mipmap.ic_launcher);

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

    // English: Returns the total number of items in the list.
    // Italiano: Restituisce il numero totale di elementi nella lista.
    @Override
    public int getItemCount() { return patientList.size(); }

    // English: ViewHolder for a patient item.
    // Italiano: ViewHolder per un elemento paziente.
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
