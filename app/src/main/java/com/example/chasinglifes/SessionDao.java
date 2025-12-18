package com.example.chasinglifes;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Session session);

    // Nuovo metodo per trovare una sessione tramite il suo codice
    @Query("SELECT * FROM sessions WHERE code = :code LIMIT 1")
    Session findByCode(String code);
}
