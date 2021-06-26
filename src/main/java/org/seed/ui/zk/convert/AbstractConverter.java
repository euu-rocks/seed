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
package org.seed.ui.zk.convert;

import org.seed.core.form.LabelProvider;
import org.seed.core.util.Assert;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

abstract class AbstractConverter<U, B, C extends Component> 
	implements Converter<U, B, C>  {
	
	private LabelProvider labelProvider;

	AbstractConverter(LabelProvider labelProvider) {
		Assert.notNull(labelProvider, "labelProvider");
		
		this.labelProvider = labelProvider;
	}
	
	protected LabelProvider labelProvider() {
		return labelProvider;
	}
	
	@Override
	public B coerceToBean(U u, C c, BindContext ctx) {
		throw new UnsupportedOperationException("only meant for reading");
	}
	
}
