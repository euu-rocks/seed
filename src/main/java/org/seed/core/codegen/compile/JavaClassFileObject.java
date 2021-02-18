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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.tools.SimpleJavaFileObject;

import org.springframework.util.Assert;

import static org.seed.core.codegen.CodeUtils.*;

class JavaClassFileObject extends SimpleJavaFileObject {
	
	private final String qualifiedName;
	
	private ByteArrayOutputStream byteCodeOutputStream;
	
	private byte[] byteCode;

	JavaClassFileObject(String qualifiedName) {
		super(createClassURI(qualifiedName), Kind.CLASS);
		this.qualifiedName = qualifiedName;
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
		byteCodeOutputStream = new ByteArrayOutputStream();
		return byteCodeOutputStream;
	}
	
	byte[] getByteCode() {
		if (byteCode == null) {
			Assert.state(byteCodeOutputStream != null, "byte code not available");
			byteCode = byteCodeOutputStream.toByteArray();
			byteCodeOutputStream = null;
		}
		return byteCode;
	}

}
