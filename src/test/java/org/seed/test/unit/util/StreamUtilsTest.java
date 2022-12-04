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
package org.seed.test.unit.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import org.junit.jupiter.api.Test;

import org.seed.core.util.StreamUtils;

class StreamUtilsTest {
	
	static final String TEST = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod "
			+ "tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.";
	
	@Test
	void testCompressText() {
		String compressed = StreamUtils.compress(TEST);
		assertNotNull(compressed);
		assertEquals(TEST, StreamUtils.decompress(compressed));
	}
	
	@Test
	void testStreamText() {
		InputStream stream = StreamUtils.getStringAsStream(TEST);
		assertNotNull(stream);
		assertEquals(TEST, StreamUtils.getStreamAsText(stream));
	}
	
}
