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
package org.seed.core.entity.transform;

import java.util.ArrayList;
import java.util.List;

import org.seed.core.entity.EntityField;
import org.seed.core.entity.NestedEntity;

import org.springframework.util.Assert;

public class NestedTransformer {
	
	private NestedEntity sourceNested;
	
	private NestedEntity targetNested;
	
	private final List<TransformerElement> elements = new ArrayList<>();
	
	public NestedTransformer() {}
	
	public NestedTransformer(NestedEntity sourceNested, NestedEntity targetNested) {
		Assert.notNull(sourceNested, "sourceNested is null");
		Assert.notNull(sourceNested, "targetNested is null");
		
		this.sourceNested = sourceNested;
		this.targetNested = targetNested;
	}
	
	public String getName() {
		return sourceNested.getName() + " -> " + targetNested.getName();
 	}
	
	public NestedEntity getSourceNested() {
		return sourceNested;
	}

	public void setSourceNested(NestedEntity sourceNested) {
		this.sourceNested = sourceNested;
	}

	public NestedEntity getTargetNested() {
		return targetNested;
	}

	public void setTargetNested(NestedEntity targetNested) {
		this.targetNested = targetNested;
	}
	
	public boolean containsElement(EntityField sourceField, EntityField targetField) {
		Assert.notNull(sourceField, "sourceField is null");
		Assert.notNull(targetField, "targetField is null");
		
		if (elements != null) {
			for (TransformerElement element : elements) {
				if (element.getSourceField().equals(sourceField) &&
					element.getTargetField().equals(targetField)) {
					return true;
				}
			}
		}
		return false;
	}

	public List<TransformerElement> getElements() {
		return elements;
	}

	public void addElement(TransformerElement element) {
		Assert.notNull(element, "element is null");
		
		elements.add(element);
	}
	
	public void removeElement(TransformerElement element) {
		Assert.notNull(element, "element is null");
		
		elements.remove(element);
	}
	
}
