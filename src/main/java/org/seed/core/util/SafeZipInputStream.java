/**
 * Seed
 * Copyright (C) 2021 EUU⛰ROCKS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.seed.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.util.FastByteArrayOutputStream;

public class SafeZipInputStream extends ZipInputStream {
	
	private static final int BUFFER_SIZE = 8 * 1024;			// 8 KB
	
	private static final int THRESHOLD_ENTRIES = 10000;
	
	private static final int THRESHOLD_ENTRY_SIZE = 100000000;  // 100 MB 
	
	private static final int THRESHOLD_TOTAL_SIZE = 1000000000; // 1 GB 
	
	private static final double THRESHOLD_RATIO = 10;
	
	private final byte[] buffer;
	
	private int totalSizeArchive = 0;
	
	private int totalEntryArchive = 0;

	public SafeZipInputStream(InputStream inputStream) {
		super(inputStream);
		buffer = new byte[BUFFER_SIZE];
	}
	
	public ZipEntry getNextEntrySafe() throws IOException {
		if (++totalEntryArchive > THRESHOLD_ENTRIES) {
			throw new IOException("ZipBombAttack: too much entries: " + totalEntryArchive);
	    }
		return getNextEntry();
	}
	
	public byte[] readSafe(ZipEntry zipEntry) throws IOException {
		try (FastByteArrayOutputStream byteArrayOutputStream = new FastByteArrayOutputStream()) {
			int totalSizeEntry = 0;
			int numBytes = -1;
			while ((numBytes = read(buffer)) > 0) {
				byteArrayOutputStream.write(buffer, 0, numBytes);
				totalSizeEntry += numBytes;
			    totalSizeArchive += numBytes;
			    
			    if (totalSizeEntry > THRESHOLD_ENTRY_SIZE) {
			    	throw new IOException("ZipBombAttack: uncompressed entry is too large: " + totalSizeEntry);
			    }
			    if (totalSizeArchive > THRESHOLD_TOTAL_SIZE) {
			    	throw new IOException("ZipBombAttack: uncompressed data is too large: " + totalSizeArchive);
			    }
			    
			    final double compressionRatio = ((double) totalSizeEntry) / zipEntry.getCompressedSize();
			    if (compressionRatio > THRESHOLD_RATIO) {
			    	throw new IOException("ZipBombAttack: compression ratio is suspicious: " + compressionRatio);
			    }
			}
			return byteArrayOutputStream.toByteArray();
		}
	}
	
}
