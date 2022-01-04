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

import org.seed.core.data.QueryCursor;
import org.seed.core.data.SystemObject;
import org.seed.core.util.Assert;

import org.zkoss.zul.AbstractListModel;

@SuppressWarnings("serial")
public abstract class LoadOnDemandListModel<T extends SystemObject> extends AbstractListModel<T> {
	
	private final transient QueryCursor<T> cursor;
	
	private transient List<T> chunk;
	
	private final boolean nullable;
	
	private int chunkIndex = -1;
	
	protected LoadOnDemandListModel(QueryCursor<T> cursor, boolean nullable) {
		Assert.notNull(cursor, "cursor");
		
		this.cursor = cursor;
		this.nullable = nullable;
	}
	
	@Override
	public final T getElementAt(int index) {
		if (nullable) {
			if (index == 0) {
				return null;
			}
			index--;
		}
		final int newChunkIndex = index / cursor.getChunkSize();
		if (chunkIndex != newChunkIndex) {
			chunkIndex = newChunkIndex;
			cursor.setChunkIndex(newChunkIndex);
			chunk = loadChunk(cursor);
		}
		return chunk.get(index % cursor.getChunkSize());
	}
	
	@Override
	public final int getSize() {
		return cursor.getTotalCount() + (nullable ? 1 : 0);
	}
	
	protected abstract List<T> loadChunk(QueryCursor<T> cursor);

}
