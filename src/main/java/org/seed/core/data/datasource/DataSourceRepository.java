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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.seed.C;
import org.seed.InternalException;
import org.seed.core.data.AbstractSystemEntityRepository;
import org.seed.core.form.LabelProvider;
import org.seed.core.util.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DataSourceRepository extends AbstractSystemEntityRepository<IDataSource> {
	
	private static final Logger log = LoggerFactory.getLogger(DataSourceRepository.class);
	
	private static final String QUOTE = "'";
	
	@Autowired
	private javax.sql.DataSource sqlDataSource;
	
	@Autowired
	private LabelProvider labelProvider;
	
	public DataSourceRepository() {
		super(DataSourceMetadata.class);
	}
	
	public DataSourceResult query(IDataSource dataSource, Map<String, Object> parameters) {
		try (Session session = getSession()) {
			return query(dataSource, parameters, session);
		}
	}
	
	public DataSourceResult query(IDataSource dataSource, Map<String, Object> parameters, Session session) {
		try {
			return new DefaultDataSourceResult(query(dataSource, parameters, session, false), 
											   getMetadata(dataSource, parameters));
		} 
		catch (SQLException ex) {
			throw new InternalException(ex);
		}
	}
	
	void testQuery(IDataSource dataSource, Map<String, Object> parameters) {
		try (Session session = getSession()) {
			query(dataSource, parameters, session, true);
		}
	}
	
	private ResultSetMetaData getMetadata(IDataSource dataSource, Map<String, Object> parameters) {
		Assert.notNull(dataSource, C.DATASOURCE);
		Assert.notNull(parameters, "parameters");
		
		final String sql = buildSQLQuery(dataSource, parameters);
		try (Connection connection = sqlDataSource.getConnection()) {
			try (Statement statement = connection.createStatement();
				 ResultSet resultSet = statement.executeQuery(sql)) {
				return resultSet.getMetaData();
			}
		} 
		catch (SQLException ex) {
			throw new InternalException(ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	private List<Object[]> query(IDataSource dataSource, Map<String, Object> parameters, Session session, boolean testQuery) {
		Assert.notNull(dataSource, C.DATASOURCE);
		Assert.notNull(parameters, "parameters");
		Assert.notNull(session, C.SESSION);
		
		final String sql = buildSQLQuery(dataSource, parameters);
		return testQuery
				? session.createSQLQuery(sql).setMaxResults(1).list()
				: session.createSQLQuery(sql).list();
	}
	
	private String buildSQLQuery(IDataSource dataSource, Map<String, Object> parameters) {
		String sql = dataSource.getContent();
		Assert.stateAvailable(sql, C.CONTENT);
		
		for (String contentParameter : dataSource.getContentParameterSet()) {
			final Object paramValue = parameters.get(contentParameter);
			Assert.stateAvailable(paramValue, "parameter value " + contentParameter);
			sql = sql.replace('{' + contentParameter + '}', formatParameter(paramValue));
		}
		if (log.isDebugEnabled()) {
			log.debug("[{}] {}", dataSource.getName(), sql);
		}
		return sql;
	}
	
	private String formatParameter(Object parameter) {
		if (parameter instanceof String) {
			return QUOTE + parameter + QUOTE;
		}
		if (parameter instanceof BigDecimal) {
			return labelProvider.formatBigDecimal((BigDecimal) parameter);
		}
		if (parameter instanceof Date) {
			return labelProvider.formatDate((Date) parameter);
		}
		return parameter.toString();
	}
	
}
