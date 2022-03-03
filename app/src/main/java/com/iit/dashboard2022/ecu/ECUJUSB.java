package com.iit.dashboard2022.ecu;

import android.os.SystemClock;

import com.iit.dashboard2022.util.ByteSplit;
import com.iit.dashboard2022.util.Toaster;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class ECUJUSB {
    private final ByteArrayOutputStream JUSB_data = new ByteArrayOutputStream();
    private final ECU mainECU;
    long JUSB_requesting = 0;
    private boolean JUSB_flagReceived = false;
    private int JUSB_size = -1;
    private int JUSB_uncompressed_size = -1;

    public ECUJUSB(ECU mainECU) {
        this.mainECU = mainECU;
    }

    private void resetJUSB() {
        JUSB_requesting = 0;
        JUSB_size = -1;
        JUSB_uncompressed_size = -1;
        JUSB_flagReceived = false;
        JUSB_data.reset();
    }

    private int matchJUSBFlag(byte[] data) {
        for (int i = 0; i < data.length; i += 8) {
            int j;
            for (j = i; j < i + 8; j++) {
                if (data[j] != 0)
                    break;
            }
            if (j - i == 8)
                return i + 8;
        }
        return -1;
    }

    boolean receive(byte[] data) {
        if (SystemClock.elapsedRealtime() - JUSB_requesting > 30000) {
            Toaster.showToast("Requesting JSON from USB Timeout", Toaster.Status.WARNING);
            resetJUSB();
            return false;
        }
        int cutoff;
        if (!JUSB_flagReceived && (cutoff = matchJUSBFlag(data)) != -1) {
            JUSB_flagReceived = true;
            if (data.length == cutoff)
                return true;
            data = Arrays.copyOfRange(data, cutoff, data.length);
        }
        if (JUSB_flagReceived && JUSB_size == -1) {
            JUSB_size = (int) ByteSplit.getUnsignedInt(data);
            if (data.length == 8)
                return true;
            data = Arrays.copyOfRange(data, 8, data.length);
        }
        if (JUSB_flagReceived && JUSB_size != -1 && JUSB_uncompressed_size == -1) {
            JUSB_uncompressed_size = (int) ByteSplit.getUnsignedInt(data);
            if (data.length == 8)
                return true;
            data = Arrays.copyOfRange(data, 8, data.length);
        }
        if (JUSB_size != -1) {
            try {
                JUSB_data.write(data);
                if (JUSB_data.size() >= JUSB_size) {
                    Inflater decompressor = new Inflater();
                    decompressor.setInput(JUSB_data.toByteArray(), 0, JUSB_size);
                    byte[] result = new byte[JUSB_uncompressed_size + 1];
                    int resultLength = decompressor.inflate(result);
                    decompressor.end();

                    String outputString = new String(result, 0, resultLength, StandardCharsets.UTF_8);

                    resetJUSB();

                    mainECU.loadJSONString(outputString);
                }
                mainECU.issueCommand(ECUCommands.PRINT_LOOKUP);
            } catch (DataFormatException e) {
                Toaster.showToast("USB Serial JSON decompression failed", Toaster.Status.ERROR);
            } catch (IOException ignored) {
            }
            return true;
        }
        return false;
    }

    public void request() {
        resetJUSB();
        Toaster.showToast("Requesting JSON over USB", Toaster.Status.INFO);
        JUSB_requesting = SystemClock.elapsedRealtime();
        mainECU.issueCommand(ECUCommands.PRINT_LOOKUP);
    }

}
