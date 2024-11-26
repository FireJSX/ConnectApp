package com.example.connectapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NfcActivity extends AppCompatActivity {

    private static final String TAG = "NfcActivity";
    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        // NFC-Adapter initialisieren
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC wird auf diesem Gerät nicht unterstützt", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, "Bitte NFC in den Geräteeinstellungen aktivieren.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Foreground Dispatch aktivieren
        enableForegroundDispatch();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Foreground Dispatch deaktivieren
        disableForegroundDispatch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.d(TAG, "onNewIntent aufgerufen");
        String action = intent.getAction();

        // Überprüfen, ob ein NFC-Tag erkannt wurde
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            Toast.makeText(this, "NFC-Tag erkannt!", Toast.LENGTH_SHORT).show();

            // Tag auslesen (wenn vorhanden)
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                Log.d(TAG, "NFC-Tag ID: " + bytesToHex(tag.getId()));
            }

            // Überprüfen, ob eine NDEF-Nachricht enthalten ist
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
        // Intent für das Erkennen von NFC-Tags
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // PendingIntent, das ausgelöst wird, wenn ein NFC-Tag erkannt wird
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        // IntentFilter für unterstützte Aktionen
        IntentFilter[] filters = new IntentFilter[]{
                new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        };

        // Technologielisten für die unterstützten Tag-Typen (optional, hier leer)
        String[][] techLists = new String[][]{};

        // Foreground Dispatch aktivieren
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, techLists);
    }

    private void disableForegroundDispatch() {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    private NdefMessage createNdefMessage(String text) {
        // Beispiel-Nachricht erstellen
        NdefRecord record = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            record = NdefRecord.createTextRecord("en", text);
        }
        return new NdefMessage(new NdefRecord[]{record});
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}



/*

package com.example.connectapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // NFC-Adapter initialisieren
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC wird auf diesem Gerät nicht unterstützt", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private NdefMessage generateNdefMessage() {
        // Daten aus den Eingabefeldern sammeln
        String name = ((EditText) findViewById(R.id.input_name)).getText().toString();
        String phone = ((EditText) findViewById(R.id.input_phone)).getText().toString();
        String email = ((EditText) findViewById(R.id.input_email)).getText().toString();
        String address = ((EditText) findViewById(R.id.input_address)).getText().toString();

        // Nur die ausgewählten Daten (basierend auf Checkboxen) hinzufügen
        StringBuilder vCard = new StringBuilder();
        vCard.append("BEGIN:VCARD\n");
        vCard.append("VERSION:3.0\n");
        vCard.append("FN:").append(name).append("\n");

        if (((CheckBox) findViewById(R.id.check_share_phone)).isChecked() && !phone.isEmpty()) {
            vCard.append("TEL:").append(phone).append("\n");
        }
        if (((CheckBox) findViewById(R.id.check_share_email)).isChecked() && !email.isEmpty()) {
            vCard.append("EMAIL:").append(email).append("\n");
        }
        if (((CheckBox) findViewById(R.id.check_share_address)).isChecked() && !address.isEmpty()) {
            vCard.append("ADR:").append(address).append("\n");
        }
        vCard.append("END:VCARD");

        // Erstellen der NDEF-Nachricht
        NdefRecord record = NdefRecord.createMime("text/vcard", vCard.toString().getBytes());
        return new NdefMessage(new NdefRecord[]{record});
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (nfcAdapter != null) {
            // Setze die NDEF-Nachricht direkt (über Foreground Dispatch)
            enableForegroundDispatch();  // Aktivieren des Empfangs von NFC-Nachrichten

            Toast.makeText(this, "Bereit für NFC-Datenübertragung", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "NFC ist nicht verfügbar", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispatch();  // Deaktivieren des Empfangs von NFC-Nachrichten
    }

    private void enableForegroundDispatch() {
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);
        IntentFilter[] filters = new IntentFilter[]{};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, null);
    }

    private void disableForegroundDispatch() {
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Toast.makeText(this, "NDEF Nachricht empfangen!", Toast.LENGTH_SHORT).show();

            // Erhaltene NDEF-Nachricht auslesen
            if (intent.hasExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)) {
                NdefMessage[] messages = (NdefMessage[]) intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                if (messages != null && messages.length > 0) {
                    NdefMessage message = messages[0];
                    String messageText = new String(message.getRecords()[0].getPayload());
                    Toast.makeText(this, "Empfangene Nachricht: " + messageText, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Kein NDEF Tag entdeckt", Toast.LENGTH_SHORT).show();
        }
    }
}

*/

