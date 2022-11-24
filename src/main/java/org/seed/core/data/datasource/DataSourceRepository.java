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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.query.internal.AbstractProducedQuery;

import org.seed.C;
import org.seed.InternalException;
import org.seed.LabelProvider;
import org.seed.core.data.AbstractSystemEntityRepository;
import org.seed.core.data.SystemObject;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class DataSourceRepository extends AbstractSystemEntityRepository<IDataSource> {
	
	private static final Logger log = LoggerFactory.getLogger(DataSourceRepository.class);
	
	@Autowired
	private LabelProvider labelProvider;
	
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
		final String sql = buildQuery(dataSource, parameters);
		switch(dataSource.getType()) {
			case SQL:
				return session.doReturningWork(new ReturningWork<ResultSetMetaData>() {

					@Override
					public ResultSetMetaData execute(Connection connection) throws SQLException {
						try (Statement statement = connection.createStatement();
							 ResultSet resultSet = statement.executeQuery(sql)) {
							return resultSet.getMetaData();
						}
					}
				});
				
			case HQL:
				final Query<?> query = session.createQuery(sql);
				return session.doReturningWork(new ReturningWork<ResultSetMetaData>() {
					
					@Override
					public ResultSetMetaData execute(Connection connection) throws SQLException {
						try (PreparedStatement statement = connection.prepareStatement(getSQL(query))) {
							int idx = 1;
							for (String contentParameter : dataSource.getContentParameterSet()) {
								final Object paramValue = parameters.get(contentParameter);
								statement.setObject(idx++, paramValue);
							}
							try (ResultSet resultSet = statement.executeQuery()) {
								return resultSet.getMetaData();
							}
						}
					}
				});

			default:
				throw new UnsupportedOperationException(dataSource.getType().name());	
		}
	}
	
	private List<Object> query(IDataSource dataSource, Map<String, Object> parameters, Session session, boolean testQuery) {
		final String queryString = buildQuery(dataSource, parameters);
		switch (dataSource.getType()) {
			case SQL:
				final NativeQuery<?> sqlQuery = session.createSQLQuery(queryString);
				if (testQuery) {
					sqlQuery.setMaxResults(1);
				}
				return MiscUtils.castList(sqlQuery.list());
			
			case HQL:
				final Query<?> query = session.createQuery(queryString);
				for (String contentParameter : dataSource.getContentParameterSet()) {
					final Object paramValue = parameters.get(contentParameter);
					query.setParameter(contentParameter, paramValue);
				}
				if (testQuery) {
					query.setMaxResults(1);
				}
				return MiscUtils.castList(query.list());
			
			default:
				throw new UnsupportedOperationException(dataSource.getType().name());
		}
	}
	
	private String buildQuery(IDataSource dataSource, Map<String, Object> parameters) {
		String query = dataSource.getContent();
		Assert.stateAvailable(query, C.CONTENT);
		
		if (dataSource.getType() == DataSourceType.SQL) {
			for (String contentParameter : dataSource.getContentParameterSet()) {
				final Object paramValue = parameters.get(contentParameter);
				Assert.stateAvailable(paramValue, "parameter value " + contentParameter);
				query = query.replace('{' + contentParameter + '}', formatParameter(paramValue));
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("[{}] {}", dataSource.getName(), query);
		}
		return query;
	}
	
	private String formatParameter(Object parameter) {
		if (parameter instanceof String) {
			return StringUtils.quote((String) parameter);
		}
		if (parameter instanceof BigDecimal) {
			return labelProvider.formatBigDecimal((BigDecimal) parameter);
		}
		if (parameter instanceof Date) {
			return labelProvider.formatDate((Date) parameter);
		}
		if (parameter instanceof SystemObject) {
			final Long id = ((SystemObject) parameter).getId();
			return id != null ? id.toString() : null;
		}
		return parameter != null ? parameter.toString() : "null";
	}
	
	private static String getSQL(Query<?> query) {
		final AbstractProducedQuery<?> producedQuery = query.unwrap(AbstractProducedQuery.class);
        return producedQuery.getProducer().getFactory().getQueryPlanCache()
        	.getHQLQueryPlan(producedQuery.getQueryString(), false, Collections.emptyMap())
            .getSqlStrings()[0];
	}
	
}
