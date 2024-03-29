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
package org.seed.core.entity.value;

import org.seed.core.api.EntityObject;
import org.seed.core.data.SystemObject;
import org.seed.core.entity.EntityStatus;

/**
 * A ValueObject represents a SystemEntity that was generated 
 * based on custom entity metadata definition.
 * It provides the ID of the entity on which this object is based 
 * and the current entity status value.
 * 
 * @author seed-master
 *
 */
public interface ValueObject extends SystemObject, EntityObject {
	
	Long getEntityId();
	
	EntityStatus getEntityStatus();
	
}
