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
package org.seed.core.form.layout;

import java.util.Map;
import java.util.Map.Entry;

import org.seed.C;
import org.seed.core.util.Assert;

public class LayoutElementAttributes {
	
	public static final String A_ALIGN         = "align";
	public static final String A_AUTOPAGING    = "autopaging";
	public static final String A_BORDER        = "border";
	public static final String A_BUTTONVISIBLE = "buttonVisible";
	public static final String A_CHECKED       = "checked";
	public static final String A_CLASS         = "class";
	public static final String A_COLS          = "cols";
	public static final String A_CONTENT       = "content";
	public static final String A_CONTENTTYPE   = "contentType";
	public static final String A_CONTEXT       = "context";
	public static final String A_DISABLED      = "disabled";
	public static final String A_FILENAME      = "fileName";
	public static final String A_HFLEX         = "hflex";
	public static final String A_HEIGHT        = "height";
	public static final String A_ICONSCLASS    = "iconSclass";
	public static final String A_ID            = "id";
	public static final String A_INPLACE       = "inplace";
	public static final String A_INSTANT       = "instant";
	public static final String A_LABEL         = "label";
	public static final String A_MANDATORY     = "mandatory";
	public static final String A_MAXLENGTH     = "maxlength";
	public static final String A_MODEL         = "model";
	public static final String A_MOLD          = "mold";
	public static final String A_NAME          = "name";
	public static final String A_ONCHANGE      = "onChange";
	public static final String A_ONCHANGING    = "onChanging";
	public static final String A_ONCHECK       = "onCheck";
	public static final String A_ONCLICK       = "onClick";
	public static final String A_ONDOUBLECLICK = "onDoubleClick";
	public static final String A_ONSELECT      = "onSelect";
	public static final String A_ORIENT        = "orient";
	public static final String A_PLACEHOLDER   = "placeholder";
	public static final String A_PRE           = "pre";
	public static final String A_READONLY      = "readonly";
	public static final String A_ROWS          = "rows";
	public static final String A_SCLASS        = "sclass";
	public static final String A_SELECTEDITEM  = "selectedItem";
	public static final String A_SIZABLE       = "sizable";
	public static final String A_SIZE          = "size";
	public static final String A_SORT          = "sort";
	public static final String A_STYLE         = "style";
	public static final String A_TEMPLATE      = "template";
	public static final String A_TOOLTIPTEXT   = "tooltiptext";
	public static final String A_TYPE          = "type";
	public static final String A_VALIGN        = "valign";
	public static final String A_VALUE         = "value";
	public static final String A_VAR           = "var";
	public static final String A_VFLEX         = "vflex";
	public static final String A_VISIBLE       = "visible";
	public static final String A_WIDTH         = "width";
	
	public static final String PRE_SUBFORM     = "sub_";
	public static final String PRE_RELATION    = "rel_";
	
	public static final String V_0             = "0";
	public static final String V_1             = "1";
	public static final String V_MIN           = "min";
	public static final String V_RIGHT         = "right";
	public static final String V_TOP           = "top";
	public static final String V_TRUE          = "true";
	
	private Integer columns;
	
	private Integer rows;
	
	private Integer maxlength;
	
	private String hflex;
	
	private String label;
	
	private String placeholder;
	
	private String style;
	
	private String width;
	
	private String height;
	
	private TextfieldType type;
	
	private Alignment align;
	
	private Alignment valign;
	
	private Orientation orient;
	
	private boolean inplace;
	
	public LayoutElementAttributes(LayoutElement element) {
		Assert.notNull(element, C.ELEMENT);
		
		if (element.hasAttributes()) {
			init(element.getAttributes());
		}
	}

	public Integer getColumns() {
		return columns;
	}

	public void setColumns(Integer columns) {
		this.columns = columns;
	}

	public Integer getRows() {
		return rows;
	}

	public void setRows(Integer rows) {
		this.rows = rows;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Integer getMaxlength() {
		return maxlength;
	}

	public void setMaxlength(Integer maxlength) {
		this.maxlength = maxlength;
	}

	public String getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getHflex() {
		return hflex;
	}

	public void setHflex(String hflex) {
		this.hflex = hflex;
	}

	public String getWidth() {
		return width;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public TextfieldType getType() {
		return type;
	}

	public void setType(TextfieldType type) {
		this.type = type;
	}
	
	public Alignment getAlign() {
		return align;
	}

	public void setAlign(Alignment align) {
		this.align = align;
	}

	public Alignment getValign() {
		return valign;
	}

	public void setValign(Alignment valign) {
		this.valign = valign;
	}

	public Orientation getOrient() {
		return orient;
	}

	public void setOrient(Orientation orient) {
		this.orient = orient;
	}

	public boolean isInplace() {
		return inplace;
	}

	public void setInplace(boolean inplace) {
		this.inplace = inplace;
	}

	public void applyTo(LayoutElement element) {
		Assert.notNull(element, C.ELEMENT);
		
		element.setOrRemoveAttribute(A_COLS, columns);
		element.setOrRemoveAttribute(A_ROWS, rows);
		element.setOrRemoveAttribute(A_HFLEX, hflex);
		element.setOrRemoveAttribute(A_LABEL, label);
		element.setOrRemoveAttribute(A_MAXLENGTH, maxlength);
		element.setOrRemoveAttribute(A_ORIENT, orient);
		element.setOrRemoveAttribute(A_PLACEHOLDER, placeholder);
		element.setOrRemoveAttribute(A_STYLE, style);
		element.setOrRemoveAttribute(A_TYPE, type);
		element.setOrRemoveAttribute(A_ALIGN, align);
		element.setOrRemoveAttribute(A_VALIGN, valign);
		element.setOrRemoveAttribute(A_WIDTH, width);
		element.setOrRemoveAttribute(A_HEIGHT, height);
		element.setOrRemoveAttribute(A_INPLACE, inplace);
		
		if (columns != null && hflex != null) {
			element.removeAttribute(A_HFLEX);
		}
	}
	
	private void init(Map<String, String> attributeMap) {
		Assert.notNull(attributeMap, "attributeMap");
		
		for (Entry<String, String> entry : attributeMap.entrySet()) {
			switch (entry.getKey()) {
				case A_COLS:
					columns = Integer.parseInt(entry.getValue());
					break;
					
				case A_ROWS:
					rows = Integer.parseInt(entry.getValue());
					break;
					
				case A_HFLEX:
					hflex = entry.getValue();
					break;
					
				case A_LABEL:
					label = entry.getValue();
					break;
					
				case A_ORIENT:
					orient = Orientation.valueOf(entry.getValue().toUpperCase());
					break;
					
				case A_MAXLENGTH:
					maxlength = Integer.parseInt(entry.getValue());
					break;
					
				case A_PLACEHOLDER:
					placeholder = entry.getValue();
					break;
					
				case A_STYLE:
					style = entry.getValue();
					break;
					
				case A_WIDTH:
					width = entry.getValue();
					break;
					
				case A_HEIGHT:
					height = entry.getValue();
					break;
					
				case A_TYPE:
					type = TextfieldType.valueOf(entry.getValue().toUpperCase());
					break;
					
				case A_ALIGN:
					align = Alignment.valueOf(entry.getValue().toUpperCase());
					break;
					
				case A_VALIGN:
					valign = Alignment.valueOf(entry.getValue().toUpperCase());
					break;
					
				case A_INPLACE:
					inplace = Boolean.parseBoolean(entry.getValue());
					break;
					
				default:
					// do nothing
					break;
			}
		}
	}
	
}
