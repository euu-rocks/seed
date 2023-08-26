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
import org.seed.Seed;
import org.seed.core.config.SystemLog;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

public interface BeanUtils {
	
	static <T> T instantiate(Class<T> typeClass) {
		Assert.notNull(typeClass, C.TYPECLASS);
		try {
			return typeClass.getDeclaredConstructor().newInstance();
		} 
		catch (Exception ex) {
			SystemLog.logError(ex);
			throw new InternalException(ex);
		}
	}
	
	static <T> List<T> getBeans(ApplicationContext applicationContext, Class<T> type) {
		Assert.notNull(applicationContext, C.CONTEXT);
		Assert.notNull(type, C.TYPE);
		
		return valueList(applicationContext.getBeansOfType(type));
	}
	
	static <T> List<Class<? extends T>> getImplementingClasses(Class<T> typeClass) {
		Assert.notNull(typeClass, C.TYPECLASS);
		
		return findClasses(new AssignableTypeFilter(typeClass));
	}
	
	static List<Class<?>> getAnnotatedClasses(Class<? extends Annotation> annotationClass) {
		Assert.notNull(annotationClass, "annotationClass");
		
		return findClasses(new AnnotationTypeFilter(annotationClass));
	}
	
	static <T> T callIs(Object object, String propertyName) {
		Assert.notNull(propertyName, C.PROPERTYNAME);
		
		return callMethod(object, "is".concat(StringUtils.capitalize(propertyName)));
	}
	
	static <T> T callGetter(Object object, String propertyName) {
		Assert.notNull(propertyName, C.PROPERTYNAME);
		
		return callMethod(object, "get".concat(StringUtils.capitalize(propertyName)));
	}
	
	static void callSetter(Object object, String propertyName, Object ...parameters) {
		Assert.notNull(propertyName, C.PROPERTYNAME);
		
		callMethod(object, "set".concat(StringUtils.capitalize(propertyName)), parameters);
	}
	
	@SuppressWarnings("unchecked")
	static <T> T callMethod(Object object, String methodName, Object ...parameters) {
		Assert.notNull(object, C.OBJECT);
		Assert.notNull(methodName, "method name");
		final var method = firstMatch(object.getClass().getMethods(), 
									  methd -> methd.getName().equals(methodName));
		Assert.stateAvailable(method, object.getClass().getName() + '.' + methodName);
		return (T) ReflectionUtils.invokeMethod(method, object, parameters);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> List<Class<? extends T>> findClasses(TypeFilter typeFilter) {
		final var listClasses = new ArrayList<Class<? extends T>>();
		final var scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(typeFilter);
		try {
			for (final var beanDef : scanner.findCandidateComponents(Seed.BASE_APPLICATION_PACKAGE)) {
				listClasses.add((Class<? extends T>) Class.forName(beanDef.getBeanClassName()));
			}
		}
		catch (ClassNotFoundException cnfex) {
			SystemLog.logError(cnfex);
			throw new InternalException(cnfex);
		}
		return listClasses;
	}
	
}
