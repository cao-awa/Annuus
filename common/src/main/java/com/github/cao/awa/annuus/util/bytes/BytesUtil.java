package com.github.cao.awa.annuus.util.bytes;

public class BytesUtil {
    public static final byte[] EMPTY = new byte[0];

    public static void xor(byte[] target, byte[] xor) {
        for (int i = 0; i < target.length; i++) {
            target[i] ^= xor[i];
        }
    }

    public static void reverse(byte[] bytes) {
        for (int start = 0, end = bytes.length - 1; start < end; start++, end--) {
            bytes[end] ^= bytes[start];
            bytes[start] ^= bytes[end];
            bytes[end] ^= bytes[start];
        }
    }

    public static byte[] reverseRound(byte[] source, int round) {
        byte[] result = new byte[round];

        int length = source.length;

        System.arraycopy(
                source,
                0,
                result,
                round - length,
                length
        );

        return result;
    }

    public static byte[] skp(byte[] bytes, byte target) {
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == target) {
                continue;
            }
            byte[] result = new byte[bytes.length - i];
            System.arraycopy(bytes,
                             i,
                             result,
                             0,
                             result.length
            );
            return result;
        }

        return new byte[0];
    }

    public static void skd(byte[] bytes, byte target) {
        skd(bytes,
            target,
            (byte) 0
        );
    }

    public static void skd(byte[] bytes, byte target, byte fill) {
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == target) {
                continue;
            }
            for (int i1 = 0; i1 < bytes.length; i1++) {
                if (i < bytes.length) {
                    bytes[i1] = bytes[i++];
                } else {
                    bytes[i1] = fill;
                }
            }
        }
    }

    public static byte[] concat(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] result = new byte[length];
        int cur = 0;
        for (byte[] array : arrays) {
            if (array.length == 0) {
                continue;
            }
            System.arraycopy(array,
                             0,
                             result,
                             cur,
                             array.length
            );
            cur += array.length;
        }
        return result;
    }

    public static byte[] array(byte... bytes) {
        return bytes;
    }

    public static byte[] array(int... bytes) {
        byte[] result = new byte[bytes.length];
        int index = 0;
        for (int i : bytes) {
            result[index++] = (byte) i;
        }
        return result;
    }

    public static byte setOne(byte b, int index) {
        return (byte) (b | (byte) (1 << index));
    }

    public static byte setZero(byte b, int index) {
        return (byte) (b & (byte) ~(1 << index));
    }

    public static byte setReverse(byte b, int index) {
        return (byte) (b ^ (byte) (1 << index));
    }

    public static byte getBit(byte b, int index) {
        return (byte) ((b) >> (index) & 1);
    }
}
