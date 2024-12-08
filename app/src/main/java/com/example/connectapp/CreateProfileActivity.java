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

        // Überprüfen, ob wir ein Profil zum Bearbeiten haben
        Intent intent = getIntent();
        if (intent.hasExtra("editProfile")) {
            profileToEdit = intent.getParcelableExtra("editProfile");
            profilePosition = intent.getIntExtra("profilePosition", -1);

            // Felder mit den Daten des zu bearbeitenden Profils füllen
            if (profileToEdit != null) {
                profileNameEditText.setText(profileToEdit.getProfileName());
                nameEditText.setText(profileToEdit.getName());
                lastNameEditText.setText(profileToEdit.getLastName());
                phoneEditText.setText(profileToEdit.getPhone());
                emailEditText.setText(profileToEdit.getEmail());
                addressEditText.setText(profileToEdit.getAddress());
            }
        }

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

        // Validierung der Eingaben:
        // Nur Vorname und Telefonnummer sind erforderlich, alle anderen Felder sind optional
        if (name.isEmpty()) {
            Toast.makeText(this, "Bitte gib deinen Vornamen ein!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.isEmpty() && email.isEmpty()) {
            Toast.makeText(this, "Bitte gib deine Telefonnummer oder E-Mail-Adresse ein!", Toast.LENGTH_SHORT).show();
            return;
        }
        // Wenn Profilname nicht angegeben, auf Unbenannt setzen
        if (profileName.isEmpty()) {
            profileName = generateUniqueProfileName("Unbenannt");
        }

        // Neues oder bearbeitetes Profil erstellen
        Profile newProfile = new Profile(profileName, name, lastName, phone, email, address);

        // Wenn wir ein Profil bearbeiten, die Position übergeben
        Intent resultIntent = new Intent();
        resultIntent.putExtra("newProfile", newProfile);
        resultIntent.putExtra("isDefault", isDefault); // Ist es das Standardprofil?

        // Falls es bearbeitet wird, auch die Position weitergeben
        if (profilePosition != -1) {
            resultIntent.putExtra("profilePosition", profilePosition);
        }

        setResult(RESULT_OK, resultIntent); // Ergebnis für die aufrufende Activity setzen

        finish(); // Beendet die Activity und kehrt zur aufrufenden zurück
    }

    private String generateUniqueProfileName(String baseName) {
        Intent intent = getIntent();
        ArrayList<Profile> existingProfiles = intent.getParcelableArrayListExtra("existingProfiles");
        if (existingProfiles == null) {
            return baseName; // Kein Konflikt, Basisname verwenden
        }

        String uniqueName = baseName;
        int counter = 1;

        // Überprüfe, ob der Name bereits existiert
        while (profileExists(uniqueName, existingProfiles)) {
            uniqueName = baseName + " (" + counter + ")";
            counter++;
        }

        return uniqueName;
    }

    private boolean profileExists(String profileName, ArrayList<Profile> existingProfiles) {
        for (Profile profile : existingProfiles) {
            if (profile.getProfileName().equals(profileName)) {
                return true;
            }
        }
        return false;
    }
}
