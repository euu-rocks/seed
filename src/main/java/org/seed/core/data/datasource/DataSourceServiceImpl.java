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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.seed.C;
import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.data.ValidationException;
import org.seed.core.data.dbobject.DBObjectService;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityDependent;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFieldGroup;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityService;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.NestedEntity;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class DataSourceServiceImpl extends AbstractApplicationEntityService<IDataSource>
	implements DataSourceService, EntityDependent<IDataSource> {
	
	@Autowired
	private EntityService entitySerice;
	
	@Autowired
	private DataSourceRepository repository;
	
	@Autowired
	private DataSourceValidator validator;
	
	@Override
	public DataSourceParameter createParameter(IDataSource dataSource) {
		Assert.notNull(dataSource, C.DATASOURCE);
		
		final DataSourceParameter param = new DataSourceParameter();
		dataSource.addParameter(param);
		return param;
	}
	
	@Override
	public List<IDataSource> findUsage(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		final List<IDataSource> result = new ArrayList<>();
		for (IDataSource dataSource : repository.find()) {
			if (dataSource.hasParameters()) {
				for (DataSourceParameter parameter : dataSource.getParameters()) {
					if (entity.equals(parameter.getReferenceEntity())) {
						result.add(dataSource);
						break;
					}
				}
			}
		}
		return result;
	}

	@Override
	public List<IDataSource> findUsage(EntityField entityField) {
		return Collections.emptyList();
	}

	@Override
	public List<IDataSource> findUsage(EntityFieldGroup fieldGroup) {
		return Collections.emptyList();
	}
	
	@Override
	public List<IDataSource> findUsage(EntityStatus entityStatus) {
		return Collections.emptyList();
	}
	
	@Override
	public List<IDataSource> findUsage(EntityFunction entityFunction) {
		return Collections.emptyList();
	}

	@Override
	public List<IDataSource> findUsage(NestedEntity nestedEntity) {
		return Collections.emptyList();
	}
	
	@Override
	public DataSourceResult query(IDataSource dataSource, Map<String, Object> parameters, Session session) {
		return repository.query(dataSource, parameters, session);
	}
	
	@Override
	public DataSourceResult query(IDataSource dataSource, Map<String, Object> parameters) {
		return repository.query(dataSource, parameters);
	}
	
	@Override
	protected void analyzeNextVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		if (analysis.getModule().getDataSources() != null) {
			for (IDataSource dataSource : analysis.getModule().getDataSources()) {
				if (currentVersionModule == null) {
					analysis.addChangeNew(dataSource);
				}
				else {
					final IDataSource currentVersionDataSource =
						currentVersionModule.getDataSourceByUid(dataSource.getUid());
					if (currentVersionDataSource == null) {
						analysis.addChangeNew(dataSource);
					}
					else if (!dataSource.isEqual(currentVersionDataSource)) {
						analysis.addChangeModify(dataSource);
					}
				}
			}
		}
	}
	
	@Override
	protected void analyzeCurrentVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		if (currentVersionModule.getDataSources() != null) {
			for (IDataSource currentVersionDataSource : currentVersionModule.getDataSources()) {
				if (analysis.getModule().getDataSourceByUid(currentVersionDataSource.getUid()) == null) {
					analysis.addChangeDelete(currentVersionDataSource);
				}
			}
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends ApplicationEntityService<ApplicationEntity>>[] getImportDependencies() {
		return new Class[] { DBObjectService.class, EntityService.class };
	}

	@Override
	public void importObjects(TransferContext context, Session session) {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(session, C.SESSION);
		
		if (context.getModule().getDataSources() != null) {
			for (IDataSource dataSource : context.getModule().getDataSources()) {
				final IDataSource currentVersionDataSource = findByUid(session, dataSource.getUid());
				((DataSourceMetadata) dataSource).setModule(context.getModule());
				if (currentVersionDataSource != null) {
					((DataSourceMetadata) currentVersionDataSource).copySystemFieldsTo(dataSource);
					session.detach(currentVersionDataSource);
				}
				if (dataSource.hasParameters()) {
					importParameters(dataSource, currentVersionDataSource, session);
				}
				repository.save(dataSource, session);
			}
		}
	}
	
	private void importParameters(IDataSource dataSource, IDataSource currentVersionDataSource, Session session) {
		for (DataSourceParameter parameter : dataSource.getParameters()) {
			parameter.setDataSource(dataSource);
			if (parameter.getReferenceEntityUid() != null) {
				parameter.setReferenceEntity(
					entitySerice.findByUid(session, parameter.getReferenceEntityUid()));
			}
			final DataSourceParameter currentVersionParameter =
				currentVersionDataSource != null
					? currentVersionDataSource.getParameterByUid(parameter.getUid())
					: null;
			if (currentVersionParameter != null) {
				currentVersionParameter.copySystemFieldsTo(parameter);
			}
		}
	}
	
	@Override
	public void deleteObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, C.MODULE);
		Assert.notNull(currentVersionModule, "currentVersionModule");
		Assert.notNull(session, C.SESSION);
		
		if (currentVersionModule.getDataSources() != null) {
			for (IDataSource currentVersionDataSource : currentVersionModule.getDataSources()) {
				if (module.getDataSourceByUid(currentVersionDataSource.getUid()) == null) {
					session.delete(currentVersionDataSource);
				}
			}
		}
	}
	
	@Override
	@Secured("ROLE_ADMIN_DATASOURCE")
	public void saveObject(IDataSource dataSource) throws ValidationException {
		Assert.notNull(dataSource, C.DATASOURCE);
		
		cleanup(dataSource);
		super.saveObject(dataSource);
	}
	
	@Override
	@Secured("ROLE_ADMIN_DATASOURCE")
	public void deleteObject(IDataSource dataSource) throws ValidationException {
		super.deleteObject(dataSource);
	}
	
	@Override
	protected DataSourceRepository getRepository() {
		return repository;
	}
	
	@Override
	protected DataSourceValidator getValidator() {
		return validator;
	}
	
	private void cleanup(IDataSource dataSource) {
		if (dataSource.hasParameters()) {
			for (DataSourceParameter parameter : dataSource.getParameters()) {
				// only reference parameters can have reference entity
				if (!parameter.getType().isReference()) {
					parameter.setReferenceEntity(null);
				}
			}
		}
	}
 
}
