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

import org.seed.core.data.Cursor;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityService;
import org.seed.core.task.AbstractSystemJob;
import org.seed.core.task.SystemTask;

public class FullTextIndexJob extends AbstractSystemJob {
	
	private static final int CHUNK_SIZE = 500;
	
	private EntityService entityService;
	
	private FullTextSearch fullTextSearch;
	
	private ValueObjectService valueObjectService;
	
	public FullTextIndexJob() {
		super(SystemTask.FULLTEXTSEARCH_INDEXALL);
	}
	
	@Override
	protected void init() {
		entityService = getBean(EntityService.class);
		fullTextSearch = getBean(FullTextSearch.class);
		valueObjectService = getBean(ValueObjectService.class);
	}
	
	@Override
	protected void execute() {
		if (!fullTextSearch.isAvailable()) {
			logWarning("full text search is not available");
			return;
		}
		
		fullTextSearch.deleteIndex();
		logInfo("index deleted");
		
		int countIndexedObjects = 0;
		for (Entity entity : entityService.findNonGenericEntities()) {
			if (entity.hasFullTextSearchFields() && valueObjectService.existObjects(entity)) {
				logInfo("indexing entity: " + entity.getInternalName());
				
				int idx = 0;
				int chunkIdx = 0;
				final Cursor<ValueObject> cursor = valueObjectService.createCursor(entity, CHUNK_SIZE);
				while (idx < cursor.getTotalCount()) {
					logInfo(" indexing chunk from: " + cursor.getStartIndex() + " (" + cursor.getChunkSize() + ')');
					
					final List<ValueObject> chunk = valueObjectService.loadChunk(cursor);
					if (fullTextSearch.indexChunk(entity, chunk)) {
						countIndexedObjects += chunk.size();
					}
					cursor.setChunkIndex(chunkIdx++);
					idx += chunk.size();
				}
			}
		}
		
		if (countIndexedObjects > 0) {
			logInfo("indexing finished");
		}
		logInfo(countIndexedObjects + " objects indexed");
	}

}
