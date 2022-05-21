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
package org.seed.core.form;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum FormActionType {
					 // first (if default is true)
	// 			default defsel list   detail subfrm tmpl-list	 		tmpl-detail  		icon
	CUSTOM		(false, false, false, true,  true,  Template.SELECT,	null,		  		"z-icon-exclamation"),
	OVERVIEW	(true,  true,  false, true,  false, null,				null,		  		"z-icon-arrow-left"),
	BACKSEARCH	(true,  true,  true,  false, false, Template.SEARCH,	null,		  		"z-icon-arrow-left"),
	SAVE		(false, true,  false, true,  false, null,		 		Template.DIRTY,	  	"z-icon-save"),
	REFRESH		(false, true,  true,  true,  false, null,		 		Template.NOTNEW, 	"z-icon-refresh"),
	SEARCH		(false, true,  true,  true,  false, null,				null,     	  		"z-icon-search"),
	NEWOBJECT	(false, true,  true,  true,  true, 	null,		 		null,     	  		"z-icon-file-o"),
	DETAIL		(false, true,  true,  false, false, Template.SELECT,	null, 		  		"z-icon-edit"),
	DELETE  	(false, true,  true,  true,  true,	Template.SELECT,	Template.NOTNEW,	"z-icon-remove"),
	PRINT		(false, true,  true,  true,  false, Template.PRINT,		Template.PRINT,	    "z-icon-print"),
	STATUS		(true,  false, true,  true,  false, Template.STATUS,	Template.STATUS,	null),
	TRANSFORM	(true,  false, true,  true,  false, Template.TRANSFORM, Template.TRANSFORM, null),
	SELECTCOLS	(true,	false, true,  false, false, null,		 		null,		  		"z-icon-columns"),
	EXPORTLIST	(false, true,  true,  false, false, null,				null,				"z-icon-download");
	
	// if true, action is always present and can't be deseleted 
	public final boolean isDefault;
	
	// action is optional but common and therefore default selected 
	public final boolean isDefaultSelected;
	
	public final boolean isVisibleAtList;
	
	public final boolean isVisibleAtDetail;
	
	public final boolean isVisibleAtSubform;
	
	private final Template listTemplate;
	
	private final Template detailTemplate;
	
	private final String icon;
	
	private FormActionType(boolean isDefault, boolean isDefaultSelected,
						   boolean isVisibleAtList, boolean isVisibleAtDetail, 
						   boolean isVisibleAtSubform, 
						   Template listTemplate, Template detailTemplate, 
						   String icon) {
		
		this.isDefault = isDefault;
		this.isDefaultSelected = isDefaultSelected;
		this.isVisibleAtList = isVisibleAtList;
		this.isVisibleAtDetail = isVisibleAtDetail;
		this.isVisibleAtSubform = isVisibleAtSubform;
		this.listTemplate = listTemplate;
		this.detailTemplate = detailTemplate;
		this.icon = icon;
	}
	
	public String getListTemplate() {
		return listTemplate != null ? listTemplate.getName() : null;
	}

	public String getDetailTemplate() {
		return detailTemplate != null ? detailTemplate.getName() : null;
	}

	public String getIcon() {
		return icon;
	}
	
	// if isDefault is true
	boolean comesFirst() {
		return isDefaultSelected;
	}
	
	static List<FormActionType> defaultActionTypes(boolean isList, boolean comesFirst) {
		return Arrays.stream(values())
					 .filter(type -> (type.isDefault && 
									  ((comesFirst && type.comesFirst()) ||
									  (!comesFirst && !type.comesFirst())) &&
										  ((isList && type.isVisibleAtList) ||
										  (!isList && type.isVisibleAtDetail))))
					 .collect(Collectors.toList());
	}
	
	private enum Template {
		
		DIRTY,
		NOTNEW,
		PRINT,
		SEARCH,
		SELECT,
		STATUS,
		TRANSFORM,
		HISTORY;
		
		private String getName() {
			return name().toLowerCase();
		}
		
	}

}