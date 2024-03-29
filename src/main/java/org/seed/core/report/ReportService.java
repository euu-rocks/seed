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

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import org.seed.core.application.ApplicationEntityService;
import org.seed.core.data.ValidationException;
import org.seed.core.user.User;

public interface ReportService extends ApplicationEntityService<Report> {
	
	List<Report> getReports(User user, Session session);
	
	ReportDataSource createDataSource(Report report);
	
	List<ReportPermission> getAvailablePermissions(Report report, Session session);
	
	void setDataSourceParameters(Session session, Report report, Map<String,String> parameters) throws ParseException;
	
	byte[] generateReport(Report report, ReportFormat format, Session session) throws ValidationException;
	
}
