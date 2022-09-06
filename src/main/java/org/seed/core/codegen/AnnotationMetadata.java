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

import static org.seed.core.util.CollectionUtils.notEmpty;

import java.util.Collections;
import java.util.Map;

import org.seed.core.util.Assert;

public final class AnnotationMetadata {
	
	final Class<?> annotationClass;
	
	final String singleValue;
	
	final Map<String, Object> parameterMap;
	
	AnnotationMetadata(Class<?> annotationClass) {
		this(annotationClass, (Map<String, Object>) null);
	}
	
	AnnotationMetadata(Class<?> annotationClass, String singleValue) {
		Assert.notNull(annotationClass, "annotationClass");
		Assert.notNull(singleValue, "singleValue");
		
		this.annotationClass = annotationClass;
		this.singleValue = singleValue;
		parameterMap = null;
	}
	
	AnnotationMetadata(Class<?> annotationClass, String name, Object value) {
		this(annotationClass, Collections.singletonMap(name, value));
	}

	AnnotationMetadata(Class<?> annotationClass, Map<String, Object> parameterMap) {
		Assert.notNull(annotationClass, "annotationClass");
		
		this.annotationClass = annotationClass;
		this.parameterMap = parameterMap;
		singleValue = null;
	}

	boolean hasParameters() {
		return notEmpty(parameterMap);
	}

}
