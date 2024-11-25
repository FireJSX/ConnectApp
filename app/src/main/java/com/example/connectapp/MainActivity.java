package com.example.connectapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Profile> profileList = new ArrayList<>();
    private ArrayAdapter<Profile> profileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView profileListView = findViewById(R.id.profile_list_view);
        FloatingActionButton fabAddProfile = findViewById(R.id.fab_add_profile);

        // Beispiel: Ein vorgegebenes Profil hinzufügen
        profileList.add(new Profile("Jonas", "1234", "jonas@email.com", "Musterstr. 1"));

        // Adapter für die ListView
        profileAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, profileList);
        profileListView.setAdapter(profileAdapter);

        // Klick auf ein Profil
        profileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                Profile selectedProfile = profileList.get(position);
                openNfcActivity(selectedProfile);  // NFC-Activity starten
            }
        });

        // Plus-Symbol für das Hinzufügen neuer Profile
        fabAddProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateProfileActivity.class);
                startActivityForResult(intent, 1);  // 1 ist der Request Code
            }
        });
    }

    private void openNfcActivity(Profile profile) {
        // Starte die NFC-Activity und übergebe das Profil
        Intent intent = new Intent(MainActivity.this, NfcActivity.class);
        intent.putExtra("profile_name", profile.getName());
        intent.putExtra("profile_phone", profile.getPhone());
        intent.putExtra("profile_email", profile.getEmail());
        intent.putExtra("profile_address", profile.getAddress());
        startActivity(intent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {  // 1 ist der Request-Code
            Profile newProfile = data.getParcelableExtra("newProfile");  // Hole das Profil-Objekt
            if (newProfile != null) {
                profileList.add(newProfile);  // Füge das neue Profil zur Liste hinzu
                profileAdapter.notifyDataSetChanged();  // Aktualisiere die Anzeige
            }
        }
    }

}

