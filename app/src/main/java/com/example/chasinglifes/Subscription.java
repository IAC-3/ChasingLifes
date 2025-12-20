package com.example.chasinglifes;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

// Definisce una tabella di collegamento tra User e Patient
@Entity(tableName = "subscriptions", 
        primaryKeys = {"username", "patientId"}, // Chiave primaria composta
        foreignKeys = {
            @ForeignKey(entity = User.class, 
                        parentColumns = "username", 
                        childColumns = "username", 
                        onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = Patient.class, 
                        parentColumns = "id", 
                        childColumns = "patientId", 
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("patientId")}
)
public class Subscription {

    @NonNull
    public String username;

    public int patientId;

    public Subscription(@NonNull String username, int patientId) {
        this.username = username;
        this.patientId = patientId;
    }
}
