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

import static org.seed.core.util.CollectionUtils.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.data.Options;
import org.seed.core.data.ValidationException;
import org.seed.core.data.datasource.IDataSource;
import org.seed.core.data.datasource.DataSourceDependent;
import org.seed.core.data.datasource.DataSourceResult;
import org.seed.core.data.datasource.DataSourceService;
import org.seed.core.user.User;
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserGroupDependent;
import org.seed.core.user.UserGroupService;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class ReportServiceImpl extends AbstractApplicationEntityService<Report>
	implements ReportService, DataSourceDependent<Report>, UserGroupDependent<Report> {
	
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
	public List<Report> getReports(User user, Session session) {
		Assert.notNull(user, C.USER);
		
		return subList(getObjects(session), report -> report.checkPermissions(user));
	}
	
	@Override
	@Secured("ROLE_ADMIN_REPORT")
	public ReportDataSource createDataSource(Report report) {
		Assert.notNull(report, C.REPORT);
		
		final ReportDataSource dataSource = new ReportDataSource();
		report.addDataSource(dataSource);
		return dataSource;
	}
	
	@Override
	public byte[] generateReport(Report report, ReportFormat format, Session session) throws ValidationException {
		Assert.notNull(report, C.REPORT);
		Assert.notNull(session, C.SESSION);
		Assert.notNull(format, "format");
		
		validator.validateGenerate(report);
		final ReportGenerator generator = generatorProvider.getGenerator(report, format);
		if (report.hasDataSources()) {
			// query data sources
			for (ReportDataSource reportDataSource : report.getDataSources()) {
				final Map<String,Object> parameterMap = new HashMap<>();
				final IDataSource dataSource = reportDataSource.getDataSource();
				if (dataSource.hasParameters()) {
					dataSource.getParameters()
							  .forEach(param -> parameterMap.put(param.getName(),param.getValue()));
				}
				final DataSourceResult result = dataSourceService.query(dataSource, parameterMap, session);
				generator.addDataSourceResult(reportDataSource, result);
			}
		}
		return generator.generate();
	}
	
	@Override
	public List<ReportPermission> getAvailablePermissions(Report report, Session session) {
		Assert.notNull(report, C.REPORT);
		Assert.notNull(report, C.SESSION);
		
		return filterAndConvert(userGroupService.findNonSystemGroups(session), 
								not(report::containsPermission), 
								group -> createPermission(report, group));
	}
	
	@Override
	protected void analyzeNextVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		Assert.notNull(analysis, C.ANALYSIS);
		
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
	}
	
	@Override
	protected void analyzeCurrentVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		filterAndForEach(currentVersionModule.getReports(), 
						 report -> analysis.getModule().getReportByUid(report.getUid()) == null, 
						 analysis::addChangeDelete);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends ApplicationEntityService<ApplicationEntity>>[] getImportDependencies() {
		return new Class[] { DataSourceService.class, UserGroupService.class };
	}
	
	@Override
	public void importObjects(TransferContext context, Session session) throws ValidationException {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(session, C.SESSION);
		
		if (context.getModule().getReports() != null) {
			for (Report report : context.getModule().getReports()) {
				final Report currentVersionReport = findByUid(session, report.getUid());
				((ReportMetadata) report).setModule(context.getModule());
				if (currentVersionReport != null) {
					((ReportMetadata) currentVersionReport).copySystemFieldsTo(report);
					session.detach(currentVersionReport);
				}
				if (report.hasDataSources()) {
					initDataSources(report, currentVersionReport, session);
				}
				if (report.hasPermissions()) {
					initPermissions(report, currentVersionReport, session);
				}
				saveObject(report, session);
			}
		}
	}
	
	private void initDataSources(Report report, Report currentVersionReport, Session session) {
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
	
	private void initPermissions(Report report, Report currentVersionReport, Session session) {
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
	
	@Override
	public void deleteObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, C.MODULE);
		Assert.notNull(currentVersionModule, "currentVersionModule");
		Assert.notNull(session, C.SESSION);
		
		filterAndForEach(currentVersionModule.getReports(), 
						 report -> module.getReportByUid(report.getUid()) == null, 
						 session::delete);
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
	public List<Report> findUsage(IDataSource dataSource, Session session) {
		Assert.notNull(dataSource, C.DATASOURCE);
		Assert.notNull(session, C.SESSION);
		
		return subList(getObjects(session),
					   report -> anyMatch(report.getDataSources(), 
							   			  reportDS -> dataSource.equals(reportDS.getDataSource())));
	}
	
	@Override
	public List<Report> findUsage(UserGroup userGroup, Session session) {
		Assert.notNull(userGroup, C.USERGROUP);
		Assert.notNull(session, C.SESSION);
		
		return subList(getObjects(session), 
					   report -> anyMatch(report.getPermissions(), 
										  perm -> userGroup.equals(perm.getUserGroup())));
	}

	@Override
	protected ReportRepository getRepository() {
		return repository;
	}

	@Override
	protected ReportValidator getValidator() {
		return validator;
	}
	
	private static ReportPermission createPermission(Report report, UserGroup group) {
		final ReportPermission permission = new ReportPermission();
		permission.setReport(report);
		permission.setUserGroup(group);
		return permission;
	}

}
