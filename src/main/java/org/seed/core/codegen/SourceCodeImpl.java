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

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class SourceCodeImpl<T> implements SourceCode<T> {
	
	private final ClassMetadata classMetadata;
	
	private final String content;
	
	public SourceCodeImpl(String qualifiedName, String content) {
		this(new ClassMetadata(qualifiedName), content);
	}
	
	SourceCodeImpl(ClassMetadata classMetadata, String content) {
		Assert.notNull(classMetadata, "classMetadata is null");
		Assert.state(StringUtils.hasText(content), "content is empty");
		
		this.classMetadata = classMetadata;
		this.content = content;
	}
	
	@Override
	public String getQualifiedName() {
		return classMetadata.getQualifiedName();
	}
	
	@Override
	public String getPackageName() {
		return classMetadata.packageName;
	}
	
	@Override
	public String getClassName() {
		return classMetadata.className;
	}
	
	@Override
	public String getContent() {
		return content;
	}
	
	@Override
	public String toString() {
		return getQualifiedName();
	}
	
}
