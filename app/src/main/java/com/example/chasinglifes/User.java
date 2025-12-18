package com.example.chasinglifes;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {

    @PrimaryKey
    @NonNull
    private String username;

    @NonNull
    private String password;

    private boolean isOperator;

    public User(@NonNull String username, @NonNull String password, boolean isOperator) {
        this.username = username;
        this.password = password;
        this.isOperator = isOperator;
    }

    // Getters
    @NonNull
    public String getUsername() {
        return username;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    public boolean isOperator() {
        return isOperator;
    }
}
