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

import static org.seed.ui.zk.component.ComponentUtils.*;

import org.seed.Seed;
import org.seed.core.data.SystemObject;

import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

public class SystemFields extends Div {
	
	private static final long serialVersionUID = -7947167674639048365L;
	
	private static final String LABEL_PROGRESS   = "...";
	private static final String LABEL_INCREATION = "label.increation";
	private static final String LABEL_MODIFIEDON = "data.systemfield.modifiedon";
	
	private final Label labelCreated = new Label();
	private final Label labelModified = new Label();
	
	public SystemFields() {
		setClass(CLASS_FOOTER);
		appendChild(labelCreated);
		appendChild(space());
		appendChild(labelModified);
	}

	public void setObject(SystemObject object) {
		if (object != null) {
			// creation info
			if (object.isNew()) {
				labelCreated.setValue(Seed.getLabel(LABEL_INCREATION) + LABEL_PROGRESS);
			}
			else {
				labelCreated.setValue(Seed.getLabel(LABEL_CREATEDON) + LABEL_SUFFIX + formatDate(object.getCreatedOn()) + ' ' +
									  Seed.getLabel(LABEL_BY) + LABEL_SUFFIX + object.getCreatedBy());
				labelCreated.setTooltiptext(formatDateTime(object.getCreatedOn()));
			}
			
			// last modification info
			final boolean isModified = object.getModifiedOn() != null;
			labelModified.setVisible(isModified);
			if (isModified) {
				labelModified.setValue(Seed.getLabel(LABEL_MODIFIEDON) + LABEL_SUFFIX + formatDate(object.getModifiedOn()) + ' ' +
									   Seed.getLabel(LABEL_BY) + LABEL_SUFFIX + object.getModifiedBy());
				labelModified.setTooltiptext(formatDateTime(object.getModifiedOn()));
			}
		}
	}
	
}
