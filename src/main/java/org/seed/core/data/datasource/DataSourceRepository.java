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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.query.Query;
import org.hibernate.query.internal.AbstractProducedQuery;

import org.seed.InternalException;
import org.seed.core.data.AbstractSystemEntityRepository;
import org.seed.core.data.SystemObject;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class DataSourceRepository extends AbstractSystemEntityRepository<IDataSource> {
	
	private static final Logger log = LoggerFactory.getLogger(DataSourceRepository.class);
	
	public DataSourceRepository() {
		super(DataSourceMetadata.class);
	}
	
	@Override
	public Session getSession() {
		return super.getSession();
	}
	
	public DataSourceResult query(IDataSource dataSource, Map<String, Object> parameters) {
		try (Session session = getSession()) {
			return query(dataSource, parameters, session);
		}
	}
	
	public DataSourceResult query(IDataSource dataSource, Map<String, Object> parameters, Session session) {
		try {
			return new DefaultDataSourceResult(query(dataSource, parameters, session, false), 
											   getMetadata(dataSource, parameters, session));
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
	
	private ResultSetMetaData getMetadata(IDataSource dataSource, Map<String, Object> parameters, Session session) {
		final String queryString;
		switch(dataSource.getType()) {
			case SQL:
				queryString = buildSqlQueryString(dataSource);
				break;
				
			case HQL:
				queryString = getSQL(createHqlQuery(dataSource, parameters, session));
				break;
				
			default:
				throw new UnsupportedOperationException(dataSource.getType().name());	
		}
		
		return session.doReturningWork(new ReturningWork<ResultSetMetaData>() {
			
			@Override
			public ResultSetMetaData execute(Connection connection) throws SQLException {
				try (PreparedStatement statement = connection.prepareStatement(queryString)) {
					setParameterValues(statement, dataSource, parameters);
					try (ResultSet resultSet = statement.executeQuery()) {
						return resultSet.getMetaData();
					}
				}
			}
		});
	}
	
	private List<Object> query(IDataSource dataSource, Map<String, Object> parameters, 
							   Session session, boolean testQuery) {
		final Query<?> query;
		switch (dataSource.getType()) {
			case SQL:
				query = createSqlQuery(dataSource, parameters, session);
				break;
				
			case HQL:
				query = createHqlQuery(dataSource, parameters, session);
				break;
			
			default:
				throw new UnsupportedOperationException(dataSource.getType().name());
		}
		if (testQuery) {
			query.setMaxResults(1);
		}
		return MiscUtils.castList(query.list());
	}
	
	private static Query<?> createSqlQuery(IDataSource dataSource, Map<String, Object> paramMap, Session session) {
		final var parameters = dataSource.getContentParameters();
		final var queryString = buildSqlQueryString(dataSource);
		final var query = session.createNativeQuery(queryString);
		int idx = 1;
		for (DataSourceParameter parameter : parameters) {
			final var paramValue = paramMap.get(parameter.getName());
			query.setParameter(idx++, paramValue);	
		}
		return query;
	}
	
	private static Query<?> createHqlQuery(IDataSource dataSource, Map<String, Object> paramMap, Session session) {
		final var query = session.createQuery(dataSource.getContent());
		for (String paramName : dataSource.getContentParameterNames()) {
			final var paramValue = paramMap.get(paramName);
			query.setParameter(paramName, paramValue);
		}
		return query;
	}
	
	private static void setParameterValues(PreparedStatement statement, IDataSource dataSource, Map<String, Object> parameters) throws SQLException {
		int idx = 1;
		for (DataSourceParameter parameter : dataSource.getContentParameters()) {
			var paramValue = parameters.get(parameter.getName());
			Assert.stateAvailable(paramValue, "parameter " + parameter.getName());
			switch (parameter.getType()) {
				case TEXT:
					statement.setString(idx++, (String) paramValue);
					break;
					
				case INTEGER:
					statement.setInt(idx++, ((Number) paramValue).intValue());
					break;
					
				case DOUBLE:
					statement.setDouble(idx++, ((Number) paramValue).doubleValue());
					break;
					
				case DECIMAL:
					statement.setBigDecimal(idx, (BigDecimal) paramValue);
					break;
					
				case DATE:
					statement.setDate(idx++, new Date(((java.util.Date) paramValue).getTime()));
					break;
					
				case LONG:
					statement.setLong(idx++, ((Number) paramValue).longValue());
					break;
					
				case BOOLEAN:
					statement.setBoolean(idx++, (Boolean) paramValue);
					break;
				
				case REFERENCE:
					statement.setLong(idx++, ((SystemObject) paramValue).getId());
					break;
					
				default:
					throw new UnsupportedOperationException(parameter.getType().name());
			}
		}
	}
	
	private static String buildSqlQueryString(IDataSource dataSource) {
		final var paramNames = dataSource.getContentParameterNames();
		var queryString = dataSource.getContent();
		for (String paramName : paramNames) {
			queryString = queryString.replace('{' + paramName + '}', "?");
		}
		if (log.isDebugEnabled()) {
        	log.debug(queryString);
        }
		return queryString;
	}
	
	@SuppressWarnings("deprecation")
	private static String getSQL(Query<?> query) {
		final var producedQuery = query.unwrap(AbstractProducedQuery.class);
        final var sql = producedQuery.getProducer().getFactory().getQueryPlanCache()
            	.getHQLQueryPlan(producedQuery.getQueryString(), false, Collections.emptyMap())
                .getSqlStrings()[0];
        if (log.isDebugEnabled()) {
        	log.debug(sql);
        }
		return sql;
	}
	
}
