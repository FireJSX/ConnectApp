package com.example.connectapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.ContactsContract;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class ProfileCreationDialog {

    private Context context;
    private ArrayList<Profile> profileList;

    public ProfileCreationDialog(Context context, ArrayList<Profile> profileList) {
        this.context = context;
        this.profileList = profileList;
    }

    public void showProfileOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Option wählen");

        // Optionen definieren
        String[] options = {"Profil manuell erstellen", "Kontakt importieren"};

        // Klick-Listener für die Optionen
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Profil manuell erstellen
                    Intent intent = new Intent(context, CreateProfileActivity.class);
                    intent.putParcelableArrayListExtra("existingProfiles", profileList);
                    ((Activity) context).startActivityForResult(intent, MainActivity.REQUEST_CODE_CREATE_PROFILE);
                    break;

                case 1: // Kontakte importieren
                    // Berechtigungsprüfung hier anfordern
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                            // Berechtigung anfordern
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_CONTACTS}, MainActivity.REQUEST_CODE_IMPORT_CONTACT);
                        } else {
                            // Berechtigung wurde bereits erteilt, importiere Kontakte
                            importContacts();
                        }
                    } else {
                        // Keine Laufzeitberechtigung erforderlich für API < 23
                        importContacts();
                    }
                    break;
            }
        });

        // Dialog anzeigen
        builder.create().show();
    }

    private void importContacts() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        ((Activity) context).startActivityForResult(intent, MainActivity.REQUEST_CODE_IMPORT_CONTACT);
    }
}

