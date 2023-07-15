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
package org.seed.core.data;

import java.util.Date;

import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.hibernate.event.spi.SaveOrUpdateEventListener;

import org.seed.C;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;

@SuppressWarnings("serial")
public class SystemObjectEventListener implements SaveOrUpdateEventListener {

	@Override
	public void onSaveOrUpdate(SaveOrUpdateEvent event) {
		final var object = (AbstractSystemObject) 
				(event.getEntity() != null
					? event.getEntity()
					: event.getObject());
		Assert.stateAvailable(object, C.OBJECT);
		
		object.setOrderIndexes();
		if (object.getCreatedOn() == null) {
			object.setCreatedOn(new Date());
			object.setCreatedBy(MiscUtils.getUserName());
		}
		else {
			object.setModifiedOn(new Date());
			object.setModifiedBy(MiscUtils.getUserName());
		}
	}

}
