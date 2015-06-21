package com.tata.trackit;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.nfc.tech.TagTechnology;
import android.os.Bundle;
import android.util.Log;
public class MainActivity extends Activity {
	private NfcAdapter mNfcAdapter;
	private static final String TAG = "MainActivity";
	private final String[][] mTechList = new String[][] { new String[] {
			NfcA.class.getName(), NfcB.class.getName(), NfcF.class.getName(),
			NfcV.class.getName(), NdefFormatable.class.getName(),
			TagTechnology.class.getName(), IsoDep.class.getName(),
			MifareClassic.class.getName(), MifareUltralight.class.getName(),
			Ndef.class.getName() } };
	IntentFilter mNfc_Filter[] ;
	PendingIntent mNfc_PendingIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG,"onCreate");	
		setContentView(R.layout.activity_main);
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		mNfc_PendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter action_tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED); 
		IntentFilter action_ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED); 
		IntentFilter action_tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED); 
		mNfc_Filter = new IntentFilter[] {action_tech,action_ndef,action_tag};  
		onNewIntent(getIntent());	
	}

	@Override
	protected void onResume() {
		super.onResume();
		mNfcAdapter.enableForegroundDispatch(this, mNfc_PendingIntent,
				mNfc_Filter, mTechList);
		
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.e(TAG,"onPause");
		if(mNfcAdapter != null && mNfcAdapter.isEnabled()){
			mNfcAdapter.disableForegroundDispatch(this);
		}          
		
	}

	@Override
	public void onNewIntent(Intent intent) {
		Log.e(TAG,"onNewIntent " +intent.getAction());
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())||NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
		    readFromTag(intent);
		}
		
	}

	private boolean readFromTag(Intent intent) {
		boolean flag = false;
		try {
			byte[] extr_id_bytes = intent
					.getByteArrayExtra(NfcAdapter.EXTRA_ID);
			Log.e(TAG, "extr_id_bytes: " + extr_id_bytes.length);
			Log.e(TAG, "tagFromIntent:" + ByteArrayToHexString(extr_id_bytes));
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return flag;

	}

	private String ByteArrayToHexString(byte[] inarray) { // converts byte
															// arrays to string
		int i, j, in;
		String[] hex = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A",
				"B", "C", "D", "E", "F" };
		String out = "";

		for (j = 0; j < inarray.length; ++j) {
			in = (int) inarray[j] & 0xff;
			i = (in >> 4) & 0x0f;
			out += hex[i];
			i = in & 0x0f;
			out += hex[i];
		}
		return out;
	}
}
