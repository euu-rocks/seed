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

import org.seed.Seed;

import org.springframework.util.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Space;
import org.zkoss.zul.impl.InputElement;

abstract class ComponentUtils {
	
	static final String STYLE_MANDATORY = "border:1px solid #FF8888;border-radius:3px";
	
	static final String CLASS_MANDATORY = "alpha-mandatory";
	static final String CLASS_FOOTER	= "alpha-status-footer";
	
	static final String ICON_CIRCLE 	= "z-icon-circle alpha-icon-lg";
	
	static final String STYLE_INFO 		= "color:green;cursor:default"; 
	static final String STYLE_WARNIUNG 	= "color:orange;cursor:default";
	static final String STYLE_ERROR 	= "color:red;cursor:default";
	static final String STYLE_UNDEFINED = "color:lightgrey;cursor:default";
	
	static final String LABEL_SUFFIX	= ": ";
	static final String LABEL_CREATEDON = "data.systemfield.createdon";
	static final String LABEL_BY		= "label.by";
	
	private ComponentUtils() {}
	
	static String formatDate(Date date) {
		return Seed.getLabelProvider().formatDate(date);
	}
	
	static String formatDateTime(Date date) {
		return Seed.getLabelProvider().formatDateTime(date);
	}
	
	static void setMandatoryStatus(InputElement inputElement) {
		setMandatoryStatus(inputElement, inputElement.getText());
	}
	
	static void setMandatoryStatus(InputElement inputElement, String value) {
		inputElement.setClass(StringUtils.hasText(value) ? null : CLASS_MANDATORY);
	}
	
	static void setMandatoryStatusStyle(InputElement inputElement) {
		setMandatoryStatusStyle(inputElement, inputElement.getText());
	}
	
	static void setMandatoryStatusStyle(InputElement inputElement, String value) {
		inputElement.setStyle(StringUtils.hasText(value) ? null : STYLE_MANDATORY);
	}
	
	static void postOnChangeEvent(Component component, Object data) {
		Events.postEvent(Events.ON_CHANGE, component, data);
	}
	
	static Space space() {
		return new Space();
	}
	
}
