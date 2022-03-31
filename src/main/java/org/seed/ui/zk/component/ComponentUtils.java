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
package org.seed.ui.zk.component;

import java.util.Date;

import org.seed.LabelProvider;
import org.seed.Seed;
import org.seed.core.util.Assert;

import org.springframework.util.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Space;
import org.zkoss.zul.impl.InputElement;

abstract class ComponentUtils {
	
	static final String CLASS_MANDATORY = "alpha-mandatory";
	
	static final String STYLE_MANDATORY = "border:1px solid #FF8888;border-radius:3px";
	
	private static LabelProvider labelProvider;
	
	private ComponentUtils() {}
	
	static String getLabel(String labelKey) {
		return getLabelProvider().getLabel(labelKey);
	}
	
	static String formatDate(Date date) {
		return getLabelProvider().formatDate(date);
	}
	
	static String formatDateTime(Date date) {
		return getLabelProvider().formatDateTime(date);
	}
	
	static void setMandatoryStatus(InputElement inputElement) {
		inputElement.setClass(StringUtils.hasText(inputElement.getText()) ? null : CLASS_MANDATORY);
	}
	
	static void setMandatoryStatusStyle(InputElement inputElement) {
		inputElement.setStyle(StringUtils.hasText(inputElement.getText()) ? null : STYLE_MANDATORY);
	}
	
	static void postOnChangeEvent(Component component, Object data) {
		Events.postEvent(Events.ON_CHANGE, component, data);
	}
	
	static Space space() {
		return new Space();
	}
	
	private static LabelProvider getLabelProvider() {
		if (labelProvider == null) {
			labelProvider = Seed.getBean(LabelProvider.class);
			Assert.stateAvailable(labelProvider, "labelProvider");
		}
		return labelProvider;
	}
	
}
