package fr.devnied.bitlib;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public static final int BYTE_SIZE = 8;
	/**
	 * Constant for byte size (float)
	 */
	public static final float BYTE_SIZE_F = 8.0f;
	/**
	 * Integer size in byte
	 */
	private static final int INTEGER_BYTE_SIZE = 4;
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
	 * Constructor of the class
	 * 
	 * @param pByte
	 *            byte read
	 */
	public BitUtils(final byte pByte[]) {
		byteTab = new byte[pByte.length];
		System.arraycopy(pByte, 0, byteTab, 0, pByte.length);
	}

	/**
	 * Constructor for empty byte tab
	 * 
	 * @param pSize
	 *            the size of the tab in bit
	 */
	public BitUtils(final int pSize) {
		byteTab = new byte[(int) Math.ceil(pSize / BYTE_SIZE_F)];
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
	 * @param pLenght
	 *            size of mask
	 * @return the mask in byte
	 */
	public byte getMask(final int pIndex, final int pLenght) {
		byte ret = (byte) DEFAULT_VALUE;
		// Add X 0 to the left
		ret = (byte) (ret << pIndex);
		ret = (byte) ((ret & DEFAULT_VALUE) >> pIndex);
		// Add X 0 to the right
		int dec = BYTE_SIZE - (pLenght + pIndex);
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
		if (getNextInt(1) == 1) {
			ret = true;
		}
		return ret;
	}

	/**
	 * Method to get The next bytes with the specified size
	 * 
	 * @param pSize
	 *            the length of byte to read
	 * @return a byte tab
	 */
	public byte[] getNextByte(final int pSize) {
		byte[] tab = new byte[(int) Math.ceil(pSize / BYTE_SIZE_F)];

		if (currentBitIndex % BYTE_SIZE != 0) {
			int index = 0;
			int max = currentBitIndex + pSize;
			while (currentBitIndex < max) {
				int mod = currentBitIndex % BYTE_SIZE;
				int modTab = index % BYTE_SIZE;
				int lenght = Math.min(max - currentBitIndex, Math.min(BYTE_SIZE - mod, BYTE_SIZE - modTab));
				byte val = (byte) (byteTab[currentBitIndex / BYTE_SIZE] & getMask(mod, lenght));
				if (mod != 0) {
					val = (byte) (val << Math.min(mod, BYTE_SIZE - lenght));
				} else {
					val = (byte) ((val & DEFAULT_VALUE) >> modTab);
				}
				tab[index / BYTE_SIZE] |= val;
				currentBitIndex += lenght;
				index += lenght;
			}
		} else {
			System.arraycopy(byteTab, currentBitIndex / BYTE_SIZE, tab, 0, tab.length);
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
		Date date = null;
		// create date formatter
		SimpleDateFormat sdf = new SimpleDateFormat(pPattern);
		// get String
		String dateTxt = getNextString(pSize);
		try {
			date = sdf.parse(dateTxt);
		} catch (ParseException e) {
			LOGGER.error("Parsing date error. date:" + dateTxt + " pattern:" + pPattern, e);
		}
		return date;
	}

	/**
	 * This method is used to get the next String in hexa
	 * 
	 * @param pSize
	 *            the length of the string in bit
	 * @return the string
	 */
	public String getNextHexaString(final int pSize) {
		return BytesUtils.bytesToStringNoSpace(getNextByte(pSize));
	}

	/**
	 * This method is used to get an integer with the specified size
	 * 
	 * @param pLenght
	 *            the length of the data to read in bit
	 * @return an integer
	 */
	public int getNextInt(final int pLenght) {
		// allocate Size of Integer
		ByteBuffer buffer = ByteBuffer.allocate(INTEGER_BYTE_SIZE);
		// final value
		int finalValue = 0;
		// Incremental value
		int currentValue = 0;
		// Size to read
		int readSize = pLenght;
		// length max of the index
		int max = currentBitIndex + pLenght;
		while (currentBitIndex < max) {
			int mod = currentBitIndex % BYTE_SIZE;
			// apply the mask to the selected byte
			currentValue = (byteTab[currentBitIndex / BYTE_SIZE] & getMask(mod, readSize)) & DEFAULT_VALUE;
			// Shift right the read value
			int dec = Math.max(BYTE_SIZE - (mod + readSize), 0);
			currentValue = ((currentValue & DEFAULT_VALUE) >>> dec) & DEFAULT_VALUE;
			// Shift left the previously read value and add the current value
			finalValue = (finalValue << Math.min(readSize, BYTE_SIZE)) | currentValue;
			// calculate read value size
			int val = (BYTE_SIZE - mod);
			// Decrease the size left
			readSize = readSize - val;
			currentBitIndex = Math.min(currentBitIndex + val, max);
		}
		buffer.putInt(finalValue);
		// reset the current bytebuffer index to 0
		buffer.rewind();
		// return integer
		return buffer.getInt();
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
	 * @param pChartset
	 *            the charset
	 * @return the string
	 */
	public String getNextString(final int pSize, final Charset pCharset) {
		return new String(getNextByte(pSize), pCharset);
	}

	/**
	 * Reset the current bit index to the initial position
	 */
	public void reset() {
		setCurrentBitIndex(0);
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
	 * @param pLenght
	 *            the length of the data in bits
	 */
	public void setNextByte(final byte[] pValue, final int pLenght) {
		int totalSize = (int) Math.ceil(pLenght / BYTE_SIZE_F);
		ByteBuffer buffer = ByteBuffer.allocate(totalSize);
		int size = totalSize - pValue.length;
		for (int i = 0; i < size; i++) {
			buffer.put((byte) 0);
		}
		buffer.put(pValue);
		byte tab[] = buffer.array();
		if (currentBitIndex % BYTE_SIZE != 0) {
			int index = 0;
			int max = currentBitIndex + pLenght;
			while (currentBitIndex < max) {
				int mod = currentBitIndex % BYTE_SIZE;
				int modTab = index % BYTE_SIZE;
				int lenght = Math.min(max - currentBitIndex, Math.min(BYTE_SIZE - mod, BYTE_SIZE - modTab));
				byte val = (byte) (tab[index / BYTE_SIZE] & getMask(modTab, lenght));
				if (mod == 0) {
					val = (byte) (val << Math.min(modTab, BYTE_SIZE - lenght));
				} else {
					val = (byte) ((val & DEFAULT_VALUE) >> mod);
				}
				byteTab[currentBitIndex / BYTE_SIZE] |= val;
				currentBitIndex += lenght;
				index += lenght;
			}

		} else {
			System.arraycopy(tab, 0, byteTab, currentBitIndex / BYTE_SIZE, tab.length);
			currentBitIndex += pLenght;
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
		// create date formatter
		SimpleDateFormat sdf = new SimpleDateFormat(pPattern);
		// get String
		setNextString(sdf.format(pValue));
	}

	/**
	 * Method to write Hexa String with the max length
	 * 
	 * @param pValue
	 *            the value to write
	 * @param pLenght
	 *            the length of the data in bits
	 */
	public void setNextHexaString(final String pValue, final int pLenght) {
		setNextByte(BytesUtils.fromString(pValue), pLenght);
	}

	/**
	 * Add Integer to the current position with the specified size
	 * 
	 * @param pLenght
	 *            the length of the integer
	 */
	public void setNextInteger(final int pValue, final int pLenght) {
		// size to wrote
		int writeSize = pLenght;
		while (writeSize > 0) {
			// modulo
			int mod = currentBitIndex % BYTE_SIZE;
			byte ret = 0;
			if (mod == 0 && writeSize <= BYTE_SIZE || pLenght < BYTE_SIZE - mod) {
				// shift left value
				ret = (byte) (pValue << BYTE_SIZE - (writeSize + mod));
			} else {
				// shift right
				int taille = Integer.toBinaryString(pValue).length();
				ret = (byte) (pValue >> (writeSize - taille - (BYTE_SIZE - taille - mod)));
			}
			byteTab[currentBitIndex / BYTE_SIZE] |= ret;
			int val = Math.min(writeSize, BYTE_SIZE - mod);
			writeSize -= val;
			currentBitIndex += val;
		}
	}

	/**
	 * Method to write String
	 * 
	 * @param pValue
	 *            the string to write
	 */
	public void setNextString(final String pValue) {
		setNextByte(pValue.getBytes(Charset.defaultCharset()), pValue.length() * BYTE_SIZE);
	}
}
