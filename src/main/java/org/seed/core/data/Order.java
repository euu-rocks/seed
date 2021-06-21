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

import java.util.List;

public interface Order {
	
	int getOrder();
	
	void setOrder(int order);
	
	static void setOrderIndexes(List<? extends Order> list) {
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				list.get(i).setOrder(i + 1);
			}
		}
	}
	
	static void sort(List<? extends Order> list) {
		if (list != null) {
			list.sort((Order order1, Order order2) -> Integer.compare(order1.getOrder(), 
																	  order2.getOrder()));
		}
	}
	
}
