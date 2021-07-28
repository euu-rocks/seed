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
package org.seed.core.entity.autonum;

import javax.annotation.Nullable;

import org.hibernate.Session;

import org.seed.core.data.SystemEntityService;
import org.seed.core.entity.EntityField;

public interface AutonumberService extends SystemEntityService<Autonumber> {
	
	String getNextValue(EntityField entityField, @Nullable Session session);
	
	void deleteAutonumber(EntityField entityField, Session session);
	
}
