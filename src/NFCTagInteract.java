

/*
 * Basic infrastructure for NFC Interaction.  Very useful for other android apps.
 * This is meant for communication with only device to tag, not device to device
 * 
 * Author: Erik Taheri
 * 
 */

import java.util.Arrays;

import com.example.nfc_interaction.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;

public class NFCTagInteract extends Activity {
    // textview that will display NFC Data 
	private TextView nfcInfoTextView;
    // get default nfc adapter for the device
	private NfcAdapter devNFCAdapter;
    // Intent that will send nfc tag info
	private PendingIntent nfcPendingIntent;
	// Will save the tag intent into its buffer 
    private IntentFilter[] nfcIntentFilters;
    // Save the NFC Tech data
    private String[][] nfcTechLists;
 
    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        //set main view
        setContentView(R.layout.activity_tag_dispatch);
        // set variable for text view
        nfcInfoTextView = (TextView)findViewById(R.id.test_text);
        // get the devices nfc adapter
        devNFCAdapter = NfcAdapter.getDefaultAdapter(this);
        
        // check if it is enabled
        if (devNFCAdapter != null) {
            nfcInfoTextView.setText("Read an NFC tag");
        } else {
            nfcInfoTextView.setText("This phone is not NFC enabled.");
        }
 
        // create an intent with tage data
        nfcPendingIntent = PendingIntent.getActivity(this, 0,
            new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
 
        // create intent filter for MIME data
        IntentFilter ndefIntent = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefIntent.addDataType("*/*");
            nfcIntentFilters = new IntentFilter[] { ndefIntent };
        } catch (Exception e) {
            Log.e("TagDispatch", e.toString());
        }
 
        nfcTechLists = new String[][] { new String[] { NfcF.class.getName() } };
    }
 
    @Override
    public void onNewIntent(Intent intent) {
        String action = intent.getAction();
        // get the data from the tage
        Tag nfc_tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        // string that hold val of tag data
        String s = action + "\n\n" + nfc_tag.toString();
        // create the parceable object to parse through all nfc data, but only get the text
        Parcelable[] data = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        // parse the data
        if (data != null) {
            try {
                for (int i = 0; i < data.length; i++) {
                	NdefRecord [] nfc_records = ((NdefMessage)data[i]).getRecords();
                    for (int j = 0; j < nfc_records.length; j++) {
                        if (nfc_records[j].getTnf() == NdefRecord.TNF_WELL_KNOWN &&
                            Arrays.equals(nfc_records[j].getType(), NdefRecord.RTD_TEXT)) {
                        	byte[] nfc_payload = nfc_records[j].getPayload();
                            String textEncoding = ((nfc_payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
                            int langCodeLen = nfc_payload[0] & 0077;
                            // concat data to end of string 
                            s += ("\n\nNdefMessage[" + i + "], NdefRecord[" + j + "]:\n\"" +
                                 new String(nfc_payload, langCodeLen + 1, nfc_payload.length - langCodeLen - 1,
                                 textEncoding) + "\"");
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("TagDispatch", e.toString());
            }
        }
        // set the textview to the NFC text data
        nfcInfoTextView.setText(s);
    }
 
    @Override
    public void onResume() {
        super.onResume();
 
        if (devNFCAdapter != null)
            devNFCAdapter.enableForegroundDispatch(this, nfcPendingIntent, nfcIntentFilters, nfcTechLists);
    }
 
    @Override
    public void onPause() {
        super.onPause();
 
        if (devNFCAdapter != null)
            devNFCAdapter.disableForegroundDispatch(this);
    }
}