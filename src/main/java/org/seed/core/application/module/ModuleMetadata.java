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
import java.util.List;

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
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.TransferableObject;
import org.seed.core.customcode.CustomCode;
import org.seed.core.customcode.CustomCodeMetadata;
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
	private List<EntityMetadata> entities;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	private List<FilterMetadata> filters;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	private List<TransformerMetadata> transformers;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	private List<TransferMetadata> transfers;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	private List<FormMetadata> forms;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	private List<MenuMetadata> menus;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	private List<TaskMetadata> tasks;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	private List<UserGroupMetadata> userGroups;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	private List<DBObjectMetadata> dbObjects;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	private List<DataSourceMetadata> dataSources;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	private List<ReportMetadata> reports;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	private List<CustomCodeMetadata> customCodes;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	private List<RestMetadata> rests;
	
	private String uid;
	
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
	@SuppressWarnings("unchecked")
	@XmlTransient
	public List<Entity> getEntities() {
		final List<?> list = getEntityMetadata();
		return (List<Entity>) list;
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
	@SuppressWarnings("unchecked")
	@XmlTransient
	public List<Filter> getFilters() {
		final List<?> list =  getFilterMetadata();
		return (List<Filter>) list;
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
	@SuppressWarnings("unchecked")
	@XmlTransient
	public List<Transformer> getTransformers() {
		final List<?> list =  getTransformerMetadata();
		return (List<Transformer>) list;
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
	@SuppressWarnings("unchecked")
	@XmlTransient
	public List<Transfer> getTransfers() {
		final List<?> list = getTransferMetadata();
		return (List<Transfer>) list;
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
	@SuppressWarnings("unchecked")
	@XmlTransient
	public List<Form> getForms() {
		final List<?> list = getFormMetadata();
		return (List<Form>) list;
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
	@SuppressWarnings("unchecked")
	@XmlTransient
	public List<Menu> getMenus() {
		final List<?> list = getMenuMetadata();
		return (List<Menu>) list;
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
	@SuppressWarnings("unchecked")
	@XmlTransient
	public List<Task> getTasks() {
		final List<?> list = getTaskMetadata();
		return (List<Task>) list;
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
	@SuppressWarnings("unchecked")
	@XmlTransient
	public List<UserGroup> getUserGroups() {
		final List<?> list = getUserGroupMetadata();
		return (List<UserGroup>) list;
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
	@SuppressWarnings("unchecked")
	public List<DBObject> getDBObjects() {
		final List<?> list = getDbObjectMetadata();
		return (List<DBObject>) list;
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
	@SuppressWarnings("unchecked")
	public List<IDataSource> getDataSources() {
		final List<?> list = getDataSourceMetadata();
		return (List<IDataSource>) list;
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
	@SuppressWarnings("unchecked")
	public List<Report> getReports() {
		final List<?> list = getReportMetadata();
		return (List<Report>) list;
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
	@SuppressWarnings("unchecked")
	public List<CustomCode> getCustomCodes() {
		final List<?> list = getCustomCodeMetadata();
		return (List<CustomCode>) list;
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
	@SuppressWarnings("unchecked")
	public List<Rest> getRests() {
		final List<?> list = getRestMetadata();
		return (List<Rest>) list;
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
		Assert.notNull(parameter, "parameter");
		
		if (parameters == null) {
			parameters = new ArrayList<>();
		}
		parameter.setModule(this);
		parameters.add(parameter);
	}
	
	@Override
	public void removeParameter(ModuleParameter parameter) {
		Assert.notNull(parameter, "parameter");
		
		getParameters().remove(parameter);
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
