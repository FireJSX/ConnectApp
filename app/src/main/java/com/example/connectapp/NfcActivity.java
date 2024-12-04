package com.example.connectapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class NfcActivity extends AppCompatActivity {

    private static final String TAG = "NfcActivity";
    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        // NFC-Adapter initialisieren
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // Überprüfe NFC-Verfügbarkeit
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC wird auf diesem Gerät nicht unterstützt", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, "Bitte NFC in den Geräteeinstellungen aktivieren.", Toast.LENGTH_SHORT).show();
        }

        // Profildaten aus dem Intent abrufen
        String profileName = getIntent().getStringExtra("profile_name");
        String firstName = getIntent().getStringExtra("profile_first_name");
        String lastName = getIntent().getStringExtra("profile_last_name");
        String phone = getIntent().getStringExtra("profile_phone");
        String email = getIntent().getStringExtra("profile_email");
        String address = getIntent().getStringExtra("profile_address");

        // Profildaten in der Oberfläche anzeigen
        displayProfileData(profileName, firstName, lastName, phone, email, address);

        // QR-Code generieren
        generateQRCode(profileName, firstName, lastName, phone, email, address);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableForegroundDispatch();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispatch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.d(TAG, "onNewIntent aufgerufen");
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            Toast.makeText(this, "NFC-Tag erkannt!", Toast.LENGTH_SHORT).show();

            // Tag auslesen (falls vorhanden)
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                Log.d(TAG, "NFC-Tag ID: " + bytesToHex(tag.getId()));
            }

            // Überprüfen, ob eine NDEF-Nachricht vorhanden ist
            if (intent.hasExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)) {
                NdefMessage[] messages = (NdefMessage[]) intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

                if (messages != null && messages.length > 0) {
                    String receivedMessage = new String(messages[0].getRecords()[0].getPayload());
                    Toast.makeText(this, "Empfangene Nachricht: " + receivedMessage, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void enableForegroundDispatch() {
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        IntentFilter[] filters = new IntentFilter[]{
                new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        };

        String[][] techLists = new String[][]{};

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, techLists);
    }

    private void disableForegroundDispatch() {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    private void displayProfileData(String profileName, String firstName, String lastName, String phone, String email, String address) {
        ((TextView) findViewById(R.id.nfc_status)).setText("NFC is enabled!");
        ((TextView) findViewById(R.id.profile_name)).setText("Profile Name: " + profileName);
        ((TextView) findViewById(R.id.first_name)).setText("First Name: " + firstName);
        ((TextView) findViewById(R.id.last_name)).setText("Last Name: " + lastName);
        ((TextView) findViewById(R.id.phone)).setText("Phone: " + phone);
        ((TextView) findViewById(R.id.email)).setText("Email: " + email);
        ((TextView) findViewById(R.id.address)).setText("Address: " + address);
    }

    private void generateQRCode(String profileName, String firstName, String lastName, String phone, String email, String address) {
        // vCard-Daten für den QR-Code
        String vCardData = "BEGIN:VCARD\n" +
                "VERSION:3.0\n" +
                "N:" + lastName + ";" + firstName + ";;;\n" +  // Nachname und Vorname
                "FN:" + firstName + " " + lastName + "\n" +   // Vollständiger Name
                "TEL;TYPE=CELL:" + phone + "\n" +            // Telefonnummer
                "EMAIL:" + email + "\n" +                    // E-Mail-Adresse
                "ADR:;;" + address + "\n" +                  // Adresse
                "END:VCARD";

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(vCardData, BarcodeFormat.QR_CODE, 400, 400);

            ImageView qrCodeView = findViewById(R.id.qr_code_view);
            qrCodeView.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "QR-Code-Generierung fehlgeschlagen", e);
        }
    }


    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
