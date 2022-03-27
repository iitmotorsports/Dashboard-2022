package com.iit.dashboard2022.ecu;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.iit.dashboard2022.logging.Log;
import com.iit.dashboard2022.logging.ToastLevel;

import java.io.File;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class ECULogger {
    //TODO: Separate all errors into error page

    public ECULogger() {
    }

    @WorkerThread
    public static String stringifyLogFile(File file) {
        /*
        if (file == null) {
            return null;
        }
        byte[] bytes = LogFileIO.getBytes(file);
        String jsonStr = LogFileIO.getString(file, LOG_MAP_END);
        int logStart = jsonStr.getBytes().length;
        StringBuilder stringFnl = new StringBuilder();
        stringFnl.append(jsonStr);

        long[] IDs = new long[4];

        for (int i = logStart; i < file.length(); i += 16) {
            byte[] epochB = new byte[8];
            byte[] msg = new byte[8];
            try {
                System.arraycopy(bytes, i, epochB, 0, 8);
                System.arraycopy(bytes, i + 8, msg, 0, 8);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                Log.toast("Warning: log file has leftover bytes", ToastLevel.WARNING);
                break;
            }
            long epoch = ByteBuffer.wrap(epochB).order(ByteOrder.LITTLE_ENDIAN).getLong();
            ECU.interpretMsg(IDs, msg);
            stringFnl.append(epoch).append(" ").append(IDs[0]).append(" ").append(IDs[1]).append(" ").append(IDs[2]).append("\n");
        }

        String fnl = stringFnl.toString();
        fnl = fnl.replace("\"", "\\\"");
        fnl = fnl.replace("\n", "\\n");

        if (fnl.length() != 0) {
            return fnl;
        }

        Log.toast("Returning string interpretation", ToastLevel.WARNING);
        return LogFileIO.getString(file);

         */
        return null;
    }

    @WorkerThread
    public static String interpretRawData(String jsonStr, byte[] raw_data, int dataStart) {
        //TODO: Check this string
        JsonReader reader = new JsonReader(new StringReader(jsonStr));
        reader.setLenient(true);
        ECUMessageHandler localEcuKeyMap = new ECUMessageHandler(JsonParser.parseReader(reader));
        StringBuilder output = new StringBuilder(raw_data.length);

        if (localEcuKeyMap.loaded()) {
            long[] IDs = new long[4];
            for (int i = dataStart; i < raw_data.length; i += 8) {
                byte[] data_block = new byte[8];
                try {
                    System.arraycopy(raw_data, i, data_block, 0, 8);
                } catch (ArrayIndexOutOfBoundsException e) {
                    continue;
                }
                ECU.interpretMsg(IDs, data_block);
                output.append(ECU.formatMsg(0, localEcuKeyMap.getTag((int) IDs[0]), localEcuKeyMap.getStr((int) IDs[1]), IDs[2]));
            }
        }

        return output.toString();
    }

    public void write(byte[] bytes) {
        /*
        logFile.write(bytes);

         */
    }
}
