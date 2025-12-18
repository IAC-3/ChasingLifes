package com.example.chasinglifes;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PatientDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Patient patient);

    @Update
    void update(Patient patient);

    @Delete
    void delete(Patient patient);

    // Query generica per la sessione (usata dall'utente)
    @Query("SELECT * FROM patients WHERE sessionCode = :sessionCode ORDER BY id DESC")
    List<Patient> getPatientsBySession(String sessionCode);

    // --- NUOVO METODO SPECIFICO PER L'OPERATORE ---
    /**
     * Seleziona solo i pazienti (non dispersi) inseriti da un operatore specifico in una data sessione.
     */
    @Query("SELECT * FROM patients WHERE sessionCode = :sessionCode AND operatorUsername = :operatorUsername AND status != 'MISSING' ORDER BY id DESC")
    List<Patient> getOperatorPatientsForSession(String sessionCode, String operatorUsername);


    @Query("SELECT * FROM patients WHERE status = 'MISSING' AND name LIKE :name AND surname LIKE :surname")
    List<Patient> findMissingByName(String name, String surname);

    @Query("SELECT * FROM patients WHERE status = 'MISSING' AND distinguishingMarks LIKE :marks")
    List<Patient> findMissingByMarks(String marks);
}
