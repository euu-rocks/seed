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

import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.application.AbstractRestController;
import org.seed.core.config.OpenSessionInViewFilter;
import org.seed.core.user.Authorisation;
import org.seed.core.util.MiscUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/seed/rest/report")
public class ReportRestController extends AbstractRestController<Report> {
	
	@Autowired
	private ReportService service;
	
	@Override
	protected ReportService getService() {
		return service;
	}
	
	@Override
	@ApiOperation(value = "getAllReports", notes="returns a list of all authorized reports")
	@GetMapping
	public List<Report> getAll(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session) {
		return getAll(session, report -> checkPermissions(session, report));
	}
	
	@Override
	@ApiOperation(value = "getReportById", notes="returns the report with the given id")
	@GetMapping(value = "/{id}")
	public Report get(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
					  @PathVariable(C.ID) Long id) {
		final Report report = super.get(session, id);
		if (report == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, C.REPORT + ' ' + id);
		}
		else if (!checkPermissions(session, report)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		return report;
	}
	
	@ApiOperation(value = "generateReport", notes="generates and downloads the report with the given id")
	@GetMapping(value = "/{id}/generate")
	public ResponseEntity<ByteArrayResource> generate(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
													  @RequestParam Map<String,String> parameters,
													  @PathVariable(C.ID) Long id) {
		final Report report = super.get(session, id);
		if (report == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, C.REPORT + ' ' + id);
		}
		else if (!checkPermissions(session, report) || !isAuthorised(session, Authorisation.PRINT_REPORTS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		
		final ReportFormat reportFormat;
		if (ReportFormat.PDF.name().equalsIgnoreCase(parameters.get("format"))) {
			reportFormat = ReportFormat.PDF;
		}
		else {
			reportFormat = ReportFormat.EXCEL;
		}
		final String fileName = report.getName() + '_' + 
								MiscUtils.getTimestampString()  + '.' + 
								reportFormat.fileType;
		try {
			service.setDataSourceParameters(session, report, parameters);
			return download(fileName, service.generateReport(report, reportFormat, session));
		} 
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, ex.getMessage());
		}
	}
	
}
