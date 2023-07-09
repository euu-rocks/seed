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
package org.seed.core.util;

import static org.seed.core.util.CollectionUtils.*;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.seed.C;
import org.seed.InternalException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

public interface BeanUtils {
	
	static <T> T instantiate(Class<T> clas) {
		Assert.notNull(clas, "class");
		try {
			return clas.getDeclaredConstructor().newInstance();
		} 
		catch (Exception ex) {
			throw new InternalException(ex);
		}
	}
	
	static <T> List<T> getBeans(ApplicationContext applicationContext, Class<T> type) {
		Assert.notNull(applicationContext, C.CONTEXT);
		Assert.notNull(type, C.TYPE);
		
		return valueList(applicationContext.getBeansOfType(type));
	}
	
	static <T> List<Class<? extends T>> getImplementingClasses(Class<T> typeClass) {
		Assert.notNull(typeClass, "typeClass");
		
		return findClasses(new AssignableTypeFilter(typeClass));
	}
	
	static List<Class<?>> getAnnotatedClasses(Class<? extends Annotation> annotationClass) {
		Assert.notNull(annotationClass, "annotationClass");
		
		return findClasses(new AnnotationTypeFilter(annotationClass));
	}
	
	static <T> T callIs(Object object, String propertyName) {
		Assert.notNull(propertyName, "property name");
		
		return callMethod(object, "is" + StringUtils.capitalize(propertyName));
	}
	
	static <T> T callGetter(Object object, String propertyName) {
		Assert.notNull(propertyName, "property name");
		
		return callMethod(object, "get" + StringUtils.capitalize(propertyName));
	}
	
	static void callSetter(Object object, String propertyName, Object ...parameters) {
		Assert.notNull(propertyName, "property name");
		
		callMethod(object, "set" + StringUtils.capitalize(propertyName), parameters);
	}
	
	@SuppressWarnings("unchecked")
	static <T> T callMethod(Object object, String methodName, Object ...parameters) {
		Assert.notNull(object, C.OBJECT);
		Assert.notNull(methodName, "method name");
		final var method = firstMatch(object.getClass().getMethods(), 
									  methd -> methd.getName().equals(methodName));
		if (method != null) {
			return (T) ReflectionUtils.invokeMethod(method, object, parameters);
		}
		else {
			throw new IllegalStateException("method not found: " + 
											object.getClass().getName() + '.' + 
											methodName);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T> List<Class<? extends T>> findClasses(TypeFilter typeFilter) {
		final var listClasses = new ArrayList<Class<? extends T>>();
		final var scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(typeFilter);
		try {
			for (final var beanDef : scanner.findCandidateComponents("org.seed")) {
				listClasses.add((Class<? extends T>) Class.forName(beanDef.getBeanClassName()));
			}
		}
		catch (ClassNotFoundException cnfex) {
			throw new InternalException(cnfex);
		}
		return listClasses;
	}
	
}
