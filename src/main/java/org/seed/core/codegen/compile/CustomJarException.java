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

import java.util.List;

import org.seed.InternalException;

public class CustomJarException extends InternalException {

	private static final long serialVersionUID = 1556363470857538095L;
	
	private static final int MAX_MESSAGE_LEN = 1024;

	CustomJarException(List<JavaClassFileObject> notDefinedClasses) {
		super(createMessage(notDefinedClasses));
	}
	
	private static String createMessage(List<JavaClassFileObject> notDefinedClasses) {
		final StringBuilder buf = new StringBuilder();
		for (JavaClassFileObject classFile : notDefinedClasses) {
			if (buf.length() + classFile.getQualifiedName().length() + 1 <= MAX_MESSAGE_LEN) {
				if (buf.length() > 0) {
					buf.append(',');
				}
				buf.append(classFile.getQualifiedName());
			}
			else {
				if (buf.length() + 3 <= MAX_MESSAGE_LEN) {
					buf.append("...");
				}
				break;
			}
		}
		return buf.toString();
	}
	
}
