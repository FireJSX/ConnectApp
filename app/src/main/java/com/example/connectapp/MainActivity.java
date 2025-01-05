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
import android.util.Log;
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

        // Adapter initialisieren
        profileAdapter = new ProfileAdapter(this, profileList);
        profileListView.setAdapter(profileAdapter);

        loadProfiles();


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
        intent.putExtra("profile_street", profile.getStreet());
        intent.putExtra("profile_house_number", profile.getHouseNumber());
        intent.putExtra("profile_postal_code", profile.getPostalCode());
        intent.putExtra("profile_city", profile.getCity());
        intent.putExtra("profile_country", profile.getCountry());
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
                String contactStreet = null;
                String contactPostalCode = null;
                String contactCity = null;
                String contactCountry = null;
                String contactHouseNumber = null;

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
                                        int streetIndex = addressCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET);
                                        int postalCodeIndex = addressCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE);
                                        int cityIndex = addressCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY);
                                        int countryIndex = addressCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY);

                                        contactStreet = addressCursor.getString(streetIndex);
                                        contactHouseNumber = extractHouseNumber(contactStreet); // Hausnummer extrahieren
                                        contactStreet = removeHouseNumber(contactStreet); // Hausnummer aus Straße entfernen
                                        contactPostalCode = addressCursor.getString(postalCodeIndex);
                                        contactCity = addressCursor.getString(cityIndex);
                                        contactCountry = addressCursor.getString(countryIndex);
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
                        intent.putExtra("street", contactStreet);
                        intent.putExtra("houseNumber", contactHouseNumber);
                        intent.putExtra("postalCode", contactPostalCode);
                        intent.putExtra("city", contactCity);
                        intent.putExtra("country", contactCountry);
                        startActivityForResult(intent, REQUEST_CODE_CREATE_PROFILE);
                    }
                }
            }
        }
    }
    private String extractHouseNumber(String street) {
        if (street != null && street.matches(".*\\s\\d+[a-zA-Z]?$")) {
            return street.substring(street.lastIndexOf(" ") + 1);
        }
        return "";
    }

    private String removeHouseNumber(String street) {
        if (street != null && street.matches(".*\\s\\d+[a-zA-Z]?$")) {
            return street.substring(0, street.lastIndexOf(" "));
        }
        return street;
    }

    private void handleDefaultProfileFlag(Profile profile, int position) {
        // Wenn das Profil als Standard markiert wird
        if (profile.isDefaultProfile()) {
            // Nur das neue Standardprofil setzen, wenn es nicht bereits das Standardprofil ist
            if (defaultProfilePosition != position) {
                // Wenn bereits ein Standardprofil vorhanden ist, setzen wir es zurück
                if (defaultProfilePosition != -1) {
                    Profile oldDefaultProfile = profileList.get(defaultProfilePosition);
                    oldDefaultProfile.setDefaultProfile(false); // Das alte Standardprofil zurücksetzen
                }

                // Das neue Profil als Standardprofil setzen
                setDefaultProfile(position);

                // Das neue Standardprofil an den Anfang der Liste verschieben
                moveProfileToTop(position);
            }
        } else if (defaultProfilePosition == position) {
            // Wenn das Standardprofil nicht mehr als Standard markiert werden soll
            defaultProfilePosition = -1;
            saveDefaultProfileToPreferences(-1);
        }
    }


    private void moveProfileToTop(int position) {
        // Profil an den Anfang der Liste verschieben
        Profile profile = profileList.get(position);
        profileList.remove(position);
        profileList.add(0, profile); // Profil an den Anfang der Liste setzen

        // Wenn das Standardprofil verschoben wird, muss der Adapter neu geladen werden
        profileAdapter.notifyDataSetChanged();
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
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            // JSON-Array zum Speichern der Profile
            JSONArray jsonArray = new JSONArray();
            String defaultProfileName = ""; // Variable für den Namen des Standardprofils

            // Füge jedes Profil der JSON-Liste hinzu
            for (Profile profile : profileList) {
                JSONObject profileJson = profile.toJson(); // Konvertiere das Profile zu JSON
                jsonArray.put(profileJson);

                // Wenn es das Standardprofil ist, speichern wir den Namen separat
                if (profile.isDefaultProfile()) {
                    defaultProfileName = profile.getProfileName(); // Speichern des Namens des Standardprofils
                }
            }

            // Speichere die Liste der Profile und den Namen des Standardprofils
            editor.putString(PROFILES_KEY, jsonArray.toString());
            editor.putString(DEFAULT_PROFILE_KEY, defaultProfileName); // Speichern des Standardprofilnamens

            // Anwenden der Änderungen
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private void loadProfiles() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Log.d("SharedPreferences", "Stored data: " + sharedPreferences.getAll().toString());

        // Hole den gespeicherten String mit den Profilen
        String profilesString = sharedPreferences.getString(PROFILES_KEY, null);

        // Hole den Namen des Standardprofils (anstatt der Position)
        String defaultProfileName = sharedPreferences.getString(DEFAULT_PROFILE_KEY, null);

        // Wenn Profile vorhanden sind
        if (profilesString != null) {
            try {
                // Profile als JSONArray parsen
                JSONArray jsonArray = new JSONArray(profilesString);
                profileList.clear(); // Lösche die aktuelle Liste, bevor neue Profile hinzugefügt werden

                // Füge die Profile zur Liste hinzu
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Profile profile = Profile.fromJson(jsonObject); // Konvertiere das JSONObject zu einem Profile
                    profileList.add(profile); // Füge das Profile der Liste hinzu
                }

                // Wenn ein Standardprofil vorhanden ist
                if (defaultProfileName != null) {
                    // Suche das Standardprofil anhand des Namens
                    for (int i = 0; i < profileList.size(); i++) {
                        Profile profile = profileList.get(i);
                        if (profile.getProfileName().equals(defaultProfileName)) {
                            // Das Standardprofil an den Anfang verschieben
                            Profile defaultProfile = profileList.remove(i);
                            profileList.add(0, defaultProfile); // Füge es an den Anfang der Liste
                            break; // Profil gefunden, Schleife verlassen
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            // Wenn keine Profile vorhanden sind, setze das Standardprofil auf null
            defaultProfileName = null;
        }

        // Aktualisiere die Adapter-Daten
        profileAdapter.notifyDataSetChanged();
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