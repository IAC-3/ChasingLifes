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

    @Query("SELECT * FROM patients WHERE sessionCode = :sessionCode ORDER BY id DESC")
    List<Patient> getPatientsBySession(String sessionCode);

    @Query("SELECT * FROM patients WHERE sessionCode = :sessionCode AND operatorUsername = :operatorUsername AND status != 'MISSING' ORDER BY id DESC")
    List<Patient> getOperatorPatientsForSession(String sessionCode, String operatorUsername);

    // --- METODI DI RICERCA RIPRISTINATI ---
    @Query("SELECT * FROM patients WHERE status = 'MISSING' AND name = :name AND surname = :surname")
    List<Patient> findMissingByExactName(String name, String surname);

    @Query("SELECT * FROM patients WHERE status = 'MISSING' AND distinguishingMarks = :marks")
    List<Patient> findMissingByExactMarks(String marks);
}
