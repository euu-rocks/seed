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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.seed.C;
import org.seed.InternalException;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

public abstract class BeanUtils {
	
	private static final String PACKAGE_SCAN_ROOT = "org.seed";
	
	private BeanUtils() {}
	
	public static <T> T instantiate(Class<T> clas) {
		try {
			return clas.getDeclaredConstructor().newInstance();
		} 
		catch (Exception ex) {
			throw new InternalException(ex);
		}
	}
	
	public static <T> List<T> getBeans(ApplicationContext applicationContext, Class<T> type) {
		Assert.notNull(applicationContext, C.CONTEXT);
		Assert.notNull(type, C.TYPE);
		
		return applicationContext.getBeansOfType(type).values()
									.stream().collect(Collectors.toList());
	}
	
	public static <T> List<Class<? extends T>> getImplementingClasses(Class<T> typeClass) {
		Assert.notNull(typeClass, "typeClass");
		
		return findClasses(new AssignableTypeFilter(typeClass));
	}
	
	public static List<Class<?>> getAnnotatedClasses(Class<? extends Annotation> annotationClass) {
		Assert.notNull(annotationClass, "annotationClass");
		
		return findClasses(new AnnotationTypeFilter(annotationClass));
	}
	
	@SuppressWarnings("unchecked")
	private static <T> List<Class<? extends T>> findClasses(TypeFilter typeFilter) {
		final List<Class<? extends T>> listClasses = new ArrayList<>();
		final ClassPathScanningCandidateComponentProvider scanner =
				new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(typeFilter);
		for (BeanDefinition beanDef : scanner.findCandidateComponents(PACKAGE_SCAN_ROOT)) {
			try {
				listClasses.add((Class<? extends T>) Class.forName(beanDef.getBeanClassName()));
			} 
			catch (ClassNotFoundException cnfex) {
				throw new InternalException(cnfex);
			}
		}
		return listClasses;
	}
	
}
