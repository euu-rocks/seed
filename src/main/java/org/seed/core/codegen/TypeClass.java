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
package org.seed.core.codegen;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import org.seed.C;
import org.seed.core.util.Assert;

import static org.seed.core.codegen.CodeUtils.*;

public final class TypeClass {
	
	public final String packageName;
	
	public final String className;
	
	public final Class<?> genericClass; 
	
	public final TypeClass[] typeClasses;
	
	TypeClass(String qualifiedName) {
		this(extractPackageName(qualifiedName), extractClassName(qualifiedName));
	}
	
	TypeClass(Class<?> typeClass, TypeClass ...typeClasses) {
		this(extractPackageName(typeClass.getName()), 
			 typeClass.getSimpleName(), null, typeClasses);
	}
	
	TypeClass(String packageName, String className) {
		this(packageName, className, null);
	}
	
	TypeClass(String packageName, String className, Class<?> genericClass, TypeClass ...typeClasses) {
		Assert.notNull(packageName, C.PACKAGENAME);
		Assert.notNull(className, C.CLASSNAME);
		
		this.packageName = packageName;
		this.className = className;
		this.genericClass = genericClass;
		this.typeClasses = typeClasses;
	}
	
	String getQualifiedName() {
		return CodeUtils.getQualifiedName(packageName, className);
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
					.append(packageName)
					.append(className)
					.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		final TypeClass otherType = (TypeClass) obj;
		return new EqualsBuilder()
					.append(packageName, otherType.packageName)
					.append(className, otherType.className)
					.isEquals();
	}
	
	static void sort(List<TypeClass> list) {
		if (list != null) {
			list.sort((TypeClass typeClass1, TypeClass typeClass2) -> 
							typeClass1.getQualifiedName().compareTo(typeClass2.getQualifiedName()));
		}
	}
	
}
