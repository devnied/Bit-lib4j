package fr.devnied.bitlib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to manage bit with java
 *
 * @author Millau Julien
 *
 */
public final class BitUtils {
	/***
	 * Bit utils class logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BitUtils.class.getName());
	/**
	 * Constant for byte size
	 */
	public static final int BYTE_SIZE = Byte.SIZE;
	/**
	 * 255 init value
	 */
	private static final int DEFAULT_VALUE = 0xFF;
	/**
	 * Mask used to compute {@code index % BYTE_SIZE} (bit offset inside a byte)
	 */
	private static final int BIT_MASK = BYTE_SIZE - 1;
	/**
	 * Shift used to compute {@code index / BYTE_SIZE} (byte index)
	 */
	private static final int BYTE_SHIFT = 3;
	/**
	 * Constant for the default charset (Charset.forName is used instead of
	 * StandardCharsets to stay compatible with Android API level lower than 19)
	 */
	private static final Charset DEFAULT_CHARSET = Charset.forName("ASCII");

	/**
	 * Simple date format (yyyMMdd)
	 */
	public static final String DATE_FORMAT = "yyyyMMdd";

	/**
	 * Max number of date formatters kept per thread (safeguard against
	 * unbounded growth with dynamic patterns)
	 */
	private static final int MAX_CACHED_DATE_FORMATS = 32;

	/**
	 * Per-thread cache of date formatters, keyed by pattern.<br>
	 * {@link SimpleDateFormat} is expensive to build and not thread safe, so each
	 * thread keeps its own instances. A formatter captures the default time zone
	 * and locale of the thread at its first use with a given pattern.
	 */
	private static final ThreadLocal<Map<String, SimpleDateFormat>> DATE_FORMAT_CACHE = new ThreadLocal<Map<String, SimpleDateFormat>>() {
		@Override
		protected Map<String, SimpleDateFormat> initialValue() {
			return new HashMap<String, SimpleDateFormat>();
		}
	};

	/**
	 * Table of read byte
	 */
	private final byte[] byteTab;

	/**
	 * Current index
	 */
	private int currentBitIndex;

	/**
	 * Size in bit of the byte tab
	 */
	private final int size;

	/**
	 * Constructor of the class
	 *
	 * @param pByte
	 *            byte read
	 */
	public BitUtils(final byte[] pByte) {
		byteTab = Arrays.copyOf(pByte, pByte.length);
		size = pByte.length * BYTE_SIZE;
	}

	/**
	 * Constructor for empty byte tab
	 *
	 * @param pSize
	 *            the size of the tab in bit
	 */
	public BitUtils(final int pSize) {
		byteTab = new byte[(pSize + BYTE_SIZE - 1) / BYTE_SIZE];
		size = pSize;
	}

	/**
	 * Add pIndex to the current value of bitIndex
	 *
	 * @param pIndex
	 *            the value to add to bitIndex
	 */
	public void addCurrentBitIndex(final int pIndex) {
		currentBitIndex += pIndex;
		if (currentBitIndex < 0) {
			currentBitIndex = 0;
		}
	}

	/**
	 * Getter for the currentBitIndex
	 *
	 * @return the currentBitIndex
	 */
	public int getCurrentBitIndex() {
		return currentBitIndex;
	}

	/**
	 * Method to get all data
	 *
	 * @return a byte tab which contain all data
	 */
	public byte[] getData() {
		return Arrays.copyOf(byteTab, byteTab.length);
	}

	/**
	 * Compute a byte mask (as an int between 0 and 0xFF) with the bits
	 * [pIndex, pIndex + pLength) set (bit 0 being the most significant bit).
	 *
	 * @param pIndex
	 *            start index of the mask
	 * @param pLength
	 *            size of mask
	 * @return the mask as an int (0..0xFF)
	 */
	private static int mask(final int pIndex, final int pLength) {
		int ret = DEFAULT_VALUE >>> pIndex;
		int dec = BYTE_SIZE - (pLength + pIndex);
		if (dec > 0) {
			ret &= DEFAULT_VALUE << dec;
		}
		return ret;
	}

	/**
	 * This method is used to get a mask dynamically
	 *
	 * @param pIndex
	 *            start index of the mask
	 * @param pLength
	 *            size of mask
	 * @return the mask in byte
	 */
	public byte getMask(final int pIndex, final int pLength) {
		return (byte) mask(pIndex, pLength);
	}

	/**
	 * Get the Next boolean (read 1 bit)
	 *
	 * @return true or false
	 */
	public boolean getNextBoolean() {
		final int index = currentBitIndex;
		final boolean ret = (byteTab[index >> BYTE_SHIFT] >> (BIT_MASK - (index & BIT_MASK)) & 1) != 0;
		currentBitIndex = index + 1;
		return ret;
	}

	/**
	 * Method used to get the next byte and shift read data to the beginning of
	 * the array.<br>
	 * (Ex 00110000b if we start read 2 bit at index 2 the data returned will be
	 * 11000000b)
	 *
	 * @param pSize
	 *            the size in bit to read
	 * @return the byte array read
	 */
	public byte[] getNextByte(final int pSize) {
		return getNextByte(pSize, true);
	}

	/**
	 * Method to get The next bytes with the specified size
	 *
	 * @param pSize
	 *            the size in bit to read
	 * @param pShift
	 *            boolean to indicate if the data read will be shift to the
	 *            left.<br>
	 *            <ul>
	 *            <li>if true : (Ex 10110000b if we start read 2 bit at index 2
	 *            the returned data will be 11000000b)</li>
	 *            <li>if false : (Ex 10110000b if we start read 2 bit at index 2
	 *            the returned data will be 00110000b)</li>
	 *            </ul>
	 * @return a byte array
	 */
	public byte[] getNextByte(final int pSize, final boolean pShift) {
		final int nbBytes = (pSize + BYTE_SIZE - 1) / BYTE_SIZE;
		final byte[] tab = new byte[nbBytes];
		if (nbBytes == 0) {
			return tab;
		}
		final int start = currentBitIndex;
		final int rem = pSize & BIT_MASK;
		if ((start & BIT_MASK) == 0) {
			// Aligned read: bulk copy, then clear the unused trailing bits
			System.arraycopy(byteTab, start >> BYTE_SHIFT, tab, 0, nbBytes);
			if (rem != 0) {
				tab[nbBytes - 1] &= DEFAULT_VALUE << BYTE_SIZE - rem;
			}
		} else if (pShift || rem == 0) {
			readShifted(tab, start, pSize);
		} else {
			readUnshifted(tab, start, pSize);
		}
		currentBitIndex = start + pSize;
		return tab;
	}

	/**
	 * Unaligned read with data shifted to the beginning of the array: each
	 * output byte is a sliding window over two consecutive source bytes.
	 *
	 * @param pTab
	 *            destination array (already sized)
	 * @param pStart
	 *            start bit index (not byte aligned)
	 * @param pSize
	 *            the size in bit to read
	 */
	private void readShifted(final byte[] pTab, final int pStart, final int pSize) {
		final int end = pStart + pSize;
		final int mod = pStart & BIT_MASK;
		final int rightShift = BYTE_SIZE - mod;
		final int nbBytes = pTab.length;
		int k = pStart >> BYTE_SHIFT;
		for (int i = 0; i < nbBytes; i++, k++) {
			int val = (byteTab[k] & DEFAULT_VALUE) << mod;
			if ((k + 1) << BYTE_SHIFT < end) {
				val |= (byteTab[k + 1] & DEFAULT_VALUE) >>> rightShift;
			}
			pTab[i] = (byte) val;
		}
		final int rem = pSize & BIT_MASK;
		if (rem != 0) {
			pTab[nbBytes - 1] &= DEFAULT_VALUE << BYTE_SIZE - rem;
		}
	}

	/**
	 * Unaligned read without shift: bits keep their position inside the byte
	 * (low part of source byte k, high part of source byte k + 1).
	 *
	 * @param pTab
	 *            destination array (already sized)
	 * @param pStart
	 *            start bit index (not byte aligned)
	 * @param pSize
	 *            the size in bit to read
	 */
	private void readUnshifted(final byte[] pTab, final int pStart, final int pSize) {
		final int end = pStart + pSize;
		final int mod = pStart & BIT_MASK;
		final int available = BYTE_SIZE - mod;
		final int nbBytes = pTab.length;
		int k = pStart >> BYTE_SHIFT;
		for (int i = 0; i < nbBytes; i++, k++) {
			int remaining = end - (k << BYTE_SHIFT) - mod;
			int length = Math.min(remaining, available);
			int val = byteTab[k] & mask(mod, length);
			remaining -= length;
			if (remaining > 0) {
				val |= byteTab[k + 1] & DEFAULT_VALUE << BYTE_SIZE - Math.min(remaining, mod);
			}
			pTab[i] = (byte) val;
		}
		pTab[nbBytes - 1] &= DEFAULT_VALUE >>> mod - 1;
	}

	/**
	 * Get the (cached, per-thread) date formatter for the given pattern
	 *
	 * @param pPattern
	 *            the Date pattern
	 * @return a date formatter, never shared between threads
	 */
	private static SimpleDateFormat getDateFormat(final String pPattern) {
		Map<String, SimpleDateFormat> cache = DATE_FORMAT_CACHE.get();
		SimpleDateFormat sdf = cache.get(pPattern);
		if (sdf == null) {
			sdf = new SimpleDateFormat(pPattern);
			if (cache.size() >= MAX_CACHED_DATE_FORMATS) {
				cache.clear();
			}
			cache.put(pPattern, sdf);
		}
		return sdf;
	}

	/**
	 * Method to get the next date
	 *
	 * @param pSize
	 *            the size of the string date in bit
	 * @param pPattern
	 *            the Date pattern
	 * @return a date object or null
	 */
	public Date getNextDate(final int pSize, final String pPattern) {
		return getNextDate(pSize, pPattern, false);
	}

	/**
	 * Method to get the next date
	 *
	 * @param pSize
	 *            the size of the string date in bit
	 * @param pPattern
	 *            the Date pattern
	 * @param pUseBcd
	 *            get the Date with BCD format (Binary coded decimal)
	 * @return a date object or null
	 */
	public Date getNextDate(final int pSize, final String pPattern, final boolean pUseBcd) {
		Date date = null;
		// get date formatter
		SimpleDateFormat sdf = getDateFormat(pPattern);
		// get String
		String dateTxt;
		if (pUseBcd) {
			dateTxt = getNextHexaString(pSize);
		} else {
			dateTxt = getNextString(pSize);
		}

		try {
			date = sdf.parse(dateTxt);
		} catch (ParseException e) {
			LOGGER.error("Parsing date error. date:" + dateTxt + " pattern:" + pPattern, e);
		}
		return date;
	}

	/**
	 * This method is used to get the next String in Hexa
	 *
	 * @param pSize
	 *            the length of the string in bit
	 * @return the string
	 */
	public String getNextHexaString(final int pSize) {
		final int nbBytes = (pSize + BYTE_SIZE - 1) / BYTE_SIZE;
		if (nbBytes <= 0) {
			return "";
		}
		// Same read as getNextByte(pSize, true), but the hexa chars are produced
		// directly from the internal buffer (no intermediate byte array)
		final int start = currentBitIndex;
		final int end = start + pSize;
		final int mod = start & BIT_MASK;
		final int rem = pSize & BIT_MASK;
		final int rightShift = BYTE_SIZE - mod;
		final int last = nbBytes - 1;
		final char[] chars = new char[nbBytes << 1];
		int k = start >> BYTE_SHIFT;
		int j = 0;
		for (int i = 0; i < nbBytes; i++, k++) {
			int val = (byteTab[k] & DEFAULT_VALUE) << mod;
			if (mod != 0 && (k + 1) << BYTE_SHIFT < end) {
				val |= (byteTab[k + 1] & DEFAULT_VALUE) >>> rightShift;
			}
			if (i == last && rem != 0) {
				val &= DEFAULT_VALUE << BYTE_SIZE - rem;
			}
			val &= DEFAULT_VALUE;
			chars[j++] = BytesUtils.HEX_DIGITS[val >>> 4];
			chars[j++] = BytesUtils.HEX_DIGITS[val & 0xF];
		}
		currentBitIndex = end;
		return new String(chars);
	}

	/**
	 * Method used to get get a signed long with the specified size
	 * @param pLength length of long to get (must be lower than 64)
	 * @return the long value
	 */
	public long getNextLongSigned(final int pLength) {
		if (pLength > Long.SIZE) {
			throw new IllegalArgumentException("Long overflow with length > 64");
		}
		long decimal = getNextLong(pLength);
		long signMask = 1L << pLength - 1;

		if ( (decimal & signMask) != 0) {
			return - (signMask - (signMask ^ decimal));
		}
		return decimal;
	}

	/**
	 * Method used to get get a signed integer with the specified size
	 * @param pLength the length of the integer (must be lower than 32)
	 * @return the integer value
	 */
	public int getNextIntegerSigned(final int pLength) {
		if (pLength > Integer.SIZE) {
			throw new IllegalArgumentException("Integer overflow with length > 32");
		}
		return (int) getNextLongSigned(pLength);
	}

	/**
	 * This method is used to get a long with the specified size
	 *
	 * Be careful with java long bit sign. This method doesn't handle signed values.<br>
	 * For that, @see BitUtils.getNextLongSigned()
	 *
	 * @param pLength
	 *            the length of the data to read in bit
	 * @return an long
	 */
	public long getNextLong(final int pLength) {
		long finalValue = 0;
		int index = currentBitIndex;
		int remaining = pLength;
		while (remaining > 0) {
			int mod = index & BIT_MASK;
			int available = BYTE_SIZE - mod;
			int take = remaining < available ? remaining : available;
			int chunk = (byteTab[index >> BYTE_SHIFT] & DEFAULT_VALUE) >>> available - take & (1 << take) - 1;
			finalValue = finalValue << take | chunk;
			remaining -= take;
			index += take;
		}
		currentBitIndex = index;
		return finalValue;
	}

	/**
	 * This method is used to get an integer with the specified size
	 *
	 * Be careful with java integer bit sign. This method doesn't handle signed values.<br>
	 * For that, @see BitUtils.getNextIntegerSigned()
	 *
	 * @param pLength
	 *            the length of the data to read in bit
	 * @return an integer
	 */
	public int getNextInteger(final int pLength) {
		return (int) (getNextLong(pLength));
	}

	/**
	 * This method is used to get the next String with the specified size with
	 * the charset ASCII
	 *
	 * @param pSize
	 *            the length of the string in bit
	 * @return the string
	 */
	public String getNextString(final int pSize) {
		return getNextString(pSize, DEFAULT_CHARSET);
	}

	/**
	 * This method is used to get the next String with the specified size
	 *
	 * @param pSize
	 *            the length of the string int bit
	 * @param pCharset
	 *            the charset
	 * @return the string
	 */
	public String getNextString(final int pSize, final Charset pCharset) {
		final int start = currentBitIndex;
		if (((start | pSize) & BIT_MASK) == 0 && pSize > 0) {
			// Aligned read of whole bytes: decode directly from the internal
			// buffer (no intermediate byte array)
			final int offset = start >> BYTE_SHIFT;
			final int nbBytes = pSize >> BYTE_SHIFT;
			if (offset + nbBytes > byteTab.length) {
				throw new ArrayIndexOutOfBoundsException(offset + nbBytes);
			}
			currentBitIndex = start + pSize;
			return new String(byteTab, offset, nbBytes, pCharset);
		}
		return new String(getNextByte(pSize, true), pCharset);
	}

	/**
	 * Method used to get the size of the bit array
	 *
	 * @return the size in bits of the current bit array
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Reset the current bit index to the initial position
	 */
	public void reset() {
		setCurrentBitIndex(0);
	}

	/**
	 * Method used to clear data and reset current bit index
	 */
	public void clear() {
		Arrays.fill(byteTab, (byte) 0);
		reset();
	}

	/**
	 * Set to 0 the next N bits
	 *
	 * @param pLength
	 *            the number of bit to set at 0
	 */
	public void resetNextBits(final int pLength) {
		if (pLength <= 0) {
			return;
		}
		int index = currentBitIndex;
		final int end = index + pLength;
		int k = index >> BYTE_SHIFT;
		final int mod = index & BIT_MASK;
		if (mod != 0) {
			// Partial first byte
			int length = Math.min(end - index, BYTE_SIZE - mod);
			byteTab[k] &= ~mask(mod, length);
			index += length;
			k++;
		}
		// Full bytes
		final int fullBytes = end - index >> BYTE_SHIFT;
		if (fullBytes > 0) {
			Arrays.fill(byteTab, k, k + fullBytes, (byte) 0);
			k += fullBytes;
			index += fullBytes << BYTE_SHIFT;
		}
		// Partial last byte
		final int rest = end - index;
		if (rest > 0) {
			byteTab[k] &= DEFAULT_VALUE >>> rest;
		}
		currentBitIndex = end;
	}

	/**
	 * Setter currentBitIndex
	 *
	 * @param pCurrentBitIndex
	 *            the currentBitIndex to set
	 */
	public void setCurrentBitIndex(final int pCurrentBitIndex) {
		currentBitIndex = pCurrentBitIndex;
	}

	/**
	 * Method to set a boolean
	 *
	 * @param pBoolean
	 *            the boolean to set
	 */
	public void setNextBoolean(final boolean pBoolean) {
		final int index = currentBitIndex;
		byteTab[index >> BYTE_SHIFT] |= pBoolean ? 0x80 >>> (index & BIT_MASK) : 0;
		currentBitIndex = index + 1;
	}

	/**
	 * Method to write bytes with the max length
	 *
	 * @param pValue
	 *            the value to write
	 * @param pLength
	 *            the length of the data in bits
	 */
	public void setNextByte(final byte[] pValue, final int pLength) {
		setNextByte(pValue, pLength, true);
	}

	/**
	 * Method to write bytes with the max length
	 *
	 * @param pValue
	 *            the value to write
	 * @param pLength
	 *            the length of the data in bits
	 * @param pPadBefore
	 *            if true pad with 0
	 */
	public void setNextByte(final byte[] pValue, final int pLength, final boolean pPadBefore) {
		final int totalSize = (pLength + BYTE_SIZE - 1) / BYTE_SIZE;
		final byte[] src;
		if (pValue.length >= totalSize) {
			// No padding needed: read directly from the input (only the first
			// totalSize bytes are used)
			src = pValue;
		} else {
			src = new byte[totalSize];
			System.arraycopy(pValue, 0, src, pPadBefore ? totalSize - pValue.length : 0, pValue.length);
		}
		final int start = currentBitIndex;
		final int mod = start & BIT_MASK;
		int k = start >> BYTE_SHIFT;
		if (mod == 0) {
			final int rem = pLength & BIT_MASK;
			if (rem == 0 || totalSize <= 0) {
				System.arraycopy(src, 0, byteTab, k, totalSize);
			} else {
				System.arraycopy(src, 0, byteTab, k, totalSize - 1);
				// Last partial byte: write only the 'rem' first bits and keep
				// the bits located after the field untouched
				final int m = DEFAULT_VALUE << BYTE_SIZE - rem;
				final int last = k + totalSize - 1;
				byteTab[last] = (byte) (byteTab[last] & ~m | src[totalSize - 1] & m);
			}
		} else {
			final int end = start + pLength;
			final int leftShift = BYTE_SIZE - mod;
			for (int i = 0; i < totalSize; i++, k++) {
				int val = src[i] & DEFAULT_VALUE;
				int bits = end - (k << BYTE_SHIFT) - mod;
				if (bits < BYTE_SIZE) {
					// Last byte: keep only the top 'bits' bits
					val &= DEFAULT_VALUE << BYTE_SIZE - bits;
				}
				byteTab[k] |= val >>> mod;
				if ((k + 1) << BYTE_SHIFT < end) {
					byteTab[k + 1] |= val << leftShift;
				}
			}
		}
		currentBitIndex = start + pLength;
	}

	/**
	 * Method to write a date
	 *
	 * @param pValue
	 *            the value to write
	 * @param pPattern
	 *            the Date pattern
	 */
	public void setNextDate(final Date pValue, final String pPattern) {
		setNextDate(pValue, pPattern, false);
	}

	/**
	 * Method to write a date
	 *
	 * @param pValue
	 *            the value to write
	 * @param pPattern
	 *            the Date pattern
	 * @param pUseBcd
	 *            write date as BCD (binary coded decimal)
	 */
	public void setNextDate(final Date pValue, final String pPattern, final boolean pUseBcd) {
		// get date formatter
		String value = getDateFormat(pPattern).format(pValue);

		if (pUseBcd) {
			setNextHexaString(value, value.length() * 4);
		} else {
			setNextString(value, value.length() * 8);
		}
	}

	/**
	 * Method to write Hexa String with the max length
	 *
	 * @param pValue
	 *            the value to write
	 * @param pLength
	 *            the length of the data in bits
	 */
	public void setNextHexaString(final String pValue, final int pLength) {
		setNextByte(BytesUtils.fromString(pValue), pLength);
	}

	/**
	 * Add Long to the current position with the specified size
	 *
	 * Be careful with java long bit sign
	 *
	 * @param pValue
	 *            the value to set
	 *
	 * @param pLength
	 *            the length of the long
	 */
	public void setNextLong(final long pValue, final int pLength) {

		if (pLength > Long.SIZE) {
			throw new IllegalArgumentException("Long overflow with length > 64");
		}

		setNextValue(pValue, pLength, Long.SIZE - 1);
	}

	/**
	 * Add Value to the current position with the specified size
	 *
	 * @param pValue
	 *            value to add
	 * @param pLength
	 *            length of the value
	 * @param pMaxSize
	 *            max size in bits
	 */
	private void setNextValue(final long pValue, final int pLength, final int pMaxSize) {
		long value = pValue;
		// Set to max value if pValue cannot be stored on pLength bits.
		long bitMax = 1L << Math.min(pLength, pMaxSize);
		if (bitMax > 0 && pValue >= bitMax) {
			value = bitMax - 1;
		}
		// Keep only the pLength low bits (two's complement for negative values)
		// so that the sign extension cannot corrupt the bits located before the
		// current position when the write is not byte aligned.
		if (pLength < Long.SIZE) {
			value &= (1L << pLength) - 1;
		}
		int index = currentBitIndex;
		int writeSize = pLength;
		while (writeSize > 0) {
			int mod = index & BIT_MASK;
			// Align the 'writeSize' remaining bits of the value on the current
			// bit position: bits above the byte are dropped by the byte cast
			int shift = writeSize + mod - BYTE_SIZE;
			byteTab[index >> BYTE_SHIFT] |= shift >= 0 ? value >>> shift : value << -shift;
			int written = Math.min(writeSize, BYTE_SIZE - mod);
			writeSize -= written;
			index += written;
		}
		currentBitIndex = index;
	}

	/**
	 * Add Integer to the current position with the specified size
	 *
	 * Be careful with java integer bit sign
	 *
	 * @param pValue
	 *            the value to set
	 *
	 * @param pLength
	 *            the length of the integer
	 */
	public void setNextInteger(final int pValue, final int pLength) {

		if (pLength > Integer.SIZE) {
			throw new IllegalArgumentException("Integer overflow with length > 32");
		}

		setNextValue(pValue, pLength, Integer.SIZE - 1);
	}

	/**
	 * Method to write String
	 *
	 * @param pValue
	 *            the string to write
	 *
	 * @param pLength
	 *            the length of the integer
	 */
	public void setNextString(final String pValue, final int pLength) {
		setNextString(pValue, pLength, true);
	}

	/**
	 * Method to write a String
	 *
	 * @param pValue
	 *            the string to write
	 * @param pLength
	 *            the string length
	 * @param pPaddedBefore
	 *            indicate if the string is padded before or after
	 */
	public void setNextString(final String pValue, final int pLength, final boolean pPaddedBefore) {
		setNextByte(pValue.getBytes(DEFAULT_CHARSET), pLength, pPaddedBefore);
	}
}
