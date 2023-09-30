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

import static org.seed.core.util.CollectionUtils.subList;

import java.util.List;

public enum FormActionType {
					 // comes first (if default is true)
	// 			default defsel visibility    		  tmpl-list	 		 tmpl-detail  		 icon
	CUSTOM		(false, false, detail()|sub(),		  Template.SELECT,	 null,		  		 "z-icon-exclamation"),
	OVERVIEW	(true,  true,  detail(),			  null,				 null,		  		 "z-icon-arrow-left"),
	BACKSEARCH	(true,  true,  list(),				  Template.SEARCH,	 null,		  		 "z-icon-arrow-left"),
	SAVE		(false, true,  detail(), 			  null,				 Template.DIRTY,	 "z-icon-save"),
	REFRESH		(false, true,  list()|detail(), 	  null,		 		 Template.NOTNEW, 	 "z-icon-refresh"),
	SEARCH		(false, true,  list()|detail(), 	  null,				 null,     	  		 "z-icon-search"),
	NEWOBJECT	(false, true,  list()|detail()|sub(), null,		 		 null,     	  		 "z-icon-file-o"),
	DETAIL		(false, true,  list(), 				  Template.SELECT,	 null, 		  		 "z-icon-edit"),
	DELETE  	(false, true,  list()|detail()|sub(), Template.SELECT,	 Template.NOTNEW,	 "z-icon-remove"),
	PRINT		(false, true,  list()|detail(),		  Template.PRINT,	 Template.PRINT,	 "z-icon-print"),
	STATUS		(true,  false, list()|detail(),		  Template.STATUS,	 Template.STATUS,    null),
	TRANSFORM	(true,  false, list()|detail(),		  Template.TRANSFORM,Template.TRANSFORM, null),
	SELECTCOLS	(true,	false, list(), 				  null,		 		 null,				 "z-icon-columns"),
	EXPORTLIST	(false, true,  list(),				  null,				 null,				 "z-icon-download");
	
	private static final int VISIBILITY_LIST    = 0B001;
	private static final int VISIBILITY_DETAIL  = 0B010;
	private static final int VISIBILITY_SUBFORM = 0B100;
	
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
	
	private FormActionType(boolean isDefault, boolean isDefaultSelected, int visibility, 
						   Template listTemplate, Template detailTemplate, String icon) {
		this.isDefault = isDefault;
		this.isDefaultSelected = isDefaultSelected;
		this.isVisibleAtList = (visibility & VISIBILITY_LIST) != 0;
		this.isVisibleAtDetail = (visibility & VISIBILITY_DETAIL) != 0;
		this.isVisibleAtSubform = (visibility & VISIBILITY_SUBFORM) != 0;
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
	
	boolean comesFirst() {
		return isDefault && isDefaultSelected;
	}
	
	static List<FormActionType> defaultActionTypes(boolean isList, boolean comesFirst) {
		return subList(values(), type -> (type.isDefault && 
										  ((comesFirst && type.comesFirst()) ||
										  (!comesFirst && !type.comesFirst())) &&
										  ((isList && type.isVisibleAtList) ||
										  (!isList && type.isVisibleAtDetail))));
	}
	
	private static int list() {
		return VISIBILITY_LIST;
	}
	
	private static int detail() {
		return VISIBILITY_DETAIL;
	}
	
	private static int sub() {
		return VISIBILITY_SUBFORM;
	}
	
	private enum Template {
		
		DIRTY,
		NOTNEW,
		PRINT,
		SEARCH,
		SELECT,
		STATUS,
		TRANSFORM;
		
		private String getName() {
			return name().toLowerCase();
		}
		
	}

}