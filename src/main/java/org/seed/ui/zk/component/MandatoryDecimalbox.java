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

import org.seed.C;

import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Decimalbox;

public class MandatoryDecimalbox extends Decimalbox {

	private static final long serialVersionUID = 5458565181103966855L;
	
	private boolean mandatory;
	
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
		if (mandatory) {
			ComponentUtils.setMandatoryStatus(this);
		}
	}
	
	@Override
	public void service(AuRequest request, boolean everError) {
		super.service(request, everError);
		if (mandatory && Events.ON_CHANGING.equals(request.getCommand())) {
			ComponentUtils.setMandatoryStatus(this, (String) request.getData().get(C.VALUE));
		}
	}
	
	@Override
	public void setRawValue(Object value) {
		super.setRawValue(value);
		if (mandatory) {
			ComponentUtils.setMandatoryStatus(this);
		}
	}
}
