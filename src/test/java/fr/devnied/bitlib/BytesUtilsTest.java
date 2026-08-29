package fr.devnied.bitlib;

import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
	public void testToBinaryEmptyArray() {
		Assertions.assertThat(BytesUtils.toBinary(new byte[] {})).isEqualTo(null);
	}

	@Test
	public void testFromStringEmpty() {
		Assertions.assertThat(BytesUtils.fromString("")).isEqualTo(new byte[] {});
	}

	@Test(expected = IllegalArgumentException.class)
	public void testByteArrayToIntNegativeStartPos() {
		BytesUtils.byteArrayToInt(new byte[] { 0x01, 0x02 }, -1, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testByteArrayToIntZeroLength() {
		BytesUtils.byteArrayToInt(new byte[] { 0x01, 0x02 }, 0, 0);
	}

	@Test
	public final void privateConstructor() throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		final Constructor<?> constructor = BytesUtils.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		Assertions.assertThat(constructor.newInstance()).isNotNull();
	}


	// ------------------------------------------------------------------
	// Contract based tests
	// ------------------------------------------------------------------

	/**
	 * byteArrayToInt: 1 to 4 bytes are accepted, anything else is rejected with
	 * an IllegalArgumentException
	 */
	@Test
	public void testByteArrayToIntLengths() {
		Assertions.assertThat(BytesUtils.byteArrayToInt(new byte[] { 0x01, 0x00, 0x00 })).isEqualTo(65536);
		Assertions.assertThat(BytesUtils.byteArrayToInt(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF })).isEqualTo(16777215);
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.byteArrayToInt(new byte[0]));
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.byteArrayToInt(new byte[] { 1, 2, 3, 4, 5 }));
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.byteArrayToInt(null));
	}

	/**
	 * byteArrayToInt with offset: multi-byte windows, high bit set in the
	 * window, exact exception type for invalid arguments
	 */
	@Test
	public void testByteArrayToIntWithOffset() {
		Assertions.assertThat(BytesUtils.byteArrayToInt(tab, 1, 2)).isEqualTo(0x92AB);
		Assertions.assertThat(BytesUtils.byteArrayToInt(tab, 2, 1)).isEqualTo(0xAB);
		Assertions.assertThat(BytesUtils.byteArrayToInt(tab, 3, 1)).isEqualTo(0x3A);
		Assertions.assertThat(BytesUtils.byteArrayToInt(tab, 0, 4)).isEqualTo(0x1292AB3A);
		Assertions.assertThat(BytesUtils.byteArrayToInt(new byte[] { 0, 1, 2, 3, 4 }, 1, 4)).isEqualTo(0x01020304);
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.byteArrayToInt(new byte[] { 1, 2, 3, 4, 5 }, 0, 5));
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.byteArrayToInt(tab, 0, -1));
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.byteArrayToInt(tab, 4, 1));
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.byteArrayToInt(tab, 2, 3));
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.byteArrayToInt(null, 0, 1));
	}

	/**
	 * bytesToString / bytesToStringNoSpace edge cases
	 */
	@Test
	public void testBytesToStringEdgeCases() {
		Assertions.assertThat(BytesUtils.bytesToString(new byte[0])).isEqualTo("");
		Assertions.assertThat(BytesUtils.bytesToString(new byte[0], true)).isEqualTo("");
		Assertions.assertThat(BytesUtils.bytesToString(null, true)).isEqualTo("");
		Assertions.assertThat(BytesUtils.bytesToString(null, false)).isEqualTo("");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(new byte[0])).isEqualTo("");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(new byte[0], true)).isEqualTo("");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(null, true)).isEqualTo("");
		// the one-arg overload is the same as truncate = false
		Assertions.assertThat(BytesUtils.bytesToString(tab2, false)).isEqualTo(BytesUtils.bytesToString(tab2));
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(tab2, false)).isEqualTo(BytesUtils.bytesToStringNoSpace(tab2));
		// truncate only removes the leading zero bytes
		Assertions.assertThat(BytesUtils.bytesToString(new byte[] { 0x00, 0x00, 0x0A, 0x00 }, true)).isEqualTo("0A 00");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(new byte[] { 0x00, 0x0A }, true)).isEqualTo("0A");
		// single byte, upper case
		Assertions.assertThat(BytesUtils.bytesToString(new byte[] { (byte) 0xAB })).isEqualTo("AB");
	}

	/**
	 * bytesToStringNoSpace(byte): leading zero digit and sign bit
	 */
	@Test
	public void testBytesToStringNoSpaceSingleByte() {
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace((byte) 0x0A)).isEqualTo("0A");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace((byte) 0x7F)).isEqualTo("7F");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace((byte) 0x80)).isEqualTo("80");
	}

	/**
	 * fromString accepts lower case digits
	 */
	@Test
	public void testFromStringLowerCase() {
		Assertions.assertThat(BytesUtils.fromString("ab")).isEqualTo(new byte[] { (byte) 0xAB });
		Assertions.assertThat(BytesUtils.fromString("aBcD")).isEqualTo(new byte[] { (byte) 0xAB, (byte) 0xCD });
	}

	/**
	 * fromString ignores whitespaces anywhere (Character.isWhitespace
	 * semantic: EM SPACE is a separator, NO-BREAK SPACE is not)
	 */
	@Test
	public void testFromStringWhitespaces() {
		Assertions.assertThat(BytesUtils.fromString("0 0 5 4")).isEqualTo(new byte[] { 0x00, 0x54 });
		Assertions.assertThat(BytesUtils.fromString("00\t54\n")).isEqualTo(new byte[] { 0x00, 0x54 });
		Assertions.assertThat(BytesUtils.fromString("00\r\n54")).isEqualTo(new byte[] { 0x00, 0x54 });
		Assertions.assertThat(BytesUtils.fromString("   ")).isEqualTo(new byte[0]);
		Assertions.assertThat(BytesUtils.fromString("00\u200354")).isEqualTo(new byte[] { 0x00, 0x54 });
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.fromString("00\u00A054"));
	}

	/**
	 * fromString rejects invalid characters, odd length and null with an
	 * IllegalArgumentException
	 */
	@Test
	public void testFromStringInvalid() {
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.fromString("0G"));
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.fromString("0x12"));
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.fromString("-1"));
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.fromString("é"));
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.fromString("éé"));
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.fromString("5"));
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.fromString(" 00 5"));
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.fromString(null));
	}

	/**
	 * fromString digit decoding follows Character.digit: non ASCII decimal
	 * digits (here full width 1 and 2) are accepted
	 */
	@Test
	public void testFromStringUnicodeDigits() {
		Assertions.assertThat(BytesUtils.fromString("\uFF11\uFF12")).isEqualTo(new byte[] { 0x12 });
	}

	/**
	 * fromString is the inverse of bytesToString / bytesToStringNoSpace
	 */
	@Test
	public void testFromStringRoundTrip() {
		Assertions.assertThat(BytesUtils.fromString(BytesUtils.bytesToString(tab))).isEqualTo(tab);
		Assertions.assertThat(BytesUtils.fromString(BytesUtils.bytesToStringNoSpace(tab))).isEqualTo(tab);
		Assertions.assertThat(BytesUtils.fromString(BytesUtils.bytesToString(tab2))).isEqualTo(tab2);
		Assertions.assertThat(BytesUtils.fromString(BytesUtils.bytesToStringNoSpace(tab2))).isEqualTo(tab2);
	}

	/**
	 * matchBitByBitIndex on every index, exact exception type
	 */
	@Test
	public void testMatchBitByBitIndexAllIndexes() {
		for (int i = 0; i < 32; i++) {
			Assertions.assertThat(BytesUtils.matchBitByBitIndex(-1, i)).isTrue();
			Assertions.assertThat(BytesUtils.matchBitByBitIndex(0, i)).isFalse();
			Assertions.assertThat(BytesUtils.matchBitByBitIndex(1 << i, i)).isTrue();
		}
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.matchBitByBitIndex(0, -1));
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.matchBitByBitIndex(0, 32));
	}

	/**
	 * setBit: idempotence, sign bit, every index, exact exception type
	 */
	@Test
	public void testSetBitEdgeCases() {
		Assertions.assertThat(BytesUtils.setBit((byte) 0xFF, 3, true)).isEqualTo((byte) 0xFF);
		Assertions.assertThat(BytesUtils.setBit((byte) 0x00, 3, false)).isEqualTo((byte) 0x00);
		Assertions.assertThat(BytesUtils.setBit((byte) 0x7F, 7, true)).isEqualTo((byte) 0xFF);
		Assertions.assertThat(BytesUtils.setBit((byte) 0xFF, 7, false)).isEqualTo((byte) 0x7F);
		for (int i = 0; i < 8; i++) {
			Assertions.assertThat(BytesUtils.setBit((byte) 0x00, i, true)).isEqualTo((byte) (1 << i));
			Assertions.assertThat(BytesUtils.setBit((byte) 0xFF, i, false)).isEqualTo((byte) ~(1 << i));
		}
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.setBit((byte) 0xFF, -1, false));
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.setBit((byte) 0xFF, 8, false));
	}

	/**
	 * toBinary with the sign bit set
	 */
	@Test
	public void testToBinaryNegativeByte() {
		Assertions.assertThat(BytesUtils.toBinary(new byte[] { (byte) 0x80 })).isEqualTo("10000000");
		Assertions.assertThat(BytesUtils.toBinary(new byte[] { (byte) 0x80, 0x01 })).isEqualTo("1000000000000001");
	}

	/**
	 * toByteArray always returns 4 bytes and is the inverse of byteArrayToInt
	 */
	@Test
	public void testToByteArrayRoundTrip() {
		Assertions.assertThat(BytesUtils.toByteArray(-1))
				.isEqualTo(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF });
		int[] values = { 0, 1, -1, Integer.MIN_VALUE, Integer.MAX_VALUE, 0x12345678, -256, 256 };
		for (int value : values) {
			Assertions.assertThat(BytesUtils.toByteArray(value).length).isEqualTo(4);
			Assertions.assertThat(BytesUtils.byteArrayToInt(BytesUtils.toByteArray(value))).isEqualTo(value);
		}
	}

	/**
	 * fromString decode table: every char up to U+00FF is either a hexa digit,
	 * a whitespace (skipped) or rejected, including the boundaries of the ASCII
	 * table (DEL, U+0080)
	 */
	@Test
	public void testFromStringAsciiTable() {
		for (char c = 0; c < 256; c++) {
			final String input = "0" + c;
			if (Character.digit(c, 16) >= 0) {
				Assertions.assertThat(BytesUtils.fromString(input)).isEqualTo(new byte[] { (byte) Character.digit(c, 16) });
			} else if (Character.isWhitespace(c)) {
				// the whitespace is skipped: one digit left, odd length
				Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.fromString(input));
				Assertions.assertThat(BytesUtils.fromString(input + "5")).isEqualTo(new byte[] { 0x05 });
			} else {
				Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.fromString(input));
			}
		}
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.fromString("\u007F0"));
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.fromString("0\u007F"));
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.fromString("\u00800"));
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.fromString("0\u0080"));
		Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.fromString("\u00FF0"));
	}

	/**
	 * Error messages identify the problem (character, index, length) without
	 * echoing the input, which may hold sensitive data
	 */
	@Test
	public void testFromStringErrorMessages() {
		IllegalArgumentException e = Assert.assertThrows(IllegalArgumentException.class,
				() -> BytesUtils.fromString("4111 1111 1111 1111 SMITH"));
		Assertions.assertThat(e.getMessage()).contains("'S'").contains("index 20");
		Assertions.assertThat(e.getMessage().contains("4111")).isFalse();
		e = Assert.assertThrows(IllegalArgumentException.class, () -> BytesUtils.fromString("4111 1111 5"));
		Assertions.assertThat(e.getMessage()).contains("even-length").contains("9 hexadecimal digits");
		Assertions.assertThat(e.getMessage().contains("4111")).isFalse();
	}
}
