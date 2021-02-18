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
package org.seed.core.data.datasource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import org.seed.core.data.AbstractSystemEntityRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

@Repository
public class DataSourceRepository extends AbstractSystemEntityRepository<DataSource> {
	
	private final static Logger log = LoggerFactory.getLogger(DataSourceRepository.class);
	
	@Autowired
	private javax.sql.DataSource sqlDataSource;
	
	public DataSourceRepository() {
		super(DataSourceMetadata.class);
	}
	
	public DataSourceResult query(DataSource dataSource, Map<String, Object> parameters) {
		try (Session session = getSession()) {
			return query(dataSource, parameters, session);
		}
	}
	
	public DataSourceResult query(DataSource dataSource, Map<String, Object> parameters, Session session) {
		try {
			return new DefaultDataSourceResult(query(dataSource, parameters, session, false), 
											   getMetadata(dataSource, parameters));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	void testQuery(DataSource dataSource, Map<String, Object> parameters) {
		try (Session session = getSession()) {
			query(dataSource, parameters, session, true);
		}
	}
	
	private ResultSetMetaData getMetadata(DataSource dataSource, Map<String, Object> parameters) {
		Assert.notNull(dataSource, "dataSource is null");
		Assert.notNull(parameters, "parameters is null");
		
		final String sql = buildSQLQuery(dataSource, parameters);
		try (Connection connection = sqlDataSource.getConnection()) {
			try (Statement statement = connection.createStatement();
				 ResultSet resultSet = statement.executeQuery(sql)) {
				return resultSet.getMetaData();
			}
		} 
		catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	private List<Object[]> query(DataSource dataSource, Map<String, Object> parameters, Session session, boolean testQuery) {
		Assert.notNull(dataSource, "dataSource is null");
		Assert.notNull(parameters, "parameters is null");
		Assert.notNull(session, "session is null");
		
		final String sql = buildSQLQuery(dataSource, parameters);
		return testQuery
				? session.createSQLQuery(sql).setMaxResults(1).list()
				: session.createSQLQuery(sql).list();
	}
	
	private static String buildSQLQuery(DataSource dataSource, Map<String, Object> parameters) {
		String sql = dataSource.getContent();
		Assert.state(sql != null, "content not available");
		
		for (String contentParameter : dataSource.getContentParameterSet()) {
			final Object paramValue = parameters.get(contentParameter);
			Assert.state(paramValue != null, "parameter '" + contentParameter + "' not available");
			
			sql = sql.replace('{' + contentParameter + '}', paramValue.toString());
		}
		if (log.isDebugEnabled()) {
			log.debug('[' + dataSource.getName() + "] " + sql);
		}
		return sql;
	}
	
}
