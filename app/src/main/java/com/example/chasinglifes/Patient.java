package com.example.chasinglifes;

// Semplice classe Java, SENZA annotazioni di Room.
public class Patient {

    private String name;
    private String surname;
    private String distinguishingMarks;
    private String photoUri; // String, non Uri, per semplicità
    private String conditions;
    private boolean unidentified;
    private boolean deceased;

    public Patient(String name, String surname, String distinguishingMarks, String conditions, boolean unidentified, boolean deceased) {
        this.name = name;
        this.surname = surname;
        this.distinguishingMarks = distinguishingMarks;
        this.conditions = conditions;
        this.unidentified = unidentified;
        this.deceased = deceased;
    }

    // Getters
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getDistinguishingMarks() { return distinguishingMarks; }
    public String getPhotoUri() { return photoUri; }
    public String getConditions() { return conditions; }
    public boolean isUnidentified() { return unidentified; }
    public boolean isDeceased() { return deceased; }
}
