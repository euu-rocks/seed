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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.persistence.Table;
import javax.sql.DataSource;

import org.hibernate.Session;

import org.seed.core.util.MiscUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.AbstractResourceAccessor;

@Component
public class SchemaManager {
	
	private static final Logger log = LoggerFactory.getLogger(SchemaManager.class);
	
	private static final String TABLE_CHANGELOG = ChangeLog.class.getAnnotation(Table.class).name();
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private Limits limits;
	
	@Value("classpath:liquibase/system-changelog.json")
    private Resource systemChangeLogResource;
	
	private String systemChangeLog;
	
	private Boolean isPostgres;
	
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
	
	SchemaConfiguration loadSchemaConfiguration(Session session) {
		final List<SchemaConfiguration> list = session.createQuery("SELECT c FROM SchemaConfiguration c", 
		   		 SchemaConfiguration.class).getResultList();
		Assert.state(list.size() <= 1, "ambiguous configuration");
		return list.isEmpty() ? null : list.get(0);
	}
	
	void updateSchemaConfiguration(SchemaVersion baseVersion, Session session) {
		// upgrade all versions from baseVersion + 1 to latest version
		for (int v = baseVersion.ordinal() + 1; v <= SchemaVersion.lastVersion().ordinal(); v++) {
			final SchemaVersion version = SchemaVersion.getVersion(v);
			final ChangeLog changeLog = new ChangeLog();
			changeLog.setChangeSet(loadSchemaUpdateChangeSet(version));
			session.saveOrUpdate(changeLog);
		}
	}
	
	void updateSchema() {
		final long startTime = System.currentTimeMillis();
		try (Connection connection = dataSource.getConnection()) {
			final String changeLog = getChangeLog(loadCustomChangeSets(connection));
			log.debug("changelog content:\r\n" + changeLog);
			createLiquibase(connection, changeLog).update(new Contexts());
		} 
		catch (Exception ex) {
			throw new ConfigurationException("failed to update schema", ex);
		}
		log.info("Schema updated in " + MiscUtils.formatDuration(startTime));
	}
	
	private String getChangeLog(String customChangeSets) {
		return systemChangeLog
				.replace("<#UID_LEN#>", String.valueOf(limits.getLimit("field.uid.length")))
				.replace("<#IDENT_LEN#>", String.valueOf(limits.getLimit("entity.identifier.length")))
				.replace("<#STRING_LEN#>", String.valueOf(limits.getLimit("entity.stringfield.length")))
				.replace("<#USERNAME_LEN#>", String.valueOf(limits.getLimit("user.name.length")))
				.replace("<#USERROLE_LEN#>", String.valueOf(limits.getLimit("user.role.length")))
				.replace("<#PWD_LEN#>", String.valueOf(limits.getLimit("user.pwd.length")))
				.replace("<#PARAMNAME_LEN#>", String.valueOf(limits.getLimit("parameter.name.length")))
				.replace("<#PARAM_LEN#>", String.valueOf(limits.getLimit("parameter.value.length")))
				.replace("<#BLOB_TYPE#>", isPostgres() ? "bytea" : "BLOB")
				.replace("<#CHANGE_SETS#>", customChangeSets);
	}
	
	private String loadCustomChangeSets(Connection connection) throws SQLException {
		final StringBuilder buf = new StringBuilder();
		if (existChangeLogTable(connection)) {
			try (Statement statement = connection.createStatement();
				 ResultSet resultSet = statement.executeQuery("SELECT changeset FROM " + 
															  TABLE_CHANGELOG + " ORDER BY id")) {
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
			throw new ConfigurationException("failed to load " + resourceName, ex);
		}
	}
	
	private boolean existChangeLogTable(Connection connection) throws SQLException {
		try (ResultSet resultSet = connection.getMetaData().getTables(null, null, 
																	  TABLE_CHANGELOG, 
																	  new String[] {"TABLE"})) {
			while (resultSet.next()) { 
				if (TABLE_CHANGELOG.equalsIgnoreCase(resultSet.getString("TABLE_NAME"))) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isPostgres() {
		if (isPostgres == null) {
			try (Connection con = dataSource.getConnection()) {
				isPostgres = DriverManager.getDriver(con.getMetaData().getURL())
										  .getClass().getName().contains("postgresql");
			} catch (SQLException ex) {
				throw new RuntimeException(ex);
			}
		}
		return isPostgres;
	}
	
	private Liquibase createLiquibase(Connection connection, String changeLogAsString) 
			throws SQLException, LiquibaseException {
		final Database database = DatabaseFactory.getInstance()
				.findCorrectDatabaseImplementation(new JdbcConnection(connection));
		return new Liquibase("changelog.json", new StringResourceAccessor(changeLogAsString), 
																		  database);
	}
	
	private class StringResourceAccessor extends AbstractResourceAccessor {
		
		private final String string;
		
		StringResourceAccessor(String string) {
			Assert.notNull(string, "string is null");
			this.string = string;
		}

		@Override
		public Set<InputStream> getResourcesAsStream(String path) throws IOException {
			return Collections.singleton(
					   new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8))
				   );
		}

		@Override
		public Set<String> list(String relativeTo, String path, boolean includeFiles, 
				boolean includeDirectories, boolean recursive) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public ClassLoader toClassLoader() {
			return null;
		}

	}
	
}
