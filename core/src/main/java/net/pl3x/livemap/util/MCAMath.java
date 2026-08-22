package net.pl3x.livemap.util;

/**
 * Math helpers for calculating MCA related data.
 */
public final class MCAMath {
    private MCAMath() {
    }

    /**
     * Having a long array where each long contains as many values as
     * fit in it without overflowing, returning the "valueIndex"-th
     * value when each value has "bitsPerValue" bits.
     *
     * @param data         Raw long array data
     * @param valueIndex   Index of requested value
     * @param bitsPerValue Number of bits per value
     * @return The requested value at specified index
     */
    public static long getValueFromLongArray(long[] data, int valueIndex, int bitsPerValue) {
        int valuesPerLong = 64 / bitsPerValue;
        int longIndex = valueIndex / valuesPerLong;
        int bitIndex = (valueIndex % valuesPerLong) * bitsPerValue;

        long value = data[longIndex] >>> bitIndex;

        return value & (0xFFFFFFFFFFFFFFFFL >>> -bitsPerValue);
    }

    /**
     * Treating the long array "data" as a continuous stream of
     * bits, returning the "valueIndex"-th value when each
     * value has "bitsPerValue" bits.
     *
     * @param data         Raw long array data
     * @param valueIndex   Index of requested value
     * @param bitsPerValue Number of bits per value
     * @return The requested value at specified index
     */
    public static long getValueFromLongStream(long[] data, int valueIndex, int bitsPerValue) {
        int bitIndex = valueIndex * bitsPerValue;
        int firstLong = bitIndex >> 6; // index / 64
        int bitoffset = bitIndex & 0x3F; // Math.floorMod(index, 64)

        long value = data[firstLong] >>> bitoffset;

        if (bitoffset > 0 && firstLong + 1 < data.length) {
            long value2 = data[firstLong + 1];
            value2 = value2 << -bitoffset;
            value = value | value2;
        }

        return value & (0xFFFFFFFFFFFFFFFFL >>> -bitsPerValue);
    }

    /**
     * Extracts the 4 bits of the upper or lower side of the byte stored in <code>value</code>.
     * <p>
     * The value is treated as an unsigned byte.
     *
     * @param value Byte to use
     * @param upper True for upper half
     * @return The specified half of byte
     */
    public static int getByteHalf(int value, boolean upper) {
        return ((value & 0xFF) >> (upper ? 4 : 0)) & 0xF;
    }

    /**
     * Computes the ceiling of the base-2 logarithm of <code>n</code>.
     *
     * @param n Value to be used as input
     * @return Ceiling of the base-2 logarithm of x
     */
    public static int ceilLog2(int n) {
        return Integer.SIZE - Integer.numberOfLeadingZeros(n - 1);
    }
}
