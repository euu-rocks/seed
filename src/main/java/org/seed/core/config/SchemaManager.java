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

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.SortedSet;

import javax.annotation.PostConstruct;
import javax.persistence.Table;
import javax.sql.DataSource;

import org.hibernate.Session;

import org.seed.core.config.changelog.ChangeLog;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.AbstractResourceAccessor;
import liquibase.resource.InputStreamList;

@Component
public class SchemaManager {
	
	private static final Logger log = LoggerFactory.getLogger(SchemaManager.class);
	
	private static final String TABLE_CHANGELOG = ChangeLog.class.getAnnotation(Table.class).name();
	
	private static final String FILENAME_CHANGELOG = "changelog.json"; 
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private Limits limits;
	
	@Value("classpath:liquibase/system-changelog.json")
    private Resource systemChangeLogResource;
	
	private String systemChangeLog;
	
	private DatabaseInfo databaseInfo;
	
	@PostConstruct
	private void init() {
		Assert.notNull(systemChangeLogResource, "system-changelog not found");
		try {
			systemChangeLog = MiscUtils.getResourceAsText(systemChangeLogResource);
		} 
		catch (Exception ex) {
			throw new ConfigurationException("failed to load system-changelog ", ex);
		}
	}
	
	public synchronized DatabaseInfo getDatabaseInfo() {
		if (databaseInfo == null) {
			try (Connection connection = dataSource.getConnection()) {
				final DatabaseMetaData dbMeta = connection.getMetaData();
				databaseInfo = new DatabaseInfo(dbMeta.getDatabaseProductName(),
												dbMeta.getDatabaseProductVersion());
			}
			catch (SQLException ex) {
				throw new ConfigurationException("database detection failed", ex);
			}
		}
		return databaseInfo;
	}
	
	synchronized SchemaConfiguration loadSchemaConfiguration(Session session) {
		final List<SchemaConfiguration> list = 
				session.createQuery("select c from SchemaConfiguration c", SchemaConfiguration.class)
					   .getResultList();
		Assert.state(list.size() <= 1, "ambiguous configuration");
		return list.isEmpty() ? null : list.get(0);
	}
	
	synchronized void updateSchemaConfiguration(SchemaVersion baseVersion, Session session) {
		// upgrade all versions from baseVersion + 1 to latest version
		for (int v = baseVersion.ordinal() + 1; v <= SchemaVersion.lastVersion().ordinal(); v++) {
			final SchemaVersion version = SchemaVersion.getVersion(v);
			final ChangeLog changeLog = new ChangeLog();
			changeLog.setChangeSet(loadSchemaUpdateChangeSet(version));
			session.saveOrUpdate(changeLog);
		}
	}
	
	synchronized void updateSchema() {
		final long startTime = System.currentTimeMillis();
		try (Connection connection = dataSource.getConnection()) {
			final String customChangeSets = loadCustomChangeSets(connection);
			final String changeLog = replaceLimits(systemChangeLog.replace("<#CHANGE_SETS#>", customChangeSets));
			log.debug("changelog content:\r\n{}", changeLog);
			createLiquibase(connection, changeLog).update(new Contexts());
		} 
		catch (Exception ex) {
			throw new ConfigurationException("failed to update schema", ex);
		}
		if (log.isInfoEnabled()) {
			log.info("Schema updated in {}", MiscUtils.formatDuration(startTime));
		}
	}
	
	private String replaceLimits(String text) {
		return text.replace("<#UID_LEN#>", String.valueOf(limits.getLimit(Limits.LIMIT_UID_LENGTH)))
				   .replace("<#IDENT_LEN#>", String.valueOf(limits.getLimit(Limits.LIMIT_IDENTIFIER_LENGTH)))
				   .replace("<#STRING_LEN#>", String.valueOf(limits.getLimit(Limits.LIMIT_TEXT_LENGTH)))
				   .replace("<#USERNAME_LEN#>", String.valueOf(limits.getLimit(Limits.LIMIT_USER_LENGTH)))
				   .replace("<#USERROLE_LEN#>", String.valueOf(limits.getLimit(Limits.LIMIT_ROLE_LENGTH)))
				   .replace("<#PWD_LEN#>", String.valueOf(limits.getLimit(Limits.LIMIT_PWD_LENGTH)))
				   .replace("<#PARAMNAME_LEN#>", String.valueOf(limits.getLimit(Limits.LIMIT_PARAM_NAME_LENGTH)))
				   .replace("<#PARAM_LEN#>", String.valueOf(limits.getLimit(Limits.LIMIT_PARAM_VALUE_LENGTH)))
				   .replace("<#BLOB_TYPE#>", getDatabaseInfo().isPostgres() ? "bytea" : "BLOB");
	}
	
	private String loadCustomChangeSets(Connection connection) throws SQLException {
		final StringBuilder buf = new StringBuilder();
		if (existChangeLogTable(connection)) {
			try (Statement statement = connection.createStatement();
				 ResultSet resultSet = statement.executeQuery("select changeset from sys_changelog order by id")) {
				while (resultSet.next()) {
					if (buf.length() > 0) {
						buf.append(',');
					}
					buf.append(resultSet.getString(1));
				}
			}
		}
		return buf.toString();
	}
	
	private String loadSchemaUpdateChangeSet(SchemaVersion version) {
		final String resourceName = "classpath:liquibase/system-update-" + version + ".json";
		try {
			return MiscUtils.getResourceAsText(resourceLoader.getResource(resourceName));
		} 
		catch (Exception ex) {
			throw new ConfigurationException("failed to load: " + resourceName, ex);
		}
	}
	
	private boolean existChangeLogTable(Connection connection) throws SQLException {
		try (ResultSet resultSet = connection.getMetaData().getTables(null, null, 
																	  TABLE_CHANGELOG, 
																	  new String[] { "TABLE" })) {
			while (resultSet.next()) { 
				if (TABLE_CHANGELOG.equalsIgnoreCase(resultSet.getString("TABLE_NAME"))) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static Liquibase createLiquibase(Connection connection, String changeLogAsString) 
			throws LiquibaseException {
		final Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
																	new JdbcConnection(connection));
		return new Liquibase(FILENAME_CHANGELOG, 
							 new StringResourceAccessor(changeLogAsString), 
							 database);
	}
	
	private static class StringResourceAccessor extends AbstractResourceAccessor {
		
		private final String text;
		
		private StringResourceAccessor(String text) {
			this.text = text;
		}

		@Override
		public InputStreamList openStreams(String relativeTo, String streamPath) throws IOException {
			Assert.state(FILENAME_CHANGELOG.equals(streamPath), "unknown path: " + streamPath);
			
			return new InputStreamList(null, MiscUtils.getStringAsStream(text)); 
		}

		@Override
		public SortedSet<String> list(String relativeTo, String path, boolean recursive, 
									  boolean includeFiles, boolean includeDirectories) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public SortedSet<String> describeLocations() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public boolean equals(Object obj) {
			return super.equals(obj);
		}
		
		@Override
		public int hashCode() {
			return super.hashCode();
		}

	}
	
}
