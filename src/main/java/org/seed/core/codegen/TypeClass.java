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

import java.util.Comparator;

import org.springframework.util.Assert;

import static org.seed.core.codegen.CodeUtils.*;

public final class TypeClass {
	
	static final Comparator<TypeClass> COMPARATOR = new Comparator<TypeClass>() {
		@Override
		public int compare(TypeClass typeClass1, TypeClass typeClass2) {
			return typeClass1.getQualifiedName().compareTo(typeClass2.getQualifiedName());
		}
	};
	
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
		Assert.notNull(packageName, "packageName is null");
		Assert.notNull(className, "className is null");
		
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
		final int prime = 31;
		int result = prime + ((packageName == null) ? 0 : packageName.hashCode());
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		final TypeClass other = (TypeClass) obj;
		return packageName.equals(other.packageName) &&
			   className.equals(other.className);
	}
	
}
