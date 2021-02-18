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
package org.seed.ui.zk;

import org.seed.ui.TreeNode;

import org.zkoss.zul.AbstractTreeModel;

public class TreeModel extends AbstractTreeModel<TreeNode> {

	private static final long serialVersionUID = 8554616573929028472L;

	public TreeModel(TreeNode root) {
		super(root);
	}

	@Override
	public boolean isLeaf(TreeNode node) {
		return node.countChildren() == 0;
	}

	@Override
	public TreeNode getChild(TreeNode parent, int index) {
		return parent.getChildAt(index);
	}

	@Override
	public int getChildCount(TreeNode parent) {
		return parent.countChildren();
	}

}
