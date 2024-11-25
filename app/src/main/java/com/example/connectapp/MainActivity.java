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
