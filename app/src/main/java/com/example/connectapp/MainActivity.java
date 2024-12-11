package com.example.connectapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CREATE_PROFILE = 1;
    private static final int REQUEST_CODE_EDIT_PROFILE = 2;
    private static final String PREFS_NAME = "profiles_prefs";
    private static final String PROFILES_KEY = "profiles_list";
    private static final String DEFAULT_PROFILE_KEY = "default_profile";

    private ArrayList<Profile> profileList = new ArrayList<>();
    private ProfileAdapter profileAdapter;
    private int defaultProfilePosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView profileListView = findViewById(R.id.profile_list_view);
        FloatingActionButton fabAddProfile = findViewById(R.id.fab_add_profile);

        // Lade gespeicherte Profile
        loadProfiles();

        // Adapter initialisieren
        profileAdapter = new ProfileAdapter(this, profileList);
        profileListView.setAdapter(profileAdapter);

        // Profil auswählen
        profileListView.setOnItemClickListener((parent, view, position, id) -> {
            Profile selectedProfile = profileList.get(position);
            openNfcActivity(selectedProfile);
        });

        // Neues Profil hinzufügen
        fabAddProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateProfileActivity.class);
            intent.putParcelableArrayListExtra("existingProfiles", profileList);
            startActivityForResult(intent, REQUEST_CODE_CREATE_PROFILE);
        });
    }

    private void openNfcActivity(Profile profile) {
        Intent intent = new Intent(MainActivity.this, NfcActivity.class);
        intent.putExtra("profile_name", profile.getProfileName());
        intent.putExtra("profile_first_name", profile.getName());
        intent.putExtra("profile_last_name", profile.getLastName());
        intent.putExtra("profile_phone", profile.getPhone());
        intent.putExtra("profile_email", profile.getEmail());
        intent.putExtra("profile_address", profile.getAddress());
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == REQUEST_CODE_CREATE_PROFILE || requestCode == REQUEST_CODE_EDIT_PROFILE)
                && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("newProfile")) {
                Profile newProfile = data.getParcelableExtra("newProfile");
                if (newProfile != null) {
                    if (requestCode == REQUEST_CODE_CREATE_PROFILE) {
                        profileList.add(newProfile);
                        handleDefaultProfileFlag(newProfile, profileList.size() - 1);
                    } else {
                        int position = data.getIntExtra("profilePosition", -1);
                        if (position != -1) {
                            profileList.set(position, newProfile);
                            handleDefaultProfileFlag(newProfile, position);
                        }
                    }
                    profileAdapter.notifyDataSetChanged();
                    saveProfiles();
                    Toast.makeText(this, "Änderungen gespeichert!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void handleDefaultProfileFlag(Profile profile, int position) {
        if (profile.isDefaultProfile()) {
            setDefaultProfile(position);
        } else if (defaultProfilePosition == position) {
            defaultProfilePosition = -1;
            saveDefaultProfileToPreferences(-1);
        }
    }

    public void editProfile(int position) {
        Profile profileToEdit = profileList.get(position);
        Intent intent = new Intent(MainActivity.this, CreateProfileActivity.class);
        intent.putExtra("editProfile", profileToEdit);
        intent.putExtra("profilePosition", position);
        startActivityForResult(intent, REQUEST_CODE_EDIT_PROFILE);
    }

    public void deleteProfile(int position) {
        profileList.remove(position);
        if (position == defaultProfilePosition) {
            defaultProfilePosition = -1;
            saveDefaultProfileToPreferences(-1);
        } else if (position < defaultProfilePosition) {
            defaultProfilePosition--;
        }
        profileAdapter.notifyDataSetChanged();
        saveProfiles();
        Toast.makeText(this, "Profil gelöscht!", Toast.LENGTH_SHORT).show();
    }

    private void setDefaultProfile(int position) {
        if (position < 0 || position >= profileList.size()) return;

        // Deaktiviere das Standardprofil, wenn es existiert
        if (defaultProfilePosition >= 0 && defaultProfilePosition < profileList.size()) {
            profileList.get(defaultProfilePosition).setDefaultProfile(false);
        }

        // Setze das neue Standardprofil
        defaultProfilePosition = position;
        profileList.get(position).setDefaultProfile(true);

        // Speichern des Standardprofils in SharedPreferences
        saveDefaultProfileToPreferences(position);
        profileAdapter.notifyDataSetChanged();
    }

    private void saveDefaultProfileToPreferences(int position) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(DEFAULT_PROFILE_KEY, position).apply();
    }

    private void saveProfiles() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        JSONArray jsonArray = new JSONArray();
        for (Profile profile : profileList) {
            try {
                JSONObject jsonObject = profile.toJson();  // Sicherstellen, dass Profile zu JSON konvertiert werden
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        editor.putString(PROFILES_KEY, jsonArray.toString());
        editor.apply();
    }

    private void loadProfiles() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String profilesString = sharedPreferences.getString(PROFILES_KEY, null);

        if (profilesString != null) {
            try {
                JSONArray jsonArray = new JSONArray(profilesString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Profile profile = Profile.fromJson(jsonObject);  // Verwenden der von dir definierten fromJson-Methode
                    profileList.add(profile);
                }

                // Standardprofil-Position laden
                defaultProfilePosition = sharedPreferences.getInt(DEFAULT_PROFILE_KEY, -1);

                // Überprüfen, ob das geladene Standardprofil gültig ist
                if (defaultProfilePosition >= 0 && defaultProfilePosition < profileList.size()) {
                    Profile defaultProfile = profileList.remove(defaultProfilePosition);
                    profileList.add(0, defaultProfile);  // Standardprofil an den Anfang verschieben
                } else {
                    // Falls die Position ungültig ist, auf -1 setzen
                    defaultProfilePosition = -1;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
