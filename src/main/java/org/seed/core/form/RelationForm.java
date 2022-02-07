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

import org.seed.C;
import org.seed.core.entity.EntityRelation;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.util.Assert;

public class RelationForm {
	
	private final EntityRelation relation;
	
	private ValueObject selectedObject;
	
	RelationForm(EntityRelation relation) {
		Assert.notNull(relation, C.RELATION);
		this.relation = relation;
	}
	
	public String getUid() {
		return relation.getUid();
	}

	public EntityRelation getRelation() {
		return relation;
	}

	public ValueObject getSelectedObject() {
		return selectedObject;
	}

	public void setSelectedObject(ValueObject selectedObject) {
		this.selectedObject = selectedObject;
	}

}
