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
package org.seed.core.config.changelog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import org.seed.C;
import org.seed.InternalException;
import org.seed.core.data.SystemEntity;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;
import org.seed.core.util.UID;

import liquibase.change.Change;
import liquibase.change.core.AddForeignKeyConstraintChange;
import liquibase.change.custom.CustomChangeWrapper;
import liquibase.changelog.ChangeLogChild;
import liquibase.changelog.ChangeSet;
import liquibase.exception.CustomChangeException;
import liquibase.serializer.core.yaml.YamlChangeLogSerializer;

public abstract class AbstractChangeLogBuilder<T extends SystemEntity>
	implements ChangeLogBuilder<T> {
	
	private static class JsonChangeLogSerializer extends YamlChangeLogSerializer {
		
		private static final String[] FILE_EXTENSIONS = new String[] { "json" }; 
		
		private static final String PADDING = "  ";
		
		@Override
	    public <T extends ChangeLogChild> void write(List<T> children, OutputStream out) throws IOException {
	        final Writer writer = new OutputStreamWriter(out, MiscUtils.CHARSET);
	        int i = 0;
	        for (T child : children) {
	            String serialized = serialize(child, true);
	            if (++i < children.size()) {
	                serialized = serialized.replaceFirst("}\\s*$", "},\n");
	            }
	            writer.write(PADDING + serialized.replaceAll("\n", '\n' + PADDING) + '\n');
	        }
	        writer.flush();
	    }

	    @Override
	    public String[] getValidFileExtensions() {
	        return FILE_EXTENSIONS;
	    }
	    
	}
	
	private ChangeSet changeSet;
	
	private ReferenceChangeLog referenceChangeLog;
	
	protected T currentVersionObject;
	
	protected T nextVersionObject;
	
	@Override
	public ChangeLogBuilder<T> setReferenceChangeLog(ReferenceChangeLog referenceChangeLog) {
		this.referenceChangeLog = referenceChangeLog;
		return this;
	}

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
		return build(changeSet);
	}
	
	protected void checkValid() {
		Assert.stateAvailable(currentVersionObject != null || nextVersionObject != null, 
					 		  "current or next version object");
	}
	
	protected boolean isUpdateChange() {
		return currentVersionObject != null && nextVersionObject != null;
	}
	
	protected void addChange(Change change) {
		Assert.notNull(change, "change");
		
		if (referenceChangeLog != null && 
			change instanceof AddForeignKeyConstraintChange) {
			referenceChangeLog.addChange(change);
		}
		else {
			getChangeSet().addChange(change);
		}
	}
	
	protected void addChange(AbstractCustomChange customChange) {
		Assert.notNull(customChange, "customChange");
		try {
			final CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
			changeWrapper.setClass(customChange.getClass().getName());
			changeWrapper.setParam(C.NAME, customChange.getParameterName());
			changeWrapper.setParam(C.VALUE, customChange.getParameterValue());
			getChangeSet().addChange(changeWrapper);
		}
		catch (CustomChangeException ccex) {
			throw new InternalException(ccex);
		}
	}
	
	private ChangeSet getChangeSet() {
		if (changeSet == null) {
			changeSet = createChangeSet();
		}
		return changeSet;
	}
	
	static ChangeLog build(ChangeSet changeSet) {
		return changeSet != null 
				? createChangeLog(changeSet) 
				: null;
	}
	
	static ChangeSet createChangeSet() {
		return new ChangeSet(UID.createUID(), MiscUtils.geUserName(), 
					false, false, null, null, null, false, null, null);
	}
	
	private static ChangeLog createChangeLog(ChangeSet changeSet) {
		final ChangeLog changeLog = new ChangeLog();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			new JsonChangeLogSerializer().write(Collections.singletonList(changeSet), baos);
			changeLog.setChangeSet(MiscUtils.toString(baos.toByteArray()));
			return changeLog;
		} 
		catch (IOException ioex) {
			throw new InternalException(ioex);
		}
	}

}
