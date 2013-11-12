package fr.devnied.bitlib;

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

		Assertions.assertThat(BytesUtils.fromString("00 54 0A9B")).isEqualTo(new byte[] { 0x00, 0x54, 0x0A, (byte) 0x9B });
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
	public void testMatchBit() {
		Assertions.assertThat(BytesUtils.matchBitByValue(5, 1)).isEqualTo(true);
		Assertions.assertThat(BytesUtils.matchBitByValue(5, 4)).isEqualTo(true);
		Assertions.assertThat(BytesUtils.matchBitByValue(5, 8)).isEqualTo(false);

		Assertions.assertThat(BytesUtils.matchBitByValue(2, 2)).isEqualTo(true);

		Assertions.assertThat(BytesUtils.matchBitByValue(15, 1)).isEqualTo(true);
		Assertions.assertThat(BytesUtils.matchBitByValue(15, 2)).isEqualTo(true);
		Assertions.assertThat(BytesUtils.matchBitByValue(15, 4)).isEqualTo(true);
		Assertions.assertThat(BytesUtils.matchBitByValue(15, 8)).isEqualTo(true);

		Assertions.assertThat(BytesUtils.matchBitByValue(10, 8)).isEqualTo(true);
		Assertions.assertThat(BytesUtils.matchBitByValue(10, 2)).isEqualTo(true);
		Assertions.assertThat(BytesUtils.matchBitByValue(10, 1)).isEqualTo(false);
	}

}
