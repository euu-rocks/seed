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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

import org.seed.InternalException;

import org.springframework.core.io.Resource;
import org.springframework.util.FastByteArrayOutputStream;

public abstract class StreamUtils {
	
	private static final Charset CHARSET = MiscUtils.CHARSET;
	
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
	
	public static SafeZipInputStream getZipStream(byte[] bytes) {
		return new SafeZipInputStream(new ByteArrayInputStream(bytes));
	}
	
	public static String compress(String text) {
		if (text != null) {
			try {
				final byte[] compressedBytes = compress(text.getBytes(CHARSET));
				return MiscUtils.toString(Base64.getEncoder().encode(compressedBytes));
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
	
	public static String getStreamAsText(InputStream intputStream) {
		try (final var stream = intputStream) {
			return org.springframework.util.StreamUtils.copyToString(stream, CHARSET);
		}
		catch (IOException ex) {
			throw new InternalException(ex);
		}
	}
	
	private static byte[] compress(byte[] bytes) throws IOException {
		if (bytes != null) {
			try (final var stream = new FastByteArrayOutputStream()) {
				try (final var deflaterStream = new DeflaterOutputStream(stream)) {
					deflaterStream.write(bytes);
				}
				return stream.toByteArray();
			}
		}
		return bytes;
	}
	
	private static byte[] decompress(byte[] bytes) throws IOException {
		if (bytes != null) {
			try (final var stream = new FastByteArrayOutputStream()) {
				try (final var inflaterStream = new InflaterOutputStream(stream)) {
					inflaterStream.write(bytes);   
				}
				return stream.toByteArray();
			}
		}
		return bytes;
	}

}
