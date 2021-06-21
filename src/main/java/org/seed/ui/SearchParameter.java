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

import java.util.Map;

import org.seed.core.entity.filter.CriterionOperator;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.util.Assert;

public class SearchParameter {
	
	public final ValueObject searchObject;
	
	public final Map<Long, Map<String, CriterionOperator>> mapOperators;

	public SearchParameter(ValueObject searchObject, Map<Long, Map<String, CriterionOperator>> mapOperators) {
		Assert.notNull(searchObject, "searchObject");
		Assert.notNull(mapOperators, "mapOperators");
		
		this.searchObject = searchObject;
		this.mapOperators = mapOperators;
	}
	
}
