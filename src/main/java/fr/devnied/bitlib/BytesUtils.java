package fr.devnied.bitlib;

import java.math.BigInteger;
import java.util.Locale;

/**
 * Class used to manage String/byte converter
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
	 * No space format
	 */
	private static final String FORMAT_NOSPACE = "%02x";
	/**
	 * Space format
	 */
	private static final String FORMAT_SPACE = "%02x ";

	/**
	 * Default mask
	 */
	private static final int DEFAULT_MASK = 0xFF;

	/**
	 * Method to convert bytes to string with space between bytes
	 * 
	 * @param pBytes
	 *            bytes to convert
	 * @return a string
	 */
	public static String bytesToString(final byte[] pBytes) {
		return formatByte(pBytes, FORMAT_SPACE);
	}

	/**
	 * Method to convert bytes to string without space between bytes
	 * 
	 * @param pBytes
	 *            bytes to convert
	 * @return a string
	 */
	public static String bytesToStringNoSpace(final byte[] pBytes) {
		return formatByte(pBytes, FORMAT_NOSPACE);
	}

	/**
	 * Private method to format byte to hexa string
	 * 
	 * @param pByte
	 *            the byte to format
	 * @param pFormat
	 *            the format
	 * @return a string containing the requested string
	 */
	private static String formatByte(final byte[] pByte, final String pFormat) {
		StringBuffer sb = new StringBuffer();
		if (pByte == null) {
			sb.append("");
		} else {
			for (byte b : pByte) {
				sb.append(String.format(pFormat, b & DEFAULT_MASK));
			}
		}
		return sb.toString().toUpperCase(Locale.getDefault()).trim();
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
		String text = pData.replace(" ", "");
		if (text.length() % 2 != 0) {
			throw new IllegalArgumentException("Hex binary needs to be even-length :" + pData);
		}
		byte[] commandByte = new byte[Math.round(text.length() / (float) 2.0)];
		int j = 0;
		for (int i = 0; i < text.length(); i += 2) {
			Integer val = Integer.parseInt(text.substring(i, i + 2), HEXA);
			commandByte[j++] = val.byteValue();
		}
		return commandByte;
	}

	/**
	 * Test if bit at given index of given value is = 1
	 * 
	 * @param pVal
	 *            value to test
	 * @param pBitIndex
	 *            bit index
	 * @return true bit at given index of give value is = 1
	 */
	public static boolean matchBitByBitIndex(final int pVal, final int pBitIndex) {
		return (pVal & 1 << pBitIndex) != 0;
	}

	/**
	 * Test if both bit representation of initial value and bit representation of value to compare = 1
	 * 
	 * @param pInitialValue
	 *            initial value to compare
	 * @param pValueToCompare
	 *            value to compare
	 * @return true if both bit representation of initial value and bit representation of value to compare = 1
	 */
	public static boolean matchBitByValue(final int pInitialValue, final int pValueToCompare) {
		return matchBitByBitIndex(pInitialValue, MAX_BIT_INTEGER - Integer.numberOfLeadingZeros(pValueToCompare));
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
	 * private constructor
	 */
	private BytesUtils() {
	}
}
