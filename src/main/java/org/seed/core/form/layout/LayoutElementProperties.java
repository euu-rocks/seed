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

import org.springframework.util.Assert;

public class LayoutElementProperties {
	
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
	
	public LayoutElementProperties(LayoutElement element) {
		Assert.notNull(element, "element is null");
		
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
		Assert.notNull(element, "element is null");
		
		element.setOrRemoveAttribute("cols", columns);
		element.setOrRemoveAttribute("rows", rows);
		element.setOrRemoveAttribute("hflex", hflex);
		element.setOrRemoveAttribute("label", label);
		element.setOrRemoveAttribute("orient", orient);
		element.setOrRemoveAttribute("maxlength", maxlength);
		element.setOrRemoveAttribute("placeholder", placeholder);
		element.setOrRemoveAttribute("style", style);
		element.setOrRemoveAttribute("type", type);
		element.setOrRemoveAttribute("align", align);
		element.setOrRemoveAttribute("valign", valign);
		element.setOrRemoveAttribute("width", width);
		element.setOrRemoveAttribute("height", height);
		element.setOrRemoveAttribute("inplace", inplace);
		
		if (columns != null && hflex != null) {
			element.removeAttribute("hflex");
		}
	}
	
	private void init(Map<String, String> attributeMap) {
		Assert.notNull(attributeMap, "attributeMap is null");
		
		for (Entry<String, String> entry : attributeMap.entrySet()) {
			switch (entry.getKey()) {
				case "cols":
					columns = Integer.parseInt(entry.getValue());
					break;
					
				case "rows":
					rows = Integer.parseInt(entry.getValue());
					break;
					
				case "hflex":
					hflex = entry.getValue();
					break;
					
				case "label":
					label = entry.getValue();
					break;
					
				case "orient":
					orient = Orientation.valueOf(entry.getValue().toUpperCase());
					break;
					
				case "maxlength":
					maxlength = Integer.parseInt(entry.getValue());
					break;
					
				case "placeholder":
					placeholder = entry.getValue();
					break;
					
				case "style":
					style = entry.getValue();
					break;
					
				case "width":
					width = entry.getValue();
					break;
					
				case "height":
					height = entry.getValue();
					break;
					
				case "type":
					type = TextfieldType.valueOf(entry.getValue().toUpperCase());
					break;
					
				case "align":
					align = Alignment.valueOf(entry.getValue().toUpperCase());
					break;
					
				case "valign":
					valign = Alignment.valueOf(entry.getValue().toUpperCase());
					break;
					
				case "inplace":
					inplace = Boolean.parseBoolean(entry.getValue());
					break;
			}
		}
		
	}
	
}
