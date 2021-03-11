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
package org.seed.ui.zk;

import java.util.List;

import org.seed.core.data.Cursor;
import org.seed.core.data.SystemObject;

import org.springframework.util.Assert;
import org.zkoss.zul.AbstractListModel;

@SuppressWarnings("serial")
public abstract class LoadOnDemandListModel extends AbstractListModel<SystemObject> {
	
	private final Cursor cursor;
	
	private final boolean nullable;
	
	private List<? extends SystemObject> chunk;
	
	private int chunkIndex = -1;
	
	public LoadOnDemandListModel(Cursor cursor, boolean nullable) {
		Assert.notNull(cursor, "cursor is null");
		
		this.cursor = cursor;
		this.nullable = nullable;
	}
	
	@Override
	public SystemObject getElementAt(int index) {
		if (nullable) {
			if (index == 0) {
				return null;
			}
			index--;
		}
		final int chunkIndex = index / cursor.getChunkSize();
		if (this.chunkIndex != chunkIndex) {
			this.chunkIndex = chunkIndex;
			cursor.setChunkIndex(chunkIndex);
			chunk = loadChunk(cursor);
		}
		return chunk.get(index % cursor.getChunkSize());
	}
	
	@Override
	public int getSize() {
		return cursor.getTotalCount() + (nullable ? 1 : 0);
	}
	
	protected abstract List<? extends SystemObject> loadChunk(Cursor cursor);

}
