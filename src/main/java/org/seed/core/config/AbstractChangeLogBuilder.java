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
package org.seed.core.config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.seed.InternalException;
import org.seed.core.data.SystemEntity;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;
import org.seed.core.util.UID;

import liquibase.change.Change;
import liquibase.changelog.ChangeLogChild;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.serializer.core.yaml.YamlChangeLogSerializer;

public abstract class AbstractChangeLogBuilder<T extends SystemEntity>
	implements ChangeLogBuilder<T> {
	
	private ChangeSet changeSet;
	
	protected T currentVersionObject;
	
	protected T nextVersionObject;
	
	@Override
	public ChangeLogBuilder<T> setCurrentVersionObject(T currentVersionObject) {
		this.currentVersionObject = currentVersionObject;
		return this;
	}
	
	@Override
	public ChangeLogBuilder<T> setNextVersionObject(T nextVersionObject) {
		this.nextVersionObject = nextVersionObject;
		return this;
	}
	
	@Override
	public ChangeLog build() {
		if (changeSet != null) {
			final ChangeLog changeLog = new ChangeLog();
			changeLog.setChangeSet(toJson(changeSet));
			return changeLog;
		}
		return null;
	}
	
	protected void addChange(Change change) {
		Assert.notNull(change, "change");
		
		if (changeSet == null) {
			changeSet = new ChangeSet(UID.createUID(), MiscUtils.geUserName(), 
									  false, false, "", null, null, 
									  true, null, new DatabaseChangeLog());
		}
		changeSet.addChange(change);
	}
	
	private static String toJson(ChangeSet changeSet) {
		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			new JsonChangeLogSerializer().write(Collections.singletonList(changeSet), baos);
			return baos.toString(StandardCharsets.UTF_8);
		} 
		catch (IOException ioex) {
			throw new InternalException(ioex);
		}
	}
	
	private static class JsonChangeLogSerializer extends YamlChangeLogSerializer {
		
		private static final String[] FILE_EXTENSIONS = new String[] { "json" }; 
		
		private static final String PADDING = "  ";
		
		@Override
	    public <T extends ChangeLogChild> void write(List<T> children, OutputStream out) throws IOException {
	        final Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
	        int i = 0;
	        for (T child : children) {
	            String serialized = serialize(child, true);
	            if (++i < children.size()) {
	                serialized = serialized.replaceFirst("}\\s*$", "},\n");
	            }
	            writer.write(PADDING + serialized.replaceAll("\n", '\n' + PADDING));
	            writer.write('\n');
	        }
	        writer.flush();
	    }

	    @Override
	    public String[] getValidFileExtensions() {
	        return FILE_EXTENSIONS;
	    }
	    
	}
	
}
