/*******************************************************************************
 * Copyright (C) 2022 Fred D7e (https://github.com/yafred)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.yafred.asn1.runtime;

import java.io.IOException;
import java.io.OutputStream;

import java.util.BitSet;


public class BERWriter {
    static byte[] bitMask = new byte[] {
            (byte) 0x80, (byte) 0x40, (byte) 0x20, (byte) 0x10, (byte) 0x08, (byte) 0x04,
            (byte) 0x02, (byte) 0x01
        };
    OutputStream out = null;
    byte[] buffer = null;
    int dataSize = 0;
    boolean flushFlag = false; // true when buffer has been written to out and no data has been encoded yet.
    int increment;

    public BERWriter(OutputStream out, int initialSize, int increment) {
        this.out = out;
        this.increment = increment;
        buffer = new byte[initialSize];
    }

    public BERWriter(OutputStream out) {
        this(out, 100, 100);
    }

    public BERWriter() {
        this(null);
    }

    public void flush() throws IOException {
        if (out != null) {
            out.write(buffer, buffer.length - dataSize, dataSize);
         }

        flushFlag = true;
    }

    void increaseDataSize(int nBytes) {
        if (flushFlag) {
            flushFlag = false;
            dataSize = 0;
        }

        if ((dataSize + nBytes) > buffer.length) {
            byte[] tempBuffer = new byte[buffer.length + increment + nBytes];
            System.arraycopy(buffer, buffer.length - dataSize, tempBuffer,
                tempBuffer.length - dataSize, dataSize);
            buffer = tempBuffer;
        }

        dataSize += nBytes;
    }

    // Reminder on java types
    // byte 8bits
    // short 16 bits
    // int 32 bits
    // long 64 bits
    public int writeInteger(Byte value) {
        return writeInteger(value.longValue());
    }

    public int writeInteger(Short value) {
        return writeInteger(value.longValue());
    }

    public int writeInteger(Integer value) {
        return writeInteger(value.longValue());
    }

    public int writeInteger(Long value) {
        return writeInteger(java.math.BigInteger.valueOf(value));
    }

    public int writeInteger(java.math.BigInteger value) {
        return writeOctetString(value.toByteArray());
    }

    /*
     * BitSet is not the best way to hold a bitstring
     * We have to scan all the bits of the BitSet
     */
    public int writeBitString(BitSet value) {
        // find last true bit
        int significantBitNumber = value.length();

        // count number of bytes
        int nBytes = 0;
        int nPadding = 0; // bit string is left aligned 

        if (significantBitNumber == 0) {
            nBytes = 0;
        } else {
            nBytes = significantBitNumber / 8;
            nPadding = significantBitNumber % 8;

            if (nPadding != 0) {
                nBytes += 1;
            }
        }

        // put into writer
        increaseDataSize(nBytes);

        int currentIndex = buffer.length - dataSize;
        int maskId = 0;

        for (int i = 0; i < significantBitNumber; i++) {
            if (value.get(i)) {
                buffer[currentIndex] |= bitMask[maskId];
            }

            if (maskId == 7) {
                currentIndex++;
                maskId = 0;
            } else {
                maskId++;
            }
        }

        // leading byte is number of unused bit in last byte
        increaseDataSize(1);

        if (nPadding == 0) {
            buffer[buffer.length - dataSize] = 0;
        } else {
            buffer[buffer.length - dataSize] = (byte) (8 - nPadding);
        }

        return nBytes + 1; // bit string bytes + leading byte (containing num of padding bits)
    }

    public int writeBoolean(Boolean value) {
        boolean boolValue = value.booleanValue();
        increaseDataSize(1);

        if (boolValue) {
            buffer[buffer.length - dataSize] = (byte)0xFF;
        } else {
            buffer[buffer.length - dataSize] = 0x00;
        }

        return 1;
    }

    public int writeRestrictedCharacterString(String value) {
        byte[] bytes = value.getBytes();

        return writeOctetString(bytes);
    }

    /**
 	 * a 32bit int provides for a maximum length of 2147483647 (> 2G)
     */
    public int writeLength(int value) {
        if (value < 0) {
            throw new RuntimeException("negative length");
        }

        int nBytes = 0;

        if (value > 0xFFFFFF) {
            nBytes = 5;
        } else if (value > 0xFFFF) {
            nBytes = 4;
        } else if (value > 0xFF) {
            nBytes = 3;
        } else if (value > 0x7F) {
            nBytes = 2;
        } else {
            nBytes = 1;
        }

        int nShift = 0;

        for (int i = nBytes; i > 1; i--, nShift += 8) {
            increaseDataSize(1);
            buffer[buffer.length - dataSize] = (byte) (value >> nShift);
        }

        // first byte is either number of subsequent bytes or the length itself
        increaseDataSize(1);

        if (nBytes > 1) {
            buffer[buffer.length - dataSize] = (byte) ((nBytes - 1) | 0x80);
        } else {
            buffer[buffer.length - dataSize] = (byte) (value);
        }

        return nBytes;
    }

    public int writeOctetString(byte[] value) {
        increaseDataSize(value.length);
        System.arraycopy(value, 0, buffer, buffer.length - dataSize, value.length);

        return value.length;
    }
	
	public int writeByte(byte value) {
		increaseDataSize(1);
        buffer[buffer.length - dataSize] = value;
		
		return 1;
	}
	
	public int writeObjectIdentifier(long[] value) {
		if(value == null) {
			throw new RuntimeException("Object Identifier cannot be null");
		}
		if(value.length < 2) {
			throw new RuntimeException("Object Identifier must have at least 2 arcs");
		}
		if(value[0] > 2) {
			throw new RuntimeException("Object Identifier first arc must be 0, 1 or 2");
		}
		if(value[0] == 0 && value[1] > 39) {
			throw new RuntimeException("Object Identifier second arc must be < 40 when first arc is 0");
		}
		if(value[0] == 1 && (value[1] == 0 || value[1] > 39)) {
			throw new RuntimeException("Object Identifier second arc must be > 0 and < 40 when first arc is 1");
		}
		
		int size = 0; 
		for(int i = value.length-1; i > 1; i--) {
			long arc = value[i];
			boolean isLast = true;
			do {
				long aByte = arc % 128;
				arc = arc / 128;
				if(isLast) {
					isLast = false;
				}
				else {
					aByte |= 0x80;
				}
				writeByte((byte)aByte);				
				size++;
			}
			while(arc > 0);
		}
		
		long arc = 40 * value[0] + value[1];
		boolean isLast = true;
		do {
			long aByte = arc % 128;
			arc = arc / 128;
			if(isLast) {
				isLast = false;
			}
			else {
				aByte |= 0x80;
			}
			writeByte((byte)aByte);				
			size++;
		}
		while(arc > 0);
		
		return size;
	}

	public int writeRelativeOID(long[] value) {
		int size = 0; 
		for(int i = value.length-1; i >= 0; i--) {
			long arc = value[i];
			boolean isLast = true;
			do {
				long aByte = arc % 128;
				arc = arc / 128;
				if(isLast) {
					isLast = false;
				}
				else {
					aByte |= 0x80;
				}
				writeByte((byte)aByte);				
				size++;
			}
			while(arc > 0);
		}

		return size;
	}
	
    /**
     * Provides a buffer containing the encoded data.
     * Returns null if no data has been encoded since the buffer has been flushed.
     */
    public byte[] getTraceBuffer() {
        byte[] copy = null;

        if (dataSize > 0) {
            copy = new byte[dataSize];
            System.arraycopy(buffer, buffer.length - dataSize, copy, 0, dataSize);
        }

        return copy;
    }
}
