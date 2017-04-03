package fr.devnied.bitlib;

import java.math.BigInteger;

/**
 * Class used to manage String/byte/int converter
 *
 * @author Millau Julien
 *
 */
public final class BytesUtils {

	/**
	 * Integer bit size
	 */
	private static final int MAX_BIT_INTEGER = 31;

	/**
	 * Constant for Hexa
	 */
	private static final int HEXA = 16;

	/**
	 * Byte left mask
	 */
	private static final int LEFT_MASK = 0xF0;

	/**
	 * Byte right mask
	 */
	private static final int RIGHT_MASK = 0xF;

	/**
	 * Char digit 0 (0x30) :<br>
	 * <ul>
	 * <li>char 0 = 0x30 + 0x0
	 * <li>char 1 = 0x30 + 0x1
	 * <li>...
	 * <li>char 9 = 0x30 + 0x9
	 * </ul>
	 */
	private static final int CHAR_DIGIT_ZERO = 0x30;

	/**
	 * Char digit 7 (0x37) :<br>
	 * <ul>
	 * <li>char A = 0x37 + 0xA
	 * <li>char B = 0x37 + 0xB
	 * <li>...
	 * <li>char F = 0x37 + 0xF
	 * </ul>
	 */
	private static final int CHAR_DIGIT_SEVEN = 0x37;

	/**
	 * Char space
	 */
	private static final char CHAR_SPACE = (char) 0x20;

	/**
	 * Method used to convert byte array to int
	 *
	 * @param byteArray
	 *            byte array to convert
	 * @return int value
	 */
	public static int byteArrayToInt(final byte[] byteArray) {
		if (byteArray == null) {
			throw new IllegalArgumentException("Parameter 'byteArray' cannot be null");
		}
		return byteArrayToInt(byteArray, 0, byteArray.length);
	}

	/**
	 * Method used to convert byte array to int
	 *
	 * @param byteArray
	 *            byte array to convert
	 * @param startPos
	 *            start position in array in the
	 * @param length
	 *            length of data
	 * @return int value of byte array
	 */
	public static int byteArrayToInt(final byte[] byteArray, final int startPos, final int length) {
		if (byteArray == null) {
			throw new IllegalArgumentException("Parameter 'byteArray' cannot be null");
		}
		if (length <= 0 || length > 4) {
			throw new IllegalArgumentException("Length must be between 1 and 4. Length = " + length);
		}
		if (startPos < 0 || byteArray.length < startPos + length) {
			throw new IllegalArgumentException("Length or startPos not valid");
		}
		int value = 0;
		for (int i = 0; i < length; i++) {
			value += (byteArray[startPos + i] & 0xFF) << 8 * (length - i - 1);
		}
		return value;
	}

	/**
	 * Method to convert bytes to string with space between bytes
	 *
	 * @param pBytes
	 *            Bytes to convert
	 *
	 * @return a string
	 */
	public static String bytesToString(final byte[] pBytes) {
		return formatByte(pBytes, true, false);
	}

	/**
	 * Method to convert bytes to string with space between bytes
	 *
	 * bytes to convert
	 *
	 * @param pBytes
	 *            Bytes to convert
	 * @param pTruncate
	 *            true to remove 0 left byte value
	 * @return a string
	 */
	public static String bytesToString(final byte[] pBytes, final boolean pTruncate) {
		return formatByte(pBytes, true, pTruncate);
	}

	/**
	 * Method to convert byte to string without space between byte
	 *
	 * @param pByte
	 *            byte to convert
	 * @return a string
	 */
	public static String bytesToStringNoSpace(final byte pByte) {
		return formatByte(new byte[] { pByte }, false, false);
	}

	/**
	 * Method to convert bytes to string without space between bytes
	 *
	 * @param pBytes
	 *            bytes to convert
	 * @return a string
	 */
	public static String bytesToStringNoSpace(final byte[] pBytes) {
		return formatByte(pBytes, false, false);
	}

	/**
	 * Method to convert bytes to string without space between bytes
	 *
	 * @param pBytes
	 *            bytes to convert
	 * @param pTruncate
	 *            true to remove 0 left byte value
	 * @return a string
	 */
	public static String bytesToStringNoSpace(final byte[] pBytes, final boolean pTruncate) {
		return formatByte(pBytes, false, pTruncate);
	}

	/**
	 * Private method to format bytes to hexa string
	 *
	 * @param pByte
	 *            the bytes to format
	 * @param pSpace
	 *            true if add spaces between bytes
	 * @param pTruncate
	 *            true to remove 0 left bytes value
	 * @return a string containing the requested string
	 */
	private static String formatByte(final byte[] pByte, final boolean pSpace, final boolean pTruncate) {
		String result;
		if (pByte == null) {
			result = "";
		} else {
			int i = 0;
			if (pTruncate) {
				while (i < pByte.length && pByte[i] == 0) {
					i++;
				}
			}
			if (i < pByte.length) {
				int sizeMultiplier = pSpace ? 3 : 2;
				char[] c = new char[(pByte.length - i) * sizeMultiplier];
				byte b;
				for (int j = 0; i < pByte.length; i++, j++) {
					b = (byte) ((pByte[i] & LEFT_MASK) >> 4);
					c[j] = (char) (b > 9 ? b + CHAR_DIGIT_SEVEN : b + CHAR_DIGIT_ZERO);
					b = (byte) (pByte[i] & RIGHT_MASK);
					c[++j] = (char) (b > 9 ? b + CHAR_DIGIT_SEVEN : b + CHAR_DIGIT_ZERO);
					if (pSpace) {
						c[++j] = CHAR_SPACE;
					}
				}
				result = pSpace ? new String(c, 0, c.length - 1) : new String(c);
			} else {
				result = "";
			}
		}
		return result;
	}

	/**
	 * Method to get bytes form string
	 *
	 * @param pData
	 *            String to parse
	 * @return a table of string
	 */
	public static byte[] fromString(final String pData) {
		if (pData == null) {
			throw new IllegalArgumentException("Argument can't be null");
		}
		StringBuilder sb = new StringBuilder(pData);
		int j = 0;
		for (int i = 0; i < sb.length(); i++) {
			if (!Character.isWhitespace(sb.charAt(i))) {
				sb.setCharAt(j++, sb.charAt(i));
			}
		}
		sb.delete(j, sb.length());
		if (sb.length() % 2 != 0) {
			throw new IllegalArgumentException("Hex binary needs to be even-length :" + pData);
		}
		byte[] result = new byte[sb.length() / 2];
		j = 0;
		for (int i = 0; i < sb.length(); i += 2) {
			result[j++] = (byte) ((Character.digit(sb.charAt(i), 16) << 4) + Character.digit(sb.charAt(i + 1), 16));
		}
		return result;
	}

	/**
	 * Test if bit at given index of given value is = 1.
	 *
	 * @param pVal
	 *            value to test
	 * @param pBitIndex
	 *            bit index between 0 and 31
	 * @return true bit at given index of give value is = 1
	 */
	public static boolean matchBitByBitIndex(final int pVal, final int pBitIndex) {
		if (pBitIndex < 0 || pBitIndex > MAX_BIT_INTEGER) {
			throw new IllegalArgumentException(
					"parameter 'pBitIndex' must be between 0 and 31. pBitIndex=" + pBitIndex);
		}
		return (pVal & 1 << pBitIndex) != 0;
	}

	/**
	 * Method used to set a bit index to 1 or 0.
	 *
	 * @param pData
	 *            data to modify
	 * @param pBitIndex
	 *            index to set
	 * @param pOn
	 *            set bit at specified index to 1 or 0
	 * @return the modified byte
	 */
	public static byte setBit(final byte pData, final int pBitIndex, final boolean pOn) {
		if (pBitIndex < 0 || pBitIndex > 7) {
			throw new IllegalArgumentException("parameter 'pBitIndex' must be between 0 and 7. pBitIndex=" + pBitIndex);
		}
		byte ret = pData;
		if (pOn) { // Set bit
			ret |= 1 << pBitIndex;
		} else { // Unset bit
			ret &= ~(1 << pBitIndex);
		}
		return ret;
	}

	/**
	 * Convert byte array to binary String
	 *
	 * @param pBytes
	 *            byte array to convert
	 * @return a binary representation of the byte array
	 */
	public static String toBinary(final byte[] pBytes) {
		String ret = null;
		if (pBytes != null && pBytes.length > 0) {
			BigInteger val = new BigInteger(bytesToStringNoSpace(pBytes), HEXA);
			StringBuilder build = new StringBuilder(val.toString(2));
			// left pad with 0 to fit byte size
			for (int i = build.length(); i < pBytes.length * BitUtils.BYTE_SIZE; i++) {
				build.insert(0, 0);
			}
			ret = build.toString();
		}
		return ret;
	}

	/**
	 * Method used to convert integer to byet array
	 *
	 * @param value
	 *            the value to convert
	 * @return a byte array
	 */
	public static byte[] toByteArray(final int value) {
		return new byte[] { //
				(byte) (value >> 24), //
				(byte) (value >> 16), //
				(byte) (value >> 8), //
				(byte) value //
		};
	}

	/**
	 * private constructor
	 */
	private BytesUtils() {
	}
}
