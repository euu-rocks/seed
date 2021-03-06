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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.hibernate.Session;

import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.data.Options;
import org.seed.core.data.ValidationException;
import org.seed.core.data.datasource.DataSource;
import org.seed.core.data.datasource.DataSourceDependent;
import org.seed.core.data.datasource.DataSourceParameter;
import org.seed.core.data.datasource.DataSourceResult;
import org.seed.core.data.datasource.DataSourceService;
import org.seed.core.user.User;
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserGroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class ReportServiceImpl extends AbstractApplicationEntityService<Report>
	implements ReportService, DataSourceDependent {
	
	@Autowired
	private ReportRepository repository;
	
	@Autowired
	private ReportValidator validator;
	
	@Autowired
	private DataSourceService dataSourceService;
	
	@Autowired
	private UserGroupService userGroupService;
	
	@Autowired
	private ReportGeneratorProvider generatorProvider;
	
	@Override
	public Report createInstance(@Nullable Options options) {
		final ReportMetadata instance = (ReportMetadata) super.createInstance(options);
		instance.createLists();
		return instance;
	}
	
	@Override
	public List<Report> getReports(User user) {
		Assert.notNull(user, "user is null");
		
		final List<Report> result = new ArrayList<>();
		for (Report report : findAllObjects()) {
			if (report.checkPermissions(user, null)) {
				result.add(report);
			}
		}
		return result;
	}
	
	@Override
	@Secured("ROLE_ADMIN_REPORT")
	public ReportDataSource createDataSource(Report report) {
		Assert.notNull(report, "report is null");
		
		final ReportDataSource dataSource = new ReportDataSource();
		report.addDataSource(dataSource);
		return dataSource;
	}
	
	@Override
	public byte[] generateReport(Report report, ReportFormat format) throws ValidationException {
		Assert.notNull(report, "report is null");
		Assert.notNull(format, "format is null");
		
		validator.validateGenerate(report);
		final ReportGenerator generator = generatorProvider.getGenerator(report, format);
		if (report.hasDataSources()) {
			// query data sources
			for (ReportDataSource reportDataSource : report.getDataSources()) {
				final Map<String,Object> parameterMap = new HashMap<String,Object>();
				final DataSource dataSource = reportDataSource.getDataSource();
				if (dataSource.hasParameters()) {
					for (DataSourceParameter parameter : dataSource.getParameters()) {
						parameterMap.put(parameter.getName(), parameter.getValue());
					}
				}
				final DataSourceResult result = dataSourceService.query(dataSource, 
																		parameterMap);
				generator.addDataSourceResult(reportDataSource, result);
			}
		}
		return generator.generate();
	}
	
	@Override
	public List<ReportPermission> getAvailablePermissions(Report report) {
		Assert.notNull(report, "report is null");
		
		final List<ReportPermission> result = new ArrayList<>();
		for (UserGroup group : userGroupService.findAllObjects()) {
			boolean found = false;
			if (report.hasPermissions()) {
				for (ReportPermission permission : report.getPermissions()) {
					if (permission.getUserGroup().equals(group)) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				final ReportPermission permission = new ReportPermission();
				permission.setReport(report);
				permission.setUserGroup(group);
				result.add(permission);
			}
		}
		return result;
	}
	
	@Override
	public void analyzeObjects(ImportAnalysis analysis, Module currentVersionModule) {
		Assert.notNull(analysis, "analysis is null");
		
		if (analysis.getModule().getReports() != null) {
			for (Report report : analysis.getModule().getReports()) {
				if (currentVersionModule == null) {
					analysis.addChangeNew(report);
				}
				else {
					final Report currentVersionReport =
						currentVersionModule.getReportByUid(report.getUid());
					if (currentVersionReport == null) {
						analysis.addChangeNew(report);
					}
					else if (!report.isEqual(currentVersionReport)) {
						analysis.addChangeModify(report);
					}
				}
			}
		}
		if (currentVersionModule != null && currentVersionModule.getReports() != null) {
			for (Report currentVersionReport : currentVersionModule.getReports()) {
				if (analysis.getModule().getReportByUid(currentVersionReport.getUid()) == null) {
					analysis.addChangeDelete(currentVersionReport);
				}
			}
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends ApplicationEntityService<? extends ApplicationEntity>>[] getImportDependencies() {
		return (Class<? extends ApplicationEntityService<? extends ApplicationEntity>>[]) 
				new Class[] { DataSourceService.class, UserGroupService.class };
	}
	
	@Override
	public void importObjects(TransferContext context, Session session) {
		Assert.notNull(context, "context is null");
		Assert.notNull(session, "session is null");
		try {
			if (context.getModule().getReports() != null) {
				for (Report report : context.getModule().getReports()) {
					final Report currentVersionReport = findByUid(report.getUid());
					((ReportMetadata) report).setModule(context.getModule());
					if (currentVersionReport != null) {
						((ReportMetadata) currentVersionReport).copySystemFieldsTo(report);
						session.detach(currentVersionReport);
					}
					if (report.hasDataSources()) {
						for (ReportDataSource dataSource : report.getDataSources()) {
							dataSource.setReport(report);
							dataSource.setDataSource(dataSourceService.findByUid(session, dataSource.getDataSourceUid()));
							final ReportDataSource currentVersionDataSource =
								currentVersionReport != null
									? currentVersionReport.getDataSourceByUid(dataSource.getUid())
									: null;
							if (currentVersionDataSource != null) {
								currentVersionDataSource.copySystemFieldsTo(dataSource);
							}
						}
					}
					if (report.hasPermissions()) {
						for (ReportPermission permission : report.getPermissions()) {
							permission.setReport(report);
							permission.setUserGroup(userGroupService.findByUid(session, permission.getUserGroupUid()));
							final ReportPermission currentVersionPermission =
								currentVersionReport != null
									? currentVersionReport.getPermissionByUid(permission.getUid())
									: null;
							if (currentVersionPermission != null) {
								currentVersionPermission.copySystemFieldsTo(permission);
							}
						}
					}
					saveObject(report, session);
				}
			}
		}
		catch (ValidationException vex) {
			throw new RuntimeException(vex);
		}
	}
	
	@Override
	public void deleteObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, "module is null");
		Assert.notNull(currentVersionModule, "currentVersionModule is null");
		Assert.notNull(session, "session is null");
		
		if (currentVersionModule.getReports() != null) {
			for (Report currentVersionReport : currentVersionModule.getReports()) {
				if (module.getReportByUid(currentVersionReport.getUid()) == null) {
					session.delete(currentVersionReport);
				}
			}
		}
	}
	
	@Override
	@Secured("ROLE_ADMIN_REPORT")
	public void saveObject(Report report) throws ValidationException {
		super.saveObject(report);
	}
	
	@Override
	@Secured("ROLE_ADMIN_REPORT")
	public void deleteObject(Report report) throws ValidationException {
		super.deleteObject(report);
	}

	@Override
	public List<Report> findUsage(DataSource dataSource) {
		return repository.find(queryParam("dataSource", dataSource));
	}

	@Override
	protected ReportRepository getRepository() {
		return repository;
	}

	@Override
	protected ReportValidator getValidator() {
		return validator;
	}
	
}
