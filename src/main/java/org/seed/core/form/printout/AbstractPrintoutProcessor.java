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
package org.seed.core.form.printout;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.seed.C;
import org.seed.core.data.FileObject;
import org.seed.core.entity.Entity;
import org.seed.core.entity.NestedEntity;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.form.FormPrintout;
import org.seed.core.form.LabelProvider;
import org.seed.core.util.Assert;
import org.seed.core.util.ObjectAccess;

public abstract class AbstractPrintoutProcessor extends ObjectAccess implements PrintoutProcessor {
	
	private static final String PATTERN_START = "{{";
	private static final String PATTERN_END = "}}";
	
	private final Entity entity;
	
	private final LabelProvider labelProvider;
	
	public AbstractPrintoutProcessor(Entity entity, LabelProvider labelProvider) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(labelProvider, "labelPrivider");
		
		this.entity = entity;
		this.labelProvider = labelProvider;
	}
	
	protected NestedEntity findNestedProperty(String text) {
		Assert.notNull(text, C.TEXT);
		
		int idx = 0;
		while (idx < text.length()) {
			final int start = findPatternStart(text, idx);
			if (start < 0) {
				break;
			}
			
			final int end = findPatternEnd(text, start);
			if (end < 0) {
				return null;
			}
			
			final String[] keyParts = getKey(text, start, end).split("\\.");
			if (keyParts.length > 1) {
				final NestedEntity nested = entity.getNestedByInternalName(keyParts[0]);
				if (nested != null) {
					return nested;
				}
			}
			
			idx = end + PATTERN_END.length();
		}
		return null;
	}
	
	protected String replace(String text, ValueObject valueObject, NestedEntity nestedEntity, ValueObject nestedObject) {
		Assert.notNull(text, C.TEXT);
		Assert.notNull(valueObject, "valueObject");
		
		if (findPatternStart(text, 0) < 0) {
			return text;
		}
		
		int idx = 0;
		final StringBuilder buf = new StringBuilder();
		while (idx < text.length()) {
			final int start = findPatternStart(text, idx);
			if (start < 0) {
				buf.append(text.substring(idx));
				break;
			}
			
			final int end = findPatternEnd(text, start);
			if (end < 0) {
				return "error: {{unbalanced braces}}";
			}
			
			buf.append(text.substring(idx, start))
			   .append(replaceKey(getKey(text, start, end), 
					   			  valueObject, nestedEntity, nestedObject));
			
			idx = end + PATTERN_END.length();
		}
		return buf.toString();
	}
	
	
	private String replaceKey(String key, ValueObject valueObject, NestedEntity nestedEntity, ValueObject nestedObject) {
		if (nestedEntity != null && key.startsWith(nestedEntity.getInternalName() + '.')) {
			return replaceKey(key.substring(key.indexOf('.') + 1), nestedObject);
		}
		return replaceKey(key, valueObject);
	}
	
	private String replaceKey(String key, ValueObject valueObject) {
		try {
			final int idx = key.indexOf('.');
			if (idx >= 0) {
				final Object object = callGetter(valueObject, key.substring(0, idx));
				if (object != null) {
					if (object instanceof ValueObject) {
						return replaceKey(key.substring(idx + 1), (ValueObject) object);
					}
					else {
						throw new IllegalStateException();
					}
				}
				return "";
			}
			
			final Object value = formatValue(callGetter(valueObject, key));
			return value != null 
					? value.toString() 
					: "";
		}
		catch (IllegalStateException ex) { // getter not found
			return PATTERN_START + key + PATTERN_END;
		}
	}
	
	private Object formatValue(Object value) {
		if (value != null) {
			if (value instanceof Date) {
				return labelProvider.formatDate((Date) value);
			}
			if (value instanceof BigDecimal) {
				return labelProvider.formatBigDecimal((BigDecimal) value);
			}
			if (value instanceof Boolean) {
				return labelProvider.formatBoolean((Boolean) value);
			}
			if (value instanceof FileObject) {
				return ((FileObject) value).getName();
			}
			if (value instanceof byte[]) {
				return '<' + labelProvider.getLabel("label.image") + '>';
			}
		}
		return value;
	}
	
	protected static InputStream getInputStream(FormPrintout printout) {
		return new ByteArrayInputStream(printout.getContent());
	}
	
	@SuppressWarnings("unchecked")
	protected static List<ValueObject> getNestedObjects(ValueObject valueObject, NestedEntity nestedEntity) {
		return (List<ValueObject>) callGetter(valueObject, nestedEntity.getInternalName());
	}
	
	private static int findPatternStart(String text, int idx) {
		return text.indexOf(PATTERN_START, idx);
	}
	
	private static int findPatternEnd(String text, int start) {
		return text.indexOf(PATTERN_END, start + PATTERN_START.length());
	}
	
	private static String getKey(String text, int start, int end) {
		return text.substring(start + PATTERN_START.length(), end).trim();
	}
	
}