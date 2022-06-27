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

import java.util.List;

import org.seed.core.api.DBCursor;
import org.seed.core.data.QueryCursor;
import org.seed.core.util.Assert;

public abstract class ValueObjectCursor<T extends ValueObject> implements DBCursor<T> {
	
	private final QueryCursor<T> queryCursor;
	
	private final int totalChunkCount;
	
	private int chunkIndex = 0;
	
	public ValueObjectCursor(QueryCursor<T> queryCursor) {
		Assert.notNull(queryCursor, "query cursor");
		
		this.queryCursor = queryCursor;
		int chunkCount = getTotalCount() / getChunkSize();
		if (chunkCount * getChunkSize() < getTotalCount()) {
			chunkCount++;
		}
		totalChunkCount = chunkCount;
	}
	
	protected abstract List<T> loadChunk(QueryCursor<T> cursor);
	
	@Override
	public int getTotalCount() {
		return queryCursor.getTotalCount();
	}
	
	@Override
	public int getChunkSize() {
		return queryCursor.getChunkSize();
	}
	
	@Override
	public boolean hasNextChunk() {
		return chunkIndex < totalChunkCount;
	}
	
	@Override
	public int getChunkCount() {
		return totalChunkCount;
	}
	
	@Override
	public List<T> loadChunk() {
		queryCursor.setChunkIndex(chunkIndex++);
		return loadChunk(queryCursor);
	}
	
}
