package com.example.connectapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CreateProfileActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText phoneEditText;
    private EditText emailEditText;
    private EditText addressEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        nameEditText = findViewById(R.id.input_name);
        phoneEditText = findViewById(R.id.input_phone);
        emailEditText = findViewById(R.id.input_email);
        addressEditText = findViewById(R.id.input_address);
    }

    public void saveProfile(View view) {
        // Daten sammeln und Profil erstellen
        String name = nameEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String address = addressEditText.getText().toString();

        // Profil hinzufügen (optional: überprüfen, ob alle Felder ausgefüllt sind)
        if (!name.isEmpty() && !phone.isEmpty()) {
            // Nach dem Erstellen eines neuen Profils
            Profile newProfile = new Profile(name, phone, email, address);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("newProfile", newProfile);  // newProfile ist ein Profile-Objekt
            setResult(RESULT_OK, resultIntent);  // Das Ergebnis für die aufrufende Activity setzen
            finish();  // Beendet die Activity und kehrt zur aufrufenden zurück
        } else {
            Toast.makeText(this, "Bitte alle Felder ausfüllen!", Toast.LENGTH_SHORT).show();
        }
    }
}
