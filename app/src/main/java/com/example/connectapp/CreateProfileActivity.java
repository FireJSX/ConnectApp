package com.example.connectapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class CreateProfileActivity extends AppCompatActivity {
    private EditText profileNameEditText;
    private EditText nameEditText;
    private EditText lastNameEditText;
    private EditText phoneEditText;
    private EditText emailEditText;
    private EditText streetEditText;
    private EditText houseNumberEditText;
    private EditText postalCodeEditText;
    private EditText cityEditText;
    private EditText countryEditText;
    private CheckBox defaultProfileCheckBox; // CheckBox für Standardprofil
    private Button saveProfileButton;
    private TextView addressTitle; // Der Button "+ Adresse"
    private LinearLayout addressLayout; // Die Layout-Gruppe für die Adresseingabefelder

    private Profile profileToEdit; // Variable für das zu bearbeitende Profil
    private int profilePosition = -1; // Position des zu bearbeitenden Profils, standardmäßig -1 für "neu"
    private ArrayList<Profile> existingProfiles; // Vorhandene Profile
    private ProfileSpinnerAdapter profileSpinnerAdapter;  // Adapter für den Spinner
    private Spinner profileSpinner;  // Spinner für Profile

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
        streetEditText = findViewById(R.id.input_street);
        houseNumberEditText = findViewById(R.id.input_house_number);
        postalCodeEditText = findViewById(R.id.input_postal_code);
        cityEditText = findViewById(R.id.input_city);
        countryEditText = findViewById(R.id.input_country);
        defaultProfileCheckBox = findViewById(R.id.checkbox_default_profile); // CheckBox initialisieren
        saveProfileButton = findViewById(R.id.save_profile_button);

        // Der "+ Adresse"-TextView und das Layout für die Adresse
        addressTitle = findViewById(R.id.address_title);
        addressLayout = findViewById(R.id.address_layout);

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
            if (address != null) {
                addressTitle.setText("- Adresse");
                addressLayout.setVisibility(View.VISIBLE); // Falls Adresse übergeben wurde, sichtbar machen
                String[] addressParts = address.split(", ");
                if (addressParts.length > 0) streetEditText.setText(addressParts[0]);
                if (addressParts.length > 1) houseNumberEditText.setText(addressParts[1]);
                if (addressParts.length > 2) postalCodeEditText.setText(addressParts[2]);
                if (addressParts.length > 3) cityEditText.setText(addressParts[3]);
                if (addressParts.length > 4) countryEditText.setText(addressParts[4]);
            }
        }
        if (intent.hasExtra("editProfile")) {
            profileToEdit = intent.getParcelableExtra("editProfile");
            profilePosition = intent.getIntExtra("profilePosition", -1);

            Log.d("CreateProfileActivity", "Profile Position: " + profilePosition); // Debugging

            if (profileToEdit != null) {
                Log.d("CreateProfileActivity", "Profile found, loading data..."); // Debugging

                // Felder mit den Daten des zu bearbeitenden Profils füllen
                profileNameEditText.setText(profileToEdit.getProfileName());
                nameEditText.setText(profileToEdit.getName());
                lastNameEditText.setText(profileToEdit.getLastName());
                phoneEditText.setText(profileToEdit.getPhone());
                emailEditText.setText(profileToEdit.getEmail());

                // Adressfelder füllen
                streetEditText.setText(profileToEdit.getStreet());
                houseNumberEditText.setText(profileToEdit.getHouseNumber());
                postalCodeEditText.setText(profileToEdit.getPostalCode());
                cityEditText.setText(profileToEdit.getCity());
                countryEditText.setText(profileToEdit.getCountry());

                // Checkbox-Status setzen
                defaultProfileCheckBox.setChecked(profileToEdit.isDefaultProfile());
            } else {
                Log.e("CreateProfileActivity", "Profile to edit is null.");
            }
        }

        // Vorhandene Profile laden
        existingProfiles = intent.getParcelableArrayListExtra("existingProfiles");

        // Falls existingProfiles null ist, initialisiere es als leere Liste
        if (existingProfiles == null) {
            existingProfiles = new ArrayList<>();
        }


        // Click-Listener für den "+ Adresse"-Button
        addressTitle.setOnClickListener(v -> {
            if (addressLayout.getVisibility() == View.GONE) {
                addressLayout.setVisibility(View.VISIBLE);
                addressTitle.setText("- Adresse");
            } else {
                addressLayout.setVisibility(View.GONE);
                addressTitle.setText("+ Adresse");
            }
        });

        // Click-Listener für den "Speichern"-Button
        saveProfileButton.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        // Eingaben sammeln
        String profileName = profileNameEditText.getText().toString().trim();
        profileName = generateUniqueProfileName(profileName);
        String name = nameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String street = streetEditText.getText().toString().trim();
        String houseNumber = houseNumberEditText.getText().toString().trim();
        String postalCode = postalCodeEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();
        String country = countryEditText.getText().toString().trim();

        // Adresse zusammenstellen
        String address = street + " " + houseNumber;
        if (!postalCode.isEmpty()) {
            address += " " + postalCode;
        }
        if (!city.isEmpty()) {
            address += " " + city;
        }
        if (!country.isEmpty()) {
            address += " " + country;
        }

        // Bereinige die Adresse von überflüssigen Leerzeichen und Kommas
        address = address.replaceAll("\\s+", " ").trim();

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
        Profile newProfile = new Profile(profileName, name, lastName, phone, email, street, houseNumber, postalCode, city, country, isDefault);

        // Wenn das Profil als Standardprofil gesetzt wurde, an den Anfang der Liste verschieben
        if (isDefault) {
            // Entferne alle Standardprofile von der Liste, falls bereits eines existiert
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                existingProfiles.removeIf(Profile::isDefaultProfile);
            }
            // Füge das neue Profil an den Anfang der Liste
            existingProfiles.add(0, newProfile);
        } else {
            // Wenn kein Standardprofil gesetzt ist, füge es einfach ans Ende
            existingProfiles.add(newProfile);
        }

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


    private String generateUniqueProfileName(String profileName) {
        // Hier kannst du eine Methode einbauen, um einen einzigartigen Profilnamen zu generieren,
        // falls der Name bereits existiert.
        return profileName;
    }

    private String getDefaultProfileName() {
        for (Profile profile : existingProfiles) {
            if (profile.isDefaultProfile()) {
                return profile.getProfileName();
            }
        }
        return "";
    }
}
