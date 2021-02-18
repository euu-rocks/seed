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

public class BorderLayoutProperties {
	
	private final LayoutAreaProperties north;
	
	private final LayoutAreaProperties east;
	
	private final LayoutAreaProperties center;
	
	private final LayoutAreaProperties west;
	
	private final LayoutAreaProperties south;
	
	public BorderLayoutProperties() {
		north = new LayoutAreaProperties();
		east = new LayoutAreaProperties();
		center = new LayoutAreaProperties();
		west = new LayoutAreaProperties();
		south = new LayoutAreaProperties();
	}
	
	public BorderLayoutProperties(LayoutElement element) {
		Assert.notNull(element, "element is null");
		Assert.state(element.is(LayoutElement.BORDERLAYOUT), "element is no borderlayout");
		
		north = createProperties(BorderLayoutArea.NORTH, element);
		east = createProperties(BorderLayoutArea.EAST, element);
		center = createProperties(BorderLayoutArea.CENTER, element);
		west = createProperties(BorderLayoutArea.WEST, element);
		south = createProperties(BorderLayoutArea.SOUTH, element);
	}
	
	public LayoutAreaProperties[] getLayoutAreaProperties() {
		return new LayoutAreaProperties[] { north, east, center, west, south};
	}
	
	public LayoutAreaProperties getNorth() {
		return north;
	}

	public LayoutAreaProperties getEast() {
		return east;
	}

	public LayoutAreaProperties getCenter() {
		return center;
	}

	public LayoutAreaProperties getWest() {
		return west;
	}

	public LayoutAreaProperties getSouth() {
		return south;
	}
	
	void applyTo(LayoutElement element) {
		Assert.notNull(element, "element is null");
		Assert.state(element.is(LayoutElement.BORDERLAYOUT), "element is no borderlayout");
		
		for (LayoutAreaProperties properties : getLayoutAreaProperties()) {
			if (properties.visible) {
				properties.applyTo(element.getChild(properties.getLayoutArea().name));
			}
		}
	}
	
	public static LayoutAreaProperties createAreaProperties(LayoutElement elemArea) {
		BorderLayoutArea area = BorderLayoutArea.valueOf(elemArea.getName().toUpperCase());
		return new LayoutAreaProperties(area, elemArea);
	}
	
	private LayoutAreaProperties createProperties(BorderLayoutArea area, LayoutElement elemLayout) {
		return new LayoutAreaProperties(area, elemLayout.getChild(area.name));
	}

	public static class LayoutAreaProperties {
		
		private BorderLayoutArea layoutArea;
		
		private String title;
		
		private String size;
		
		private Integer maxsize;
		
		private boolean visible;
		
		private boolean splittable;
		
		private boolean collapsible;
		
		private boolean autoscroll;
		
		LayoutAreaProperties() {
			visible = true;
		}
		
		LayoutAreaProperties(BorderLayoutArea layoutArea, LayoutElement element) {
			this.layoutArea = layoutArea;
			if (element != null) {
				visible = true;
				if (element.hasAttributes()) {
					init(element.getAttributes());
				}
			}
		}

		public BorderLayoutArea getLayoutArea() {
			return layoutArea;
		}

		public boolean isVisible() {
			return visible;
		}

		public void setVisible(boolean visible) {
			this.visible = visible;
		}

		public boolean isSplittable() {
			return splittable;
		}

		public void setSplittable(boolean splittable) {
			this.splittable = splittable;
		}

		public boolean isCollapsible() {
			return collapsible;
		}

		public void setCollapsible(boolean collapsible) {
			this.collapsible = collapsible;
		}

		public boolean isAutoscroll() {
			return autoscroll;
		}

		public void setAutoscroll(boolean autoscroll) {
			this.autoscroll = autoscroll;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getSize() {
			return size;
		}

		public void setSize(String size) {
			this.size = size;
		}

		public Integer getMaxsize() {
			return maxsize;
		}

		public void setMaxsize(Integer maxsize) {
			this.maxsize = maxsize;
		}
		
		void applyTo(LayoutElement element) {
			Assert.notNull(element, "element is null");
			
			element.setOrRemoveAttribute("title", title);
			element.setOrRemoveAttribute("size", size);
			element.setOrRemoveAttribute("maxsize", maxsize);
			element.setOrRemoveAttribute("splittable", splittable);
			element.setOrRemoveAttribute("collapsible", collapsible);
			element.setOrRemoveAttribute("autoscroll", autoscroll);
		}
		
		private void init(Map<String, String> attributeMap) {
			Assert.notNull(attributeMap, "attributeMap is null");
			
			for (Entry<String, String> entry : attributeMap.entrySet()) {
				switch (entry.getKey()) {
					case "title":
						title = entry.getValue();
						break;
					case "size":
						size = entry.getValue();
						break;
					case "maxsize":
						maxsize = Integer.parseInt(entry.getValue());
						break;
					case "splittable":
						splittable = Boolean.parseBoolean(entry.getValue());
						break;
					case "collapsible":
						collapsible = Boolean.parseBoolean(entry.getValue());
						break;
					case "autoscroll":
						autoscroll = Boolean.parseBoolean(entry.getValue());
						break;
				}
			}
			
		}
	}
	
}
