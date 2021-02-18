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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.seed.core.data.SystemObject;

import org.springframework.util.Assert;

public class DragDropListManager {
	
	private final List<SystemObject>[] lists;
	
	public DragDropListManager() {
		this(2);
	}
	
	@SuppressWarnings("unchecked")
	public DragDropListManager(int numLists) {
		Assert.state(numLists > 1, "illegal numLists: " + numLists);
		
		lists = new List[numLists];
		for (int i = 0; i < numLists; i++) {
			lists[i] = new ArrayList<>();
		}
	}
	
	public List<SystemObject> getList(int listNum) {
		Assert.state(listNum >= 0 && listNum < lists.length, "illegal listNum: " + listNum);
		
		return lists[listNum];
    }
	
	public void drop(SystemObject item, int listNum) {
		final int curListNum = getListNum(item);
		if (listNum != curListNum) { // ignore drop to same list
			lists[curListNum].remove(item);
			getList(listNum).add(item);
		}
    }
	
	public void insert(SystemObject base, SystemObject item, int listNum) {
		Assert.notNull(base, "base is null");
		
		final List<SystemObject> list = getList(listNum);
		final int curListNum = getListNum(item);
		if (curListNum == listNum) {
			Collections.swap(list, list.indexOf(base), list.indexOf(item));
		}
		else {
    		lists[curListNum].remove(item);
    		list.add(list.indexOf(base), item);
    	}
    }
	
	public void selectAll() {
		Assert.state(lists.length == 2, "unsupported if listNum != 2");
		
		lists[1].addAll(lists[0]);
		lists[0].clear();
	}
	
	private int getListNum(SystemObject item) { 
		Assert.notNull(item, "item is null");
		
		for (int i = 0; i < lists.length; i++) {
    		if (lists[i].contains(item)) {
    			return i;
    		}
		}
    	throw new IllegalStateException("unknown item: " + item);
    }
	
}
