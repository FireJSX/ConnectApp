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

    private ArrayList<Profile> profileList = new ArrayList<>();
    private ProfileAdapter profileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView profileListView = findViewById(R.id.profile_list_view);
        FloatingActionButton fabAddProfile = findViewById(R.id.fab_add_profile);

        // Lade gespeicherte Profile
        loadProfiles();

        // Verwende den benutzerdefinierten Adapter
        profileAdapter = new ProfileAdapter(this, profileList);
        profileListView.setAdapter(profileAdapter);

        // Klick auf ein Profil
        profileListView.setOnItemClickListener((parent, view, position, id) -> {
            Profile selectedProfile = profileList.get(position);
            openNfcActivity(selectedProfile);
        });

        // Neues Profil hinzufügen
        fabAddProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateProfileActivity.class);
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
                        // Neues Profil hinzufügen
                        profileList.add(newProfile);
                    } else {
                        // Profil bearbeiten
                        int position = data.getIntExtra("profilePosition", -1);
                        if (position != -1) {
                            profileList.set(position, newProfile);
                        }
                    }
                    profileAdapter.notifyDataSetChanged();
                    saveProfiles();
                    Toast.makeText(this, "Änderungen gespeichert!", Toast.LENGTH_SHORT).show();
                }
            }
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
        profileAdapter.notifyDataSetChanged();
        saveProfiles();
        Toast.makeText(this, "Profil gelöscht!", Toast.LENGTH_SHORT).show();
    }

    private void saveProfiles() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        JSONArray jsonArray = new JSONArray();
        for (Profile profile : profileList) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("profileName", profile.getProfileName());
                jsonObject.put("name", profile.getName());
                jsonObject.put("lastName", profile.getLastName());
                jsonObject.put("phone", profile.getPhone());
                jsonObject.put("email", profile.getEmail());
                jsonObject.put("address", profile.getAddress());
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
                    Profile profile = new Profile(
                            jsonObject.getString("profileName"),
                            jsonObject.getString("name"),
                            jsonObject.getString("lastName"),
                            jsonObject.getString("phone"),
                            jsonObject.getString("email"),
                            jsonObject.getString("address")
                    );
                    profileList.add(profile);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
