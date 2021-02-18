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

import org.seed.core.config.ApplicationContextProvider;
import org.seed.core.data.FileObject;
import org.seed.core.form.LabelProvider;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.image.AImage;
import org.zkoss.zk.ui.Component;

public class ValueConverter implements Converter<Object, Object, Component> {

	@Override
	public Object coerceToUi(Object beanProp, Component component, BindContext ctx) {
		if (beanProp != null) {
			if (beanProp instanceof String) {
				return beanProp;
			}
			if (beanProp instanceof Date) {
				return ApplicationContextProvider.getBean(LabelProvider.class).formatDate((Date) beanProp);
			}
			if (beanProp instanceof BigDecimal) {
				return ApplicationContextProvider.getBean(LabelProvider.class).formatBigDecimal((BigDecimal) beanProp);
			}
			if (beanProp instanceof Boolean) {
				return ApplicationContextProvider.getBean(LabelProvider.class).formatBoolean((Boolean) beanProp);
			}
			if (beanProp instanceof FileObject) {
				return ((FileObject) beanProp).getName();
			}
			if (beanProp instanceof byte[]) {
				try {
					return new AImage(null, (byte[]) beanProp);
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
			return beanProp.toString();
		}
		return null;
	}

	@Override
	public Object coerceToBean(Object compAttr, Component component, BindContext ctx) {
		throw new UnsupportedOperationException("only meant for reading");
	}

}
