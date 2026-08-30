# Changelog

Format: [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [1.7.0] - Unreleased

Binary compatible with 1.6.0. The bytes produced or parsed can differ: check
*Changed* when upgrading.

### Performance
- Reading and writing long fields is 5x to 26x faster (64 bytes at an unaligned
  index: 14 ns).
- Integer and long fields are read about 5x faster (`getNextLong(64)`: 2.9 ns).
- Hexa strings and byte conversions are about 4x faster.
- Dates are twice as fast to read and four times as fast to write, through a
  per-thread formatter cache.
- Writing a value shorter than its field no longer allocates.
- Writing a small integer field costs about 0.6 ns more, in exchange for the
  guarantees below.

### Fixed
- A field can be written anywhere in the buffer without disturbing its
  neighbours.
- Writing over existing data replaces it, so a buffer can be reused.
- Out-of-range values keep their sign and saturate to the bounds of the field.
- Oversized lengths, oversized buffers and invalid hexa strings are reported
  instead of silently producing wrong data.
- Zero-length reads are usable and return an empty result.

### Changed
- Reads and writes stay within the size of the buffer and fail without touching
  the data or the index.
- `getNextByte(size, false)` returns the bits in place, in
  `ceil((index % 8 + size) / 8)` bytes: 8 bits at index 4 of `{0x44, 0xFE}` give
  `{0x04, 0xF0}`.
- The current index stays within `[0, size]`.
- Padding before a length not multiple of 8 right-aligns the value on the field:
  `setNextByte({0x05}, 12)` writes `0000 0000 0101b`.
- A date formatter keeps the time zone and locale of its first use in the
  thread.
- Unparseable dates are logged at WARN without a stack trace, and error messages
  never echo the input.

### Documentation and tests
- Javadoc states the contract of every method.
- 132 tests pin it (32 → 101 and 16 → 31).

## [1.6.0] and earlier

See the [GitHub releases](https://github.com/devnied/Bit-lib4j/releases).

[1.7.0]: https://github.com/devnied/Bit-lib4j/compare/bit-lib4j-1.6.0...master
