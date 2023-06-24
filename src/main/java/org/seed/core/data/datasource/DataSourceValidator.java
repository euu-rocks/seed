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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.AbstractSystemObject;
import org.seed.core.data.DataException;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.SystemObject;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationErrors;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataSourceValidator extends AbstractSystemEntityValidator<IDataSource> {
	
	@Autowired
	private DataSourceRepository repository;
	
	private List<DataSourceDependent<? extends SystemEntity>> dataSourceDependents;
	
	@Override
	public void validateDelete(IDataSource dataSource) throws ValidationException {
		Assert.notNull(dataSource, C.DATASOURCE);
		final ValidationErrors errors = createValidationErrors(dataSource);
		
		try (Session session = repository.getSession()) {
			for (DataSourceDependent<? extends SystemEntity> dependent : getDataSourceDependents()) {
				for (SystemEntity systemEntity : dependent.findUsage(dataSource, session)) {
					if (C.REPORT.equals(getEntityType(systemEntity))) {
						errors.addError("val.inuse.datasourcereport", systemEntity.getName());
					}
				}
			}
		}
		validate(errors);
	}
	
	@Override
	public void validateSave(IDataSource dataSource) throws ValidationException {
		Assert.notNull(dataSource, C.DATASOURCE);
		final ValidationErrors errors = createValidationErrors(dataSource);
		
		if (isEmpty(dataSource.getName())) {
			errors.addEmptyName();
		}
		else if (!isNameLengthAllowed(dataSource.getName())) {
			errors.addOverlongName(getMaxNameLength());
		}
		if (isEmpty(dataSource.getContent())) {
			errors.addEmptyField("label.sqlstatement");
		}
		else if (validateContent(dataSource, errors)) {
			if (dataSource.hasParameters()) {
				validateParameters(dataSource, errors);
			}
			for (String contentParameter : dataSource.getContentParameterNames()) {
				if (dataSource.getParameterByName(contentParameter) == null) {
					errors.addError("val.query.noparam", contentParameter);
				}
			}
		}
		
		// test query
		if (errors.isEmpty()) {	
			try {
				repository.testQuery(dataSource, createTestParameterMap(dataSource));
			}
			catch (PersistenceException pex) {
				throw new ValidationException(new ValidationError(dataSource, "val.illegal.sqlstatement"), 
											  new DataException(pex));
			}
		}
		validate(errors);
	}
	
	void validateParameterValues(IDataSource dataSource, Map<String, Object> parameters) 
			throws ValidationException {
		Assert.notNull(dataSource, C.DATASOURCE);
		
		if (!dataSource.hasParameters()) {
			return;
		}
		final ValidationErrors errors = createValidationErrors(dataSource);
		for (DataSourceParameter parameter : dataSource.getParameters()) {
			final Object value = parameters.get(parameter.getName());
			if (value == null) {
				errors.addError("val.empty.parametervalue", parameter.getName());
			}
			else if (value instanceof SystemObject && ((SystemObject) value).isNew()) {
				errors.addError("val.empty.parameterobjectid", parameter.getName());
			}
		}
		validate(errors);
	}
	
	private boolean validateContent(IDataSource dataSource, ValidationErrors errors) {
		if (dataSource.getType().isSQL() && 
			!dataSource.getContent().toLowerCase().contains("select ")) {
			errors.addError("val.query.noselect");
			return false;
		}
		else if (dataSource.getType().isHQL() && 
				 !dataSource.getContent().toLowerCase().contains("from ")) {
			errors.addError("val.query.nofrom");
			return false;
		}
		return true;
	}
 	
	@SuppressWarnings("unchecked")
	private void validateParameters(IDataSource dataSource, final ValidationErrors errors) {
		for (DataSourceParameter parameter : dataSource.getParameters()) {
			// validate name
			if (isEmpty(parameter.getName())) {
				errors.addEmptyField("label.paramname");
			}
			else if (!isNameUnique(parameter.getName(), dataSource.getParameters())) {
				errors.addError("val.ambiguous.param", parameter.getName());
			}
			// validate type
			if (isEmpty(parameter.getType())) {
				errors.addEmptyField("label.paramtype");
			}
			else if (parameter.getType().isReference() && 
					 isEmpty(parameter.getReferenceEntity())) {
				errors.addEmptyField("label.refentity");
			}
		}
	}
	
	private Map<String, Object> createTestParameterMap(IDataSource dataSource) {
		final Map<String, Object> parameterMap = new HashMap<>();
		for (String contentParameter : dataSource.getContentParameterNames()) {
			final DataSourceParameter parameter = dataSource.getParameterByName(contentParameter);
			Assert.stateAvailable(parameterMap, "parameterMap");
			
			Object value;
			switch (parameter.getType()) {
				case TEXT:
					value = "text";
					break;
					
				case INTEGER:
					value = 1;
					break;
					
				case DOUBLE:
					value = 1.23d;
					break;
					
				case DECIMAL:
					value = BigDecimal.valueOf(1.23d);
					break;
					
				case DATE:
					value = new Date();
					break;
				
				case LONG:
					value = 1L;
					break;
					
				case BOOLEAN:
					value = Boolean.TRUE;
					break;
					
				case REFERENCE:
					value = new AbstractSystemObject() {
						
						@Override
						public Long getId() {
							return 0L;
						}
						
					};
					break;
					
				default:
					throw new UnsupportedOperationException(parameter.getType().name());
			}
			parameterMap.put(contentParameter, value);
		}
		return parameterMap;
	}
	
	private List<DataSourceDependent<? extends SystemEntity>> getDataSourceDependents() {
		if (dataSourceDependents == null) {
			dataSourceDependents = MiscUtils.castList(getBeans(DataSourceDependent.class));
		}
		return dataSourceDependents;
	}
	
}
