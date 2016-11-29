package fr.devnied.bitlib;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Test;

/**
 * Class to test the Utils class bytesUtils
 *
 * @author Millau Julien
 *
 */
public final class BytesUtilsTest {

	/**
	 * First test byte tab
	 */
	private final byte[] tab = new byte[] { 0x12, (byte) 0x92, (byte) 0xAB, 0x3A };
	/**
	 * Second test byte tab
	 */
	private final byte[] tab2 = new byte[] { 0x00, (byte) 0x01, (byte) 0x02, 0x45 };

	/**
	 *
	 */
	@Test
	public void testByteArrayToInt() {
		Assertions.assertThat(BytesUtils.byteArrayToInt(BytesUtils.fromString("00000000"))).isEqualTo(0);
		Assertions.assertThat(BytesUtils.byteArrayToInt(BytesUtils.fromString("00000001"))).isEqualTo(1);
		Assertions.assertThat(BytesUtils.byteArrayToInt(BytesUtils.fromString("7FFFFFFF")))
				.isEqualTo(Integer.MAX_VALUE);
		Assertions.assertThat(BytesUtils.byteArrayToInt(BytesUtils.fromString("80000000")))
				.isEqualTo(Integer.MIN_VALUE);
		Assertions.assertThat(BytesUtils.byteArrayToInt(BytesUtils.fromString("00000100"))).isEqualTo(256);
		// partial int
		Assertions.assertThat(BytesUtils.byteArrayToInt(BytesUtils.fromString("0100"))).isEqualTo(256);
		Assertions.assertThat(BytesUtils.byteArrayToInt(BytesUtils.fromString("FF"))).isEqualTo(255);
		Assertions.assertThat(BytesUtils.byteArrayToInt(BytesUtils.fromString("FFFF"))).isEqualTo(65535);

		Assertions.assertThat(BytesUtils.byteArrayToInt(BytesUtils.fromString("00000100"), 2, 1)).isEqualTo(1);

		try {
			BytesUtils.byteArrayToInt(null);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(true);
		}

		try {
			BytesUtils.byteArrayToInt(null, 2, 2);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(true);
		}

		try {
			BytesUtils.byteArrayToInt(tab, 0, 10);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(true);
		}

		try {
			BytesUtils.byteArrayToInt(tab, 2, 3);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(true);
		}

	}

	/**
	 *
	 */
	@Test
	public void testBytesFromString() {
		try {
			BytesUtils.fromString(" 00 5");
			Assert.fail();
		} catch (Exception e) {
		}

		try {
			BytesUtils.fromString(null);
			Assert.fail();
		} catch (Exception e) {
		}

		Assertions.assertThat(BytesUtils.fromString(" 00 54 ")).isEqualTo(new byte[] { 0x00, 0x54 });

		Assertions.assertThat(BytesUtils.fromString("00 54 0A9B"))
				.isEqualTo(new byte[] { 0x00, 0x54, 0x0A, (byte) 0x9B });
		Assertions.assertThat(BytesUtils.fromString("000ABC")).isEqualTo(new byte[] { 0x00, 0x0A, (byte) 0xBC });
	}

	/**
	 *
	 */
	@Test
	public void testBytesToString() {
		Assertions.assertThat(BytesUtils.bytesToString(tab)).isEqualTo("12 92 AB 3A");
		Assertions.assertThat(BytesUtils.bytesToString(null)).isEqualTo("");
		Assertions.assertThat(BytesUtils.bytesToString(tab2)).isEqualTo("00 01 02 45");
	}

	/**
	 *
	 */
	@Test
	public void testBytesToStringNoSpace() {
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(tab)).isEqualTo("1292AB3A");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(null)).isEqualTo("");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(tab2)).isEqualTo("00010245");
	}

	/**
	 *
	 */
	@Test
	public void testBytesToStringNoSpacebyte() {
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace((byte) 0)).isEqualTo("00");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace((byte) 255)).isEqualTo("FF");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(null)).isEqualTo("");
	}

	/**
	 *
	 */
	@Test
	public void testBytesToStringNoSpaceTruncate() {
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(BytesUtils.toByteArray(4608), true)).isEqualTo("1200");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(BytesUtils.toByteArray(206), true)).isEqualTo("CE");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(BytesUtils.toByteArray(266), true)).isEqualTo("010A");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(BytesUtils.toByteArray(0), true)).isEqualTo("");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(BytesUtils.toByteArray(Integer.MAX_VALUE), true))
				.isEqualTo("7FFFFFFF");

		Assertions.assertThat(BytesUtils.bytesToString(BytesUtils.toByteArray(4608), true)).isEqualTo("12 00");
		Assertions.assertThat(BytesUtils.bytesToString(BytesUtils.toByteArray(206), true)).isEqualTo("CE");
		Assertions.assertThat(BytesUtils.bytesToString(BytesUtils.toByteArray(266), true)).isEqualTo("01 0A");
		Assertions.assertThat(BytesUtils.bytesToString(BytesUtils.toByteArray(0), true)).isEqualTo("");
		Assertions.assertThat(BytesUtils.bytesToString(BytesUtils.toByteArray(Integer.MAX_VALUE), true))
				.isEqualTo("7F FF FF FF");
	}

	@Test(expected = IllegalAccessException.class)
	public void testConstructorPrivate() throws Exception {
		BytesUtils.class.newInstance();
		Assert.fail("Utility class constructor should be private");
	}

	/**
	 *
	 */
	@Test
	public void testMatchBitByBitIndex() {
		Assertions.assertThat(BytesUtils.matchBitByBitIndex(1, 0)).isEqualTo(true);
		Assertions.assertThat(BytesUtils.matchBitByBitIndex(128, 7)).isEqualTo(true);
		Assertions.assertThat(BytesUtils.matchBitByBitIndex(127, 7)).isEqualTo(false);
		Assertions.assertThat(BytesUtils.matchBitByBitIndex(255, 7)).isEqualTo(true);
		Assertions.assertThat(BytesUtils.matchBitByBitIndex(0, 0)).isEqualTo(false);
		Assertions.assertThat(BytesUtils.matchBitByBitIndex(256, 8)).isEqualTo(true);
		Assertions.assertThat(BytesUtils.matchBitByBitIndex(Integer.MIN_VALUE, 31)).isEqualTo(true);
		Assertions.assertThat(BytesUtils.matchBitByBitIndex(Integer.MAX_VALUE, 31)).isEqualTo(false);

		try {
			BytesUtils.matchBitByBitIndex(0, -1);
			Assert.fail();
		} catch (Exception e) {
		}
		try {
			BytesUtils.matchBitByBitIndex(0, 32);
			Assert.fail();
		} catch (Exception e) {
		}
	}

	/**
	 *
	 */
	@Test
	public void testSetBytes() {
		byte max = (byte) 0xFF;
		byte min = 0;
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(BytesUtils.setBit(min, 7, true))).isEqualTo("80");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(BytesUtils.setBit(min, 0, true))).isEqualTo("01");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(BytesUtils.setBit(max, 4, false))).isEqualTo("EF");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(BytesUtils.setBit(max, 0, false))).isEqualTo("FE");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(BytesUtils.setBit(min, 0, false))).isEqualTo("00");
		try {
			BytesUtils.setBit(max, -1, false);
			Assert.fail();
		} catch (Exception e) {
		}
		try {
			BytesUtils.setBit(max, 8, false);
			Assert.fail();
		} catch (Exception e) {
		}
	}

	/**
	 *
	 */
	@Test
	public void testToBinary() {
		Assertions.assertThat(BytesUtils.toBinary(null)).isEqualTo(null);
		Assertions.assertThat(BytesUtils.toBinary(new byte[] { 0x44, 0x01 })).isEqualTo("0100010000000001");
		Assertions.assertThat(BytesUtils.toBinary(new byte[] { 0x00, 0x00, 0x00 }))
				.isEqualTo("000000000000000000000000");
		Assertions.assertThat(BytesUtils.toBinary(new byte[] { (byte) 0xF0, 0x00, 0x00 }))
				.isEqualTo("111100000000000000000000");
		Assertions.assertThat(BytesUtils.toBinary(new byte[] { (byte) 0xFF })).isEqualTo("11111111");
	}

	/**
	 *
	 */
	@Test
	public void testToByteArray() {
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(BytesUtils.toByteArray(0))).isEqualTo("00000000");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(BytesUtils.toByteArray(1))).isEqualTo("00000001");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(BytesUtils.toByteArray(Integer.MAX_VALUE)))
				.isEqualTo("7FFFFFFF");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(BytesUtils.toByteArray(Integer.MIN_VALUE)))
				.isEqualTo("80000000");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(BytesUtils.toByteArray(256))).isEqualTo("00000100");
	}

	@Test
	public final void privateConstructor() throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		final Constructor<?> constructor = BytesUtils.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		Assertions.assertThat(constructor.newInstance()).isNotNull();
	}

}
