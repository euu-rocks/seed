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

import javax.persistence.criteria.CriteriaQuery;

import org.seed.core.util.Tupel;

import org.springframework.util.Assert;

public final class Cursor {
	
	private final List<Tupel<Long, Long>> fullTextResult;
	
	private final CriteriaQuery<?> query;
	
	private final String hqlQuery;
	
	private final int totalCount;
	
	private final int chunkSize;
	
	private int startIndex;
	
	public Cursor(List<Tupel<Long, Long>> fullTextResult, int chunkSize) {
		Assert.notNull(fullTextResult, "fullTextResult is null");
		Assert.state(chunkSize > 0, "illegal chunk size " + chunkSize);
		
		this.query = null;
		this.hqlQuery = null;
		this.fullTextResult = fullTextResult;
		this.totalCount = fullTextResult.size();
		this.chunkSize = chunkSize;
	}

	public Cursor(String hqlQuery, int totalCount, int chunkSize) {
		Assert.notNull(hqlQuery, "hqlQuery is null");
		Assert.state(chunkSize > 0, "illegal chunk size " + chunkSize);
		
		this.query = null;
		this.fullTextResult = null;
		this.hqlQuery = hqlQuery;
		this.totalCount = totalCount;
		this.chunkSize = chunkSize;
	}
	
	public Cursor(CriteriaQuery<?> query, int totalCount, int chunkSize) {
		Assert.notNull(query, "query is null");
		Assert.state(chunkSize > 0, "illegal chunk size " + chunkSize);
		
		this.hqlQuery = null;
		this.fullTextResult = null;
		this.query = query;
		this.totalCount = totalCount;
		this.chunkSize = chunkSize;
	}
	
	public boolean isFullTextSearch() {
		return fullTextResult != null;
	}
	
	public Tupel<Long, Long> getFullTextResult(int index) {
		Assert.state(isFullTextSearch(), "not full-text search");
		Assert.state(index >= 0 && index < fullTextResult.size(), "illegal full-text result index: " + index);
		
		return fullTextResult.get(index);
	}

	public CriteriaQuery<?> getQuery() {
		return query;
	}

	public String getHqlQuery() {
		return hqlQuery;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public int getChunkSize() {
		return chunkSize;
	}

	public int getStartIndex() {
		return startIndex;
	}
	
	public void setChunkIndex(int chunkIndex) {
		Assert.state(chunkIndex >= 0, "illegal chunk index: " + chunkIndex);
		
		startIndex = chunkIndex * chunkSize;
		Assert.state(startIndex < totalCount, "chunk index too big: " + chunkIndex);
	}
	
}
