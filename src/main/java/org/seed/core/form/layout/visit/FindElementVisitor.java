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
package org.seed.core.form.layout.visit;

import org.seed.core.form.layout.LayoutElement;
import org.seed.core.form.layout.LayoutVisitor;
import org.seed.core.util.Assert;

public class FindElementVisitor implements LayoutVisitor {
	
	private final String contextId;
	
	private LayoutElement element;
	
	public FindElementVisitor(String contextId) {
		Assert.notNull(contextId, "contextId");
		this.contextId = contextId;
	}

	public LayoutElement getElement() {
		return element;
	}

	@Override
	public void visit(LayoutElement layoutElement) {
		if (element == null && 
			contextId.equals(layoutElement.getContext())) {
			element = layoutElement;
		}
	}

}
