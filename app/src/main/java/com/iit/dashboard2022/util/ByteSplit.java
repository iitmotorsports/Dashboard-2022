package com.iit.dashboard2022.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class ByteSplit {

    @SuppressWarnings("SpellCheckingInspection")
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8);

    /**
     * Returns the hex string representation of the given byte array
     *
     * @param bytes byte array
     * @return The hex string
     */
    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    /**
     * Returns the byte array representation of a hex string
     *
     * @param hex the valid hex string
     * @return byte[] byte array
     */
    public static byte[] hexToBytes(String hex) {
        hex = hex.length() % 2 != 0 ? "0" + hex : hex;
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(hex.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    public static byte[] joinArray(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }

        final byte[] result = new byte[length];

        int offset = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }

        return result;
    }

    /**
     * Reads the first four bytes of a message's array, composing them into an
     * unsigned int value
     *
     * @param data raw byte data
     * @return The unsigned int value as a long
     */
    public static long getUnsignedInt(byte[] data) {
        return ((long) ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xffffffffL);
    }
}
