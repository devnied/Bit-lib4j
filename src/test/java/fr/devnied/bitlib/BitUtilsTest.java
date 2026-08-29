package fr.devnied.bitlib;

import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Class to test the bitutils class
 * 
 * @author Millau Julien
 * 
 */
public final class BitUtilsTest {

	/**
	 * Test byte value
	 */
	private final byte[] test = new byte[] { (byte) 0x11, (byte) 0x00, (byte) 0x00, (byte) 0x67, (byte) 0xEF };

	/**
	 * Assert that the runnable fails on the documented bound check: exactly an
	 * IndexOutOfBoundsException (not an internal array or string index crash,
	 * which are subclasses) with the message of the check
	 *
	 * @param pRunnable
	 *            the call expected to fail
	 */
	private static void assertOutOfBounds(final ThrowingRunnable pRunnable) {
		IndexOutOfBoundsException e = Assert.assertThrows(IndexOutOfBoundsException.class, pRunnable);
		Assertions.assertThat(e.getClass()).isEqualTo(IndexOutOfBoundsException.class);
		Assertions.assertThat(e.getMessage()).contains("exceed the size");
	}

	/**
	 * @throws ParseException
	 */
	@Test
	public void testBitIndex() throws ParseException {

		BitUtils bit = new BitUtils(test);

		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(0);
		Assertions.assertThat(bit.getNextInteger(4)).isEqualTo(1);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(4);
		bit.setCurrentBitIndex(5);
		bit.addCurrentBitIndex(-1);
		bit.addCurrentBitIndex(1);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(5);
		Assertions.assertThat(bit.getNextInteger(3)).isEqualTo(1);
		bit.addCurrentBitIndex(-1000);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(0);

		BitUtils bit2 = new BitUtils(new byte[] { (byte) 0xFE });
		bit2.addCurrentBitIndex(7);
		Assertions.assertThat(bit2.getNextBoolean()).isEqualTo(false);

	}

	/**
	 * Test the method getData
	 */
	@Test
	public void testGetData() {

		BitUtils bit = new BitUtils(test);
		Assertions.assertThat(bit.getData()).isEqualTo(test);
	}

	/**
	 * Test the method get Hexa String
	 */
	@Test
	public void testGetHexaString() {

		BitUtils bit = new BitUtils(test);

		Assertions.assertThat(bit.getNextHexaString(8)).isEqualTo("11");
		Assertions.assertThat(bit.getNextHexaString(16)).isEqualTo("0000");
		Assertions.assertThat(bit.getNextHexaString(8)).isEqualTo("67");
		Assertions.assertThat(bit.getNextHexaString(8)).isEqualTo("EF");
	}

	@Test
	public void testGetNextByte() {
		BitUtils bit = new BitUtils(test);
		Assertions.assertThat(bit.getNextByte(4, false)).isEqualTo(new byte[] { 0x10 });
		Assertions.assertThat(bit.getNextByte(4, false)).isEqualTo(new byte[] { 0x01 });
		Assertions.assertThat(bit.getNextByte(16, false)).isEqualTo(new byte[] { 0x00, 0x00 });
		Assertions.assertThat(bit.getNextByte(2, false)).isEqualTo(new byte[] { 0x40 });
		Assertions.assertThat(bit.getNextByte(2, false)).isEqualTo(new byte[] { 0x20 });
		bit.reset();
		Assertions.assertThat(bit.getNextByte(2, false)).isEqualTo(new byte[] { 0x00 });
		Assertions.assertThat(bit.getNextByte(8, false)).isEqualTo(new byte[] { 0x11, 0x00 });
		bit.reset();
		Assertions.assertThat(bit.getNextByte(4)).isEqualTo(new byte[] { 0x10 });
		Assertions.assertThat(bit.getNextByte(4)).isEqualTo(new byte[] { 0x10 });

		bit = new BitUtils(new byte[] { 0x44, (byte) 0xFE });
		Assertions.assertThat(bit.getNextByte(4, false)).isEqualTo(new byte[] { 0x40 });
		Assertions.assertThat(bit.getNextByte(8, false)).isEqualTo(new byte[] { 0x04, (byte) 0xF0 });
		Assertions.assertThat(bit.getNextByte(4, false)).isEqualTo(new byte[] { (byte) 0x0E });
		bit.reset();
		Assertions.assertThat(bit.getNextByte(4, true)).isEqualTo(new byte[] { 0x40 });
		Assertions.assertThat(bit.getNextByte(8, true)).isEqualTo(new byte[] { (byte) 0x4F });
		Assertions.assertThat(bit.getNextByte(4, true)).isEqualTo(new byte[] { (byte) 0xE0 });
	}

	/**
	 * @throws ParseException
	 */
	@Test
	public void testGetNextDate() throws ParseException {

		String val = "20130108";
		BitUtils bit = new BitUtils(val.getBytes());
		SimpleDateFormat sdf = new SimpleDateFormat(BitUtils.DATE_FORMAT);
		Date d = sdf.parse(val);

		Assertions.assertThat(bit.getNextDate(8 * 8, BitUtils.DATE_FORMAT)).isEqualTo(d);
		bit.reset();

		String valYear = "2013";
		SimpleDateFormat sdfyear = new SimpleDateFormat("yyyy");
		Date year = sdfyear.parse(valYear);

		Assertions.assertThat(bit.getNextDate(4 * 8, "yyyy")).isEqualTo(year);
		bit.reset();

		Assertions.assertThat(bit.getNextDate(4 * 8, BitUtils.DATE_FORMAT)).isEqualTo(null);

		bit = new BitUtils(BytesUtils.fromString(val));
		Assertions.assertThat(bit.getNextDate(8 * 4, BitUtils.DATE_FORMAT, true)).isEqualTo(d);

	}

	/**
	 * Method to test the GetNextint method
	 */
	@Test
	public void testGetNextInt() {

		BitUtils bit = new BitUtils(test);

		Assertions.assertThat(bit.getNextInteger(4)).isEqualTo(1);
		Assertions.assertThat(bit.getNextInteger(20)).isEqualTo(65536);
		Assertions.assertThat(bit.getNextInteger(1)).isEqualTo(0);
		Assertions.assertThat(bit.getNextInteger(4)).isEqualTo(12);
		Assertions.assertThat(bit.getNextInteger(1)).isEqualTo(1);
		Assertions.assertThat(bit.getNextInteger(4)).isEqualTo(15);
		Assertions.assertThat(bit.getNextInteger(1)).isEqualTo(1);
		Assertions.assertThat(bit.getNextInteger(5)).isEqualTo(15);

		BitUtils bit2 = new BitUtils(new byte[] { (byte) 0x04, (byte) 0x21, 0x60 });
		Assertions.assertThat(bit2.getNextInteger(2)).isEqualTo(0);
		Assertions.assertThat(bit2.getNextInteger(14)).isEqualTo(1057);
		Assertions.assertThat(bit2.getNextInteger(1)).isEqualTo(0);
		Assertions.assertThat(bit2.getNextInteger(1)).isEqualTo(1);
		Assertions.assertThat(bit2.getNextInteger(1)).isEqualTo(1);
		Assertions.assertThat(bit2.getNextInteger(1)).isEqualTo(0);
	}

	/**
	 * Test the
	 */
	@Test
	public void testGetNextString() {

		String val = "TEST VaLeUr e'*/";
		BitUtils bit = new BitUtils(val.getBytes());
		Assertions.assertThat(bit.getNextString(val.length() * 8)).isEqualTo(val);

		BitUtils bit2 = new BitUtils(val.getBytes());
		Assertions.assertThat(bit2.getNextString(2 * 8)).isEqualTo("TE");
		Assertions.assertThat(bit2.getNextString(2 * 8)).isEqualTo("ST");
		Assertions.assertThat(bit2.getNextString(4 * 8)).isEqualTo(" VaL");
		Assertions.assertThat(bit2.getNextString(8 * 8)).isEqualTo("eUr e'*/");
	}

	/**
	 * Test the method get/set boolean
	 */
	@Test
	public void testGetSetBoolean() {

		BitUtils bit = new BitUtils(8);
		bit.setNextBoolean(false);
		bit.setNextBoolean(true);
		bit.setNextBoolean(false);
		bit.setNextBoolean(false);
		bit.reset();

		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0x40 });
		Assertions.assertThat(bit.getNextBoolean()).isEqualTo(false);
		Assertions.assertThat(bit.getNextBoolean()).isEqualTo(true);
		Assertions.assertThat(bit.getNextBoolean()).isEqualTo(false);
		Assertions.assertThat(bit.getNextBoolean()).isEqualTo(false);
	}

	/**
	 * Test the mask function
	 */
	@Test
	public void testMask() {

		BitUtils bit = new BitUtils(test);

		Assertions.assertThat(bit.getMask(0, 1)).isEqualTo((byte) 0x80);
		Assertions.assertThat(bit.getMask(2, 2)).isEqualTo((byte) 0x30);
		Assertions.assertThat(bit.getMask(0, 2)).isEqualTo((byte) 0xC0);
		Assertions.assertThat(bit.getMask(3, 4)).isEqualTo((byte) 0x1E);
		Assertions.assertThat(bit.getMask(7, 1)).isEqualTo((byte) 0x01);
		Assertions.assertThat(bit.getMask(0, 8)).isEqualTo((byte) 0xFF);
		Assertions.assertThat(bit.getMask(2, 2)).isEqualTo((byte) 0x30);
		Assertions.assertThat(bit.getMask(1, 6)).isEqualTo((byte) 0x7E);
	}

	/**
	 * Unit test for resetNextBits
	 */
	@Test
	public void testResetNextBits() {
		BitUtils bit = new BitUtils(10);
		bit.setNextInteger(1000, 8);
		bit.reset();
		Assertions.assertThat(bit.getNextHexaString(8)).isEqualTo("FF");
		bit.reset();
		bit.resetNextBits(4);
		bit.reset();
		Assertions.assertThat(bit.getNextHexaString(8)).isEqualTo("0F");
		bit = new BitUtils(new byte[] { (byte) 0xFF, (byte) 0xFF });
		bit.resetNextBits(12);
		bit.reset();
		Assertions.assertThat(bit.getNextHexaString(16)).isEqualTo("000F");
		bit = new BitUtils(new byte[] { (byte) 0xFF, (byte) 0xFF });
		bit.addCurrentBitIndex(2);
		bit.resetNextBits(11);
		bit.reset();
		Assertions.assertThat(bit.getNextHexaString(16)).isEqualTo("C007");
	}

	/**
	 * Test the method to set an integer
	 */
	@Test
	public void testSetInteger() {

		BitUtils bit3 = new BitUtils(128);
		bit3.setNextInteger(10, 6);
		bit3.setNextInteger(23, 12);
		bit3.setNextInteger(5, 8);
		bit3.setNextInteger(930, 16);
		bit3.setNextInteger(5, 3);
		bit3.setNextInteger(159, 8);
		bit3.setNextInteger(7, 3);
		bit3.reset();

		Assertions.assertThat(bit3.getNextInteger(6)).isEqualTo(10);
		Assertions.assertThat(bit3.getNextInteger(12)).isEqualTo(23);
		Assertions.assertThat(bit3.getNextInteger(8)).isEqualTo(5);
		Assertions.assertThat(bit3.getNextInteger(16)).isEqualTo(930);
		Assertions.assertThat(bit3.getNextInteger(3)).isEqualTo(5);
		Assertions.assertThat(bit3.getNextInteger(8)).isEqualTo(159);
		Assertions.assertThat(bit3.getNextInteger(3)).isEqualTo(7);

		BitUtils bit2 = new BitUtils(128);
		bit2.setNextInteger(3, 2);
		bit2.setNextInteger(1057, 14);
		bit2.setNextInteger(1, 1);
		bit2.setNextInteger(1532, 15);
		bit2.setNextInteger(8, 8);
		bit2.setNextInteger(1532, 15);

		bit2.reset();

		Assertions.assertThat(bit2.getNextInteger(2)).isEqualTo(3);
		Assertions.assertThat(bit2.getNextInteger(14)).isEqualTo(1057);
		Assertions.assertThat(bit2.getNextInteger(1)).isEqualTo(1);
		Assertions.assertThat(bit2.getNextInteger(15)).isEqualTo(1532);
		Assertions.assertThat(bit2.getNextInteger(8)).isEqualTo(8);
		Assertions.assertThat(bit2.getNextInteger(15)).isEqualTo(1532);

		BitUtils bit = new BitUtils(64);
		bit.setNextInteger(3, 2);
		bit.setNextInteger(255, 8);
		bit.setNextInteger(0, 2);
		bit.setNextInteger(15, 4);
		bit.setNextInteger(2, 2);
		bit.setNextInteger(3, 3);
		bit.setNextInteger(1, 1);
		bit.setNextInteger(0, 1);
		bit.setNextInteger(1, 1);
		bit.reset();

		Assertions.assertThat(bit.getNextInteger(2)).isEqualTo(3);
		Assertions.assertThat(bit.getNextInteger(8)).isEqualTo(255);
		Assertions.assertThat(bit.getNextInteger(2)).isEqualTo(0);
		Assertions.assertThat(bit.getNextInteger(4)).isEqualTo(15);
		Assertions.assertThat(bit.getNextInteger(2)).isEqualTo(2);
		Assertions.assertThat(bit.getNextInteger(3)).isEqualTo(3);
		Assertions.assertThat(bit.getNextInteger(1)).isEqualTo(1);
		Assertions.assertThat(bit.getNextInteger(1)).isEqualTo(0);
		Assertions.assertThat(bit.getNextInteger(1)).isEqualTo(1);

		bit.clear();
		try {
			bit.setNextInteger(500, 33);
			Assert.fail();
		} catch (IllegalArgumentException iae) {
		}
		bit.clear();
		bit.setNextInteger(127, 5);
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(5)).isEqualTo(31);
	}
	
	@Test
	public void testOverflowValueInSize() {
		BitUtils bit = new BitUtils(64);
		bit.setNextInteger(Integer.MAX_VALUE, 3);
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(3)).isEqualTo(7);
		
		bit.clear();
		bit.setNextLong(Long.MAX_VALUE, 40);
		bit.reset();
		Assertions.assertThat(bit.getNextLong(40)).isEqualTo(1099511627775L);
	}
	
	@Test
	public void testMaxMinValueInteger() {
		BitUtils bit = new BitUtils(64);
		bit.setNextInteger(Integer.MIN_VALUE, 32);
		bit.reset();
		Assertions.assertThat(bit.getNextHexaString(32)).isEqualTo("80000000");
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(32)).isEqualTo(Integer.MIN_VALUE);
		
		bit.clear();
		bit.setNextInteger(Integer.MAX_VALUE, 32);
		bit.reset();
		Assertions.assertThat(bit.getNextHexaString(32)).isEqualTo("7FFFFFFF");
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(32)).isEqualTo(Integer.MAX_VALUE);
	}
	
	/**
	 * Test the method to set an long
	 */
	@Test
	public void testSetLong() {

		BitUtils bit3 = new BitUtils(128);
		bit3.setNextLong(10, 6);
		bit3.setNextLong(23, 12);
		bit3.setNextLong(5, 8);
		bit3.setNextLong(930, 16);
		bit3.setNextLong(5, 3);
		bit3.setNextLong(159, 8);
		bit3.setNextLong(7, 3);
		bit3.reset();

		Assertions.assertThat(bit3.getNextLong(6)).isEqualTo(10);
		Assertions.assertThat(bit3.getNextLong(12)).isEqualTo(23);
		Assertions.assertThat(bit3.getNextLong(8)).isEqualTo(5);
		Assertions.assertThat(bit3.getNextLong(16)).isEqualTo(930);
		Assertions.assertThat(bit3.getNextLong(3)).isEqualTo(5);
		Assertions.assertThat(bit3.getNextLong(8)).isEqualTo(159);
		Assertions.assertThat(bit3.getNextLong(3)).isEqualTo(7);

		BitUtils bit2 = new BitUtils(128);
		bit2.setNextLong(3, 2);
		bit2.setNextLong(1057, 14);
		bit2.setNextLong(1, 1);
		bit2.setNextLong(1532, 15);
		bit2.setNextLong(8, 8);
		bit2.setNextLong(1532, 15);

		bit2.reset();

		Assertions.assertThat(bit2.getNextLong(2)).isEqualTo(3);
		Assertions.assertThat(bit2.getNextLong(14)).isEqualTo(1057);
		Assertions.assertThat(bit2.getNextLong(1)).isEqualTo(1);
		Assertions.assertThat(bit2.getNextLong(15)).isEqualTo(1532);
		Assertions.assertThat(bit2.getNextLong(8)).isEqualTo(8);
		Assertions.assertThat(bit2.getNextLong(15)).isEqualTo(1532);

		BitUtils bit = new BitUtils(64);
		bit.setNextLong(3, 2);
		bit.setNextLong(255, 8);
		bit.setNextLong(0, 2);
		bit.setNextLong(15, 4);
		bit.setNextLong(2, 2);
		bit.setNextLong(3, 3);
		bit.setNextLong(1, 1);
		bit.setNextLong(0, 1);
		bit.setNextLong(1, 1);
		bit.reset();

		Assertions.assertThat(bit.getNextLong(2)).isEqualTo(3);
		Assertions.assertThat(bit.getNextLong(8)).isEqualTo(255);
		Assertions.assertThat(bit.getNextLong(2)).isEqualTo(0);
		Assertions.assertThat(bit.getNextLong(4)).isEqualTo(15);
		Assertions.assertThat(bit.getNextLong(2)).isEqualTo(2);
		Assertions.assertThat(bit.getNextLong(3)).isEqualTo(3);
		Assertions.assertThat(bit.getNextLong(1)).isEqualTo(1);
		Assertions.assertThat(bit.getNextLong(1)).isEqualTo(0);
		Assertions.assertThat(bit.getNextLong(1)).isEqualTo(1);

		bit.reset();
		try {
			bit.setNextLong(500, 65);
			Assert.fail();
		} catch (IllegalArgumentException iae) {
		}
	}
	
	@Test
	public void testMaxMinValueLong() {
		BitUtils bit = new BitUtils(64);
		bit.setNextLong(Long.MIN_VALUE, 64);
		bit.reset();
		Assertions.assertThat(bit.getNextHexaString(64)).isEqualTo("8000000000000000");
		bit.reset();
		Assertions.assertThat(bit.getNextLong(64)).isEqualTo(Long.MIN_VALUE);
		
		bit.clear();
		bit.setNextLong(Long.MAX_VALUE, 64);
		bit.reset();
		Assertions.assertThat(bit.getNextHexaString(64)).isEqualTo("7FFFFFFFFFFFFFFF");
		bit.reset();
		Assertions.assertThat(bit.getNextLong(64)).isEqualTo(Long.MAX_VALUE);
	}
	
	@Test
	public void testSetLong64BitsSmallValue() {
		BitUtils bit = new BitUtils(64);
		bit.setNextLong(42, 64);
		bit.reset();
		Assertions.assertThat(bit.getNextLong(64)).isEqualTo(42);
		bit.clear();
		bit.setNextLong(0, 64);
		bit.reset();
		Assertions.assertThat(bit.getNextLong(64)).isEqualTo(0);
		bit.clear();
		bit.setNextLong(1, 64);
		bit.reset();
		Assertions.assertThat(bit.getNextLong(64)).isEqualTo(1);
	}

	@Test
	public void testLongMaxMinValue(){
		BitUtils bit = new BitUtils(64);
		bit.setNextHexaString("FFFFFFFFFFFFFFFF", 64);
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(64)).isEqualTo(-1);
	}

	/**
	 * Test the method to set an integer
	 */
	@Test
	public void testSetIntegerOverflow() {

		BitUtils bit = new BitUtils(128);
		bit.setNextInteger(10, 2);
		bit.setNextInteger(256, 1);
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(2)).isEqualTo(3);
		Assertions.assertThat(bit.getNextInteger(1)).isEqualTo(1);
	}

	/**
	 * Test the method to set bytes
	 */
	@Test
	public void testSetNextByte() {

		byte[] tab = new byte[] { (byte) 0x12, (byte) 0x20 };
		BitUtils bit = new BitUtils(tab.length * 8 * 2);
		bit.setNextByte(tab, tab.length * 8);
		bit.setNextByte(tab, tab.length * 8);
		bit.reset();

		Assertions.assertThat(bit.getNextHexaString(tab.length * 8)).isEqualTo("1220");
		Assertions.assertThat(bit.getNextHexaString(tab.length * 8)).isEqualTo("1220");

		byte[] tab2 = new byte[] { (byte) 0x22, (byte) 0x50 };
		BitUtils bit2 = new BitUtils(16);
		bit2.setNextInteger(3, 3);
		bit2.setNextByte(tab2, 13);
		bit2.reset();

		Assertions.assertThat(bit2.getNextHexaString(16)).isEqualTo("644A");
	}

	/**
	 * Test the method to set bytes
	 */
	@Test
	public void testSetNextByte2() {

		byte[] tab = new byte[] { (byte) 0x12 };
		BitUtils bit = new BitUtils(9);
		bit.setNextInteger(1, 1);
		bit.setNextByte(tab, 8);
		bit.reset();

		Assertions.assertThat(bit.getNextHexaString(9)).isEqualTo("8900");
		bit.reset();
		Assertions.assertThat(bit.getNextHexaString(8)).isEqualTo("89");
	}

	@Test
	public void testSetNextDate() throws ParseException {

		String val = "20130108";
		BitUtils bit = new BitUtils(100);
		SimpleDateFormat sdf = new SimpleDateFormat(BitUtils.DATE_FORMAT);
		Date d = sdf.parse(val);
		bit.setNextDate(d, BitUtils.DATE_FORMAT);
		bit.reset();
		Assertions.assertThat(sdf.format(bit.getNextDate(8 * 8, BitUtils.DATE_FORMAT))).isEqualTo(val);
		bit.reset();
		bit.setNextDate(d, BitUtils.DATE_FORMAT, true);
		bit.reset();
		Assertions.assertThat(bit.getNextHexaString(8 * 4)).isEqualTo(val);
	}

	/**
	 * Test the method to set an hexa string
	 */
	@Test
	public void testSetNextHexaString() {

		String text1 = "1122334455";
		BitUtils bit = new BitUtils(text1.length() * 4 + 1);
		bit.setNextBoolean(true);
		bit.setNextHexaString(text1, text1.length() * 4);
		bit.reset();
		Assertions.assertThat(bit.getNextBoolean()).isEqualTo(true);
		Assertions.assertThat(bit.getNextHexaString(text1.length() * 4)).isEqualTo(text1);
		bit.reset();
		Assertions.assertThat(bit.getNextBoolean()).isEqualTo(true);
		Assertions.assertThat(bit.getNextHexaString(text1.length() / 2 * 4)).isEqualTo("112230");

		bit = new BitUtils(20);
		try {
			bit.setNextHexaString("AAB", 20);
			org.junit.Assert.fail();
		} catch (IllegalArgumentException iae) {
			org.junit.Assert.assertTrue(true);
		}

	}

	/**
	 * Test the method to set an integer
	 */
	@Test
	public void testSetPaddedString() {

		String text = "123456789";
		BitUtils bit = new BitUtils(160);
		bit.setNextString(text, 10 * 8, true);
		bit.setNextString(text, 10 * 8, false);
		bit.reset();
		Assertions.assertThat(bit.getNextString(10 * 8)).isEqualTo('\0' + text);
		Assertions.assertThat(bit.getNextString(10 * 8)).isEqualTo(text + '\0');
	}

	/**
	 * Test the method to set an integer
	 */
	@Test
	public void testSetString() {

		String text1 = "test";
		String text2 = " OK";
		BitUtils bit = new BitUtils((text1.length() + text2.length()) * 8 + 5);
		bit.setNextInteger(3, 5);
		bit.setNextString(text1, text1.length() * 8);
		bit.setNextString(text2, 2 * 8);
		bit.reset();

		Assertions.assertThat(bit.getNextInteger(5)).isEqualTo(3);
		Assertions.assertThat(bit.getNextString(text1.length() * 8)).isEqualTo(text1);
		Assertions.assertThat(bit.getNextString(2 * 8)).isEqualTo(" O");
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(5)).isEqualTo(3);
		Assertions.assertThat(bit.getNextString((text1.length() + 2) * 8)).isEqualTo(text1 + " O");
	}

	/**
	 * Unit test for size
	 */
	@Test
	public void testSize() {
		BitUtils bit = new BitUtils(289);
		Assertions.assertThat(bit.getSize()).isEqualTo(289);

		bit = new BitUtils(new byte[] { 0x33, 0x12 });
		Assertions.assertThat(bit.getSize()).isEqualTo(2 * 8);
	}

	/**
	 * Unit test for signed value
	 */
	@Test
	public void testSign() {
		BitUtils bit = new BitUtils(Long.SIZE);
		bit.setNextInteger(-2, 4);
		bit.reset();
		Assertions.assertThat(bit.getNextIntegerSigned(4)).isEqualTo(-2);
		bit.clear();
		bit.setNextInteger(0, 8);
		bit.reset();
		Assertions.assertThat(bit.getNextIntegerSigned(8)).isEqualTo(0);
		bit.clear();
		bit.setNextInteger(127, 8);
		bit.reset();
		Assertions.assertThat(bit.getNextIntegerSigned(8)).isEqualTo(127);
		bit.clear();
		bit.setNextInteger(-128, 8);
		bit.reset();
		Assertions.assertThat(bit.getNextIntegerSigned(8)).isEqualTo(-128);
		bit.clear();
		bit.setNextInteger(-256, 16);
		bit.reset();
		Assertions.assertThat(bit.getNextIntegerSigned(16)).isEqualTo(-256);
		bit.clear();
		bit.setNextInteger(Integer.MIN_VALUE, Integer.SIZE);
		bit.reset();
		Assertions.assertThat(bit.getNextIntegerSigned(Integer.SIZE)).isEqualTo(Integer.MIN_VALUE);
		bit.clear();
		bit.setNextLong(Long.MIN_VALUE, Long.SIZE);
		bit.reset();
		Assertions.assertThat(bit.getNextLongSigned(Long.SIZE)).isEqualTo(Long.MIN_VALUE);
	}

	/**
	 * Unit test for getNextString with custom charset
	 */
	@Test
	public void testGetNextStringWithCharset() {
		String val = "Héllo";
		byte[] utf8Bytes = val.getBytes(StandardCharsets.UTF_8);
		BitUtils bit = new BitUtils(utf8Bytes);
		Assertions.assertThat(bit.getNextString(utf8Bytes.length * 8, StandardCharsets.UTF_8)).isEqualTo(val);
	}

	/**
	 * Unit test for signed long values
	 */
	@Test
	public void testSignedLong() {
		BitUtils bit = new BitUtils(Long.SIZE);
		bit.setNextLong(-2, 40);
		bit.reset();
		Assertions.assertThat(bit.getNextLongSigned(40)).isEqualTo(-2);
		bit.clear();
		bit.setNextLong(-128, 40);
		bit.reset();
		Assertions.assertThat(bit.getNextLongSigned(40)).isEqualTo(-128);
		bit.clear();
		bit.setNextLong(0, 40);
		bit.reset();
		Assertions.assertThat(bit.getNextLongSigned(40)).isEqualTo(0);
		bit.clear();
		bit.setNextLong(Long.MAX_VALUE, Long.SIZE);
		bit.reset();
		Assertions.assertThat(bit.getNextLongSigned(Long.SIZE)).isEqualTo(Long.MAX_VALUE);
	}

	/**
	 * Unit test for boundary clamping: value == 2^n should be clamped to 2^n - 1
	 */
	@Test
	public void testBoundaryClampingExactPowerOfTwo() {
		// 256 on 8 bits: max storable unsigned = 255, so 256 should be clamped to 255
		BitUtils bit = new BitUtils(32);
		bit.setNextInteger(256, 8);
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(8)).isEqualTo(255);

		// 4 on 2 bits: max storable = 3, so 4 should be clamped to 3
		bit.clear();
		bit.setNextInteger(4, 2);
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(2)).isEqualTo(3);

		// 8 on 3 bits: max storable = 7, so 8 should be clamped to 7
		bit.clear();
		bit.setNextInteger(8, 3);
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(3)).isEqualTo(7);

		// 2 on 1 bit: max storable = 1, so 2 should be clamped to 1
		bit.clear();
		bit.setNextInteger(2, 1);
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(1)).isEqualTo(1);

		// 65536 on 16 bits: max storable = 65535
		bit.clear();
		bit.setNextInteger(65536, 16);
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(16)).isEqualTo(65535);
	}

	/**
	 * Unit test for boundary clamping with long values
	 */
	@Test
	public void testBoundaryClampingLong() {
		// 1L << 40 on 40 bits: should be clamped to (1L << 40) - 1
		BitUtils bit = new BitUtils(64);
		long maxOn40Bits = (1L << 40) - 1;
		bit.setNextLong(1L << 40, 40);
		bit.reset();
		Assertions.assertThat(bit.getNextLong(40)).isEqualTo(maxOn40Bits);

		// Value just below the boundary should pass through unchanged
		bit.clear();
		bit.setNextLong(maxOn40Bits, 40);
		bit.reset();
		Assertions.assertThat(bit.getNextLong(40)).isEqualTo(maxOn40Bits);
	}

	/**
	 * Unit test for signed value
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testOverflowIntegerSign() {
		BitUtils bit = new BitUtils(Integer.SIZE);
		bit.setNextInteger(-2, 4);
		bit.reset();
		Assertions.assertThat(bit.getNextIntegerSigned(33)).isEqualTo(-2);
	}

	/**
	 * Unit test for signed value
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testOverflowLongSign() {
		BitUtils bit = new BitUtils(Long.SIZE);
		bit.setNextInteger(-2, 4);
		bit.reset();
		Assertions.assertThat(bit.getNextLongSigned(65)).isEqualTo(-2);
	}


	// ------------------------------------------------------------------
	// Contract based tests (constructors, index, aliasing)
	// ------------------------------------------------------------------

	/**
	 * Constructor from byte array: null is rejected
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructorNullArray() {
		new BitUtils((byte[]) null);
	}

	/**
	 * Constructor from an empty array: size 0, nothing can be read
	 */
	@Test
	public void testConstructorEmptyArray() {
		final BitUtils bit = new BitUtils(new byte[0]);
		Assertions.assertThat(bit.getSize()).isEqualTo(0);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[0]);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(0);
		Assert.assertThrows(IndexOutOfBoundsException.class, () -> bit.getNextBoolean());
		Assert.assertThrows(IndexOutOfBoundsException.class, () -> bit.getNextInteger(1));
	}

	/**
	 * Constructor from a size in bits: the backing array is rounded up to whole
	 * bytes
	 */
	@Test
	public void testConstructorSizeInBits() {
		Assertions.assertThat(new BitUtils(0).getSize()).isEqualTo(0);
		Assertions.assertThat(new BitUtils(0).getData().length).isEqualTo(0);
		Assertions.assertThat(new BitUtils(1).getData().length).isEqualTo(1);
		Assertions.assertThat(new BitUtils(8).getData().length).isEqualTo(1);
		Assertions.assertThat(new BitUtils(9).getData().length).isEqualTo(2);
		Assertions.assertThat(new BitUtils(289).getData().length).isEqualTo(37);
		Assertions.assertThat(new BitUtils(289).getSize()).isEqualTo(289);
	}

	/**
	 * The byte array given to the constructor is copied: later writes do not
	 * leak into the caller's array
	 */
	@Test
	public void testConstructorCopiesArray() {
		byte[] source = new byte[] { 0x00, 0x00 };
		BitUtils bit = new BitUtils(source);
		bit.setNextInteger(0xFF, 8);
		Assertions.assertThat(source).isEqualTo(new byte[] { 0x00, 0x00 });
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xFF, 0x00 });
	}

	/**
	 * getData returns a copy: modifying it does not alter the internal state
	 */
	@Test
	public void testGetDataReturnsCopy() {
		BitUtils bit = new BitUtils(test);
		byte[] data = bit.getData();
		data[0] = (byte) 0xFF;
		Assertions.assertThat(bit.getData()).isEqualTo(test);
		Assertions.assertThat(bit.getNextHexaString(8)).isEqualTo("11");
	}

	/**
	 * clear() zeroes all the data, resets the index and keeps the size; the
	 * caller's array is untouched
	 */
	@Test
	public void testClear() {
		byte[] source = new byte[] { (byte) 0xFF, (byte) 0xFF };
		BitUtils bit = new BitUtils(source);
		bit.setCurrentBitIndex(5);
		bit.clear();
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(0);
		Assertions.assertThat(bit.getSize()).isEqualTo(16);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x00, 0x00 });
		Assertions.assertThat(bit.getNextInteger(16)).isEqualTo(0);
		Assertions.assertThat(source).isEqualTo(new byte[] { (byte) 0xFF, (byte) 0xFF });
	}

	/**
	 * reset() only moves the index back to 0, data is preserved
	 */
	@Test
	public void testResetKeepsData() {
		BitUtils bit = new BitUtils(new byte[] { (byte) 0xA5 });
		bit.getNextInteger(5);
		bit.reset();
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(0);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xA5 });
		Assertions.assertThat(bit.getNextInteger(8)).isEqualTo(0xA5);
	}

	/**
	 * Public constants
	 */
	@Test
	public void testConstants() {
		Assertions.assertThat(BitUtils.BYTE_SIZE).isEqualTo(8);
		Assertions.assertThat(BitUtils.DATE_FORMAT).isEqualTo("yyyyMMdd");
	}

	/**
	 * Every reader moves the index by exactly the number of bits read
	 */
	@Test
	public void testReadersMoveIndex() {
		// "20130108" + "AB" + 0x1234 + 0x80
		BitUtils bit = new BitUtils(BytesUtils.fromString("3230313330313038" + "4142" + "1234" + "80"));
		bit.getNextDate(64, BitUtils.DATE_FORMAT);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(64);
		Assertions.assertThat(bit.getNextString(16)).isEqualTo("AB");
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(80);
		Assertions.assertThat(bit.getNextHexaString(16)).isEqualTo("1234");
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(96);
		Assertions.assertThat(bit.getNextByte(4)).isEqualTo(new byte[] { (byte) 0x80 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(100);
		Assertions.assertThat(bit.getNextBoolean()).isFalse();
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(101);
		Assertions.assertThat(bit.getNextLong(3)).isEqualTo(0);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(104);
	}

	/**
	 * Every writer moves the index by exactly the number of bits written
	 */
	@Test
	public void testWritersMoveIndex() {
		BitUtils bit = new BitUtils(200);
		bit.setNextInteger(1, 32);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(32);
		bit.setNextLong(1, 40);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(72);
		bit.setNextString("AB", 16);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(88);
		bit.setNextHexaString("ABCD", 16);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(104);
		bit.setNextByte(new byte[] { 0x12 }, 8);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(112);
		bit.setNextBoolean(true);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(113);
		bit.resetNextBits(7);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(120);
	}

	// ------------------------------------------------------------------
	// Bounds: end of buffer, zero length
	// ------------------------------------------------------------------

	/**
	 * Reading more bits than available throws (no silent truncation or zero
	 * padding)
	 */
	@Test
	public void testReadPastEnd() {
		final byte[] data = new byte[] { 0x0F };
		assertOutOfBounds(() -> new BitUtils(data).getNextInteger(9));
		assertOutOfBounds(() -> new BitUtils(data).getNextLong(9));
		assertOutOfBounds(() -> new BitUtils(data).getNextIntegerSigned(16));
		assertOutOfBounds(() -> new BitUtils(data).getNextByte(16));
		assertOutOfBounds(() -> new BitUtils(data).getNextByte(16, false));
		assertOutOfBounds(() -> new BitUtils(data).getNextHexaString(16));
		assertOutOfBounds(() -> new BitUtils(data).getNextString(16));
		assertOutOfBounds(() -> new BitUtils(data).getNextDate(16, "yyyy"));
		// index already at the end
		final BitUtils bit = new BitUtils(data);
		bit.setCurrentBitIndex(8);
		assertOutOfBounds(() -> bit.getNextBoolean());
		assertOutOfBounds(() -> bit.getNextInteger(1));
		// unaligned start
		final BitUtils bit2 = new BitUtils(data);
		bit2.setCurrentBitIndex(4);
		assertOutOfBounds(() -> bit2.getNextInteger(8));
	}

	/**
	 * Writing more bits than available throws
	 */
	@Test
	public void testWritePastEnd() {
		assertOutOfBounds(() -> new BitUtils(8).setNextInteger(1, 16));
		assertOutOfBounds(() -> new BitUtils(8).setNextLong(1, 16));
		assertOutOfBounds(() -> new BitUtils(8).setNextString("AB", 16));
		assertOutOfBounds(() -> new BitUtils(8).setNextHexaString("ABCD", 16));
		assertOutOfBounds(() -> new BitUtils(8).setNextByte(new byte[] { (byte) 0xAB, (byte) 0xCD }, 16));
		assertOutOfBounds(() -> new BitUtils(8).resetNextBits(16));
		final BitUtils bit = new BitUtils(8);
		bit.setCurrentBitIndex(4);
		assertOutOfBounds(() -> bit.setNextInteger(0xFF, 8));
	}

	/**
	 * Writing the very last bit of the buffer is allowed, writing past the end
	 * is not
	 */
	@Test
	public void testSetNextBooleanLastBit() {
		final BitUtils bit = new BitUtils(8);
		bit.setCurrentBitIndex(7);
		bit.setNextBoolean(true);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x01 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(8);
		Assert.assertThrows(IndexOutOfBoundsException.class, () -> bit.setNextBoolean(true));
	}

	/**
	 * Zero-length reads return an empty value and do not move the index
	 */
	@Test
	public void testZeroLengthRead() {
		BitUtils bit = new BitUtils(new byte[] { (byte) 0xFF });
		Assertions.assertThat(bit.getNextInteger(0)).isEqualTo(0);
		Assertions.assertThat(bit.getNextLong(0)).isEqualTo(0L);
		Assertions.assertThat(bit.getNextIntegerSigned(0)).isEqualTo(0);
		Assertions.assertThat(bit.getNextLongSigned(0)).isEqualTo(0L);
		Assertions.assertThat(bit.getNextByte(0)).isEqualTo(new byte[0]);
		Assertions.assertThat(bit.getNextHexaString(0)).isEqualTo("");
		Assertions.assertThat(bit.getNextString(0)).isEqualTo("");
		Assertions.assertThat(bit.getNextDate(0, BitUtils.DATE_FORMAT)).isNull();
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(0);
	}

	/**
	 * Zero-length writes are no-ops and do not move the index
	 */
	@Test
	public void testZeroLengthWrite() {
		BitUtils bit = new BitUtils(8);
		bit.setNextInteger(5, 0);
		bit.setNextLong(5, 0);
		bit.setNextString("A", 0);
		bit.setNextHexaString("AA", 0);
		bit.setNextByte(new byte[] { 0x01 }, 0);
		bit.resetNextBits(0);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(0);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x00 });

		bit = new BitUtils(new byte[] { (byte) 0xFF });
		bit.resetNextBits(0);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(0);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xFF });

		// at an unaligned index, with an empty value: no-ops, then zeros only
		bit = new BitUtils(new byte[] { (byte) 0xFF, (byte) 0xFF });
		bit.setCurrentBitIndex(3);
		bit.setNextByte(new byte[0], 0);
		bit.setNextByte(new byte[] { 0x01 }, 0);
		bit.setNextByte(new byte[0], 0, false);
		bit.setNextString("", 0);
		bit.setNextHexaString("", 0);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(3);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xFF, (byte) 0xFF });
		bit.setNextByte(new byte[0], 5);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(8);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xE0, (byte) 0xFF });
		bit.setCurrentBitIndex(13);
		bit.setNextString("", 3);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(16);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xE0, (byte) 0xF8 });

	}

	/**
	 * Mask edge cases: empty mask and mask truncated at the end of the byte
	 */
	@Test
	public void testMaskEdgeCases() {
		BitUtils bit = new BitUtils(test);
		Assertions.assertThat(bit.getMask(0, 0)).isEqualTo((byte) 0x00);
		Assertions.assertThat(bit.getMask(4, 4)).isEqualTo((byte) 0x0F);
		Assertions.assertThat(bit.getMask(6, 4)).isEqualTo((byte) 0x03);
	}

	// ------------------------------------------------------------------
	// Readers
	// ------------------------------------------------------------------

	/**
	 * Bits are read from the most significant bit (index 0) to the least
	 * significant (index 7)
	 */
	@Test
	public void testGetNextBooleanBitOrder() {
		BitUtils bit = new BitUtils(new byte[] { (byte) 0x80 });
		Assertions.assertThat(bit.getNextBoolean()).isTrue();
		for (int i = 1; i < 8; i++) {
			Assertions.assertThat(bit.getNextBoolean()).isFalse();
		}
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(8);

		bit = new BitUtils(new byte[] { (byte) 0x01 });
		for (int i = 0; i < 7; i++) {
			Assertions.assertThat(bit.getNextBoolean()).isFalse();
		}
		Assertions.assertThat(bit.getNextBoolean()).isTrue();
	}

	/**
	 * Shifted read spanning two source bytes with a size not multiple of 8:
	 * the result is left aligned and zero padded, the data is untouched
	 */
	@Test
	public void testGetNextByteShiftedSpanningBytes() {
		BitUtils bit = new BitUtils(new byte[] { (byte) 0xAA, (byte) 0xCC });
		bit.setCurrentBitIndex(5);
		// bits 5..10 of 10101010 11001100 = 010 110 -> 01011000
		Assertions.assertThat(bit.getNextByte(6)).isEqualTo(new byte[] { 0x58 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(11);

		bit = new BitUtils(new byte[] { (byte) 0xAA, (byte) 0xCC, (byte) 0xF0 });
		bit.setCurrentBitIndex(3);
		// bits 3..14 = 01010 1100110 -> 01010110 01100000
		Assertions.assertThat(bit.getNextByte(12, true)).isEqualTo(new byte[] { 0x56, 0x60 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(15);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xAA, (byte) 0xCC, (byte) 0xF0 });
	}

	/**
	 * Unshifted read inside a single byte keeps the bits at their position,
	 * shifted moves them to the most significant bits. Aligned reads are
	 * identical in both modes.
	 */
	@Test
	public void testGetNextByteWithinByteShiftVsUnshift() {
		BitUtils bit = new BitUtils(new byte[] { (byte) 0xAA, (byte) 0xCC });
		bit.setCurrentBitIndex(3);
		// bits 3..6 of 10101010 = 0101
		Assertions.assertThat(bit.getNextByte(4, false)).isEqualTo(new byte[] { 0x0A });
		bit.setCurrentBitIndex(3);
		Assertions.assertThat(bit.getNextByte(4, true)).isEqualTo(new byte[] { 0x50 });
		bit.reset();
		Assertions.assertThat(bit.getNextByte(12, false)).isEqualTo(new byte[] { (byte) 0xAA, (byte) 0xC0 });
		bit.reset();
		Assertions.assertThat(bit.getNextByte(12, true)).isEqualTo(new byte[] { (byte) 0xAA, (byte) 0xC0 });
	}

	/**
	 * Hexa string: partial bytes are padded with zeros on the right
	 */
	@Test
	public void testGetNextHexaStringPartialByte() {
		BitUtils bit = new BitUtils(new byte[] { (byte) 0xAB, (byte) 0xCD });
		Assertions.assertThat(bit.getNextHexaString(4)).isEqualTo("A0");
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(4);
		Assertions.assertThat(bit.getNextHexaString(8)).isEqualTo("BC");
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(12);
		bit.reset();
		Assertions.assertThat(bit.getNextHexaString(12)).isEqualTo("ABC0");
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(12);

		// unaligned read ending exactly at the end of the buffer
		bit = new BitUtils(new byte[] { (byte) 0xAA, (byte) 0xCC });
		bit.setCurrentBitIndex(3);
		Assertions.assertThat(bit.getNextHexaString(13)).isEqualTo("5660");
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(16);
		bit.setCurrentBitIndex(3);
		Assertions.assertThat(bit.getNextHexaString(5)).isEqualTo("50");
		bit.setCurrentBitIndex(11);
		Assertions.assertThat(bit.getNextHexaString(5)).isEqualTo("60");
		bit = new BitUtils(new byte[] { 0x1F, (byte) 0xFF });
		bit.setCurrentBitIndex(3);
		Assertions.assertThat(bit.getNextHexaString(13)).isEqualTo("FFF8");

		// the hexa reader is the (shifted) byte reader in hexa, at every index
		// and length
		final byte[] data = new byte[] { (byte) 0xA5, 0x3C, (byte) 0x96 };
		for (int index = 0; index <= 8; index++) {
			for (int length = 1; length <= 24 - index; length++) {
				bit = new BitUtils(data);
				bit.setCurrentBitIndex(index);
				String hexa = bit.getNextHexaString(length);
				bit.setCurrentBitIndex(index);
				Assertions.assertThat(hexa).isEqualTo(BytesUtils.bytesToStringNoSpace(bit.getNextByte(length)));
				Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(index + length);
			}
		}

	}

	/**
	 * More than 32 bits: the value is read as a long and narrowed to int (low
	 * 32 bits), consistently with getNextInteger(64) in testLongMaxMinValue
	 */
	@Test
	public void testGetNextIntegerWiderThan32Bits() {
		BitUtils bit = new BitUtils(new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05 });
		Assertions.assertThat(bit.getNextInteger(40)).isEqualTo(0x02030405);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(40);
	}

	/**
	 * Boundaries around the sign bit for unsigned reads
	 */
	@Test
	public void testGetNextIntegerAndLongSignBoundaries() {
		Assertions.assertThat(new BitUtils(BytesUtils.fromString("FFFFFFFF")).getNextInteger(31)).isEqualTo(Integer.MAX_VALUE);
		Assertions.assertThat(new BitUtils(BytesUtils.fromString("7FFFFFFF")).getNextInteger(31)).isEqualTo(0x3FFFFFFF);
		Assertions.assertThat(new BitUtils(BytesUtils.fromString("FFFFFFFF")).getNextInteger(32)).isEqualTo(-1);
		Assertions.assertThat(new BitUtils(BytesUtils.fromString("FFFFFFFF")).getNextLong(32)).isEqualTo(4294967295L);
		BitUtils bit = new BitUtils(BytesUtils.fromString("FFFFFFFFFF"));
		bit.setCurrentBitIndex(3);
		Assertions.assertThat(bit.getNextLong(33)).isEqualTo((1L << 33) - 1);
	}

	/**
	 * Signed reads on narrow widths (1, 2 and 5 bits)
	 */
	@Test
	public void testSignedNarrowWidths() {
		Assertions.assertThat(new BitUtils(new byte[] { (byte) 0x80 }).getNextIntegerSigned(1)).isEqualTo(-1);
		Assertions.assertThat(new BitUtils(new byte[] { (byte) 0x00 }).getNextIntegerSigned(1)).isEqualTo(0);
		Assertions.assertThat(new BitUtils(new byte[] { (byte) 0x80 }).getNextLongSigned(1)).isEqualTo(-1L);
		Assertions.assertThat(new BitUtils(new byte[] { (byte) 0xC0 }).getNextIntegerSigned(2)).isEqualTo(-1);
		Assertions.assertThat(new BitUtils(new byte[] { (byte) 0x40 }).getNextIntegerSigned(2)).isEqualTo(1);
		// 5 bits: 01111 = 15, 10000 = -16
		Assertions.assertThat(new BitUtils(new byte[] { (byte) 0x78 }).getNextIntegerSigned(5)).isEqualTo(15);
		Assertions.assertThat(new BitUtils(new byte[] { (byte) 0x80 }).getNextIntegerSigned(5)).isEqualTo(-16);
	}

	/**
	 * Signed reads on full widths, positive and negative extremes
	 */
	@Test
	public void testSignedFullWidthAndPositive() {
		Assertions.assertThat(new BitUtils(BytesUtils.fromString("7FFFFFFF")).getNextIntegerSigned(32)).isEqualTo(Integer.MAX_VALUE);
		Assertions.assertThat(new BitUtils(BytesUtils.fromString("FFFFFFFF")).getNextIntegerSigned(32)).isEqualTo(-1);
		Assertions.assertThat(new BitUtils(BytesUtils.fromString("FFFFFFFFFFFFFFFF")).getNextLongSigned(64)).isEqualTo(-1L);
		Assertions.assertThat(new BitUtils(BytesUtils.fromString("7FFFFFFFFF")).getNextLongSigned(40)).isEqualTo((1L << 39) - 1);
		Assertions.assertThat(new BitUtils(BytesUtils.fromString("8000000000")).getNextLongSigned(40)).isEqualTo(-(1L << 39));
	}

	/**
	 * Signed values written and read at a non aligned index must not corrupt
	 * the preceding bits
	 */
	@Test
	public void testSignedValueAtUnalignedIndex() {
		BitUtils bit = new BitUtils(8);
		bit.setNextInteger(3, 3);
		bit.setNextInteger(-2, 5);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x7E });
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(3)).isEqualTo(3);
		Assertions.assertThat(bit.getNextIntegerSigned(5)).isEqualTo(-2);

		bit = new BitUtils(48);
		bit.setNextLong(3, 3);
		bit.setNextLong(-2, 40);
		Assertions.assertThat(bit.getData()).isEqualTo(BytesUtils.fromString("7FFFFFFFFFC0"));
		bit.reset();
		Assertions.assertThat(bit.getNextLong(3)).isEqualTo(3);
		Assertions.assertThat(bit.getNextLongSigned(40)).isEqualTo(-2);

		// negative value spanning two bytes
		bit = new BitUtils(16);
		bit.setCurrentBitIndex(5);
		bit.setNextLong(-2, 4);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x07, 0x00 });

		bit = new BitUtils(48);
		bit.setCurrentBitIndex(3);
		bit.setNextLong(-1, 40);
		Assertions.assertThat(bit.getData()).isEqualTo(BytesUtils.fromString("1FFFFFFFFFE0"));
	}

	/**
	 * Signed and unsigned readers read the same bits
	 */
	@Test
	public void testSignedAndUnsignedReadSameBits() {
		BitUtils bit = new BitUtils(8);
		bit.setNextInteger(-2, 4);
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(4)).isEqualTo(14);
		bit.reset();
		Assertions.assertThat(bit.getNextIntegerSigned(4)).isEqualTo(-2);
	}

	/**
	 * getNextString edge cases: non ASCII byte, unaligned read, partial byte
	 */
	@Test
	public void testGetNextStringEdgeCases() {
		// non ASCII byte decoded with the ASCII charset -> replacement character
		BitUtils bit = new BitUtils(new byte[] { (byte) 0xE9 });
		Assertions.assertThat(bit.getNextString(8)).isEqualTo("\uFFFD");
		// unaligned read: bits 4..19 of "ABC" = 0x14 0x24
		bit = new BitUtils("ABC".getBytes(StandardCharsets.US_ASCII));
		bit.setCurrentBitIndex(4);
		Assertions.assertThat(bit.getNextString(16)).isEqualTo("\u0014$");
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(20);
		// length not multiple of 8: the last byte is padded with zeros
		bit = new BitUtils("AB".getBytes(StandardCharsets.US_ASCII));
		Assertions.assertThat(bit.getNextString(12)).isEqualTo("A@");
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(12);
	}

	/**
	 * getNextString with multi-byte charsets, including at an unaligned index
	 */
	@Test
	public void testGetNextStringCharsets() {
		BitUtils bit = new BitUtils(19);
		bit.setNextInteger(5, 3);
		bit.setNextByte(new byte[] { (byte) 0xC3, (byte) 0xA9 }, 16);
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(3)).isEqualTo(5);
		Assertions.assertThat(bit.getNextString(16, StandardCharsets.UTF_8)).isEqualTo("é");

		bit = new BitUtils(new byte[] { 0x00, 0x41 });
		Assertions.assertThat(bit.getNextString(16, StandardCharsets.UTF_16BE)).isEqualTo("A");
	}

	// ------------------------------------------------------------------
	// Dates
	// ------------------------------------------------------------------

	/**
	 * When the date cannot be parsed, null is returned but the bits are
	 * consumed anyway
	 */
	@Test
	public void testGetNextDateFailureMovesIndex() {
		BitUtils bit = new BitUtils("2013".getBytes(StandardCharsets.US_ASCII));
		Assertions.assertThat(bit.getNextDate(32, BitUtils.DATE_FORMAT)).isNull();
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(32);

		bit = new BitUtils(new byte[] { (byte) 0xAB, (byte) 0xCD });
		Assertions.assertThat(bit.getNextDate(16, "yyMM", true)).isNull();
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(16);
	}

	/**
	 * BCD and ASCII dates with other patterns (EMV style yyMMdd, time)
	 */
	@Test
	public void testGetNextDatePatterns() throws ParseException {
		BitUtils bit = new BitUtils(BytesUtils.fromString("240115"));
		Assertions.assertThat(bit.getNextDate(24, "yyMMdd", true)).isEqualTo(new SimpleDateFormat("yyMMdd").parse("240115"));
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(24);

		bit = new BitUtils(BytesUtils.fromString("2401"));
		Assertions.assertThat(bit.getNextDate(16, "yyMM", true)).isEqualTo(new SimpleDateFormat("yyMM").parse("2401"));

		SimpleDateFormat full = new SimpleDateFormat("yyyyMMddHHmmss");
		Date expected = full.parse("20130108123456");
		bit = new BitUtils(BytesUtils.fromString("20130108123456"));
		Assertions.assertThat(bit.getNextDate(56, "yyyyMMddHHmmss", true)).isEqualTo(expected);
		bit = new BitUtils("20130108123456".getBytes(StandardCharsets.US_ASCII));
		Assertions.assertThat(bit.getNextDate(112, "yyyyMMddHHmmss")).isEqualTo(expected);

		bit = new BitUtils("1234".getBytes(StandardCharsets.US_ASCII));
		Assertions.assertThat(bit.getNextDate(32, "HHmm")).isEqualTo(new SimpleDateFormat("HHmm").parse("1234"));

		// the 2-arg overload is the non BCD one
		Date d1 = new BitUtils("20130108".getBytes(StandardCharsets.US_ASCII)).getNextDate(64, BitUtils.DATE_FORMAT);
		Date d2 = new BitUtils("20130108".getBytes(StandardCharsets.US_ASCII)).getNextDate(64, BitUtils.DATE_FORMAT, false);
		Assertions.assertThat(d1).isEqualTo(d2);
	}

	/**
	 * Parsing follows SimpleDateFormat semantics: trailing data is ignored and
	 * out of range fields roll over (lenient)
	 */
	@Test
	public void testGetNextDateLenientParsing() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(BitUtils.DATE_FORMAT);
		BitUtils bit = new BitUtils("20130108XX".getBytes(StandardCharsets.US_ASCII));
		Assertions.assertThat(bit.getNextDate(80, BitUtils.DATE_FORMAT)).isEqualTo(sdf.parse("20130108"));
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(80);

		bit = new BitUtils(BytesUtils.fromString("20130108FF"));
		Assertions.assertThat(bit.getNextDate(40, BitUtils.DATE_FORMAT, true)).isEqualTo(sdf.parse("20130108"));

		bit = new BitUtils("20131301".getBytes(StandardCharsets.US_ASCII));
		Assertions.assertThat(bit.getNextDate(64, BitUtils.DATE_FORMAT)).isEqualTo(sdf.parse("20140101"));
	}

	/**
	 * Invalid patterns
	 */
	@Test
	public void testGetNextDateInvalidPattern() {
		final BitUtils bit = new BitUtils("20130108".getBytes(StandardCharsets.US_ASCII));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.getNextDate(64, "QQQ"));
		Assert.assertThrows(NullPointerException.class, () -> bit.getNextDate(64, null));
	}

	/**
	 * Date parsing/formatting must be thread safe
	 */
	@Test
	public void testDateMultiThread() throws Exception {
		final int threads = 8;
		final String[] patterns = { "yyyyMMdd", "yyMMdd", "yyyyMMddHHmmss", "HHmmss" };
		final String[] values = { "20130108", "130108", "20130108123456", "123456" };
		ExecutorService pool = Executors.newFixedThreadPool(threads);
		try {
			List<Future<Boolean>> results = new ArrayList<Future<Boolean>>();
			for (int t = 0; t < threads; t++) {
				final int offset = t;
				results.add(pool.submit(() -> {
					for (int i = 0; i < 2000; i++) {
						int k = (i + offset) % patterns.length;
						Date expected = new SimpleDateFormat(patterns[k]).parse(values[k]);
						BitUtils bit = new BitUtils(values[k].length() * 8);
						bit.setNextDate(expected, patterns[k]);
						bit.reset();
						if (!expected.equals(bit.getNextDate(values[k].length() * 8, patterns[k]))) {
							return false;
						}
						bit = new BitUtils(values[k].length() * 4);
						bit.setNextDate(expected, patterns[k], true);
						bit.reset();
						if (!expected.equals(bit.getNextDate(values[k].length() * 4, patterns[k], true))) {
							return false;
						}
					}
					return true;
				}));
			}
			for (Future<Boolean> result : results) {
				Assertions.assertThat(result.get(60, TimeUnit.SECONDS)).isTrue();
			}
		} finally {
			pool.shutdownNow();
		}
	}

	/**
	 * Any number of distinct patterns can be used, repeatedly
	 */
	@Test
	public void testDateManyPatterns() throws ParseException {
		for (int round = 0; round < 2; round++) {
			for (int i = 0; i < 40; i++) {
				String pattern = "yyyyMMdd'X" + i + "'";
				String value = "20130108X" + i;
				Date expected = new SimpleDateFormat(pattern).parse(value);
				BitUtils bit = new BitUtils(value.length() * 8);
				bit.setNextDate(expected, pattern);
				Assertions.assertThat(bit.getData()).isEqualTo(value.getBytes(StandardCharsets.US_ASCII));
				bit.reset();
				Assertions.assertThat(bit.getNextDate(value.length() * 8, pattern)).isEqualTo(expected);
			}
		}
	}

	/**
	 * setNextDate: encoding, index and other patterns
	 */
	@Test
	public void testSetNextDatePatternsAndIndex() throws ParseException {
		Date d = new SimpleDateFormat(BitUtils.DATE_FORMAT).parse("20130108");
		BitUtils bit = new BitUtils(64);
		bit.setNextDate(d, BitUtils.DATE_FORMAT);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(64);
		Assertions.assertThat(bit.getData()).isEqualTo("20130108".getBytes(StandardCharsets.US_ASCII));

		bit = new BitUtils(64);
		bit.setNextDate(d, BitUtils.DATE_FORMAT, true);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(32);
		Assertions.assertThat(bit.getData()).isEqualTo(BytesUtils.fromString("2013010800000000"));

		bit = new BitUtils(24);
		bit.setNextDate(d, "yyMMdd", true);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(24);
		Assertions.assertThat(bit.getData()).isEqualTo(BytesUtils.fromString("130108"));

		SimpleDateFormat full = new SimpleDateFormat("yyyyMMddHHmmss");
		Date dt = full.parse("20130108123456");
		bit = new BitUtils(112);
		bit.setNextDate(dt, "yyyyMMddHHmmss");
		bit.reset();
		Assertions.assertThat(bit.getNextDate(112, "yyyyMMddHHmmss")).isEqualTo(dt);

		bit = new BitUtils(56);
		bit.setNextDate(dt, "yyyyMMddHHmmss", true);
		Assertions.assertThat(bit.getData()).isEqualTo(BytesUtils.fromString("20130108123456"));
		bit.reset();
		Assertions.assertThat(bit.getNextDate(56, "yyyyMMddHHmmss", true)).isEqualTo(dt);
	}

	/**
	 * setNextDate at a non aligned index
	 */
	@Test
	public void testSetNextDateUnaligned() throws ParseException {
		Date d = new SimpleDateFormat(BitUtils.DATE_FORMAT).parse("20130108");
		BitUtils bit = new BitUtils(67);
		bit.setNextInteger(5, 3);
		bit.setNextDate(d, BitUtils.DATE_FORMAT);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(67);
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(3)).isEqualTo(5);
		Assertions.assertThat(bit.getNextDate(64, BitUtils.DATE_FORMAT)).isEqualTo(d);
	}

	/**
	 * setNextDate invalid arguments: null date/pattern, BCD with a pattern not
	 * producing an even number of decimal digits
	 */
	@Test
	public void testSetNextDateInvalidArguments() throws ParseException {
		final Date d = new SimpleDateFormat(BitUtils.DATE_FORMAT).parse("20130108");
		Assert.assertThrows(NullPointerException.class, () -> new BitUtils(64).setNextDate(null, BitUtils.DATE_FORMAT));
		Assert.assertThrows(NullPointerException.class, () -> new BitUtils(64).setNextDate(null, BitUtils.DATE_FORMAT, true));
		Assert.assertThrows(NullPointerException.class, () -> new BitUtils(64).setNextDate(d, null));
		Assert.assertThrows(IllegalArgumentException.class, () -> new BitUtils(64).setNextDate(d, "MMM", true));
		Assert.assertThrows(IllegalArgumentException.class, () -> new BitUtils(64).setNextDate(d, "yyyyM", true));
	}

	// ------------------------------------------------------------------
	// Writers
	// ------------------------------------------------------------------

	/**
	 * resetNextBits followed by a setter at the same index: the setter writes
	 * over the cleared bits
	 */
	@Test
	public void testOverwriteWithResetNextBits() {
		BitUtils bit = new BitUtils(new byte[] { (byte) 0xFF, (byte) 0xFF });
		bit.setCurrentBitIndex(3);
		bit.resetNextBits(7);
		bit.setCurrentBitIndex(3);
		bit.setNextInteger(0x2A, 7); // 0101010
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xEA, (byte) 0xBF });
		bit.setCurrentBitIndex(3);
		Assertions.assertThat(bit.getNextInteger(7)).isEqualTo(0x2A);

		bit = new BitUtils(new byte[] { (byte) 0xFF });
		bit.setCurrentBitIndex(2);
		bit.resetNextBits(1);
		bit.setCurrentBitIndex(2);
		bit.setNextBoolean(false);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xDF });

		bit = new BitUtils(new byte[] { (byte) 0xFF, (byte) 0xFF });
		bit.setCurrentBitIndex(4);
		bit.resetNextBits(8);
		bit.setCurrentBitIndex(4);
		bit.setNextLong(0x5A, 8);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xF5, (byte) 0xAF });
	}

	/**
	 * resetNextBits moves the index and can be chained
	 */
	@Test
	public void testResetNextBitsMovesIndex() {
		BitUtils bit = new BitUtils(new byte[] { (byte) 0xFF, (byte) 0xFF });
		bit.setCurrentBitIndex(3);
		bit.resetNextBits(7);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(10);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xE0, 0x3F });
		bit.setCurrentBitIndex(12);
		bit.resetNextBits(4);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(16);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xE0, 0x30 });
	}

	/**
	 * Negative values are written as two's complement truncated to the
	 * requested length
	 */
	@Test
	public void testSetNegativeValues() {
		BitUtils bit = new BitUtils(8);
		bit.setNextInteger(-1, 8);
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(8)).isEqualTo(255);
		bit.clear();
		bit.setNextInteger(-128, 8);
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(8)).isEqualTo(128);
		bit.clear();
		bit.setNextInteger(-1, 4);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xF0 });
		bit.clear();
		bit.setNextInteger(-8, 4);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0x80 });
		bit.clear();
		bit.setNextLong(-1L, 8);
		bit.reset();
		Assertions.assertThat(bit.getNextLong(8)).isEqualTo(255);
	}

	/**
	 * Clamping also applies at a non aligned index
	 */
	@Test
	public void testClampingUnaligned() {
		BitUtils bit = new BitUtils(16);
		bit.setCurrentBitIndex(3);
		bit.setNextInteger(0xFF, 4);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x1E, 0x00 });
		bit.setCurrentBitIndex(3);
		Assertions.assertThat(bit.getNextInteger(4)).isEqualTo(15);
	}

	/**
	 * Long values above the int range, on 32 bits and more
	 */
	@Test
	public void testSetLongLargeValues() {
		BitUtils bit = new BitUtils(48);
		bit.setNextLong(1L << 40, 41);
		bit.reset();
		Assertions.assertThat(bit.getNextLong(41)).isEqualTo(1L << 40);
		bit.clear();
		bit.setNextLong(0xFFFFFFFFL, 32);
		bit.reset();
		Assertions.assertThat(bit.getNextLong(32)).isEqualTo(0xFFFFFFFFL);
		bit.clear();
		bit.setNextLong(0x123456789AL, 40);
		Assertions.assertThat(bit.getData()).isEqualTo(BytesUtils.fromString("123456789A00"));
	}

	/**
	 * setNextByte: when the value is shorter than the length, it is padded
	 * with zeros before (default) or after
	 */
	@Test
	public void testSetNextBytePadding() {
		BitUtils bit = new BitUtils(16);
		bit.setNextByte(new byte[] { 0x12 }, 16);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x00, 0x12 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(16);

		bit = new BitUtils(16);
		bit.setNextByte(new byte[] { 0x12 }, 16, true);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x00, 0x12 });

		bit = new BitUtils(16);
		bit.setNextByte(new byte[] { 0x12 }, 16, false);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x12, 0x00 });

		// unaligned
		bit = new BitUtils(24);
		bit.setCurrentBitIndex(3);
		bit.setNextByte(new byte[] { 0x12 }, 16, true);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x00, 0x02, 0x40 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(19);

		bit = new BitUtils(24);
		bit.setCurrentBitIndex(3);
		bit.setNextByte(new byte[] { 0x12 }, 16, false);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x02, 0x40, 0x00 });

		bit = new BitUtils(24);
		bit.setCurrentBitIndex(3);
		bit.setNextByte(new byte[] { 0x12, 0x34 }, 16, true);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x02, 0x46, (byte) 0x80 });

		// empty value: only zeros are written
		bit = new BitUtils(16);
		bit.setNextByte(new byte[0], 8);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x00, 0x00 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(8);
	}

	/**
	 * setNextByte: when the value is longer than the length, the leading bits
	 * are kept whatever the padding mode
	 */
	@Test
	public void testSetNextByteTruncation() {
		BitUtils bit = new BitUtils(16);
		bit.setNextByte(new byte[] { 0x12, 0x34 }, 8);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x12, 0x00 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(8);

		bit = new BitUtils(16);
		bit.setNextByte(new byte[] { 0x12, 0x34 }, 8, true);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x12, 0x00 });

		bit = new BitUtils(16);
		bit.setNextByte(new byte[] { 0x12, 0x34 }, 8, false);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x12, 0x00 });

		bit = new BitUtils(16);
		bit.setNextByte(new byte[] { 0x12, 0x34 }, 12, false);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x12, 0x30 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(12);

		// unaligned index: the bits of the value beyond the field are dropped
		// and the bits after the field are untouched (zero buffer, then 0xFF)
		bit = new BitUtils(24);
		bit.setCurrentBitIndex(3);
		bit.setNextByte(new byte[] { 0x12, 0x3F }, 12, false);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x02, 0x46, 0x00 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(15);
		bit = new BitUtils(24);
		bit.setCurrentBitIndex(3);
		bit.setNextByte(new byte[] { 0x12, 0x3F }, 12, true);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x02, 0x46, 0x00 });
		bit = new BitUtils(24);
		bit.setCurrentBitIndex(1);
		bit.setNextHexaString("FFFF", 10);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x7F, (byte) 0xE0, 0x00 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(11);
		bit = new BitUtils(24);
		bit.setCurrentBitIndex(5);
		bit.setNextString("ABC", 12);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x02, 0x0A, 0x00 });
		bit = new BitUtils(24);
		bit.setCurrentBitIndex(6);
		bit.setNextByte(new byte[] { (byte) 0xFF }, 5, false);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x03, (byte) 0xE0, 0x00 });

		bit = new BitUtils(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF });
		bit.setCurrentBitIndex(3);
		bit.setNextByte(new byte[] { 0x12, 0x3F }, 12, false);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xE2, 0x47, (byte) 0xFF });
		bit = new BitUtils(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF });
		bit.setCurrentBitIndex(1);
		bit.setNextByte(new byte[] { 0x00, 0x00 }, 10, false);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0x80, 0x1F, (byte) 0xFF });

	}

	/**
	 * Aligned write with a length not multiple of 8 must not touch the bits
	 * after the field
	 */
	@Test
	public void testSetNextByteAlignedPartialLength() {
		BitUtils bit = new BitUtils(16);
		bit.setNextByte(new byte[] { (byte) 0xFF }, 4);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xF0, 0x00 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(4);

		bit = new BitUtils(16);
		bit.setNextByte(new byte[] { (byte) 0xFF, (byte) 0xFF }, 12);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xFF, (byte) 0xF0 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(12);
		// the field can be followed by another value
		bit.setNextInteger(0x5, 4);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xFF, (byte) 0xF5 });
	}

	@Test(expected = NullPointerException.class)
	public void testSetNextByteNull() {
		new BitUtils(16).setNextByte(null, 8);
	}

	/**
	 * setNextString: padding before by default, before/after with the flag
	 */
	@Test
	public void testSetNextStringPadding() {
		BitUtils bit = new BitUtils(16);
		bit.setNextString("A", 16);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x00, 0x41 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(16);

		bit = new BitUtils(16);
		bit.setNextString("A", 16, true);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x00, 0x41 });

		bit = new BitUtils(16);
		bit.setNextString("A", 16, false);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x41, 0x00 });

		bit = new BitUtils(16);
		bit.setNextString("", 8);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x00, 0x00 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(8);
	}

	/**
	 * setNextString: the leading chars are kept when the string is too long,
	 * only the requested bits are written
	 */
	@Test
	public void testSetNextStringTruncation() {
		BitUtils bit = new BitUtils(16);
		bit.setNextString("ABC", 16);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x41, 0x42 });

		bit = new BitUtils(16);
		bit.setNextString("ABC", 16, true);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x41, 0x42 });

		bit = new BitUtils(16);
		bit.setNextString("AB", 12);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x41, 0x40 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(12);
	}

	/**
	 * Strings are written in ASCII: non ASCII chars are replaced by '?'
	 */
	@Test
	public void testSetNextStringNonAscii() {
		BitUtils bit = new BitUtils(8);
		bit.setNextString("é", 8);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x3F });
		bit.reset();
		Assertions.assertThat(bit.getNextString(8)).isEqualTo("?");
	}

	@Test(expected = NullPointerException.class)
	public void testSetNextStringNull() {
		new BitUtils(16).setNextString(null, 8);
	}

	/**
	 * setNextHexaString: leading digits kept on truncation, padding before
	 * when the string is too short, partial byte lengths
	 */
	@Test
	public void testSetNextHexaStringPaddingAndTruncation() {
		BitUtils bit = new BitUtils(16);
		bit.setNextHexaString("1122334455", 16);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x11, 0x22 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(16);

		bit = new BitUtils(16);
		bit.setNextHexaString("11", 16);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x00, 0x11 });

		bit = new BitUtils(16);
		bit.setNextHexaString("AB", 4);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xA0, 0x00 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(4);

		bit = new BitUtils(16);
		bit.setNextHexaString("ABCD", 12);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xAB, (byte) 0xC0 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(12);

		bit = new BitUtils(16);
		bit.setNextHexaString("", 8);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x00, 0x00 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(8);
	}

	/**
	 * setNextHexaString accepts lower case digits and spaces between bytes
	 */
	@Test
	public void testSetNextHexaStringInputFormats() {
		BitUtils bit = new BitUtils(16);
		bit.setNextHexaString("ab", 8);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xAB, 0x00 });

		bit = new BitUtils(16);
		bit.setNextHexaString("11 22", 16);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x11, 0x22 });
	}

	/**
	 * setNextHexaString rejects null, invalid chars and odd length; nothing is
	 * written and the index does not move
	 */
	@Test
	public void testSetNextHexaStringInvalid() {
		final BitUtils bit = new BitUtils(16);
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.setNextHexaString(null, 8));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.setNextHexaString("GG", 8));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.setNextHexaString("AAB", 8));
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(0);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x00, 0x00 });
	}

	// ------------------------------------------------------------------
	// Round trips
	// ------------------------------------------------------------------

	/**
	 * Unsigned round trip of random values on random lengths at random
	 * (unaligned) positions
	 */
	@Test
	public void testRandomUnsignedRoundTrip() {
		Random rnd = new Random(20260827L);
		for (int iter = 0; iter < 300; iter++) {
			BitUtils bit = new BitUtils(512);
			List<long[]> fields = new ArrayList<long[]>();
			int remaining = 512;
			while (remaining >= 64) {
				int length = 1 + rnd.nextInt(64);
				long value = rnd.nextLong();
				if (length < 64) {
					value &= (1L << length) - 1;
				}
				bit.setNextLong(value, length);
				fields.add(new long[] { length, value });
				remaining -= length;
			}
			bit.reset();
			for (long[] field : fields) {
				Assertions.assertThat(bit.getNextLong((int) field[0])).isEqualTo(field[1]);
			}
			Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(512 - remaining);
		}
	}

	/**
	 * Signed round trip of random values (negative included) on random lengths
	 * at random (unaligned) positions
	 */
	@Test
	public void testRandomSignedRoundTrip() {
		Random rnd = new Random(20260827L);
		for (int iter = 0; iter < 300; iter++) {
			BitUtils bit = new BitUtils(512);
			List<long[]> fields = new ArrayList<long[]>();
			int remaining = 512;
			while (remaining >= 64) {
				int length = 1 + rnd.nextInt(64);
				long value = rnd.nextLong() >> (64 - length);
				bit.setNextLong(value, length);
				fields.add(new long[] { length, value });
				remaining -= length;
			}
			bit.reset();
			for (long[] field : fields) {
				Assertions.assertThat(bit.getNextLongSigned((int) field[0])).isEqualTo(field[1]);
			}
		}
	}

	/**
	 * Random bytes/hexa strings round trip at unaligned positions
	 */
	@Test
	public void testRandomBytesRoundTrip() {
		Random rnd = new Random(20260827L);
		for (int iter = 0; iter < 300; iter++) {
			int offset = rnd.nextInt(8);
			byte[] payload = new byte[1 + rnd.nextInt(16)];
			rnd.nextBytes(payload);
			BitUtils bit = new BitUtils(offset + payload.length * 8 + 8);
			bit.setNextInteger(0, offset);
			bit.setNextByte(payload, payload.length * 8);
			bit.reset();
			bit.getNextInteger(offset);
			Assertions.assertThat(bit.getNextByte(payload.length * 8)).isEqualTo(payload);
			bit.reset();
			bit.getNextInteger(offset);
			Assertions.assertThat(bit.getNextHexaString(payload.length * 8)).isEqualTo(BytesUtils.bytesToStringNoSpace(payload));

			bit = new BitUtils(offset + payload.length * 8 + 8);
			bit.setNextInteger(0, offset);
			bit.setNextHexaString(BytesUtils.bytesToStringNoSpace(payload), payload.length * 8);
			bit.reset();
			bit.getNextInteger(offset);
			Assertions.assertThat(bit.getNextByte(payload.length * 8)).isEqualTo(payload);
		}
	}

	// ------------------------------------------------------------------
	// Overwrite semantic and argument validation
	// ------------------------------------------------------------------

	/**
	 * Value setters overwrite the bits of the field (a bit set to 1 can be set
	 * back to 0), aligned or not, and leave the surrounding bits untouched
	 */
	@Test
	public void testValueSettersOverwriteExistingBits() {
		BitUtils bit = new BitUtils(new byte[] { (byte) 0xFF });
		bit.setNextBoolean(false);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x7F });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(1);

		bit = new BitUtils(new byte[] { (byte) 0xFF });
		bit.setCurrentBitIndex(3);
		bit.setNextBoolean(false);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xEF });

		bit = new BitUtils(new byte[] { (byte) 0xFF });
		bit.setNextInteger(0, 4);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x0F });

		bit = new BitUtils(new byte[] { (byte) 0xFF, (byte) 0xFF });
		bit.setNextInteger(0x5, 4);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x5F, (byte) 0xFF });

		bit = new BitUtils(new byte[] { (byte) 0xFF, (byte) 0xFF });
		bit.setCurrentBitIndex(3);
		bit.setNextInteger(0, 7);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xE0, 0x3F });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(10);

		bit = new BitUtils(new byte[] { (byte) 0xFF, (byte) 0xFF });
		bit.setNextLong(0, 12);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x00, 0x0F });

		bit = new BitUtils(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF });
		bit.setCurrentBitIndex(5);
		bit.setNextLong(0x2A5, 10); // 1010100101
		// 11111 1010100101 111111111 -> 11111101 01001011 11111111
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xFD, 0x4B, (byte) 0xFF });
		bit.setCurrentBitIndex(5);
		Assertions.assertThat(bit.getNextLong(10)).isEqualTo(0x2A5);
	}

	/**
	 * Byte, string and hexa setters overwrite the bits of the field, aligned
	 * or not, and leave the surrounding bits untouched
	 */
	@Test
	public void testByteSettersOverwriteExistingBits() {
		BitUtils bit = new BitUtils(new byte[] { (byte) 0xFF });
		bit.setNextString("A", 8);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x41 });

		bit = new BitUtils(new byte[] { (byte) 0xFF, (byte) 0xFF });
		bit.setCurrentBitIndex(4);
		bit.setNextString("A", 8);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xF4, 0x1F });

		bit = new BitUtils(new byte[] { (byte) 0xFF, (byte) 0xFF });
		bit.setCurrentBitIndex(4);
		bit.setNextHexaString("00", 8);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xF0, 0x0F });

		bit = new BitUtils(new byte[] { (byte) 0xFF, (byte) 0xFF });
		bit.setNextByte(new byte[] { 0x00 }, 4);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x0F, (byte) 0xFF });

		bit = new BitUtils(new byte[] { (byte) 0xFF, (byte) 0xFF });
		bit.setCurrentBitIndex(3);
		bit.setNextByte(new byte[] { 0x00 }, 6);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xE0, 0x7F });

		bit = new BitUtils(new byte[] { (byte) 0xFF, (byte) 0xFF });
		bit.setNextByte(new byte[] { 0x00, 0x00 }, 12);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x00, 0x0F });

		// multi-byte value at an unaligned index
		bit = new BitUtils(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF });
		bit.setCurrentBitIndex(3);
		bit.setNextByte(new byte[] { 0x12, 0x34 }, 16);
		// 111 0001001000110100 11111 -> 11100010 01000110 10011111 11111111
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xE2, 0x46, (byte) 0x9F, (byte) 0xFF });
		bit.setCurrentBitIndex(3);
		Assertions.assertThat(bit.getNextHexaString(16)).isEqualTo("1234");
	}

	/**
	 * Random writes over a buffer full of ones: every field reads back and the
	 * bits after the last field are untouched
	 */
	@Test
	public void testRandomRoundTripOverFilledBuffer() {
		Random rnd = new Random(20260827L);
		for (int iter = 0; iter < 300; iter++) {
			byte[] filled = new byte[64];
			Arrays.fill(filled, (byte) 0xFF);
			BitUtils bit = new BitUtils(filled);
			List<long[]> fields = new ArrayList<long[]>();
			int index = 0;
			while (index <= 512 - 64) {
				int length = 1 + rnd.nextInt(64);
				long value = rnd.nextLong();
				if (length < 64) {
					value &= (1L << length) - 1;
				}
				if (rnd.nextBoolean() || length % 8 != 0) {
					bit.setNextLong(value, length);
				} else {
					// same bits, written through setNextByte
					byte[] bytes = new byte[length / 8];
					for (int i = 0; i < bytes.length; i++) {
						bytes[i] = (byte) (value >>> (bytes.length - 1 - i) * 8);
					}
					bit.setNextByte(bytes, length);
				}
				fields.add(new long[] { length, value });
				index += length;
			}
			Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(index);
			bit.reset();
			for (long[] field : fields) {
				Assertions.assertThat(bit.getNextLong((int) field[0])).isEqualTo(field[1]);
			}
			while (bit.getCurrentBitIndex() < 512) {
				Assertions.assertThat(bit.getNextBoolean()).isTrue();
			}
		}
	}

	/**
	 * Negative sizes and lengths are rejected with an IllegalArgumentException,
	 * without moving the index or modifying the data
	 */
	@Test
	public void testNegativeLengths() {
		Assert.assertThrows(IllegalArgumentException.class, () -> new BitUtils(-1));
		final BitUtils bit = new BitUtils(new byte[] { (byte) 0xA5 });
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.getNextInteger(-1));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.getNextLong(-1));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.getNextIntegerSigned(-1));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.getNextLongSigned(-1));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.getNextByte(-1));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.getNextByte(-1, false));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.getNextHexaString(-8));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.getNextString(-8));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.getNextString(-8, StandardCharsets.UTF_8));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.getNextDate(-8, BitUtils.DATE_FORMAT));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.getNextDate(-8, BitUtils.DATE_FORMAT, true));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.setNextInteger(1, -1));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.setNextLong(1, -1));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.setNextByte(new byte[] { 1 }, -8));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.setNextByte(new byte[] { 1 }, -8, false));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.setNextString("A", -8));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.setNextString("A", -8, false));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.setNextHexaString("AA", -8));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.resetNextBits(-1));
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(0);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xA5 });
	}

	/**
	 * Unshifted read: the source bytes covered by the field are returned as
	 * they are, only the bits outside the field are cleared; nothing is moved
	 */
	@Test
	public void testGetNextByteUnshiftedSpanningBytes() {
		// 11000010 00000000, 8 bits at index 1: bits 1..7 of the first byte, bit 0 of the second
		BitUtils bit = new BitUtils(new byte[] { (byte) 0xC2, 0x00 });
		bit.setCurrentBitIndex(1);
		Assertions.assertThat(bit.getNextByte(8, false)).isEqualTo(new byte[] { 0x42, 0x00 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(9);

		bit = new BitUtils(new byte[] { (byte) 0xC2, (byte) 0x80 });
		bit.setCurrentBitIndex(1);
		Assertions.assertThat(bit.getNextByte(8, false)).isEqualTo(new byte[] { 0x42, (byte) 0x80 });
		// same read shifted: stream order, packed in one byte
		bit.setCurrentBitIndex(1);
		Assertions.assertThat(bit.getNextByte(8, true)).isEqualTo(new byte[] { (byte) 0x85 });

		// 10101010 11001100, 6 bits at index 5: 010 from AA (positions 5..7), 110 from CC (positions 0..2)
		bit = new BitUtils(new byte[] { (byte) 0xAA, (byte) 0xCC });
		bit.setCurrentBitIndex(5);
		Assertions.assertThat(bit.getNextByte(6, false)).isEqualTo(new byte[] { 0x02, (byte) 0xC0 });
		bit.setCurrentBitIndex(6);
		Assertions.assertThat(bit.getNextByte(4, false)).isEqualTo(new byte[] { 0x02, (byte) 0xC0 });

		// several source bytes: 10101010 11001100 11011101
		bit = new BitUtils(new byte[] { (byte) 0xAA, (byte) 0xCC, (byte) 0xDD });
		bit.setCurrentBitIndex(4);
		Assertions.assertThat(bit.getNextByte(16, false)).isEqualTo(new byte[] { 0x0A, (byte) 0xCC, (byte) 0xD0 });
		bit.setCurrentBitIndex(3);
		Assertions.assertThat(bit.getNextByte(12, false)).isEqualTo(new byte[] { 0x0A, (byte) 0xCC });
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xAA, (byte) 0xCC, (byte) 0xDD });

		// zero length at an unaligned index: empty, index unchanged
		bit.setCurrentBitIndex(3);
		Assertions.assertThat(bit.getNextByte(0, false)).isEqualTo(new byte[0]);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(3);

		// field ending on a byte boundary: the last source byte is returned whole
		bit = new BitUtils(new byte[] { (byte) 0xAA, (byte) 0xCC, (byte) 0xDD });
		bit.setCurrentBitIndex(4);
		Assertions.assertThat(bit.getNextByte(12, false)).isEqualTo(new byte[] { 0x0A, (byte) 0xCC });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(16);
		bit.setCurrentBitIndex(1);
		Assertions.assertThat(bit.getNextByte(15, false)).isEqualTo(new byte[] { 0x2A, (byte) 0xCC });
		bit.setCurrentBitIndex(7);
		Assertions.assertThat(bit.getNextByte(17, false)).isEqualTo(new byte[] { 0x00, (byte) 0xCC, (byte) 0xDD });
		bit = new BitUtils(new byte[] { 0x12, 0x34, 0x56, 0x78 });
		bit.setCurrentBitIndex(2);
		Assertions.assertThat(bit.getNextByte(22, false)).isEqualTo(new byte[] { 0x12, 0x34, 0x56 });

	}

	/**
	 * The current index is clamped to [0, size] by setCurrentBitIndex and
	 * addCurrentBitIndex (size included: position after the last bit)
	 */
	@Test
	public void testIndexClamping() {
		BitUtils bit = new BitUtils(16);
		bit.setCurrentBitIndex(-5);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(0);
		bit.setCurrentBitIndex(16);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(16);
		bit.setCurrentBitIndex(100);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(16);
		bit.setCurrentBitIndex(5);
		bit.addCurrentBitIndex(1000);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(16);
		bit.addCurrentBitIndex(-1000);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(0);
		bit.addCurrentBitIndex(3);
		bit.addCurrentBitIndex(-2);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(1);

		// size not multiple of 8: the logical size is the bound, not the array
		bit = new BitUtils(9);
		bit.setCurrentBitIndex(12);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(9);
		bit.setCurrentBitIndex(7);
		bit.addCurrentBitIndex(5);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(9);

		// byte array constructor: size = 8 * length
		final BitUtils bit2 = new BitUtils(new byte[] { (byte) 0xFF });
		bit2.setCurrentBitIndex(20);
		Assertions.assertThat(bit2.getCurrentBitIndex()).isEqualTo(8);
		Assert.assertThrows(IndexOutOfBoundsException.class, () -> bit2.getNextBoolean());
	}

	/**
	 * Padding before with a length not multiple of 8: the value is right
	 * aligned on the field, not on the byte boundary (nothing is lost)
	 */
	@Test
	public void testSetNextBytePadBeforePartialLength() {
		BitUtils bit = new BitUtils(16);
		bit.setNextByte(new byte[] { 0x05 }, 12);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x00, 0x50 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(12);
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(12)).isEqualTo(5);

		bit = new BitUtils(16);
		bit.setNextByte(new byte[] { 0x05 }, 12, true);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x00, 0x50 });

		bit = new BitUtils(16);
		bit.setNextByte(new byte[] { 0x01 }, 9);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x00, (byte) 0x80 });
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(9)).isEqualTo(1);

		// two bytes value on 20 bits
		bit = new BitUtils(24);
		bit.setNextByte(new byte[] { 0x12, 0x34 }, 20);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x01, 0x23, 0x40 });
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(20)).isEqualTo(0x1234);

		// same through the hexa and string writers
		bit = new BitUtils(16);
		bit.setNextHexaString("05", 12);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x00, 0x50 });
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(12)).isEqualTo(5);

		bit = new BitUtils(16);
		bit.setNextString("A", 12);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x04, 0x10 });
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(12)).isEqualTo(0x41);

		// unaligned index
		bit = new BitUtils(16);
		bit.setNextInteger(0, 3);
		bit.setNextByte(new byte[] { 0x05 }, 12);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x00, 0x0A });
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(3)).isEqualTo(0);
		Assertions.assertThat(bit.getNextInteger(12)).isEqualTo(5);

		// unchanged: padding after, length multiple of 8, value longer than the field
		bit = new BitUtils(16);
		bit.setNextByte(new byte[] { 0x05 }, 12, false);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x05, 0x00 });
		bit = new BitUtils(16);
		bit.setNextByte(new byte[] { 0x05 }, 16);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x00, 0x05 });
		bit = new BitUtils(16);
		bit.setNextByte(new byte[] { 0x12, 0x34 }, 12, true);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x12, 0x30 });

		// value bits crossing a byte boundary once right-aligned, for any rem
		bit = new BitUtils(16);
		bit.setNextByte(new byte[] { (byte) 0x81 }, 9);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x40, (byte) 0x80 });
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(9)).isEqualTo(0x81);

		bit = new BitUtils(24);
		bit.setNextByte(new byte[] { (byte) 0xAB }, 13);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x05, 0x58, 0x00 });
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(13)).isEqualTo(0xAB);

		bit = new BitUtils(24);
		bit.setNextByte(new byte[] { (byte) 0xAB }, 11);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x15, 0x60, 0x00 });
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(11)).isEqualTo(0xAB);

		bit = new BitUtils(24);
		bit.setNextByte(new byte[] { 0x12, 0x34 }, 17);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x09, 0x1A, 0x00 });
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(17)).isEqualTo(0x1234);

		bit = new BitUtils(24);
		bit.setNextByte(new byte[] { 0x01, 0x02 }, 23);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x00, 0x02, 0x04 });
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(23)).isEqualTo(0x102);

		bit = new BitUtils(24);
		bit.setNextString("AB", 21);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x02, 0x0A, 0x10 });
		bit.reset();
		Assertions.assertThat(bit.getNextInteger(21)).isEqualTo(0x4142);

		// same at an unaligned index
		bit = new BitUtils(32);
		bit.setCurrentBitIndex(5);
		bit.setNextByte(new byte[] { (byte) 0x81 }, 9);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x02, 0x04, 0x00, 0x00 });
		bit.setCurrentBitIndex(5);
		Assertions.assertThat(bit.getNextInteger(9)).isEqualTo(0x81);

		bit = new BitUtils(32);
		bit.setCurrentBitIndex(5);
		bit.setNextByte(new byte[] { 0x12, 0x34 }, 17);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x00, 0x48, (byte) 0xD0, 0x00 });
		bit.setCurrentBitIndex(5);
		Assertions.assertThat(bit.getNextInteger(17)).isEqualTo(0x1234);

	}

	/**
	 * Readers and writers are bounded by the logical size of the buffer, not
	 * by its backing array: the padding bits of a size not multiple of 8 are
	 * out of reach
	 */
	@Test
	public void testLogicalSizeBounds() {
		final BitUtils bit = new BitUtils(9);
		bit.setNextInteger(0x1FF, 9);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(9);
		assertOutOfBounds(() -> bit.getNextBoolean());
		assertOutOfBounds(() -> bit.setNextBoolean(true));
		assertOutOfBounds(() -> bit.getNextInteger(1));
		assertOutOfBounds(() -> bit.getNextByte(1, false));
		assertOutOfBounds(() -> bit.getNextHexaString(1));
		assertOutOfBounds(() -> bit.getNextString(1));
		assertOutOfBounds(() -> bit.resetNextBits(1));
		assertOutOfBounds(() -> bit.setNextInteger(1, 1));
		assertOutOfBounds(() -> bit.setNextByte(new byte[] { (byte) 0xFF }, 1));
		// zero length at the end is fine
		Assertions.assertThat(bit.getNextByte(0)).isEqualTo(new byte[0]);
		bit.setNextInteger(1, 0);
		// one bit before the end: 1 bit ok, 2 bits not
		bit.setCurrentBitIndex(8);
		assertOutOfBounds(() -> bit.getNextInteger(2));
		assertOutOfBounds(() -> bit.setNextLong(3, 2));
		Assertions.assertThat(bit.getNextInteger(1)).isEqualTo(1);
		// nothing was written past the size, the padding bits are still 0
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xFF, (byte) 0x80 });
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(9);

		// the same for a byte array (size = 8 * length)
		final BitUtils bit2 = new BitUtils(new byte[] { 0x00 });
		bit2.setCurrentBitIndex(8);
		assertOutOfBounds(() -> bit2.setNextInteger(1, 1));
		assertOutOfBounds(() -> bit2.getNextDate(8, BitUtils.DATE_FORMAT));

		// the aligned string fast path is bounded too (index unchanged)
		final BitUtils bit3 = new BitUtils(12);
		bit3.setCurrentBitIndex(8);
		assertOutOfBounds(() -> bit3.getNextString(8));
		assertOutOfBounds(() -> bit3.getNextString(8, StandardCharsets.UTF_8));
		Assertions.assertThat(bit3.getCurrentBitIndex()).isEqualTo(8);

	}

	/**
	 * addCurrentBitIndex does not overflow with extreme values
	 */
	@Test
	public void testAddCurrentBitIndexExtremeValues() {
		BitUtils bit = new BitUtils(16);
		bit.setCurrentBitIndex(5);
		bit.addCurrentBitIndex(Integer.MAX_VALUE);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(16);
		bit.addCurrentBitIndex(Integer.MIN_VALUE);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(0);
		bit.setCurrentBitIndex(5);
		bit.addCurrentBitIndex(Integer.MIN_VALUE);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(0);
	}

	/**
	 * Values that do not fit on the length are saturated on both sides: to the
	 * unsigned maximum 2^n - 1 above, to the signed minimum -2^(n-1) below (no
	 * sign flip, no wrap)
	 */
	@Test
	public void testSaturationBothSides() {
		BitUtils bit = new BitUtils(8);
		bit.setNextInteger(-9, 4);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0x80 });
		bit.reset();
		Assertions.assertThat(bit.getNextIntegerSigned(4)).isEqualTo(-8);
		bit.clear();
		bit.setNextInteger(-17, 4);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0x80 });
		bit.clear();
		bit.setNextInteger(-5, 3);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0x80 });
		bit.reset();
		Assertions.assertThat(bit.getNextIntegerSigned(3)).isEqualTo(-4);
		bit.clear();
		bit.setNextInteger(Integer.MIN_VALUE, 3);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0x80 });
		bit.clear();
		bit.setNextInteger(-129, 8);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0x80 });
		bit.reset();
		Assertions.assertThat(bit.getNextIntegerSigned(8)).isEqualTo(-128);
		// in range: written as is
		bit.clear();
		bit.setNextInteger(-128, 8);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0x80 });
		bit.clear();
		bit.setNextInteger(-8, 4);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0x80 });
		bit.clear();
		bit.setNextInteger(7, 4);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x70 });
		bit.clear();
		bit.setNextInteger(15, 4);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xF0 });
		// above the unsigned maximum: saturated to it (unchanged rule)
		bit.clear();
		bit.setNextInteger(16, 4);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0xF0 });
		// unaligned
		bit.clear();
		bit.setCurrentBitIndex(3);
		bit.setNextInteger(-100, 4);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { 0x10 });
		bit.setCurrentBitIndex(3);
		Assertions.assertThat(bit.getNextIntegerSigned(4)).isEqualTo(-8);

		// long
		bit = new BitUtils(64);
		bit.setNextLong(Long.MIN_VALUE, 63);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0x80, 0, 0, 0, 0, 0, 0, 0 });
		bit.reset();
		Assertions.assertThat(bit.getNextLongSigned(63)).isEqualTo(-(1L << 62));
		bit.clear();
		bit.setNextLong(-(1L << 62) - 1, 63);
		Assertions.assertThat(bit.getData()).isEqualTo(new byte[] { (byte) 0x80, 0, 0, 0, 0, 0, 0, 0 });
		bit.clear();
		bit.setNextLong(-549755813889L, 40);
		bit.reset();
		Assertions.assertThat(bit.getNextLongSigned(40)).isEqualTo(-549755813888L);
		// on 32 bits every int fits, on 64 bits every long fits
		bit.clear();
		bit.setNextInteger(Integer.MIN_VALUE, 32);
		bit.setNextLong(Long.MIN_VALUE, 32);
		bit.reset();
		Assertions.assertThat(bit.getNextIntegerSigned(32)).isEqualTo(Integer.MIN_VALUE);
		Assertions.assertThat(bit.getNextLongSigned(32)).isEqualTo(-(1L << 31));
		bit.clear();
		bit.setNextLong(Long.MIN_VALUE, 64);
		bit.reset();
		Assertions.assertThat(bit.getNextLongSigned(64)).isEqualTo(Long.MIN_VALUE);
	}

	/**
	 * getNextLong and getNextInteger reject more than 64 bits (like setNextLong
	 * and the signed readers) without moving the index
	 */
	@Test
	public void testGetNextLongWiderThan64Bits() {
		final BitUtils bit = new BitUtils(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF });
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.getNextLong(65));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.getNextInteger(65));
		Assert.assertThrows(IllegalArgumentException.class, () -> bit.getNextLong(Integer.MAX_VALUE));
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(0);
		Assertions.assertThat(bit.getNextLong(64)).isEqualTo(-1L);
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(64);
	}

	/**
	 * A null charset is rejected before anything is read (index unchanged), on
	 * the aligned and on the unaligned path
	 */
	@Test
	public void testGetNextStringNullCharset() {
		final BitUtils bit = new BitUtils(new byte[] { 0x41, 0x42 });
		Assert.assertThrows(NullPointerException.class, () -> bit.getNextString(8, null));
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(0);
		bit.setCurrentBitIndex(3);
		Assert.assertThrows(NullPointerException.class, () -> bit.getNextString(8, null));
		Assertions.assertThat(bit.getCurrentBitIndex()).isEqualTo(3);
	}

	/**
	 * Sizes whose byte count does not fit in an int are rejected with an
	 * IllegalArgumentException (not an obscure NegativeArraySizeException, nor
	 * an unusable buffer with a negative size)
	 */
	@Test
	public void testConstructorSizeOverflow() {
		Assert.assertThrows(IllegalArgumentException.class, () -> new BitUtils(Integer.MAX_VALUE));
		Assert.assertThrows(IllegalArgumentException.class, () -> new BitUtils(Integer.MAX_VALUE - 6));
		final byte[] tooLarge;
		try {
			tooLarge = new byte[Integer.MAX_VALUE / 8 + 1];
		} catch (OutOfMemoryError e) {
			// not enough heap for the 256 MiB array: nothing more to check here
			return;
		}
		Assert.assertThrows(IllegalArgumentException.class, () -> new BitUtils(tooLarge));
	}
}
