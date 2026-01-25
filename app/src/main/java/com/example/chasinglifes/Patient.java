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
                // L'operatore che ha inserito/modificato il paziente
                @ForeignKey(entity = User.class, parentColumns = "username", childColumns = "operatorUsername", onDelete = ForeignKey.SET_NULL),
                // L'utente che ha segnalato il disperso
                @ForeignKey(entity = User.class, parentColumns = "username", childColumns = "reporterUsername", onDelete = ForeignKey.SET_NULL)
        },
        indices = {@Index("sessionCode"), @Index("operatorUsername"), @Index("reporterUsername")}
)
public class Patient implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    private String sessionCode;
    private String operatorUsername;
    private String reporterUsername; // NUOVO CAMPO

    private String name;
    private String surname;
    private String distinguishingMarks;
    private String conditions;
    private String hospital;

    @NonNull
    private String status;
    private boolean isDeceased;

    public Patient() {}

    // --- Getters e Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSessionCode() { return sessionCode; }
    public void setSessionCode(String sessionCode) { this.sessionCode = sessionCode; }

    public String getOperatorUsername() { return operatorUsername; }
    public void setOperatorUsername(String operatorUsername) { this.operatorUsername = operatorUsername; }

    public String getReporterUsername() { return reporterUsername; }
    public void setReporterUsername(String reporterUsername) { this.reporterUsername = reporterUsername; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getDistinguishingMarks() { return distinguishingMarks; }
    public void setDistinguishingMarks(String distinguishingMarks) { this.distinguishingMarks = distinguishingMarks; }

    public String getConditions() { return conditions; }
    public void setConditions(String conditions) { this.conditions = conditions; }

    public String getHospital() { return hospital; }
    public void setHospital(String hospital) { this.hospital = hospital; }

    @NonNull
    public String getStatus() { return status; }
    public void setStatus(@NonNull String status) { this.status = status; }

    public boolean isDeceased() { return isDeceased; }
    public void setDeceased(boolean deceased) { isDeceased = deceased; }
}
