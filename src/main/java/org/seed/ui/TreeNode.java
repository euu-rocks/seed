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

import static org.seed.core.util.CollectionUtils.firstMatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.seed.C;
import org.seed.core.util.Assert;
import org.seed.core.util.NameUtils;

public final class TreeNode {
	
	public final String label;
	
	public final String viewName;
	
	public final String iconClass;
	
	public final Long formId;
	
	private TreeNode parent;
	
	private String purpose;
	
	private boolean top;
	
	private boolean link;
	
	private List<TreeNode> children;
	
	public TreeNode(String label) {
		this(label, null, null, null);
	}
	
	public TreeNode(String label, @Nullable String viewName, @Nullable String iconClass) {
		this(label, viewName, iconClass, null);
	}
	
	TreeNode(String label, @Nullable String viewName, @Nullable String iconClass, @Nullable Long formId) {
		Assert.notNull(label, C.LABEL);
		
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

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
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
	
	public String getTestClass() {
		StringBuilder buf = new StringBuilder();
		TreeNode parentNode = parent;
		while (parentNode != null && 
			   !"root".equals(parentNode.label)) {
			buf.insert(0, '-');
			buf.insert(0, NameUtils.getInternalName(parentNode.label));
			parentNode = parentNode.parent;
		}
		buf.append(NameUtils.getInternalName(label)).append("-navi");
		return buf.toString().replace('_','-').toLowerCase();
	}

	public List<TreeNode> getChildren() {
		return children;
	}
	
	public List<TreeNode> getParentWithChildren() {
		if (children != null) {
			final List<TreeNode> result = new ArrayList<>(children.size() + 1);
			result.add(new TreeNode(label, viewName, iconClass, formId));
			result.addAll(children);
			return result;
		}
		return Collections.emptyList();
	}

	public int countChildren() {
		return children != null ? children.size() : 0;
	}
	
	public TreeNode getChildAt(int index) {
		Assert.state(index >= 0 && countChildren() > index, "illegal index: " + index);
		
		return children.get(index);
	}

	public TreeNode addChild(TreeNode child) {
		Assert.notNull(child, "child");
		
		if (children == null) {
			children = new ArrayList<>();
		}
		child.parent = this;
		children.add(child);
		return child;
	}
	
	TreeNode findChild(String label) {
		Assert.notNull(label, C.LABEL);
		
		return firstMatch(children, node -> label.equalsIgnoreCase(node.getLabel()));
	}
	
}
