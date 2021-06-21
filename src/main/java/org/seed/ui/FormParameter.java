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

import org.seed.C;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.form.Form;
import org.seed.core.util.Assert;

public final class FormParameter {
	
	public final Form form;
	
	public final Long objectId;
	
	public final ValueObject object;
	
	public FormParameter(Form form) {
		this(form, null,null);
	}
	
	public FormParameter(Form form, Long objectId) {
		this(form, objectId, null);
	}
	
	public FormParameter(Form form, ValueObject object) {
		this(form, null, object);
	}
	
	public FormParameter(Form form, Long objectId, ValueObject object) {
		Assert.notNull(form, C.FORM);
		
		this.form = form;
		this.objectId = objectId;
		this.object = object;
	}
	
	public Long getObjectId() {
		return object != null ? object.getId() : objectId;
	}
	
}
