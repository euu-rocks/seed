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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;
import java.util.zip.ZipInputStream;

import org.seed.InternalException;

import org.springframework.core.io.Resource;

public abstract class StreamUtils {
	
	public static final Charset CHARSET = MiscUtils.CHARSET;
	
	private StreamUtils() {}
	
	public static String getResourceAsText(Resource resource) throws IOException {
		return getStreamAsText(resource.getInputStream());
	}
	
	public static String getFileAsText(File file) throws IOException {
		return getStreamAsText(new FileInputStream(file));
	}
	
	public static InputStream getStringAsStream(String string) {
		return new ByteArrayInputStream(string.getBytes(CHARSET));
	}
	
	public static ZipInputStream getZipStream(byte[] bytes) {
		return new ZipInputStream(new ByteArrayInputStream(bytes));
	}
	
	public static String compress(String text) {
		if (text != null) {
			try {
				final byte[] compressedBytes = compress(text.getBytes(CHARSET));
				return new String(Base64.getEncoder().encode(compressedBytes), CHARSET);
			} 
			catch (IOException ex) {
				throw new InternalException(ex);
			}
		}
		return text;
	}
	
	public static String decompress(String compressedText) {
		if (compressedText != null) {
			try {
				final byte[] decompressedBytes = decompress(Base64.getDecoder().decode(compressedText));
				return MiscUtils.toString(decompressedBytes);
			} 
			catch (IOException ex) {
				throw new InternalException(ex);
			}  
		}
		return compressedText;
	}
	
	public static byte[] compress(byte[] bytes) throws IOException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		if (bytes != null) {
			try (DeflaterOutputStream deflaterStream = new DeflaterOutputStream(out)) {
		        deflaterStream.write(bytes);    
		    }
		}
	    return out.toByteArray();
	}
	
	public static byte[] decompress(byte[] bytes) throws IOException {
	    final ByteArrayOutputStream out = new ByteArrayOutputStream();
	    if (bytes != null) {
		    try (InflaterOutputStream inflaterStream = new InflaterOutputStream(out)) {
		        inflaterStream.write(bytes);    
		    }
	    }
	    return out.toByteArray();
	}
	
	private static String getStreamAsText(InputStream stream) throws IOException {
		try (InputStream inputStream = stream) {
			return org.springframework.util.StreamUtils.copyToString(inputStream, CHARSET);
		}
	}
	
}
