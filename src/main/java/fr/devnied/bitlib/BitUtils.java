package fr.devnied.bitlib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

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
	 * Constant for byte size (float)
	 */
	public static final float BYTE_SIZE_F = Byte.SIZE;
	/**
	 * 255 init value
	 */
	private static final int DEFAULT_VALUE = 0xFF;
	/**
	 * Constant for the default charset
	 */
	private static final Charset DEFAULT_CHARSET = Charset.forName("ASCII");

	/**
	 * Simple date format (yyyMMdd)
	 */
	public static final String DATE_FORMAT = "yyyyMMdd";

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
	public BitUtils(final byte pByte[]) {
		byteTab = new byte[pByte.length];
		System.arraycopy(pByte, 0, byteTab, 0, pByte.length);
		size = pByte.length * BYTE_SIZE;
	}

	/**
	 * Constructor for empty byte tab
	 * 
	 * @param pSize
	 *            the size of the tab in bit
	 */
	public BitUtils(final int pSize) {
		byteTab = new byte[(int) Math.ceil(pSize / BYTE_SIZE_F)];
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
		byte[] ret = new byte[byteTab.length];
		System.arraycopy(byteTab, 0, ret, 0, byteTab.length);
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
		byte ret = (byte) DEFAULT_VALUE;
		// Add X 0 to the left
		ret = (byte) (ret << pIndex);
		ret = (byte) ((ret & DEFAULT_VALUE) >> pIndex);
		// Add X 0 to the right
		int dec = BYTE_SIZE - (pLength + pIndex);
		if (dec > 0) {
			ret = (byte) (ret >> dec);
			ret = (byte) (ret << dec);
		}
		return ret;
	}

	/**
	 * Get the Next boolean (read 1 bit)
	 * 
	 * @return true or false
	 */
	public boolean getNextBoolean() {
		boolean ret = false;
		if (getNextInteger(1) == 1) {
			ret = true;
		}
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
		byte[] tab = new byte[(int) Math.ceil(pSize / BYTE_SIZE_F)];

		if (currentBitIndex % BYTE_SIZE != 0) {
			int index = 0;
			int max = currentBitIndex + pSize;
			while (currentBitIndex < max) {
				int mod = currentBitIndex % BYTE_SIZE;
				int modTab = index % BYTE_SIZE;
				int length = Math.min(max - currentBitIndex, Math.min(BYTE_SIZE - mod, BYTE_SIZE - modTab));
				byte val = (byte) (byteTab[currentBitIndex / BYTE_SIZE] & getMask(mod, length));
				if (pShift || pSize % BYTE_SIZE == 0) {
					if (mod != 0) {
						val = (byte) (val << Math.min(mod, BYTE_SIZE - length));
					} else {
						val = (byte) ((val & DEFAULT_VALUE) >> modTab);
					}
				}
				tab[index / BYTE_SIZE] |= val;
				currentBitIndex += length;
				index += length;
			}
			if (!pShift && pSize % BYTE_SIZE != 0) {
				tab[tab.length - 1] = (byte) (tab[tab.length - 1] & getMask((max - pSize - 1) % BYTE_SIZE, BYTE_SIZE));
			}
		} else {
			System.arraycopy(byteTab, currentBitIndex / BYTE_SIZE, tab, 0, tab.length);
			int val = pSize % BYTE_SIZE;
			if (val == 0) {
				val = BYTE_SIZE;
			}
			tab[tab.length - 1] = (byte) (tab[tab.length - 1] & getMask(currentBitIndex % BYTE_SIZE, val));
			currentBitIndex += pSize;
		}

		return tab;
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
		// create date formatter
		SimpleDateFormat sdf = new SimpleDateFormat(pPattern);
		// get String
		String dateTxt = null;
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
		return BytesUtils.bytesToStringNoSpace(getNextByte(pSize, true));
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
		long signMask = 1 << pLength - 1;

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
		// allocate Size of Integer
		ByteBuffer buffer = ByteBuffer.allocate(BYTE_SIZE * 2);
		// final value
		long finalValue = 0;
		// Incremental value
		long currentValue = 0;
		// Size to read
		int readSize = pLength;
		// length max of the index
		int max = currentBitIndex + pLength;
		while (currentBitIndex < max) {
			int mod = currentBitIndex % BYTE_SIZE;
			// apply the mask to the selected byte
			currentValue = byteTab[currentBitIndex / BYTE_SIZE] & getMask(mod, readSize) & DEFAULT_VALUE;
			// Shift right the read value
			int dec = Math.max(BYTE_SIZE - (mod + readSize), 0);
			currentValue = (currentValue & DEFAULT_VALUE) >>> dec & DEFAULT_VALUE;
			// Shift left the previously read value and add the current value
			finalValue = finalValue << Math.min(readSize, BYTE_SIZE) | currentValue;
			// calculate read value size
			int val = BYTE_SIZE - mod;
			// Decrease the size left
			readSize = readSize - val;
			currentBitIndex = Math.min(currentBitIndex + val, max);
		}
		buffer.putLong(finalValue);
		// reset the current bytebuffer index to 0
		buffer.rewind();
		// return integer
		return buffer.getLong();
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
		int max = currentBitIndex + pLength;
		while (currentBitIndex < max) {
			int mod = currentBitIndex % BYTE_SIZE;
			int length = Math.min(max - currentBitIndex, BYTE_SIZE - mod);
			byteTab[currentBitIndex / BYTE_SIZE] &= ~getMask(mod, length);
			currentBitIndex += length;
		}
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
		if (pBoolean) {
			setNextInteger(1, 1);
		} else {
			setNextInteger(0, 1);
		}
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
		int totalSize = (int) Math.ceil(pLength / BYTE_SIZE_F);
		ByteBuffer buffer = ByteBuffer.allocate(totalSize);
		int size = Math.max(totalSize - pValue.length, 0);
		if (pPadBefore) {
			for (int i = 0; i < size; i++) {
				buffer.put((byte) 0);
			}
		}
		buffer.put(pValue, 0, Math.min(totalSize, pValue.length));
		if (!pPadBefore) {
			for (int i = 0; i < size; i++) {
				buffer.put((byte) 0);
			}
		}
		byte tab[] = buffer.array();
		if (currentBitIndex % BYTE_SIZE != 0) {
			int index = 0;
			int max = currentBitIndex + pLength;
			while (currentBitIndex < max) {
				int mod = currentBitIndex % BYTE_SIZE;
				int modTab = index % BYTE_SIZE;
				int length = Math.min(max - currentBitIndex, Math.min(BYTE_SIZE - mod, BYTE_SIZE - modTab));
				byte val = (byte) (tab[index / BYTE_SIZE] & getMask(modTab, length));
				if (mod == 0) {
					val = (byte) (val << Math.min(modTab, BYTE_SIZE - length));
				} else {
					val = (byte) ((val & DEFAULT_VALUE) >> mod);
				}
				byteTab[currentBitIndex / BYTE_SIZE] |= val;
				currentBitIndex += length;
				index += length;
			}

		} else {
			System.arraycopy(tab, 0, byteTab, currentBitIndex / BYTE_SIZE, tab.length);
			currentBitIndex += pLength;
		}
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
		// create date formatter
		SimpleDateFormat sdf = new SimpleDateFormat(pPattern);
		String value = sdf.format(pValue);

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
	protected void setNextValue(final long pValue, final int pLength, final int pMaxSize) {
		long value = pValue;
		// Set to max value if pValue cannot be stored on pLength bits.
		long bitMax = (long) Math.pow(2, Math.min(pLength, pMaxSize));
		if (pValue > bitMax) {
			value = bitMax - 1;
		}
		// size to wrote
		int writeSize = pLength;
		while (writeSize > 0) {
			// modulo
			int mod = currentBitIndex % BYTE_SIZE;
			byte ret = 0;
			if (mod == 0 && writeSize <= BYTE_SIZE || pLength < BYTE_SIZE - mod) {
				// shift left value
				ret = (byte) (value << BYTE_SIZE - (writeSize + mod));
			} else {
				// shift right
				long length = Long.toBinaryString(value).length();
				ret = (byte) (value >> writeSize - length - (BYTE_SIZE - length - mod));
			}
			byteTab[currentBitIndex / BYTE_SIZE] |= ret;
			long val = Math.min(writeSize, BYTE_SIZE - mod);
			writeSize -= val;
			currentBitIndex += val;
		}
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
		setNextByte(pValue.getBytes(Charset.defaultCharset()), pLength, pPaddedBefore);
	}
}
