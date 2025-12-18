package com.example.chasinglifes;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "patients",
        foreignKeys = {
            @ForeignKey(entity = Session.class, parentColumns = "code", childColumns = "sessionCode", onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = User.class, parentColumns = "username", childColumns = "operatorUsername", onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("sessionCode"), @Index("operatorUsername")}
)
public class Patient implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    private String sessionCode;
    private String operatorUsername;

    private String name;
    private String surname;
    private String distinguishingMarks;
    private String conditions;
    
    // NUOVO MODELLO DATI
    @NonNull
    private String status; // Sarà "IDENTIFIED", "UNIDENTIFIED", "MISSING"
    private boolean isDeceased; // Campo separato per lo stato di decesso

    public Patient() {}

    // --- Getters e Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSessionCode() { return sessionCode; }
    public void setSessionCode(String sessionCode) { this.sessionCode = sessionCode; }

    public String getOperatorUsername() { return operatorUsername; }
    public void setOperatorUsername(String operatorUsername) { this.operatorUsername = operatorUsername; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getDistinguishingMarks() { return distinguishingMarks; }
    public void setDistinguishingMarks(String distinguishingMarks) { this.distinguishingMarks = distinguishingMarks; }

    public String getConditions() { return conditions; }
    public void setConditions(String conditions) { this.conditions = conditions; }

    @NonNull
    public String getStatus() { return status; }
    public void setStatus(@NonNull String status) { this.status = status; }

    public boolean isDeceased() { return isDeceased; }
    public void setDeceased(boolean deceased) { isDeceased = deceased; }
}
