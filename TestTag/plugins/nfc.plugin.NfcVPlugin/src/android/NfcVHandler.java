package nfc.plugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.nio.ByteBuffer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.*;
import android.nfc.Tag;
import android.widget.Toast;

/*
https://books.google.at/books?id=c4QRU17e494C&pg=SA2-PA147&lpg=SA2-PA147&dq=read+and+write+nfcv&source=bl&ots=V9C-4JZR8N&sig=gjhjDZFGjewd05RQNsp-6zhZFvI&hl=de&sa=X&ved=0ahUKEwjDhYzJ1rnLAhWCVywKHVt3AaUQ6AEIZjAJ#v=onepage&q=read%20and%20write%20nfcv&f=false
*/
public class NfcVHandler {
    private static final int block = 0;

    private static final String STATUS_NFC_OK = "NFC_OK";
    private static final String STATUS_NO_NFC = "NO_NFC";
    private static final String STATUS_NFC_DISABLED = "NFC_DISABLED";
    private static final String STATUS_READING_STOPPED = "READING_STOPPED";
    private static final String STATUS_WRITING_STOPPED = "WRITING_STOPPED";
    private static final String FALSE_TAG = "FALSE_TAG";

    private NfcAdapter nfcAdapter;
    private Activity activity;
    private CallbackContext callbackContext;
    private boolean isReading, isWriting, isNfcVReading = false, isMifareReading = false;
    private int oldValue, newValue;

    public NfcVHandler(Activity activity, final CallbackContext callbackContext) {
        this.activity = activity;
        this.callbackContext = callbackContext;
    }

    public void checkNfcAvailibility() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        if (nfcAdapter == null) {
            callbackContext.error(STATUS_NO_NFC);
        } else if (!nfcAdapter.isEnabled()) {
            callbackContext.error(STATUS_NFC_DISABLED);
        } else {
            callbackContext.success(STATUS_NFC_OK);
        }
    }

    public void startReadingNfcV() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        if (nfcAdapter == null) {
            callbackContext.error(STATUS_NO_NFC);
        } else if (!nfcAdapter.isEnabled()) {
            callbackContext.error(STATUS_NFC_DISABLED);
        } else {
            this.isReading = true;
            this.isWriting = false;
            //setupMifareForegroundDispatch(getActivity(), nfcAdapter);
            setupNfcVForegroundDispatch(getActivity(), nfcAdapter);
            this.isMifareReading = false;
            this.isNfcVReading =  true;
        }
    }

    public void startReadingMifare() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        if (nfcAdapter == null) {
            callbackContext.error(STATUS_NO_NFC);
        } else if (!nfcAdapter.isEnabled()) {
            callbackContext.error(STATUS_NFC_DISABLED);
        } else {
            this.isReading = true;
            this.isWriting = false;
            setupMifareForegroundDispatch(getActivity(), nfcAdapter);
           // setupNfcVForegroundDispatch(getActivity(), nfcAdapter);
            this.isMifareReading = true;
            this.isNfcVReading =  false;
        }
    }

    public void stopReadingNfcV() {
        try {
            this.isReading = false;
            stopForegroundDispatch(getActivity(), nfcAdapter);
            callbackContext.success(STATUS_READING_STOPPED);
        } catch (IllegalStateException e) {
            callbackContext.error(e.getMessage());
        }
    }

    public void startWritingNfcV(int oldValue, int newValue) {
        nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        if (nfcAdapter == null) {
            callbackContext.error(STATUS_NO_NFC);
        } else if (!nfcAdapter.isEnabled()) {
            callbackContext.error(STATUS_NFC_DISABLED);
        } else {
            this.isWriting = true;
            this.isReading = false;
            this.oldValue = oldValue;
            this.newValue = newValue;
            setupNfcVForegroundDispatch(getActivity(), nfcAdapter);
        }
    }

    public void stopWritingNfcV() {
        try {
            this.isWriting = false;
            stopForegroundDispatch(getActivity(), nfcAdapter);
            callbackContext.success(STATUS_WRITING_STOPPED);
        } catch (IllegalStateException e) {
            callbackContext.error(e.getMessage());
        }
    }

    public void newIntent(Intent intent) {
        String action = intent.getAction();
        if ((this.isReading || this.isWriting) && NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            handleNfcVIntent(intent);
        }
    }

    private void handleNfcVIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (this.isReading) {
            try {
                String result = readHandler(tag);
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, result);
                callbackContext.sendPluginResult(pluginResult);
            } catch (Exception e) {
                callbackContext.error(e.getMessage());
            } finally {
                this.isReading = false;
            }
        } else if (this.isWriting) {
            try {
                writeNfcV(tag, oldValue, newValue);
            } catch (Exception e) {
                callbackContext.error(e.getMessage());
            } finally {
                this.isWriting = false;
            }
        }

        //stopForegroundDispatch(getActivity(), nfcAdapter);
    }
    public String readHandler(Tag tag) throws Exception {
        String trayId = readTrayIdNfcV(tag);
        String machineId = readMachineIdNfcA(tag);
        if (isNfcVReading){
            return trayId;
        }else if(isMifareReading){
            return machineId;
        }else{
		    return "-1";
		}
    }

    public String readTrayIdNfcV(Tag tag) throws Exception {
        if (tag == null) {
            callbackContext.error("NULL");
        }
        byte[] resultData = new byte[8];
        NfcV nfcv = NfcV.get(tag);
        if (nfcv != null) {
            try {
                nfcv.connect();
                byte[] cmd = new byte[]{
                        (byte) 0x00, // Flags
                        (byte) 0x23, // Command: Read multiple blocks
                        (byte) 0x00, // First block (offset)
                        (byte) 0x02  // Number of blocks
                };
                byte[] userdata = nfcv.transceive(cmd);
                // Chop off the initial 0x00 byte:
                resultData = Arrays.copyOfRange(userdata, 1, 9);
            } catch (IOException e) {
                callbackContext.error(nfcv.toString());
            } finally {
                try {
                    nfcv.close();
                } catch (IOException e) {
                }
            }
        }
        return hexToAscII(sequenceFix(bytesToHex(resultData)));
    }

    public String readMachineIdNfcA(Tag tag) throws Exception {
        if (tag == null) {
            callbackContext.error("NULL");
        }
        byte[] resultData = new byte[4];
        MifareUltralight nfca = MifareUltralight.get(tag);
        if (nfca != null) {
            try {
                nfca.connect();
//                byte[] cmd = new byte[]{
//                        (byte) 0xFF,
//                        (byte) 0xB0,
//                        (byte) 0x00,
//                        (byte) 0x08,
//                        (byte) 0x10
//                };
//                byte[] userdata = nfca.transceive(cmd);
                byte[] userdata = nfca.readPages(8);
                // Chop off the initial 0x00 byte:
                resultData = Arrays.copyOfRange(userdata, 0, 4);
            } catch (IOException e) {
                callbackContext.error(nfca.toString());
            } finally {
                try {
                    nfca.close();
                } catch (IOException e) {
                }
            }
        }
        return hexToAscII(sequenceFix(bytesToHex(resultData)));
    }

    public int readNfcV(Tag tag) throws Exception {
        if (tag == null) {
            callbackContext.error("NULL");
        }
        byte[] response = new byte[4];
        NfcV nfcv = NfcV.get(tag);
        if (nfcv != null) {
            try {
                nfcv.connect();
                byte[] request = new byte[]{
                        (byte) 0x00,                  // flag
                        (byte) 0x20,                  // command: READ ONE BLOCK
                        (byte) block                     // IMMER im gleichen Block
                };
                response = nfcv.transceive(request);
            } catch (IOException e) {
                callbackContext.error(nfcv.toString());
            } finally {
                try {
                    nfcv.close();
                } catch (IOException e) {
                }
            }
        }
        // 1. Byte: Block locking status
        byte[] value = new byte[]{response[1], response[2], response[3], response[4]};
        return ByteBuffer.wrap(value).order(java.nio.ByteOrder.BIG_ENDIAN).getInt();
    }

    public void writeNfcV(Tag tag, int oldValue, int newValue) throws Exception {
        int currentValue = readNfcV(tag);
        if (((oldValue != -1) && (oldValue != 0) && (currentValue != oldValue)) ||
                (((oldValue == -1) || (oldValue == 0)) && ((currentValue != 0) && currentValue != -1))) {
            callbackContext.error(FALSE_TAG);
            return;
        }
        byte[] data = ByteBuffer.allocate(4).putInt(newValue).array();
        if (tag == null) {
            callbackContext.error("NULL");
            return;
        }
        NfcV nfcv = NfcV.get(tag);

        nfcv.connect();

        byte[] request = new byte[7];
        request[0] = 0x00;            // flag
        request[1] = 0x21;            // command: WRITE ONE BLOCK
        request[2] = (byte) block;    // IMMER im gleichen Block speichern

        request[3] = (byte) data[0];
        request[4] = (byte) data[1];
        request[5] = (byte) data[2];
        request[6] = (byte) data[3];
        try {
            byte[] response = nfcv.transceive(request);
            if (response[0] != (byte) 0x00) {
                callbackContext.error("Error code: " + response[1]);
            }
        } catch (IOException e) {
            if (e.getMessage().equals("Tag was lost.")) {
                // continue, because of Tag bug
            } else {
                callbackContext.error("Couldn't write on Tag");
                throw e;
            }
        }
        nfcv.close();
        int result = readNfcV(tag);
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, result);
        callbackContext.sendPluginResult(pluginResult);
    }

    public static void setupNfcVForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        String[][] nfcvTechList = new String[][]{
                new String[]{NfcV.class.getName()}
        };
        String[][] miTechList = new String[][] { new String[] { MifareUltralight.class.getName() } };
        try {
            filter.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("ERROR", e);
        }
        IntentFilter[] filters = new IntentFilter[]{filter};
        adapter.enableForegroundDispatch(activity, pendingIntent, filters, nfcvTechList);
    }

    public static void setupMifareForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        String[][] nfcvTechList = new String[][]{
                new String[]{NfcV.class.getName()}
        };
        String[][] miTechList = new String[][] { new String[] { MifareUltralight.class.getName() } };
        try {
            filter.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("ERROR", e);
        }
        IntentFilter[] filters = new IntentFilter[]{filter};
        adapter.enableForegroundDispatch(activity, pendingIntent, filters, miTechList);
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String hexToAscII(String hex) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            String str = hex.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    public static String sequenceFix(String str) {
        String[] newStr = new String[8];
        String[] newStr2 = new String[8];
        if (str.length() > 8) {
            for (int i = 0; i < 8; i++) {
                if (i < 7) {
                    newStr[i] = str.substring(i, i + 1);
                    newStr2[i] = str.substring(i + 8, i + 9);
                } else if (i == 7) {
                    newStr[i] = str.substring(i, i + 1);
                    newStr2[i] = str.substring(i + 8);
                }
            }
            String output = newStr[6] + newStr[7] + newStr[4] + newStr[5] + newStr[2] + newStr[3] + newStr[0] + newStr[1] + newStr2[6] + newStr2[7];
            return output;
        } else {
            for (int i = 0; i < 8; i++) {
                if (i < 7) {
                    newStr[i] = str.substring(i, i + 1);
                } else if (i == 7) {
                    newStr[i] = str.substring(i);
                }
            }
            String output = newStr[6] + newStr[7] + newStr[4] + newStr[5] + newStr[2] + newStr[3] + newStr[0] + newStr[1];
            return output;
        }
    }

    private Activity getActivity() {
        return this.activity;
    }

    private Intent getIntent() {
        return this.activity.getIntent();
    }
}