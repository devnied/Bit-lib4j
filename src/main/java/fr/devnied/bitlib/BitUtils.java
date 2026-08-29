package fr.devnied.bitlib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class to manage bit with java.<br>
 * Writers ({@code setNext*}) overwrite the bits of the field they write and
 * leave the other bits untouched. All sizes and lengths are expressed in bits
 * and must not be negative (an {@link IllegalArgumentException} is thrown
 * otherwise). Reading or writing past the size of the buffer throws an
 * {@link IndexOutOfBoundsException}; the current index is always kept in
 * [0, size]. The size of a buffer is at most Integer.MAX_VALUE - 7 bits
 * (268 435 455 bytes) so that every index and length fits in an int.
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
	 * Maximum size of a buffer in bits (Integer.MAX_VALUE - 7): the byte count
	 * ceil(size / 8) of any larger size does not fit in an int
	 */
	private static final int MAX_SIZE = Integer.MAX_VALUE - BIT_MASK;
	/**
	 * Constant for the default charset (Charset.forName is used instead of
	 * StandardCharsets to stay compatible with Android API level lower than 19)
	 */
	private static final Charset DEFAULT_CHARSET = Charset.forName("ASCII");

	/**
	 * Simple date format (yyyyMMdd)
	 */
	public static final String DATE_FORMAT = "yyyyMMdd";

	/**
	 * Max number of date formatters kept per thread (safeguard against
	 * unbounded growth with dynamic patterns): beyond it the least recently
	 * used pattern is evicted
	 */
	private static final int MAX_CACHED_DATE_FORMATS = 32;

	/**
	 * Per-thread cache of date formatters, keyed by pattern (access ordered
	 * map, see {@link #MAX_CACHED_DATE_FORMATS}).<br>
	 * {@link SimpleDateFormat} is expensive to build and not thread safe, so each
	 * thread keeps its own instances. A formatter captures the default time zone
	 * and locale of the thread at its first use with a given pattern.<br>
	 * A plain ThreadLocal holding JDK-only values: nothing kept in the threads
	 * references the class loader of this library (no leak warning from servlet
	 * containers).
	 */
	private static final ThreadLocal<Map<String, SimpleDateFormat>> DATE_FORMAT_CACHE = new ThreadLocal<Map<String, SimpleDateFormat>>();

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
	 * Constructor of the class (the array is copied)
	 *
	 * @param pByte
	 *            byte read
	 * @throws IllegalArgumentException
	 *             if pByte holds more than Integer.MAX_VALUE / 8 bytes (the
	 *             size in bits would not fit in an int)
	 */
	public BitUtils(final byte[] pByte) {
		if (pByte.length > MAX_SIZE / BYTE_SIZE) {
			throw new IllegalArgumentException("Buffer too large: " + pByte.length + " bytes, at most " + MAX_SIZE / BYTE_SIZE
					+ " bytes (the size in bits must fit in an int)");
		}
		byteTab = Arrays.copyOf(pByte, pByte.length);
		size = pByte.length * BYTE_SIZE;
	}

	/**
	 * Constructor for empty byte tab
	 *
	 * @param pSize
	 *            the size of the tab in bit
	 * @throws IllegalArgumentException
	 *             if pSize is negative or greater than Integer.MAX_VALUE - 7
	 */
	public BitUtils(final int pSize) {
		checkLength(pSize);
		if (pSize > MAX_SIZE) {
			throw new IllegalArgumentException("Size too large: " + pSize + " bits, at most " + MAX_SIZE + " bits");
		}
		byteTab = new byte[(pSize + BYTE_SIZE - 1) / BYTE_SIZE];
		size = pSize;
	}

	/**
	 * Add pIndex to the current value of bitIndex. The resulting index is
	 * clamped to [0, size] (size included: it is the position after the last
	 * bit).
	 *
	 * @param pIndex
	 *            the value to add to bitIndex
	 */
	public void addCurrentBitIndex(final int pIndex) {
		// long arithmetic: no int overflow when pIndex is close to MIN/MAX_VALUE
		final long index = (long) currentBitIndex + pIndex;
		setCurrentBitIndex((int) Math.max(0L, Math.min(index, size)));
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
	 * Check that a size or length in bits is not negative
	 *
	 * @param pLength
	 *            the size or length to check
	 * @throws IllegalArgumentException
	 *             if pLength is negative
	 */
	private static void checkLength(final int pLength) {
		if (pLength < 0) {
			throw new IllegalArgumentException("Length in bits must not be negative: " + pLength);
		}
	}

	/**
	 * Check that the next pLength bits can be read or written: the length must
	 * not be negative and the field must not exceed the size of the buffer
	 *
	 * @param pLength
	 *            the length in bits to read or write from the current index
	 * @throws IllegalArgumentException
	 *             if pLength is negative
	 * @throws IndexOutOfBoundsException
	 *             if currentBitIndex + pLength exceeds the size
	 */
	private void checkRange(final int pLength) {
		checkLength(pLength);
		// pLength > size - currentBitIndex <=> currentBitIndex + pLength > size,
		// without int overflow since currentBitIndex is always within [0, size]
		if (pLength > size - currentBitIndex) {
			throw new IndexOutOfBoundsException("Bits [" + currentBitIndex + ", " + ((long) currentBitIndex + pLength)
					+ ") exceed the size of the buffer (" + size + " bits)");
		}
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
		if (index >= size) {
			checkRange(1);
		}
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
	 *            left. In both modes the bits outside the field are 0.<br>
	 *            <ul>
	 *            <li>if true : the field is packed at the beginning of the
	 *            array, which holds ceil(pSize / 8) bytes (Ex 10110000b if we
	 *            start read 2 bit at index 2 the returned data will be
	 *            11000000b)</li>
	 *            <li>if false : nothing is shifted, the source bytes covered by
	 *            the field are returned as they are with the bits outside the
	 *            field cleared. The array holds ceil((index % 8 + pSize) / 8)
	 *            bytes (an empty array when pSize is 0), i.e. one more than in
	 *            shifted mode when the field spans an extra source byte (Ex
	 *            10110000b if we start read 2
	 *            bit at index 2 the returned data will be 00110000b; Ex
	 *            01000100b 11111110b, 8 bits read at index 4 : 00000100b
	 *            11110000b)</li>
	 *            </ul>
	 * @return a byte array
	 */
	public byte[] getNextByte(final int pSize, final boolean pShift) {
		checkRange(pSize);
		final int start = currentBitIndex;
		final int mod = start & BIT_MASK;
		final int nbBytes;
		if (pShift || mod == 0 || pSize == 0) {
			nbBytes = (pSize + BYTE_SIZE - 1) / BYTE_SIZE;
		} else {
			// Unshifted read: the source bytes covered by the field are returned
			nbBytes = (mod + pSize + BYTE_SIZE - 1) / BYTE_SIZE;
		}
		final byte[] tab = new byte[nbBytes];
		if (nbBytes == 0) {
			return tab;
		}
		if (mod == 0) {
			// Aligned read: bulk copy, then clear the unused trailing bits
			System.arraycopy(byteTab, start >> BYTE_SHIFT, tab, 0, nbBytes);
			final int rem = pSize & BIT_MASK;
			if (rem != 0) {
				tab[nbBytes - 1] &= DEFAULT_VALUE << BYTE_SIZE - rem;
			}
		} else if (pShift) {
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
	 * Unaligned read without shift: the source bytes covered by the field are
	 * copied as they are, only the bits located outside the field (before it
	 * in the first byte, after it in the last byte) are cleared.
	 *
	 * @param pTab
	 *            destination array (already sized to the number of source
	 *            bytes covered by the field)
	 * @param pStart
	 *            start bit index (not byte aligned)
	 * @param pSize
	 *            the size in bit to read
	 */
	private void readUnshifted(final byte[] pTab, final int pStart, final int pSize) {
		final int last = pTab.length - 1;
		final int mod = pStart & BIT_MASK;
		final int endMod = mod + pSize & BIT_MASK;
		// Field bits of the first byte: [mod, 8); of the last byte: [0, endMod)
		// (the whole byte when endMod == 0)
		final int firstMask = DEFAULT_VALUE >>> mod;
		final int lastMask = endMod == 0 ? DEFAULT_VALUE : DEFAULT_VALUE << BYTE_SIZE - endMod;
		final int k = pStart >> BYTE_SHIFT;
		if (last == 0) {
			pTab[0] = (byte) (byteTab[k] & firstMask & lastMask);
			return;
		}
		// Plain loop: unshifted reads are small, System.arraycopy would cost
		// more than it saves
		pTab[0] = (byte) (byteTab[k] & firstMask);
		for (int i = 1; i < last; i++) {
			pTab[i] = byteTab[k + i];
		}
		pTab[last] = (byte) (byteTab[k + last] & lastMask);
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
		if (cache == null) {
			// Access ordered: the least recently used pattern comes first
			cache = new LinkedHashMap<String, SimpleDateFormat>(16, 0.75f, true);
			DATE_FORMAT_CACHE.set(cache);
		}
		SimpleDateFormat sdf = cache.get(pPattern);
		if (sdf == null) {
			sdf = new SimpleDateFormat(pPattern);
			if (cache.size() >= MAX_CACHED_DATE_FORMATS) {
				// Evict the least recently used formatter only
				Iterator<String> eldest = cache.keySet().iterator();
				eldest.next();
				eldest.remove();
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
	 * Method to get the next date.<br>
	 * The pSize next bits are read as a string (ASCII, or BCD when pUseBcd is
	 * true) and parsed with the pattern. When the text cannot be parsed, null
	 * is returned, the bits are consumed anyway and a warning is logged (the
	 * stack trace at debug level only). Reading 0 bit returns null without
	 * logging. The parser is a {@link SimpleDateFormat} kept per thread and per
	 * pattern: it is lenient and uses the default time zone and locale of the
	 * thread at its first use with the pattern.
	 *
	 * @param pSize
	 *            the size of the string date in bit
	 * @param pPattern
	 *            the Date pattern
	 * @param pUseBcd
	 *            get the Date with BCD format (Binary coded decimal)
	 * @return a date object or null
	 * @throws NullPointerException
	 *             if pPattern is null (nothing is read)
	 * @throws IllegalArgumentException
	 *             if pSize is negative or pPattern is invalid
	 * @throws IndexOutOfBoundsException
	 *             if pSize exceeds the remaining bits
	 */
	public Date getNextDate(final int pSize, final String pPattern, final boolean pUseBcd) {
		// get date formatter (an invalid pattern fails before any bit is read)
		SimpleDateFormat sdf = getDateFormat(pPattern);
		// get String
		String dateTxt;
		if (pUseBcd) {
			dateTxt = getNextHexaString(pSize);
		} else {
			dateTxt = getNextString(pSize);
		}
		if (dateTxt.length() == 0) {
			// Nothing read (pSize == 0): no date and no parsing error to report
			return null;
		}
		try {
			return sdf.parse(dateTxt);
		} catch (ParseException e) {
			LOGGER.warn("Parsing date error. date:{} pattern:{}", dateTxt, pPattern);
			LOGGER.debug("Parsing date error", e);
			return null;
		}
	}

	/**
	 * This method is used to get the next String in Hexa
	 *
	 * @param pSize
	 *            the length of the string in bit
	 * @return the string
	 */
	public String getNextHexaString(final int pSize) {
		checkRange(pSize);
		final int nbBytes = (pSize + BYTE_SIZE - 1) / BYTE_SIZE;
		if (nbBytes == 0) {
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
	 * Method used to get a signed long (two's complement) with the specified
	 * size.<br>
	 * On {@code pLength} bits the value range is [-2^(pLength-1), 2^(pLength-1) - 1]:
	 * the sign bit is part of the field. With {@code pLength} = 64 the field is a
	 * plain Java long, so {@link Long#MIN_VALUE} and {@link Long#MAX_VALUE} round trip.
	 *
	 * @param pLength
	 *            the length in bits of the long to read (at most 64)
	 * @return the signed long value
	 * @throws IllegalArgumentException
	 *             if pLength is negative or greater than 64
	 * @throws IndexOutOfBoundsException
	 *             if pLength exceeds the remaining bits
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
	 * Method used to get a signed integer (two's complement) with the specified
	 * size.<br>
	 * On {@code pLength} bits the value range is [-2^(pLength-1), 2^(pLength-1) - 1]:
	 * the sign bit is part of the field. With {@code pLength} = 32 the field is a
	 * plain Java int, so {@link Integer#MIN_VALUE} and {@link Integer#MAX_VALUE} round trip.
	 *
	 * @param pLength
	 *            the length in bits of the integer to read (at most 32)
	 * @return the signed integer value
	 * @throws IllegalArgumentException
	 *             if pLength is negative or greater than 32
	 * @throws IndexOutOfBoundsException
	 *             if pLength exceeds the remaining bits
	 */
	public int getNextIntegerSigned(final int pLength) {
		if (pLength > Integer.SIZE) {
			throw new IllegalArgumentException("Integer overflow with length > 32");
		}
		return (int) getNextLongSigned(pLength);
	}

	/**
	 * This method is used to get a long with the specified size (unsigned value
	 * on pLength bits, 64 at most)
	 *
	 * Be careful with java long bit sign. This method doesn't handle signed values.<br>
	 * For that, @see BitUtils.getNextLongSigned()
	 *
	 * @param pLength
	 *            the length of the data to read in bit (at most 64)
	 * @return an long
	 * @throws IllegalArgumentException
	 *             if pLength is negative or greater than 64
	 * @throws IndexOutOfBoundsException
	 *             if pLength exceeds the remaining bits
	 */
	public long getNextLong(final int pLength) {
		if (pLength > Long.SIZE) {
			throw new IllegalArgumentException("Long overflow with length > 64");
		}
		checkRange(pLength);
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
	 * This method is used to get an integer with the specified size (unsigned
	 * value on pLength bits; from 33 to 64 bits the value is read as a long and
	 * narrowed to its 32 low bits)
	 *
	 * Be careful with java integer bit sign. This method doesn't handle signed values.<br>
	 * For that, @see BitUtils.getNextIntegerSigned()
	 *
	 * @param pLength
	 *            the length of the data to read in bit (at most 64)
	 * @return an integer
	 * @throws IllegalArgumentException
	 *             if pLength is negative or greater than 64
	 * @throws IndexOutOfBoundsException
	 *             if pLength exceeds the remaining bits
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
	 *            the length of the string in bit
	 * @param pCharset
	 *            the charset
	 * @return the string
	 * @throws NullPointerException
	 *             if pCharset is null (nothing is read)
	 * @throws IllegalArgumentException
	 *             if pSize is negative
	 * @throws IndexOutOfBoundsException
	 *             if pSize exceeds the remaining bits
	 */
	public String getNextString(final int pSize, final Charset pCharset) {
		if (pCharset == null) {
			throw new NullPointerException("Charset must not be null");
		}
		checkRange(pSize);
		final int start = currentBitIndex;
		if (((start | pSize) & BIT_MASK) == 0 && pSize > 0) {
			// Aligned read of whole bytes: decode directly from the internal
			// buffer (no intermediate byte array)
			final int offset = start >> BYTE_SHIFT;
			final int nbBytes = pSize >> BYTE_SHIFT;
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
		checkRange(pLength);
		if (pLength == 0) {
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
	 * Setter currentBitIndex. The index is clamped to [0, size] (size
	 * included: it is the position after the last bit).
	 *
	 * @param pCurrentBitIndex
	 *            the currentBitIndex to set
	 */
	public void setCurrentBitIndex(final int pCurrentBitIndex) {
		currentBitIndex = Math.max(0, Math.min(pCurrentBitIndex, size));
	}

	/**
	 * Method to set a boolean
	 *
	 * @param pBoolean
	 *            the boolean to set
	 */
	public void setNextBoolean(final boolean pBoolean) {
		final int index = currentBitIndex;
		if (index >= size) {
			checkRange(1);
		}
		final int k = index >> BYTE_SHIFT;
		final int bit = 0x80 >>> (index & BIT_MASK);
		if (pBoolean) {
			byteTab[k] |= bit;
		} else {
			byteTab[k] &= ~bit;
		}
		currentBitIndex = index + 1;
	}

	/**
	 * Method to write bytes with the specified length, padded before
	 * (right-aligned on the field) when the value is shorter, see
	 * {@link #setNextByte(byte[], int, boolean)}
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
	 * Method to write bytes with the specified length.<br>
	 * When the value is shorter than pLength bits it is padded with zeros:
	 * before the value (right-aligned on the pLength bits of the field, Ex
	 * {0x05} on 12 bits writes 000000000101b) or after it (left-aligned, Ex
	 * {0x05} on 12 bits writes 000001010000b). When the value is longer than
	 * pLength bits, its leading pLength bits are written whatever the mode.
	 *
	 * @param pValue
	 *            the value to write
	 * @param pLength
	 *            the length of the data in bits
	 * @param pPadBefore
	 *            true to pad before the value (right-align), false to pad
	 *            after it (left-align)
	 * @throws NullPointerException
	 *             if pValue is null
	 * @throws IllegalArgumentException
	 *             if pLength is negative
	 * @throws IndexOutOfBoundsException
	 *             if pLength exceeds the remaining bits
	 */
	public void setNextByte(final byte[] pValue, final int pLength, final boolean pPadBefore) {
		checkRange(pLength);
		final int totalSize = (pLength + BYTE_SIZE - 1) / BYTE_SIZE;
		final byte[] src;
		if (pValue.length >= totalSize) {
			// No padding needed: read directly from the input (only the first
			// totalSize bytes are used)
			src = pValue;
		} else {
			src = new byte[totalSize];
			if (pPadBefore) {
				System.arraycopy(pValue, 0, src, totalSize - pValue.length, pValue.length);
				final int rem = pLength & BIT_MASK;
				if (rem != 0) {
					// Right-align the value on the pLength bits of the field (not
					// on the byte boundary): shift it left so that its last bit
					// lands on the last bit of the field
					final int shift = BYTE_SIZE - rem;
					for (int i = 0; i < totalSize - 1; i++) {
						src[i] = (byte) (src[i] << shift | (src[i + 1] & DEFAULT_VALUE) >>> rem);
					}
					src[totalSize - 1] = (byte) (src[totalSize - 1] << shift);
				}
			} else {
				System.arraycopy(pValue, 0, src, 0, pValue.length);
			}
		}
		final int start = currentBitIndex;
		final int mod = start & BIT_MASK;
		int k = start >> BYTE_SHIFT;
		if (mod == 0) {
			final int rem = pLength & BIT_MASK;
			if (rem == 0) {
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
			// Bits to keep in byteTab[k] / byteTab[k + 1] when a full source byte
			// is written over them (positions [mod, 8) / [0, mod) are overwritten)
			final int keepHigh = ~(DEFAULT_VALUE >>> mod);
			final int keepLow = ~(DEFAULT_VALUE << leftShift);
			for (int i = 0; i < totalSize; i++, k++) {
				int val = src[i] & DEFAULT_VALUE;
				int bits = end - (k << BYTE_SHIFT) - mod;
				if (bits >= BYTE_SIZE) {
					byteTab[k] = (byte) (byteTab[k] & keepHigh | val >>> mod);
					byteTab[k + 1] = (byte) (byteTab[k + 1] & keepLow | val << leftShift);
				} else {
					// Last byte: keep only the top 'bits' bits, overwrite the
					// field positions [mod, mod + hi) of byteTab[k] and [0, lo) of
					// byteTab[k + 1] and keep the others
					val &= DEFAULT_VALUE << BYTE_SIZE - bits;
					final int hi = Math.min(bits, leftShift);
					byteTab[k] = (byte) (byteTab[k] & ~mask(mod, hi) | val >>> mod);
					if (bits > leftShift) {
						final int lo = bits - leftShift;
						byteTab[k + 1] = (byte) (byteTab[k + 1] & ~mask(0, lo) | val << leftShift);
					}
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
	 * @throws NullPointerException
	 *             if pValue or pPattern is null (nothing is written)
	 * @throws IllegalArgumentException
	 *             if pPattern is invalid
	 * @throws IndexOutOfBoundsException
	 *             if the formatted date exceeds the remaining bits
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
	 * Method to write Hexa String with the specified length, padded before
	 * (right-aligned on the field) when the value is shorter, see
	 * {@link #setNextByte(byte[], int, boolean)} and
	 * {@link BytesUtils#fromString(String)}
	 *
	 * @param pValue
	 *            the value to write
	 * @param pLength
	 *            the length of the data in bits
	 * @throws IllegalArgumentException
	 *             if pValue is null or not a valid hexa string, or if pLength is
	 *             negative
	 * @throws IndexOutOfBoundsException
	 *             if pLength exceeds the remaining bits
	 */
	public void setNextHexaString(final String pValue, final int pLength) {
		setNextByte(BytesUtils.fromString(pValue), pLength);
	}

	/**
	 * Add Long to the current position with the specified size.<br>
	 * The value is written on pLength bits. A value that does not fit is
	 * saturated: to 2^pLength - 1 (the unsigned maximum) when it is above, to
	 * -2^(pLength - 1) (the signed minimum) when it is below. Negative values
	 * are written as two's complement on pLength bits (setNextLong(-1, 8)
	 * writes 0xFF), see {@link #getNextLong(int)} and
	 * {@link #getNextLongSigned(int)} to read them back.
	 *
	 * @param pValue
	 *            the value to set
	 * @param pLength
	 *            the length of the long in bits (at most 64)
	 * @throws IllegalArgumentException
	 *             if pLength is negative or greater than 64
	 * @throws IndexOutOfBoundsException
	 *             if pLength exceeds the remaining bits
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
		checkRange(pLength);
		long value = pValue;
		// Saturate a value that cannot be stored on pLength bits: to the
		// unsigned maximum 2^pLength - 1 when it is above...
		long bitMax = 1L << Math.min(pLength, pMaxSize);
		if (bitMax > 0 && pValue >= bitMax) {
			value = bitMax - 1;
		} else if (pValue < 0 && pLength > 0 && pLength < Long.SIZE) {
			// ... and to the signed minimum -2^(pLength - 1) when it is below
			// (any long fits on 64 bits)
			long min = -(1L << pLength - 1);
			if (pValue < min) {
				value = min;
			}
		}
		// Keep only the pLength low bits (two's complement for negative values):
		// from here the value is a plain bit pattern
		if (pLength < Long.SIZE) {
			value &= (1L << pLength) - 1;
		}
		int index = currentBitIndex;
		int writeSize = pLength;
		while (writeSize > 0) {
			int mod = index & BIT_MASK;
			int written = Math.min(writeSize, BYTE_SIZE - mod);
			// Align the 'writeSize' remaining bits of the value on the current
			// bit position: bits above the byte are dropped by the byte cast
			int shift = writeSize + mod - BYTE_SIZE;
			int bits = (int) (shift >= 0 ? value >>> shift : value << -shift);
			int k = index >> BYTE_SHIFT;
			// Overwrite the field bits (positions [mod, mod + written)) and keep
			// the others
			int m = mask(mod, written);
			byteTab[k] = (byte) (byteTab[k] & ~m | bits & m);
			writeSize -= written;
			index += written;
		}
		currentBitIndex = index;
	}

	/**
	 * Add Integer to the current position with the specified size.<br>
	 * The value is written on pLength bits. A value that does not fit is
	 * saturated: to 2^pLength - 1 (the unsigned maximum) when it is above, to
	 * -2^(pLength - 1) (the signed minimum) when it is below. Negative values
	 * are written as two's complement on pLength bits (setNextInteger(-1, 8)
	 * writes 0xFF), see {@link #getNextInteger(int)} and
	 * {@link #getNextIntegerSigned(int)} to read them back.
	 *
	 * @param pValue
	 *            the value to set
	 * @param pLength
	 *            the length of the integer in bits (at most 32)
	 * @throws IllegalArgumentException
	 *             if pLength is negative or greater than 32
	 * @throws IndexOutOfBoundsException
	 *             if pLength exceeds the remaining bits
	 */
	public void setNextInteger(final int pValue, final int pLength) {

		if (pLength > Integer.SIZE) {
			throw new IllegalArgumentException("Integer overflow with length > 32");
		}

		setNextValue(pValue, pLength, Integer.SIZE - 1);
	}

	/**
	 * Method to write String (ASCII), padded before (right-aligned on the
	 * field) when the string is shorter, see
	 * {@link #setNextString(String, int, boolean)}
	 *
	 * @param pValue
	 *            the string to write
	 * @param pLength
	 *            the length of the string in bits
	 */
	public void setNextString(final String pValue, final int pLength) {
		setNextString(pValue, pLength, true);
	}

	/**
	 * Method to write a String (ASCII), see
	 * {@link #setNextByte(byte[], int, boolean)} for the padding and
	 * truncation rules
	 *
	 * @param pValue
	 *            the string to write
	 * @param pLength
	 *            the string length in bits
	 * @param pPaddedBefore
	 *            indicate if the string is padded before or after
	 * @throws NullPointerException
	 *             if pValue is null
	 * @throws IllegalArgumentException
	 *             if pLength is negative
	 * @throws IndexOutOfBoundsException
	 *             if pLength exceeds the remaining bits
	 */
	public void setNextString(final String pValue, final int pLength, final boolean pPaddedBefore) {
		setNextByte(pValue.getBytes(DEFAULT_CHARSET), pLength, pPaddedBefore);
	}
}
