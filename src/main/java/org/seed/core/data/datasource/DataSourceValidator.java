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
import java.util.Set;

import javax.persistence.PersistenceException;

import org.seed.C;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.DataException;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationException;
import org.seed.core.form.LabelProvider;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataSourceValidator extends AbstractSystemEntityValidator<IDataSource> {
	
	@Autowired
	private DataSourceRepository repository;
	
	@Autowired
	private LabelProvider labelProvider;
	
	@Autowired
	private List<DataSourceDependent<? extends SystemEntity>> dataSourceDependents;
	
	@Override
	public void validateDelete(IDataSource dataSource) throws ValidationException {
		Assert.notNull(dataSource, C.DATASOURCE);
		final Set<ValidationError> errors = createErrorList();
		
		for (DataSourceDependent<? extends SystemEntity> dependent : dataSourceDependents) {
			for (SystemEntity systemEntity : dependent.findUsage(dataSource)) {
				if (C.REPORT.equals(getEntityType(systemEntity))) {
					errors.add(new ValidationError("val.inuse.datasourcereport", systemEntity.getName()));
				}
			}
		}
		validate(errors);
	}
	
	@Override
	public void validateSave(IDataSource dataSource) throws ValidationException {
		Assert.notNull(dataSource, C.DATASOURCE);
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(dataSource.getName())) {
			errors.add(ValidationError.emptyName());
		}
		else if (!isNameLengthAllowed(dataSource.getName())) {
			errors.add(ValidationError.overlongName(getMaxNameLength()));
		}
		if (isEmpty(dataSource.getContent())) {
			errors.add(ValidationError.emptyField("label.sqlstatement"));
		}
		else if (!dataSource.getContent().toLowerCase().contains("select ")) {
			errors.add(new ValidationError("val.query.noselect"));
		}
		else {
			if (dataSource.hasParameters()) {
				validateParameters(dataSource, errors);
			}
			for (String contentParameter : dataSource.getContentParameterSet()) {
				if (dataSource.getParameterByName(contentParameter) == null) {
					errors.add(new ValidationError("val.query.noparam", contentParameter));
				}
			}
		}
		
		// test sql
		if (errors.isEmpty()) {	
			try {
				repository.testQuery(dataSource, createTestParameterMap(dataSource));
			}
			catch (PersistenceException pex) {
				throw new ValidationException(new ValidationError("val.illegal.sqlstatement"), 
											  new DataException(pex));
			}
		}
		validate(errors);
	}
	
	@SuppressWarnings("unchecked")
	private void validateParameters(IDataSource dataSource, Set<ValidationError> errors) {
		for (DataSourceParameter parameter : dataSource.getParameters()) {
			// validate name
			if (isEmpty(parameter.getName())) {
				errors.add(ValidationError.emptyField("label.paramname"));
			}
			else if (!isNameUnique(parameter.getName(), dataSource.getParameters())) {
				errors.add(new ValidationError("val.ambiguous.param", parameter.getName()));
			}
			// validate type
			if (isEmpty(parameter.getType())) {
				errors.add(ValidationError.emptyField("label.paramtype"));
			}
			else if (parameter.getType().isReference() && 
					 isEmpty(parameter.getReferenceEntity())) {
				errors.add(ValidationError.emptyField("label.refentity"));
			}
		}
	}
	
	private Map<String, Object> createTestParameterMap(IDataSource dataSource) {
		final Map<String, Object> parameterMap = new HashMap<>();
		for (String contentParameter : dataSource.getContentParameterSet()) {
			final DataSourceParameter parameter = dataSource.getParameterByName(contentParameter);
			Assert.stateAvailable(parameterMap, "parameterMap");
			
			String value;
			switch (parameter.getType()) {
				case TEXT:
					value = "text";
					break;
				case INTEGER:
					value = "1";
					break;
				case DOUBLE:
					value = "1.234";
					break;
				case DECIMAL:
					value = labelProvider.formatBigDecimal(BigDecimal.valueOf(1.23d));
					break;
				case DATE:
					value = labelProvider.formatDate(new Date());
					break;
				default:
					throw new UnsupportedOperationException(parameter.getType().name());
			}
			parameterMap.put(contentParameter, value);
		}
		return parameterMap;
	}
	
}
