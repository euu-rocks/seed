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
package org.seed.core.codegen.compile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.List;

class CustomJarClassLoader extends URLClassLoader {
	
	private static class CustomJarStreamHandler extends URLStreamHandler {
		
		private final CustomJar customJar;
		
		private CustomJarStreamHandler(CustomJar customJar) {
			this.customJar = customJar;
		}

		@Override
		protected URLConnection openConnection(URL url) throws IOException {
			
			return new URLConnection(url) {
				
				@Override
				public void connect() throws IOException {
					connected = true;
				}
				
				@Override
		        public long getContentLengthLong() {
					return customJar.getContent().length;
				}
				
				@Override
		        public InputStream getInputStream() throws IOException {
					return new ByteArrayInputStream(customJar.getContent());
				}
			};
		}
		
	}
	
	CustomJarClassLoader(List<CustomJar> customJars, ClassLoader parent) {
		super(getCustomJarURLs(customJars), parent);
	}
	
	private static URL[] getCustomJarURLs(List<CustomJar> customJars) {
		return customJars.stream().map(CustomJarClassLoader::createURL)
						 .toArray(URL[]::new);
	}
	
	private static URL createURL(CustomJar customJar) {
		try {
			return new URL("memory", "", -1, customJar.getName(), 
						   new CustomJarStreamHandler(customJar));
		} 
		catch (MalformedURLException ex) {
			throw new CompilerException(ex);
		}
	}

}
