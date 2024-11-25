package com.example.connectapp;

import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.nfc.NfcAdapter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcEvent;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC wird auf diesem Gerät nicht unterstützt", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableForegroundDispatch();

        // Callback mit einer anonymen Klasse
        nfcAdapter.setNdefPushMessageCallback(new NfcAdapter.NdefPushMessageCallback() {
            @Override
            public NdefMessage createNdefMessage(NfcEvent event) {
                return createNdefMessage(); // Methode zum Erstellen der NDEF-Nachricht
            }
        }, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispatch();
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
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMessages != null) {
                NdefMessage message = (NdefMessage) rawMessages[0];
                String payload = new String(message.getRecords()[0].getPayload());

                // Kontakt hinzufügen
                addContact(payload);
            }
        }
    }

    // Methode zur Erstellung der NDEF-Nachricht
    public NdefMessage createNdefMessage() {
        // Holen Sie sich die Eingabewerte aus der UI
        String name = ((EditText) findViewById(R.id.input_name)).getText().toString();
        String phone = ((EditText) findViewById(R.id.input_phone)).getText().toString();

        StringBuilder payload = new StringBuilder();
        payload.append("Name: ").append(name).append("\n");
        if (((CheckBox) findViewById(R.id.check_share_phone)).isChecked()) {
            payload.append("Telefon: ").append(phone);
        }

        // Erstellen Sie eine NDEF-Nachricht
        NdefRecord record = NdefRecord.createMime("text/plain", payload.toString().getBytes());
        return new NdefMessage(new NdefRecord[]{record});
    }

    // Methode zum Hinzufügen des Kontakts
    private void addContact(String contactData) {
        String[] lines = contactData.split("\n");
        String name = lines[0].split(": ")[1];
        String phone = lines.length > 1 ? lines[1].split(": ")[1] : null;

        ContentValues values = new ContentValues();
        values.put(ContactsContract.RawContacts.ACCOUNT_TYPE, null);
        values.put(ContactsContract.RawContacts.ACCOUNT_NAME, null);

        Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);

        if (name != null) {
            values.clear();
            values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
            values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name);
            getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
        }

        if (phone != null) {
            values.clear();
            values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
            values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone);
            getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
        }

        Toast.makeText(this, "Kontakt hinzugefügt", Toast.LENGTH_SHORT).show();
    }
}
