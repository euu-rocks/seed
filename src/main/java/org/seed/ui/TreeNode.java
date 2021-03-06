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
package org.seed.ui;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

public class TreeNode {
	
	public final String label;
	
	public final String viewName;
	
	public final String iconClass;
	
	public final Long formId;
	
	private boolean top;
	
	private boolean link;
	
	private List<TreeNode> children;
	
	public TreeNode(String label, String viewName, String iconClass) {
		this (label, viewName, iconClass, null);
	}
	
	public TreeNode(String label, String viewName, String iconClass, Long formId) {
		Assert.notNull(label, "label is null");
		
		this.label = label;
		this.viewName = viewName;
		this.iconClass = iconClass;
		this.formId = formId;
	}

	public String getLabel() {
		return label;
	}

	public String getViewName() {
		return viewName;
	}

	public String getIconClass() {
		return iconClass;
	}

	public boolean isTop() {
		return top;
	}

	public void setTop(boolean top) {
		this.top = top;
	}

	public boolean isLink() {
		return link;
	}

	public void setLink(boolean link) {
		this.link = link;
	}

	public List<TreeNode> getChildren() {
		return children;
	}
	
	public List<TreeNode> getChildrenWithParent() {
		if (children != null) {
			final List<TreeNode> result = new ArrayList<>();
			result.add(new TreeNode(label, viewName, iconClass, formId));
			result.addAll(children);
			return result;
		}
		return null;
	}

	public int countChildren() {
		return children != null ? children.size() : 0;
	}
	
	public TreeNode getChildAt(int index) {
		Assert.state(countChildren() > 0, "node has no children");
		
		return children.get(index);
	}

	public TreeNode addChild(TreeNode child) {
		Assert.notNull(child, "child is null");
		
		if (children == null) {
			children = new ArrayList<>();
		}
		children.add(child);
		return child;
	}
	
}
