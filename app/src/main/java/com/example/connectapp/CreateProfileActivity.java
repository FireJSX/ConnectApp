package com.example.connectapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class CreateProfileActivity extends AppCompatActivity {
    private EditText profileNameEditText;
    private EditText nameEditText;
    private EditText lastNameEditText;
    private EditText phoneEditText;
    private EditText emailEditText;
    private EditText addressEditText;
    private CheckBox defaultProfileCheckBox; // CheckBox für Standardprofil
    private Button saveProfileButton;

    private Profile profileToEdit; // Variable für das zu bearbeitende Profil
    private int profilePosition = -1; // Position des zu bearbeitenden Profils, standardmäßig -1 für "neu"
    private ArrayList<Profile> existingProfiles; // Vorhandene Profile

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        // Initialisieren der Views
        profileNameEditText = findViewById(R.id.editTextProfileName);
        nameEditText = findViewById(R.id.input_name);
        lastNameEditText = findViewById(R.id.editTextLastName);
        phoneEditText = findViewById(R.id.input_phone);
        emailEditText = findViewById(R.id.input_email);
        addressEditText = findViewById(R.id.input_address);
        defaultProfileCheckBox = findViewById(R.id.checkbox_default_profile); // CheckBox initialisieren
        saveProfileButton = findViewById(R.id.save_profile_button);

        // Profile aus dem Intent laden
        Intent intent = getIntent();
        if (intent.hasExtra("firstName") && intent.hasExtra("lastName")) {
            // Wenn Kontaktdaten übergeben wurden, füllen wir die Felder
            String firstName = intent.getStringExtra("firstName");
            String lastName = intent.getStringExtra("lastName");
            String phone = intent.getStringExtra("phone");
            String email = intent.getStringExtra("email");
            String address = intent.getStringExtra("address");

            // Felder mit den Daten des Kontakts füllen
            nameEditText.setText(firstName);
            lastNameEditText.setText(lastName);
            phoneEditText.setText(phone);
            emailEditText.setText(email);
            addressEditText.setText(address);
        }
        if (intent.hasExtra("editProfile")) {
            profileToEdit = intent.getParcelableExtra("editProfile");
            profilePosition = intent.getIntExtra("profilePosition", -1);

            if (profileToEdit != null) {
                // Felder mit den Daten des zu bearbeitenden Profils füllen
                profileNameEditText.setText(profileToEdit.getProfileName());
                nameEditText.setText(profileToEdit.getName());
                lastNameEditText.setText(profileToEdit.getLastName());
                phoneEditText.setText(profileToEdit.getPhone());
                emailEditText.setText(profileToEdit.getEmail());
                addressEditText.setText(profileToEdit.getAddress());
                defaultProfileCheckBox.setChecked(profileToEdit.isDefaultProfile());
            }
        }

        // Vorhandene Profile laden
        existingProfiles = intent.getParcelableArrayListExtra("existingProfiles");

        // Click-Listener für den "Speichern"-Button
        saveProfileButton.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        // Eingaben sammeln
        String profileName = profileNameEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        boolean isDefault = defaultProfileCheckBox.isChecked(); // Zustand der CheckBox abfragen

        // Validierung der Eingaben
        if (name.isEmpty()) {
            Toast.makeText(this, "Bitte gib deinen Vornamen ein!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.isEmpty() && email.isEmpty()) {
            Toast.makeText(this, "Bitte gib deine Telefonnummer oder E-Mail-Adresse ein!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (profileName.isEmpty()) {
            profileName = generateUniqueProfileName("Unbenannt");
        }

        // Wenn das neue Profil als Standardprofil gesetzt wird, entferne Standardstatus bei anderen
        if (isDefault) {
            for (Profile profile : existingProfiles) {
                profile.setDefaultProfile(false);
            }
        }

        // Neues oder bearbeitetes Profil erstellen
        Profile newProfile = new Profile(profileName, name, lastName, phone, email, address, isDefault);

        // Intent für das Ergebnis erstellen
        Intent resultIntent = new Intent();
        resultIntent.putExtra("newProfile", newProfile);
        resultIntent.putExtra("isDefault", isDefault);

        // Falls es bearbeitet wird, auch die Position weitergeben
        if (profilePosition != -1) {
            resultIntent.putExtra("profilePosition", profilePosition);
        }

        setResult(RESULT_OK, resultIntent); // Ergebnis für die aufrufende Activity setzen

        finish(); // Beendet die Activity und kehrt zur aufrufenden zurück
    }

    private String generateUniqueProfileName(String baseName) {
        String uniqueName = baseName;
        int counter = 1;

        // Überprüfe, ob der Name bereits existiert
        while (profileExists(uniqueName)) {
            uniqueName = baseName + " (" + counter + ")";
            counter++;
        }

        return uniqueName;
    }

    private boolean profileExists(String profileName) {
        for (Profile profile : existingProfiles) {
            if (profile.getProfileName().equals(profileName)) {
                return true;
            }
        }
        return false;
    }
}
