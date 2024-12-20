package com.example.connectapp;

import android.content.pm.PackageManager;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_CREATE_PROFILE = 1;
    public static final int REQUEST_CODE_EDIT_PROFILE = 2;
    public static final int REQUEST_CODE_IMPORT_CONTACT = 3;
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
        fabAddProfile.setOnClickListener(v -> showProfileOptionsDialog());
    }

    private void showProfileOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Option wählen");

        String[] options = {"Profil manuell erstellen", "Kontakte importieren"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Profil manuell erstellen
                    Intent intent = new Intent(MainActivity.this, CreateProfileActivity.class);
                    intent.putParcelableArrayListExtra("existingProfiles", profileList);
                    startActivityForResult(intent, REQUEST_CODE_CREATE_PROFILE);
                    break;

                case 1: // Kontakte importieren
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_IMPORT_CONTACT);
                        } else {
                            importContacts();
                        }
                    } else {
                        importContacts();
                    }
                    break;
            }
        });

        builder.create().show();
    }

    private void importContacts() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_CODE_IMPORT_CONTACT);
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

        // Wenn Kontakte importiert wurden
        if (requestCode == REQUEST_CODE_IMPORT_CONTACT && resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            if (contactUri != null) {
                String contactName = null;
                String contactPhone = null;
                String contactEmail = null;
                String contactAddress = null;

                // Abrufen der Kontaktinformationen
                Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
                if (cursor != null) {
                    try {
                        if (cursor.moveToFirst()) {
                            int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                            contactName = cursor.getString(nameIndex);

                            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                            int hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
                            if (cursor.getInt(hasPhoneIndex) > 0) {
                                // Telefonnummer abrufen
                                Cursor phoneCursor = getContentResolver().query(
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                        new String[]{id},
                                        null
                                );
                                if (phoneCursor != null) {
                                    try {
                                        if (phoneCursor.moveToFirst()) {
                                            int phoneIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                                            contactPhone = phoneCursor.getString(phoneIndex);
                                        }
                                    } finally {
                                        phoneCursor.close();
                                    }
                                }
                            }

                            // E-Mail-Adresse abrufen
                            Cursor emailCursor = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                                    new String[]{id},
                                    null
                            );
                            if (emailCursor != null) {
                                try {
                                    if (emailCursor.moveToFirst()) {
                                        int emailIndex = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
                                        contactEmail = emailCursor.getString(emailIndex);
                                    }
                                } finally {
                                    emailCursor.close();
                                }
                            }

                            // Adresse abrufen
                            Cursor addressCursor = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = ?",
                                    new String[]{id},
                                    null
                            );
                            if (addressCursor != null) {
                                try {
                                    if (addressCursor.moveToFirst()) {
                                        int addressIndex = addressCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);
                                        contactAddress = addressCursor.getString(addressIndex);
                                    }
                                } finally {
                                    addressCursor.close();
                                }
                            }
                        }
                    } finally {
                        cursor.close();
                    }

                    if (contactName != null) {
                        String[] nameParts = contactName.split(" ");
                        String firstName = nameParts.length > 1 ? nameParts[0] : contactName;
                        String lastName = nameParts.length > 1 ? nameParts[1] : "";

                        Intent intent = new Intent(MainActivity.this, CreateProfileActivity.class);
                        intent.putExtra("firstName", firstName);
                        intent.putExtra("lastName", lastName);
                        intent.putExtra("phone", contactPhone);
                        intent.putExtra("email", contactEmail);
                        intent.putExtra("address", contactAddress);
                        startActivityForResult(intent, REQUEST_CODE_CREATE_PROFILE);
                    }
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

    private void setDefaultProfile(int position) {
        if (position < 0 || position >= profileList.size()) return;

        if (defaultProfilePosition >= 0 && defaultProfilePosition < profileList.size()) {
            profileList.get(defaultProfilePosition).setDefaultProfile(false);
        }

        defaultProfilePosition = position;
        profileList.get(position).setDefaultProfile(true);

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
                JSONObject jsonObject = profile.toJson();
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
                    Profile profile = Profile.fromJson(jsonObject);
                    profileList.add(profile);
                }

                defaultProfilePosition = sharedPreferences.getInt(DEFAULT_PROFILE_KEY, -1);

                if (defaultProfilePosition >= 0 && defaultProfilePosition < profileList.size()) {
                    Profile defaultProfile = profileList.remove(defaultProfilePosition);
                    profileList.add(0, defaultProfile);
                } else {
                    defaultProfilePosition = -1;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void editProfile(int position) {
        // Profil aus der Liste holen
        Profile profileToEdit = profileList.get(position);

        // Intent erstellen, um das Profil zu bearbeiten
        Intent intent = new Intent(MainActivity.this, CreateProfileActivity.class);
        intent.putExtra("editProfile", profileToEdit); // Profil als Extra übergeben
        intent.putExtra("profilePosition", position); // Position des Profils übergeben
        startActivityForResult(intent, REQUEST_CODE_EDIT_PROFILE); // Für den Resultat-Callback
    }

    public void deleteProfile(int position) {
        // Profil aus der Liste entfernen
        if (position >= 0 && position < profileList.size()) {
            profileList.remove(position);
            profileAdapter.notifyDataSetChanged(); // Adapter benachrichtigen, damit die Ansicht aktualisiert wird
            saveProfiles(); // Profile in den SharedPreferences speichern
            Toast.makeText(this, "Profil gelöscht", Toast.LENGTH_SHORT).show();
        }
    }
}
