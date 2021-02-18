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

import java.util.Collections;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

public final class AnnotationMetadata {
	
	final Class<?> annotationClass;
	
	final String singleValue;
	
	final Map<String, Object> parameterMap;
	
	AnnotationMetadata(Class<?> annotationClass) {
		this(annotationClass, (Map<String, Object>) null);
	}
	
	AnnotationMetadata(Class<?> annotationClass, String singleValue) {
		Assert.notNull(annotationClass, "annotationClass is null");
		Assert.notNull(singleValue, "singleValue is null");
		
		this.annotationClass = annotationClass;
		this.singleValue = singleValue;
		parameterMap = null;
	}
	
	AnnotationMetadata(Class<?> annotationClass, String name, Object value) {
		this(annotationClass, Collections.singletonMap(name, value));
	}

	AnnotationMetadata(Class<?> annotationClass, Map<String, Object> parameterMap) {
		Assert.notNull(annotationClass, "annotationClass is null");
		
		this.annotationClass = annotationClass;
		this.parameterMap = parameterMap;
		singleValue = null;
	}

	boolean hasParameters() {
		return !ObjectUtils.isEmpty(parameterMap);
	}

}
