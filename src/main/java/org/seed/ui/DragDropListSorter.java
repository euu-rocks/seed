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

import java.util.Collections;
import java.util.List;

import org.seed.C;
import org.seed.core.data.SystemObject;
import org.seed.core.util.Assert;

public class DragDropListSorter {
	
	private final List<? extends SystemObject> list;

	public DragDropListSorter(List<? extends SystemObject> list) {
		Assert.notNull(list, C.LIST);
		this.list = list;
	}
	
	public void swap(SystemObject base, SystemObject item) {
		Assert.notNull(base, C.BASE);
		Assert.notNull(item, C.ITEM);
		
		Collections.swap(list, list.indexOf(base), list.indexOf(item));
    }
}
