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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;

import org.seed.C;
import org.seed.core.util.Assert;

class DependencyFileObject implements JavaFileObject {
	
	private final String qualifiedName;
	
	private final URL url;
	
	DependencyFileObject(String qualifiedName, URI uri) {
		Assert.notNull(qualifiedName, C.QUALIFIEDNAME);
		Assert.notNull(uri, "uri");
		
		this.qualifiedName = qualifiedName;
		try {
			this.url = uri.toURL();
		}
		catch (MalformedURLException muex) {
			throw new CompilerException(muex);
		}
	}
	
	public String getQualifiedName() {
		return qualifiedName;
	}

	@Override
	public boolean delete() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CharSequence getCharContent(boolean arg0) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getLastModified() {
		return 0;
	}

	@Override
	public String getName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return url.openStream();
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Reader openReader(boolean arg0) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Writer openWriter() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public URI toUri() {
		try {
			return url.toURI();
		} 
		catch (URISyntaxException usex) {
			throw new CompilerException(usex);
		}
	}

	@Override
	public Modifier getAccessLevel() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Kind getKind() {
		return Kind.CLASS;
	}

	@Override
	public NestingKind getNestingKind() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isNameCompatible(String simpleName, Kind kind) {
		throw new UnsupportedOperationException();
	}

}
