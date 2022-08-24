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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

import org.seed.core.util.Assert;

import org.springframework.util.FastByteArrayOutputStream;

class JavaClassFileObject extends SimpleJavaFileObject {
	
	private final String qualifiedName;
	
	private FastByteArrayOutputStream byteCodeOutputStream;
	
	private byte[] byteCode;

	JavaClassFileObject(String qualifiedName) {
		super(URI.create(qualifiedName), Kind.CLASS);
		this.qualifiedName = qualifiedName;
	}
	
	JavaClassFileObject(String qualifiedName, byte[] byteCode) {
		this(qualifiedName);
		Assert.notNull(byteCode, "byte code");
		
		this.byteCode = byteCode;
	}
	
	String getQualifiedName() {
		return qualifiedName;
	}
	
	@Override
	public InputStream openInputStream() {
		return new ByteArrayInputStream(getByteCode());
	}
	
	@Override
	public OutputStream openOutputStream() {
		byteCodeOutputStream = new FastByteArrayOutputStream();
		return byteCodeOutputStream;
	}
	
	byte[] getByteCode() {
		if (byteCode == null) {
			Assert.stateAvailable(byteCodeOutputStream, "byte code");
			
			byteCode = byteCodeOutputStream.toByteArray();
			byteCodeOutputStream.close();
			byteCodeOutputStream = null;
		}
		return byteCode;
	}

}
