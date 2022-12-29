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
package org.seed.core.report;

import org.seed.C;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.ValidationErrors;
import org.seed.core.data.ValidationException;
import org.seed.core.data.datasource.IDataSource;
import org.seed.core.data.datasource.DataSourceParameter;
import org.seed.core.util.Assert;

import org.springframework.stereotype.Component;

@Component
public class ReportValidator extends AbstractSystemEntityValidator<Report> {
	
	public void validateGenerate(Report report) throws ValidationException {
		Assert.notNull(report, C.REPORT);
		final ValidationErrors errors = createValidationErrors(report);
		
		if (report.hasDataSources()) {
			for (ReportDataSource reportDataSource : report.getDataSources()) {
				final IDataSource dataSource = reportDataSource.getDataSource();
				if (dataSource.hasParameters()) {
					for (DataSourceParameter parameter : dataSource.getParameters()) {
						if (isEmpty(parameter.getValue())) {
							errors.addError("val.empty.parameter", parameter.getName(), dataSource.getName());
						}
					}
				}
			}
		}
		
		validate(errors);
	}
	
	@Override
	public void validateSave(Report report) throws ValidationException {
		Assert.notNull(report, C.REPORT);
		final ValidationErrors errors = createValidationErrors(report);
		
		if (isEmpty(report.getName())) {
			errors.addEmptyName();
		}
		else if (!isNameLengthAllowed(report.getName())) {
			errors.addOverlongName(getMaxNameLength());
		}
		
		if (report.hasDataSources()) {
			for (ReportDataSource dataSource : report.getDataSources()) {
				if (isEmpty(dataSource.getDataSource())) {
					errors.addEmptyField("label.datasource");
				}
				if (dataSource.getLabel() != null &&
					dataSource.getLabel().length() > getMaxStringLength()) {
					errors.addOverlongObjectField("label.label", "label.datasource", getMaxStringLength());
				}
			}
		}
		
		validate(errors);
	}
	
}
