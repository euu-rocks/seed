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
import java.util.SortedSet;

import javax.annotation.PostConstruct;
import javax.persistence.Table;
import javax.sql.DataSource;

import org.hibernate.Session;

import org.seed.core.config.changelog.ChangeLog;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;
import org.seed.core.util.StreamUtils;

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
	
	private static final String CHANGELOG_TABLE = ChangeLog.class.getAnnotation(Table.class).name();
	
	private static final String CHANGELOG_LOCKTABLE = "databasechangeloglock";
	
	private static final String CHANGELOG_FILENAME = "changelog.json"; 
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private SystemLog systemLog;
	
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
			systemChangeLog = StreamUtils.getResourceAsText(systemChangeLogResource);
		} 
		catch (Exception ex) {
			throw new ConfigurationException("Failed to load system-changelog ", ex);
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
				throw new ConfigurationException("Database detection failed", ex);
			}
		}
		return databaseInfo;
	}
	
	synchronized SchemaConfiguration loadSchemaConfiguration(Session session) {
		return session.createQuery("from SchemaConfiguration", SchemaConfiguration.class)
					  .uniqueResult();
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
	
	synchronized boolean updateSchema() {
		final long startTime = System.currentTimeMillis();
		try (Connection connection = dataSource.getConnection()) {
			final String customChangeSets = loadCustomChangeSets(connection);
			final String changeLog = replaceLimits(systemChangeLog.replace("<#CHANGE_SETS#>", customChangeSets));
			if (log.isDebugEnabled()) {
				log.debug("Changelog content:\r\n{}", changeLog);
			}
			createLiquibase(connection, changeLog).update(new Contexts());
			if (log.isInfoEnabled()) {
				log.info("Schema updated in {}", MiscUtils.formatDuration(startTime));
			}
			return true;
		} 
		catch (Exception ex) {
			log.warn("Failed to update schema: {}", ex.getMessage());
			systemLog.logError("systemlog.error.updateschema", ex);
			return false;
		}
	}
	
	synchronized void checkLiquibaseLock() {
		try (Connection connection = dataSource.getConnection()) {
			if (existTable(connection, CHANGELOG_LOCKTABLE) &&
				existLock(connection)) {
				log.warn("Liquibase lock detected");
				removeLock(connection);
				log.info("Liquibase lock removed");
			}
		}
		catch (SQLException sqlex) {
			log.warn("Failed to remove Liquibase lock");
		}
	}
	
	synchronized void repairSchema() {
		log.info("Attempt to repair schema");
		try (Connection connection = dataSource.getConnection()) {
			removeLastChangeLog(connection);
			log.info("Schema successfully repaired");
		}
		catch (SQLException sqlex) {
			log.warn("Failed to repair schema", sqlex);
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
		if (existTable(connection, CHANGELOG_TABLE)) {
			try (Statement statement = connection.createStatement();
				 ResultSet resultSet = statement.executeQuery("select changeset from " + CHANGELOG_TABLE + " order by id")) {
				while (resultSet.next()) {
					if (buf.length() > 0) {
						buf.append(',');
					}
					buf.append(resultSet.getString(1));
				}
			}
		}
		return buf.toString().replace('\t', ' ');
	}
	
	private String loadSchemaUpdateChangeSet(SchemaVersion version) {
		final String resourceName = "classpath:liquibase/system-update-" + version + ".json";
		try {
			return StreamUtils.getResourceAsText(resourceLoader.getResource(resourceName));
		} 
		catch (Exception ex) {
			throw new ConfigurationException("failed to load: " + resourceName, ex);
		}
	}
	
	private static boolean existLock(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery("select count(*) from " + CHANGELOG_LOCKTABLE + " where locked = true")) {
			return resultSet.next() && resultSet.getInt(1) == 1;
		}
	}
	private static void removeLock(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.executeUpdate("delete from " + CHANGELOG_LOCKTABLE);
		}
	}
	
	private static void removeLastChangeLog(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.executeUpdate("delete from " + CHANGELOG_TABLE + " where id = (select max(id) from " + CHANGELOG_TABLE + ')');
		}
	}
	
	private static boolean existTable(Connection connection, String tableName) throws SQLException {
		try (ResultSet resultSet = connection.getMetaData().getTables(null, null, tableName, new String[] { "TABLE" })) {
			while (resultSet.next()) { 
				if (tableName.equalsIgnoreCase(resultSet.getString("TABLE_NAME"))) {
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
		return new Liquibase(CHANGELOG_FILENAME, 
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
			Assert.state(CHANGELOG_FILENAME.equals(streamPath), "unknown path: " + streamPath);
			
			return new InputStreamList(null, StreamUtils.getStringAsStream(text)); 
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
