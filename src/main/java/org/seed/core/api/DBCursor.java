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
package org.seed.core.api;

import java.util.List;

/**
 * A <code>DBCursor</code> represents a query cursor in the database. 
 * Its purpose is to read in the results of a query as chunks of entity objects.
 * 
 * @author seed-master
 *
 * @param <T> the type of entity objects in the result list
 */
public interface DBCursor<T extends EntityObject> {
	
	/**
	 * Returns the number of all objects in the result list
	 * @return the number of all objects in the result list
	 */
	int getTotalCount();
	
	/**
	 * Returns the number of all chunks into which the entire result was divided
	 * @return the number of all chunks into which the entire result was divided
	 */
	int getChunkCount();
	
	/**
	 * Returns the size of a chunk. The very last chunk may contain fewer objects
	 * @return the size of a chunk
	 */
	int getChunkSize();
	
	/**
	 * Checks if there are more chunks after the current chunk
	 * @return <code>true</code> if there are more chunks after the current chunk
	 */
	boolean hasNextChunk();
	
	/**
	 * Loads the next chunk of entity objects, if any.
	 * You should always call <code>hasNextChunk</code> beforehand.
	 * @return a list of all entity objects in the next chunk
	 * @throws IllegalStateException
	 * 		   If there is no next chunk
	 */
	List<T> loadChunk();
	
}
