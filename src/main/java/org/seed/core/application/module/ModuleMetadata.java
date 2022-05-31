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
package org.seed.core.application.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.C;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.TransferableObject;
import org.seed.core.config.SchemaVersion;
import org.seed.core.customcode.CustomCode;
import org.seed.core.customcode.CustomCodeMetadata;
import org.seed.core.customcode.CustomLib;
import org.seed.core.customcode.CustomLibMetadata;
import org.seed.core.data.AbstractSystemEntity;
import org.seed.core.data.datasource.IDataSource;
import org.seed.core.data.datasource.DataSourceMetadata;
import org.seed.core.data.dbobject.DBObject;
import org.seed.core.data.dbobject.DBObjectMetadata;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityMetadata;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.filter.FilterMetadata;
import org.seed.core.entity.transfer.Transfer;
import org.seed.core.entity.transfer.TransferMetadata;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerMetadata;
import org.seed.core.form.Form;
import org.seed.core.form.FormMetadata;
import org.seed.core.form.navigation.Menu;
import org.seed.core.form.navigation.MenuMetadata;
import org.seed.core.report.Report;
import org.seed.core.report.ReportMetadata;
import org.seed.core.rest.Rest;
import org.seed.core.rest.RestMetadata;
import org.seed.core.task.Task;
import org.seed.core.task.TaskMetadata;
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserGroupMetadata;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;

import org.springframework.util.ObjectUtils;

import static org.seed.core.application.AbstractApplicationEntity.getObjectByUid;

@javax.persistence.Entity
@Table(name = "sys_module")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@XmlRootElement(name="module")
public class ModuleMetadata extends AbstractSystemEntity 
	implements Module, TransferableObject {
	
	@OneToMany(mappedBy = "module",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<ModuleParameter> parameters;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<EntityMetadata> entities;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<FilterMetadata> filters;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<TransformerMetadata> transformers;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<TransferMetadata> transfers;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<FormMetadata> forms;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<MenuMetadata> menus;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<TaskMetadata> tasks;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<UserGroupMetadata> userGroups;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<DBObjectMetadata> dbObjects;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<DataSourceMetadata> dataSources;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<ReportMetadata> reports;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<CustomCodeMetadata> customCodes;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<CustomLibMetadata> customLibs;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<RestMetadata> rests;
	
	private String uid;
	
	@Transient
	private SchemaVersion schemaVersion;
	
	@Transient
	private Map<String, byte[]> mapTransferContent;
	
	@Transient
	private List<ApplicationEntity> changedObjects;
	
	@Override
	@XmlAttribute
	public String getUid() {
		return uid;
	}
	
	@Override
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	@Override
	@XmlAttribute
	public SchemaVersion getSchemaVersion() {
		return schemaVersion;
	}

	void setSchemaVersion(SchemaVersion schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	@Override
	@XmlTransient
	public List<Entity> getEntities() {
		return MiscUtils.castList(getEntityMetadata());
	}
	
	@XmlElement(name="entity")
	@XmlElementWrapper(name="entities")
	public List<EntityMetadata> getEntityMetadata() {
		return entities;
	}
	
	public void setEntityMetadata(List<EntityMetadata> entities) {
		this.entities = entities;
	}
	
	@Override
	@XmlTransient
	public List<Filter> getFilters() {
		return MiscUtils.castList(getFilterMetadata());
	}
	
	@XmlElement(name="filter")
	@XmlElementWrapper(name="filters")
	public List<FilterMetadata> getFilterMetadata() {
		return filters;
	}
	
	public void setFilterMetadata(List<FilterMetadata> filters) {
		this.filters = filters;
	}
	
	@Override
	@XmlTransient
	public List<Transformer> getTransformers() {
		return MiscUtils.castList(getTransformerMetadata());
	}
	
	@XmlElement(name="transformer")
	@XmlElementWrapper(name="transformers")
	public List<TransformerMetadata> getTransformerMetadata() {
		return transformers;
	}
	
	public void setTransformerMetadata(List<TransformerMetadata> transformers) {
		this.transformers = transformers;
	}
	
	@Override
	@XmlTransient
	public List<Transfer> getTransfers() {
		return MiscUtils.castList(getTransferMetadata());
	}
	
	@XmlElement(name="transfer")
	@XmlElementWrapper(name="transfers")
	public List<TransferMetadata> getTransferMetadata() {
		return transfers;
	}

	public void setTransferMetadata(List<TransferMetadata> tranfers) {
		this.transfers = tranfers;
	}

	@Override
	@XmlTransient
	public List<Form> getForms() {
		return MiscUtils.castList(getFormMetadata());
	}
	
	@XmlElement(name="form")
	@XmlElementWrapper(name="forms")
	public List<FormMetadata> getFormMetadata() {
		return forms;
	}
	
	public void setFormMetadata(List<FormMetadata> forms) {
		this.forms = forms;
	}
	
	@Override
	@XmlTransient
	public List<Menu> getMenus() {
		return MiscUtils.castList(getMenuMetadata());
	}
	
	@XmlElement(name="menu")
	@XmlElementWrapper(name="menus")
	public List<MenuMetadata> getMenuMetadata() {
		return menus;
	}
	
	public void setMenuMetadata(List<MenuMetadata> menus) {
		this.menus = menus;
	}
	
	@Override
	@XmlTransient
	public List<Task> getTasks() {
		return MiscUtils.castList(getTaskMetadata());
	}
	
	@XmlElement(name="task")
	@XmlElementWrapper(name="tasks")
	public List<TaskMetadata> getTaskMetadata() {
		return tasks;
	}
	
	public void setTaskMetadata(List<TaskMetadata> tasks) {
		this.tasks = tasks;
	}
	
	@Override
	@XmlTransient
	public List<UserGroup> getUserGroups() {
		return MiscUtils.castList(getUserGroupMetadata());
	}
	
	@XmlElement(name="usergroup")
	@XmlElementWrapper(name="usergroups")
	public List<UserGroupMetadata> getUserGroupMetadata() {
		return userGroups;
	}
	
	public void setUserGroupMetadata(List<UserGroupMetadata> userGroups) {
		this.userGroups = userGroups;
	}
	
	@Override
	public List<DBObject> getDBObjects() {
		return MiscUtils.castList(getDbObjectMetadata());
	}
	
	@XmlElement(name="dbobject")
	@XmlElementWrapper(name="dbobjects")
	public List<DBObjectMetadata> getDbObjectMetadata() {
		return dbObjects;
	}

	public void setDbObjectMetadata(List<DBObjectMetadata> dbObjects) {
		this.dbObjects = dbObjects;
	}
	
	@Override
	public List<IDataSource> getDataSources() {
		return MiscUtils.castList(getDataSourceMetadata());
	}

	@XmlElement(name="datasource")
	@XmlElementWrapper(name="datasources")
	public List<DataSourceMetadata> getDataSourceMetadata() {
		return dataSources;
	}

	public void setDataSourceMetadata(List<DataSourceMetadata> dataSources) {
		this.dataSources = dataSources;
	}
	
	@Override
	public List<Report> getReports() {
		return MiscUtils.castList(getReportMetadata());
	}
	
	@XmlElement(name="report")
	@XmlElementWrapper(name="reports")
	public List<ReportMetadata> getReportMetadata() {
		return reports;
	}

	public void setReportMetadata(List<ReportMetadata> resports) {
		this.reports = resports;
	}
	
	@Override
	public List<CustomCode> getCustomCodes() {
		return MiscUtils.castList(getCustomCodeMetadata());
	}
	
	@XmlElement(name="customcode")
	@XmlElementWrapper(name="customcodes")
	public List<CustomCodeMetadata> getCustomCodeMetadata() {
		return customCodes;
	}

	public void setCustomCodeMetadata(List<CustomCodeMetadata> customCodes) {
		this.customCodes = customCodes;
	}
	
	@Override
	public List<CustomLib> getCustomLibs() {
		return MiscUtils.castList(getCustomLibMetadata());
	}
	
	@XmlElement(name="customlib")
	@XmlElementWrapper(name="customlibs")
	public List<CustomLibMetadata> getCustomLibMetadata() {
		return customLibs;
	}

	public void setCustomLibMetadata(List<CustomLibMetadata> customLibs) {
		this.customLibs = customLibs;
	}

	@Override
	public List<Rest> getRests() {
		return MiscUtils.castList(getRestMetadata());
	}
	
	@XmlElement(name="restservice")
	@XmlElementWrapper(name="restservices")
	public List<RestMetadata> getRestMetadata() {
		return rests;
	}
	
	public void setRestMetadata(List<RestMetadata> rests) {
		this.rests = rests;
	}

	@Override
	@XmlElement(name="parameter")
	@XmlElementWrapper(name="parameters")
	public List<ModuleParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<ModuleParameter> parameters) {
		this.parameters = parameters;
	}
	
	@Override
	public boolean hasParameters() {
		return !ObjectUtils.isEmpty(getParameters());
	}
	
	@Override
	public ModuleParameter getParameter(String name) {
		return getObjectByName(getParameters(), name, true);
	}
	
	@Override
	public void addParameter(ModuleParameter parameter) {
		Assert.notNull(parameter, C.PARAMETER);
		
		if (parameters == null) {
			parameters = new ArrayList<>();
		}
		parameter.setModule(this);
		parameters.add(parameter);
	}
	
	@Override
	public void removeParameter(ModuleParameter parameter) {
		Assert.notNull(parameter, C.PARAMETER);
		
		getParameters().remove(parameter);
	}
	
	@Override
	public String getFileName() {
		return getInternalName() + '_' +
				   MiscUtils.getTimestampString() +
				   ModuleTransfer.MODULE_FILE_EXTENSION;
	}

	@XmlTransient
	public List<ApplicationEntity> getChangedObjects() {
		return changedObjects;
	}

	public void setChangedObjects(List<ApplicationEntity> changedObjects) {
		this.changedObjects = changedObjects;
	}
	
	@Override
	public void removeNewObjects() {
		removeNewObjects(getParameters());
	}
	
	@Override
	public List<Entity> getTransferableEntities() {
		return getEntityMetadata().stream().filter(Entity::isTransferable)
								           .collect(Collectors.toList());
	}
	
	@Override
	public void addTransferContent(Entity entity, byte[] content) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(content, C.CONTENT);
		
		if (mapTransferContent == null) {
			mapTransferContent = new HashMap<>();
		}
		mapTransferContent.put(entity.getUid(), content);
	}
	
	@Override
	public byte[] getTransferContent(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		return mapTransferContent != null
				? mapTransferContent.get(entity.getUid())
				: null;
	}
	
	@Override
	public ModuleParameter getParameterByUid(String parameterUid) {
		Assert.notNull(parameterUid, "parameterUid");
		
		return getObjectByUid(getParameters(), parameterUid);
	}
	
	@Override
	public Entity getEntityByUid(String entityUid) {
		Assert.notNull(entityUid, "entityUid");
		
		return getObjectByUid(getEntities(), entityUid);
	}
	
	@Override
	public Filter getFilterByUid(String filterUid) {
		Assert.notNull(filterUid, "entityUid");
		
		return getObjectByUid(getFilters(), filterUid);
	}
	
	@Override
	public Transformer getTransformerByUid(String transformerUid) {
		Assert.notNull(transformerUid, "transformerUid");
		
		return getObjectByUid(getTransformers(), transformerUid);
	}
	
	@Override
	public Transfer getTransferByUid(String transferUid) {
		Assert.notNull(transferUid, "transferUid");
		
		return getObjectByUid(getTransfers(), transferUid);
	}
	
	@Override
	public Form getFormByUid(String formUid) {
		Assert.notNull(formUid, "formUid");
		
		return getObjectByUid(getForms(), formUid);
	}
	
	@Override
	public Menu getMenuByUid(String menuUid) {
		Assert.notNull(menuUid, "menuUid");
		
		return getObjectByUid(getMenus(), menuUid);
	}
	
	@Override
	public Task getTaskByUid(String taskUid) {
		Assert.notNull(taskUid, "taskUid");
		
		return getObjectByUid(getTasks(), taskUid);
	}
	
	@Override
	public UserGroup getUserGroupByUid(String groupUid) {
		Assert.notNull(groupUid, "groupUid");
		
		return getObjectByUid(getUserGroups(), groupUid);
	}
	
	@Override
	public DBObject getDBObjectByUid(String objectUid) {
		Assert.notNull(objectUid, "objectUid");
	
		return getObjectByUid(getDBObjects(), objectUid);
	}
	
	@Override
	public IDataSource getDataSourceByUid(String dataSourceUid) {
		Assert.notNull(dataSourceUid, "dataSourceUid");
		
		return getObjectByUid(getDataSources(), dataSourceUid);
	}
	
	@Override
	public CustomCode getCustomCodeByUid(String customCodeUid) {
		Assert.notNull(customCodeUid, "customCodeUid");
		
		return getObjectByUid(getCustomCodes(), customCodeUid);
	}
	
	@Override
	public CustomLib getCustomLibByUid(String customLibUid) {
		Assert.notNull(customLibUid, "customLibUid");
		
		return getObjectByUid(getCustomLibs(), customLibUid);
	}
	
	@Override
	public Report getReportByUid(String reportUid) {
		Assert.notNull(reportUid, "reportUid");
		
		return getObjectByUid(getReports(), reportUid);
	}
	
	@Override
	public Rest getRestByUid(String restUid) {
		Assert.notNull(restUid, "restUid");
		
		return getObjectByUid(getRests(), restUid);
	}
	
}
