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

import java.util.List;

import org.seed.core.application.ApplicationEntityService;
import org.seed.core.entity.Entity;

public interface TransformerService extends ApplicationEntityService<Transformer> {
	
	Transformer getTransformerByName(Entity sourceEntity, Entity targetEntity, String name);
	
	TransformerFunction createFunction(Transformer transformer);
	
	List<Transformer> findTransformers(Entity sourceEntity);
	
	List<Transformer> findTransformers(Entity sourceEntity, Entity targetEntity);
	
	List<TransformerElement> getMainObjectElements(Transformer transformer);
	
	List<NestedTransformer> getNestedTransformers(Transformer transformer);
	
	List<TransformerPermission> getAvailablePermissions(Transformer transformer);
	
	List<TransformerStatus> getAvailableStatus(Transformer transformer);
	
	void adjustElements(Transformer transformer, List<TransformerElement> elements, List<NestedTransformer> nesteds);
	
	boolean autoMatchFields(Transformer transformer, List<TransformerElement> elements);
	
	boolean autoMatchFields(NestedTransformer nestedTransformer);
	
}
