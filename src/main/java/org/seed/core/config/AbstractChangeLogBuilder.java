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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQL81Dialect;

import org.seed.core.util.MiscUtils;
import org.seed.core.util.UID;

import liquibase.changelog.ChangeLogChild;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.serializer.core.yaml.YamlChangeLogSerializer;
import liquibase.util.StringUtils;

public abstract class AbstractChangeLogBuilder {
	
	private final DatabaseChangeLog databaseChangeLog = new DatabaseChangeLog();
	
	private final List<ChangeSet> changeSets = new ArrayList<>();
	
	private final String changeSetBaseId = UID.createUID();
	
	private Dialect dialect;
	
	private Limits limits;
	
	private int changeSetCounter;
	
	protected List<ChangeLog> build() {
		final List<ChangeLog> result = new ArrayList<>(changeSets.size());
		for (ChangeSet changeSet : changeSets) {
			final ChangeLog changeLog = new ChangeLog();
			changeLog.setChangeSet(toJson(changeSet));
			result.add(changeLog);
		}
		return result;
	}
	
	protected boolean isPostgres() {
		return getDialect() instanceof PostgreSQL81Dialect;
	}
	
	protected ChangeSet createChangeSet() {
		final ChangeSet changeSet = new ChangeSet(changeSetBaseId + String.valueOf(++changeSetCounter), 
												  MiscUtils.geUserName(), false, false, "", null, null, 
												  true, null, databaseChangeLog);
		changeSets.add(changeSet);
		return changeSet;
	}
	
	protected int getLimit(String limitName) {
		if (limits == null) {
			limits = ApplicationContextProvider.getBean(Limits.class);
		}
		return limits.getLimit(limitName);
	}
	
	protected Dialect getDialect() {
		if (dialect == null) {
			dialect = ApplicationContextProvider.getBean(SessionFactoryProvider.class).getDialect();
		}
		return dialect;
	}
	
	private static String toJson(ChangeSet changeSet) {
		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			new JsonChangeLogSerializer().write(Collections.singletonList(changeSet), baos);
			return baos.toString(StandardCharsets.UTF_8);
		} 
		catch (IOException ioex) {
			throw new RuntimeException(ioex);
		}
	}
	
	private static class JsonChangeLogSerializer extends YamlChangeLogSerializer {
		
		@Override
	    public <T extends ChangeLogChild> void write(List<T> children, OutputStream out) throws IOException {
	        final Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
	        int i = 0;
	        for (T child : children) {
	            String serialized = serialize(child, true);
	            if (++i < children.size()) {
	                serialized = serialized.replaceFirst("}\\s*$", "},\n");
	            }
	            writer.write(StringUtils.indent(serialized, 2));
	            writer.write('\n');
	        }
	        writer.flush();
	    }

	    @Override
	    public String[] getValidFileExtensions() {
	        return new String[] {
	        	"json"
	        };
	    }
	    
	}
	
}
