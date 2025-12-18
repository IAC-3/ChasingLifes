package com.example.chasinglifes;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Definiamo la classe come una tabella di nome "sessions"
@Entity(tableName = "sessions")
public class Session {

    // Il campo "code" è la chiave primaria e non può essere nullo.
    @PrimaryKey
    @NonNull
    private String code;

    // Costruttore per creare un oggetto Session
    public Session(@NonNull String code) {
        this.code = code;
    }

    // Metodo "getter" per leggere il valore del codice
    @NonNull
    public String getCode() {
        return this.code;
    }
}
