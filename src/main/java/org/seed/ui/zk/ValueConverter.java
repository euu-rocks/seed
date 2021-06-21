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
package org.seed.ui.zk;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import org.seed.InternalException;
import org.seed.core.data.FileObject;
import org.seed.core.form.LabelProvider;

import org.zkoss.bind.BindContext;
import org.zkoss.image.AImage;
import org.zkoss.zk.ui.Component;

public class ValueConverter extends AbstractConverter<Object, Object, Component> {
	
	public ValueConverter(LabelProvider labelProvider) {
		super(labelProvider);
	}
	
	@Override
	public Object coerceToUi(Object beanProp, Component component, BindContext ctx) {
		if (beanProp != null) {
			if (beanProp instanceof String) {
				return beanProp;
			}
			if (beanProp instanceof Date) {
				return labelProvider().formatDate((Date) beanProp);
			}
			if (beanProp instanceof BigDecimal) {
				return labelProvider().formatBigDecimal((BigDecimal) beanProp);
			}
			if (beanProp instanceof Boolean) {
				return labelProvider().formatBoolean((Boolean) beanProp);
			}
			if (beanProp instanceof FileObject) {
				return ((FileObject) beanProp).getName();
			}
			if (beanProp instanceof byte[]) {
				return createImage((byte[]) beanProp);
			}
			return beanProp.toString();
		}
		return null;
	}
	
	private static AImage createImage(byte[] bytes) {
		try {
			return new AImage(null, bytes);
		} 
		catch (IOException ex) {
			throw new InternalException(ex);
		}
	}

}
