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

import org.seed.LabelProvider;
import org.seed.Seed;

public abstract class Converters {
	
	public static final StringConverter STRING_CONVERTER = new StringConverter();
	
	public static final ImageConverter IMAGE_CONVERTER = new ImageConverter();
	
	public static final FileIconConverter FILEICON_CONVERTER = new FileIconConverter();
	
	private static LabelProvider labelProvider;
	
	private static ValueConverter valueConverter;
	
	private static DateTimeConverter dateTimeConverter;
	
	private static TimeConverter timeConverter;
	
	private Converters() {}
	
	public static ValueConverter getValueConverter() {
		if (valueConverter == null) {
			valueConverter = new ValueConverter(getLabelProvider());
		}
		return valueConverter;
	}
	
	public static DateTimeConverter getDateTimeConverter() {
		if (dateTimeConverter == null) {
			dateTimeConverter = new DateTimeConverter(getLabelProvider());
		}
		return dateTimeConverter;
	}
	
	public static TimeConverter getTimeConverter() {
		if (timeConverter == null) {
			timeConverter = new TimeConverter(getLabelProvider());
		}
		return timeConverter;
	}
	
	private static LabelProvider getLabelProvider() {
		if (labelProvider == null) {
			labelProvider = Seed.getBean(LabelProvider.class);
		}
		return labelProvider;
	}
	
}
