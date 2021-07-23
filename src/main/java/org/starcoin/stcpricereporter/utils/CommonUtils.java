package org.starcoin.stcpricereporter.utils;

import java.util.List;

public class CommonUtils {

    public static byte hexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }

    public static byte[] hexToByteArray(String inHex) {
        String tmp = inHex.substring(0, 2);
        if (tmp.equals("0x")) {
            inHex = inHex.substring(2);
        }
        int hexlen = inHex.length();
        byte[] result;
        if (hexlen % 2 == 1) {
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = hexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }

    public static String byteListToHexWithPrefix(List<Byte> bytes) {
        return "0x" + byteListToHex(bytes);
    }

    public static String byteListToHex(List<Byte> bytes) {
        byte[] byteArray = toPrimitive(bytes.toArray(new Byte[0]));
        return byteArrayToHex(byteArray);
    }

    public static byte[] toPrimitive(final Byte[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return new byte[0];
        }
        final byte[] result = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].byteValue();
        }
        return result;
    }

    public static String byteArrayToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (int index = 0, len = bytes.length; index <= len - 1; index += 1) {
            String hex1 = Integer.toHexString((bytes[index] >> 4) & 0xF);
            String hex2 = Integer.toHexString(bytes[index] & 0xF);
            result.append(hex1);
            result.append(hex2);
        }
        return result.toString();
    }
}

