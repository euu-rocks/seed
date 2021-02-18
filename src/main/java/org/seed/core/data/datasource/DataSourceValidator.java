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

import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.DataException;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationException;
import org.seed.core.form.LabelProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class DataSourceValidator extends AbstractSystemEntityValidator<DataSource> {
	
	@Autowired
	private DataSourceRepository repository;
	
	@Autowired
	private LabelProvider labelProvider;
	
	@Autowired
	private List<DataSourceDependent> dataSourceDependents;
	
	@Override
	public void validateDelete(DataSource dataSource) throws ValidationException {
		Assert.notNull(dataSource, "dataSource is null");
		final Set<ValidationError> errors = createErrorList();
		
		for (DataSourceDependent dependent : dataSourceDependents) {
			for (SystemEntity systemEntity : dependent.findUsage(dataSource)) {
				switch (getEntityType(systemEntity)) {
					case "report":
						errors.add(new ValidationError("val.inuse.datasourcereport", systemEntity.getName()));
						break;
					default:
						throw new IllegalStateException("unhandled entity: " + getEntityType(systemEntity));
				}
			}
		}
		validate(errors);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void validateSave(DataSource dataSource) throws ValidationException {
		Assert.notNull(dataSource, "dataSource is null");
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(dataSource.getName())) {
			errors.add(new ValidationError("val.empty.field", "label.name"));
		}
		else if (!isNameLengthAllowed(dataSource.getName())) {
			errors.add(new ValidationError("val.toolong.name", String.valueOf(getMaxNameLength())));
		}
		if (isEmpty(dataSource.getContent())) {
			errors.add(new ValidationError("val.empty.field", "label.sqlstatement"));
		}
		else if (!dataSource.getContent().toLowerCase().contains("select ")) {
			errors.add(new ValidationError("val.query.noselect"));
		}
		else {
			if (dataSource.hasParameters()) {
				for (DataSourceParameter parameter : dataSource.getParameters()) {
					// validate name
					if (isEmpty(parameter.getName())) {
						errors.add(new ValidationError("val.empty.field", "label.paramname"));
					}
					else if (!isNameUnique(parameter.getName(), dataSource.getParameters())) {
						errors.add(new ValidationError("val.ambiguous.param", parameter.getName()));
					}
					// validate type
					if (isEmpty(parameter.getType())) {
						errors.add(new ValidationError("val.empty.field", "label.paramtype"));
					}
					else if (parameter.getType().isReference() && 
							 isEmpty(parameter.getReferenceEntity())) {
						errors.add(new ValidationError("val.empty.field", "label.refentity"));
					}
				}
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
	
	private Map<String, Object> createTestParameterMap(DataSource dataSource) {
		final Map<String, Object> parameterMap = new HashMap<>();
		for (String contentParameter : dataSource.getContentParameterSet()) {
			final DataSourceParameter parameter = dataSource.getParameterByName(contentParameter);
			Assert.state(parameter != null, "parameter '" + contentParameter + "' not available");
			
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
