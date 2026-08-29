package fr.devnied.bitlib;

import java.util.Arrays;

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
	 * Byte right mask
	 */
	private static final int RIGHT_MASK = 0xF;

	/**
	 * Byte mask
	 */
	private static final int BYTE_MASK = 0xFF;

	/**
	 * Hexa digits lookup table (upper case). Package private: shared with
	 * {@link BitUtils#getNextHexaString(int)}, must not be modified.
	 */
	static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
			'E', 'F' };

	/**
	 * Char space
	 */
	private static final char CHAR_SPACE = (char) 0x20;

	/**
	 * Char digit 0
	 */
	private static final char CHAR_DIGIT_ZERO = '0';

	/**
	 * Marker returned by the hexa decode table for whitespace chars
	 */
	private static final byte WHITESPACE = -2;

	/**
	 * ASCII decode table for {@link #fromString(String)}: hexa digit value,
	 * {@link #WHITESPACE} for whitespaces (see {@link Character#isWhitespace(char)})
	 * or -1 for invalid chars (same as {@link Character#digit(char, int)})
	 */
	private static final byte[] HEX_DECODE_TABLE = new byte[128];

	static {
		for (int c = 0; c < HEX_DECODE_TABLE.length; c++) {
			HEX_DECODE_TABLE[c] = Character.isWhitespace((char) c) ? WHITESPACE : (byte) Character.digit((char) c, HEXA);
		}
	}

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
			value = value << BitUtils.BYTE_SIZE | byteArray[startPos + i] & BYTE_MASK;
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
		final int b = pByte & BYTE_MASK;
		return new String(new char[] { HEX_DIGITS[b >>> 4], HEX_DIGITS[b & RIGHT_MASK] });
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
		if (pByte == null) {
			return "";
		}
		final int length = pByte.length;
		int i = 0;
		if (pTruncate) {
			while (i < length && pByte[i] == 0) {
				i++;
			}
		}
		if (i >= length) {
			return "";
		}
		final int count = length - i;
		final char[] c;
		int j = 0;
		if (pSpace) {
			c = new char[count * 3 - 1];
			// First byte, then " XX" for each following byte
			int b = pByte[i++] & BYTE_MASK;
			c[j++] = HEX_DIGITS[b >>> 4];
			c[j++] = HEX_DIGITS[b & RIGHT_MASK];
			for (; i < length; i++) {
				b = pByte[i] & BYTE_MASK;
				c[j++] = CHAR_SPACE;
				c[j++] = HEX_DIGITS[b >>> 4];
				c[j++] = HEX_DIGITS[b & RIGHT_MASK];
			}
		} else {
			c = new char[count * 2];
			for (; i < length; i++) {
				int b = pByte[i] & BYTE_MASK;
				c[j++] = HEX_DIGITS[b >>> 4];
				c[j++] = HEX_DIGITS[b & RIGHT_MASK];
			}
		}
		return new String(c);
	}

	/**
	 * Method to get bytes form string. Whitespaces (as defined by
	 * {@link Character#isWhitespace(char)}) are ignored. Digits are decoded
	 * with the semantic of {@link Character#digit(char, int)} in base 16: besides
	 * 0-9, A-F and a-f, the non ASCII decimal digits and the full width letters are
	 * accepted. Error messages never echo the input: only the offending
	 * character, its index and the length of the input.
	 *
	 * @param pData
	 *            String to parse
	 * @return a table of string
	 * @throws IllegalArgumentException
	 *             if pData is null, contains a non hexadecimal character or has
	 *             an odd number of hexadecimal digits
	 */
	public static byte[] fromString(final String pData) {
		if (pData == null) {
			throw new IllegalArgumentException("Argument can't be null");
		}
		final char[] chars = pData.toCharArray();
		final int length = chars.length;
		// Upper bound: every char is a hexa digit (shrunk below if whitespaces were skipped)
		final byte[] result = new byte[length >> 1];
		int j = 0;
		int high = 0;
		boolean pending = false;
		for (int i = 0; i < length; i++) {
			char ch = chars[i];
			int digit = ch < HEX_DECODE_TABLE.length ? HEX_DECODE_TABLE[ch] : decodeSlow(ch);
			if (digit < 0) {
				if (digit == WHITESPACE) {
					continue;
				}
				throw new IllegalArgumentException("Invalid hexadecimal character '" + ch + "' at index " + i + " (input length "
						+ length + ")");
			}
			if (pending) {
				result[j++] = (byte) ((high << 4) + digit);
				pending = false;
			} else {
				high = digit;
				pending = true;
			}
		}
		if (pending) {
			throw new IllegalArgumentException("Hex binary needs to be even-length: " + (2 * j + 1) + " hexadecimal digits (input length "
					+ length + ")");
		}
		return j == result.length ? result : Arrays.copyOf(result, j);
	}

	/**
	 * Decode a non ASCII char (same semantic as {@link Character#isWhitespace(char)}
	 * and {@link Character#digit(char, int)}, which returns -1 for invalid digits)
	 *
	 * @param pChar
	 *            the char to decode
	 * @return {@link #WHITESPACE} if the char is a whitespace, the hexa digit value otherwise
	 */
	private static int decodeSlow(final char pChar) {
		return Character.isWhitespace(pChar) ? WHITESPACE : Character.digit(pChar, HEXA);
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
		if (pOn) { // Set bit
			return (byte) (pData | 1 << pBitIndex);
		}
		// Unset bit
		return (byte) (pData & ~(1 << pBitIndex));
	}

	/**
	 * Convert byte array to binary String
	 *
	 * @param pBytes
	 *            byte array to convert
	 * @return a binary representation of the byte array
	 */
	public static String toBinary(final byte[] pBytes) {
		if (pBytes == null || pBytes.length == 0) {
			return null;
		}
		final char[] c = new char[pBytes.length * BitUtils.BYTE_SIZE];
		int j = 0;
		for (byte b : pBytes) {
			for (int i = 7; i >= 0; i--) {
				c[j++] = (char) (CHAR_DIGIT_ZERO + (b >> i & 1));
			}
		}
		return new String(c);
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
