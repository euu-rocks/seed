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
package org.seed.ui.zk.convert;

import org.seed.InternalException;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.image.AImage;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Image;

public class ImageConverter implements Converter<AImage, byte[], Image> {

	@Override
	public AImage coerceToUi(byte[] beanProp, Image component, BindContext ctx) {
		try {
			byte[] bytes = beanProp;
			if (bytes == null || bytes.length == 0) {
				bytes = ImageUtils.createPlaceholderImage(Labels.getLabel("label.uploadimage"), 
						component.getWidth() != null ? Integer.parseInt(component.getWidth()) : 100, 
						component.getHeight() != null ?	Integer.parseInt(component.getHeight()) : 75);
			}
			return new AImage(null, bytes);
        }
		catch (Exception ex) {
			throw new InternalException(ex);
		}
	}

	@Override
	public byte[] coerceToBean(AImage compAttr, Image component, BindContext ctx) {
		return compAttr.getByteData();
	}
	
}
