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
import org.seed.core.data.revision.Revision;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

@SuppressWarnings("serial")
public class RevisionInfo extends Div {
	
	private final Label labelRevision = new Label();
	
	public RevisionInfo() {
		setClass(CLASS_FOOTER);
		appendChild(labelRevision);
	}
	
	public void setRevision(Revision revision) {
		if (revision != null) {
			labelRevision.setValue(Seed.getLabel("label.revision") + LABEL_SUFFIX + revision.getId() + ' ' +
					Seed.getLabel(LABEL_CREATEDON) + LABEL_SUFFIX + formatDate(revision.getRevisionDate()) + ' ' +
					Seed.getLabel(LABEL_BY) + LABEL_SUFFIX + revision.getAuthor());
		}
	}
	
}
