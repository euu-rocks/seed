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

import java.lang.annotation.Annotation;
import java.util.List;

import org.seed.C;
import org.seed.InternalException;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

public interface BeanUtils {
	
	static <T> T instantiate(Class<T> typeClass) {
		Assert.notNull(typeClass, C.TYPECLASS);
		try {
			return typeClass.getDeclaredConstructor().newInstance();
		} 
		catch (Exception ex) {
			throw new InternalException(ex);
		}
	}
	
	static <T> List<T> getBeans(ApplicationContext applicationContext, Class<T> typeClass) {
		Assert.notNull(applicationContext, C.CONTEXT);
		Assert.notNull(typeClass, C.TYPECLASS);
		
		return CollectionUtils.valueList(applicationContext.getBeansOfType(typeClass));
	}
	
	static <T> List<Class<? extends T>> getImplementingClasses(Class<T> typeClass) {
		Assert.notNull(typeClass, C.TYPECLASS);
		
		return findClasses(new AssignableTypeFilter(typeClass));
	}
	
	static <T> List<Class<? extends T>> getAnnotatedClasses(Class<? extends Annotation> annotationClass) {
		Assert.notNull(annotationClass, "annotationClass");
		
		return findClasses(new AnnotationTypeFilter(annotationClass));
	}
	
	private static <T> List<Class<? extends T>> findClasses(TypeFilter typeFilter) {
		final var scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(typeFilter);
		return CollectionUtils.convertedList(scanner.findCandidateComponents("org.seed"), 
											 BeanUtils::toClass);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> Class<? extends T> toClass(BeanDefinition beanDef) {
		try {
			return (Class<? extends T>) Class.forName(beanDef.getBeanClassName());
		} 
		catch (Exception ex) {
			throw new InternalException(ex);
		}
	}
	
}
