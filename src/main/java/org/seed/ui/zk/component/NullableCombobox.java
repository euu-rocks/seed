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
package org.seed.ui.zk.component;

import org.seed.core.data.SystemObject;
import org.seed.core.util.Assert;

import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;

@SuppressWarnings("serial")
public class NullableCombobox extends Combobox {
	
	private boolean nullable = true;
	
	private boolean mandatory;
	
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}
	
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
		if (mandatory) {
			ComponentUtils.setMandatoryStatusStyle(this);
		}
	}
	
	@Override
	public void setReadonly(boolean readonly) {
		super.setReadonly(readonly);
		// if model set before readonly and is nullable -> remove null value
		final ListModel<SystemObject> model = getModel();
		if (readonly && model != null && model.getSize() > 0 && model.getElementAt(0) == null) {
			final ListModelList<SystemObject> newModel = new ListModelList<>(model.getSize() - 1);
			for (int i = 1; i < model.getSize(); i++) {
				newModel.add(model.getElementAt(i));
			}
			super.setModel(newModel);
		}
	}
	
	@Override
	public void setModel(ListModel<?> model) {
		Assert.notNull(model, "model");
		if (nullable && !isReadonly()) {
			final ListModelList<Object> newModel = new ListModelList<>(model.getSize() + 1);
			newModel.add(null);
			for (int i = 0; i < model.getSize(); i++) {
				newModel.add(model.getElementAt(i));
			}
			super.setModel(newModel);
		}
		else {
			super.setModel(model);
		}
	}
	
	@Override
	public void service(AuRequest request, boolean everError) {
		super.service(request, everError);
		if (mandatory && Events.ON_CHANGE.equals(request.getCommand())) {
			ComponentUtils.setMandatoryStatusStyle(this);
		}
	}
	
	@Override
	public void setSelectedItem(Comboitem item) {
		super.setSelectedItem(item);
		if (mandatory) {
			ComponentUtils.setMandatoryStatusStyle(this);
		}
	}
	
	@Override
	public void setRawValue(Object value) {
		super.setRawValue(value);
		if (mandatory) {
			ComponentUtils.setMandatoryStatusStyle(this);
		}
	}
	
}
