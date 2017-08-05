package fr.devnied.bitlib;

import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
		Assertions.assertThat(bit.getNextByte(8, false)).isEqualTo(new byte[] { 0x44 });
		bit.reset();
		Assertions.assertThat(bit.getNextByte(4)).isEqualTo(new byte[] { 0x10 });
		Assertions.assertThat(bit.getNextByte(4)).isEqualTo(new byte[] { 0x10 });

		bit = new BitUtils(new byte[] { 0x44, (byte) 0xFE });
		Assertions.assertThat(bit.getNextByte(4, false)).isEqualTo(new byte[] { 0x40 });
		Assertions.assertThat(bit.getNextByte(8, false)).isEqualTo(new byte[] { (byte) 0x4F });
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

}
