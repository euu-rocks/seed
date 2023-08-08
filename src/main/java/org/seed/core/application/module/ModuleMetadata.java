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

import static org.seed.core.application.AbstractApplicationEntity.getObjectByUid;
import static org.seed.core.util.CollectionUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
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
import org.seed.core.data.Order;
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

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	
	@OneToMany(mappedBy = "parentModule",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@OrderBy("order")
	private List<NestedModule> nesteds;
	
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
	
	@JsonIgnore
	@Transient
	private SchemaVersion schemaVersion;
	
	@JsonIgnore
	@Transient
	private List<ApplicationEntity> changedObjects;
	
	@Transient
	private Map<String, byte[]> mapTransferContent;
	
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
	@JsonIgnore
	@XmlTransient
	public List<Entity> getEntities() {
		return MiscUtils.castList(getEntityMetadata());
	}
	
	@JsonIgnore
	@XmlElement(name="entity")
	@XmlElementWrapper(name="entities")
	public List<EntityMetadata> getEntityMetadata() {
		return entities;
	}
	
	public void setEntityMetadata(List<EntityMetadata> entities) {
		this.entities = entities;
	}
	
	@Override
	@JsonIgnore
	@XmlTransient
	public List<Filter> getFilters() {
		return MiscUtils.castList(getFilterMetadata());
	}
	
	@JsonIgnore
	@XmlElement(name="filter")
	@XmlElementWrapper(name="filters")
	public List<FilterMetadata> getFilterMetadata() {
		return filters;
	}
	
	public void setFilterMetadata(List<FilterMetadata> filters) {
		this.filters = filters;
	}
	
	@Override
	@JsonIgnore
	@XmlTransient
	public List<Transformer> getTransformers() {
		return MiscUtils.castList(getTransformerMetadata());
	}
	
	@JsonIgnore
	@XmlElement(name="transformer")
	@XmlElementWrapper(name="transformers")
	public List<TransformerMetadata> getTransformerMetadata() {
		return transformers;
	}
	
	public void setTransformerMetadata(List<TransformerMetadata> transformers) {
		this.transformers = transformers;
	}
	
	@Override
	@JsonIgnore
	@XmlTransient
	public List<Transfer> getTransfers() {
		return MiscUtils.castList(getTransferMetadata());
	}
	
	@JsonIgnore
	@XmlElement(name="transfer")
	@XmlElementWrapper(name="transfers")
	public List<TransferMetadata> getTransferMetadata() {
		return transfers;
	}

	public void setTransferMetadata(List<TransferMetadata> tranfers) {
		this.transfers = tranfers;
	}

	@Override
	@JsonIgnore
	@XmlTransient
	public List<Form> getForms() {
		return MiscUtils.castList(getFormMetadata());
	}
	
	@JsonIgnore
	@XmlElement(name="form")
	@XmlElementWrapper(name="forms")
	public List<FormMetadata> getFormMetadata() {
		return forms;
	}
	
	public void setFormMetadata(List<FormMetadata> forms) {
		this.forms = forms;
	}
	
	@Override
	@JsonIgnore
	@XmlTransient
	public List<Menu> getMenus() {
		return MiscUtils.castList(getMenuMetadata());
	}
	
	@JsonIgnore
	@XmlElement(name="menu")
	@XmlElementWrapper(name="menus")
	public List<MenuMetadata> getMenuMetadata() {
		return menus;
	}
	
	public void setMenuMetadata(List<MenuMetadata> menus) {
		this.menus = menus;
	}
	
	@Override
	@JsonIgnore
	@XmlTransient
	public List<Task> getTasks() {
		return MiscUtils.castList(getTaskMetadata());
	}
	
	@JsonIgnore
	@XmlElement(name="task")
	@XmlElementWrapper(name="tasks")
	public List<TaskMetadata> getTaskMetadata() {
		return tasks;
	}
	
	public void setTaskMetadata(List<TaskMetadata> tasks) {
		this.tasks = tasks;
	}
	
	@Override
	@JsonIgnore
	@XmlTransient
	public List<UserGroup> getUserGroups() {
		return MiscUtils.castList(getUserGroupMetadata());
	}
	
	@JsonIgnore
	@XmlElement(name="usergroup")
	@XmlElementWrapper(name="usergroups")
	public List<UserGroupMetadata> getUserGroupMetadata() {
		return userGroups;
	}
	
	public void setUserGroupMetadata(List<UserGroupMetadata> userGroups) {
		this.userGroups = userGroups;
	}
	
	@Override
	@JsonIgnore
	@XmlTransient
	public List<DBObject> getDBObjects() {
		return MiscUtils.castList(getDbObjectMetadata());
	}
	
	@JsonIgnore
	@XmlElement(name="dbobject")
	@XmlElementWrapper(name="dbobjects")
	public List<DBObjectMetadata> getDbObjectMetadata() {
		return dbObjects;
	}

	public void setDbObjectMetadata(List<DBObjectMetadata> dbObjects) {
		this.dbObjects = dbObjects;
	}
	
	@Override
	@JsonIgnore
	@XmlTransient
	public List<IDataSource> getDataSources() {
		return MiscUtils.castList(getDataSourceMetadata());
	}
	
	@JsonIgnore
	@XmlElement(name="datasource")
	@XmlElementWrapper(name="datasources")
	public List<DataSourceMetadata> getDataSourceMetadata() {
		return dataSources;
	}

	public void setDataSourceMetadata(List<DataSourceMetadata> dataSources) {
		this.dataSources = dataSources;
	}
	
	@Override
	@JsonIgnore
	@XmlTransient
	public List<Report> getReports() {
		return MiscUtils.castList(getReportMetadata());
	}
	
	@JsonIgnore
	@XmlElement(name="report")
	@XmlElementWrapper(name="reports")
	public List<ReportMetadata> getReportMetadata() {
		return reports;
	}

	public void setReportMetadata(List<ReportMetadata> resports) {
		this.reports = resports;
	}
	
	@Override
	@JsonIgnore
	@XmlTransient
	public List<CustomCode> getCustomCodes() {
		return MiscUtils.castList(getCustomCodeMetadata());
	}
	
	@JsonIgnore
	@XmlElement(name="customcode")
	@XmlElementWrapper(name="customcodes")
	public List<CustomCodeMetadata> getCustomCodeMetadata() {
		return customCodes;
	}

	public void setCustomCodeMetadata(List<CustomCodeMetadata> customCodes) {
		this.customCodes = customCodes;
	}
	
	@Override
	@JsonIgnore
	@XmlTransient
	public List<CustomLib> getCustomLibs() {
		return MiscUtils.castList(getCustomLibMetadata());
	}
	
	@JsonIgnore
	@XmlElement(name="customlib")
	@XmlElementWrapper(name="customlibs")
	public List<CustomLibMetadata> getCustomLibMetadata() {
		return customLibs;
	}

	public void setCustomLibMetadata(List<CustomLibMetadata> customLibs) {
		this.customLibs = customLibs;
	}

	@Override
	@JsonIgnore
	@XmlTransient
	public List<Rest> getRests() {
		return MiscUtils.castList(getRestMetadata());
	}
	
	@JsonIgnore
	@XmlElement(name="restservice")
	@XmlElementWrapper(name="restservices")
	public List<RestMetadata> getRestMetadata() {
		return rests;
	}
	
	public void setRestMetadata(List<RestMetadata> rests) {
		this.rests = rests;
	}

	@Override
	@JsonIgnore
	@XmlElement(name="parameter")
	@XmlElementWrapper(name="parameters")
	public List<ModuleParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<ModuleParameter> parameters) {
		this.parameters = parameters;
	}
	
	@Override
	@JsonIgnore
	public boolean hasParameters() {
		return notEmpty(getParameters());
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
	@XmlElement(name="nested")
	@XmlElementWrapper(name="nesteds")
	public List<NestedModule> getNesteds() {
		return nesteds;
	}

	public void setNesteds(List<NestedModule> nesteds) {
		this.nesteds = nesteds;
	}
	
	@Override
	public boolean hasNesteds() {
		return notEmpty(getNesteds());
	}
	
	@Override
	public void addNested(NestedModule nested) {
		Assert.notNull(nested, C.NESTED);
		
		if (nesteds == null) {
			nesteds = new ArrayList<>();
		}
		nested.setParentModule(this);
		nesteds.add(nested);
	}
	
	@Override
	public void removeNested(NestedModule nested) {
		Assert.notNull(nested, C.NESTED);
		
		getNesteds().remove(nested);
	}

	@Override
	@JsonIgnore
	public String getFileName() {
		return getInternalName() + '_' +
				   MiscUtils.getTimestampString() +
				   ModuleTransfer.MODULE_FILE_EXTENSION;
	}
	
	@JsonIgnore
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
	@JsonIgnore
	@XmlTransient
	public List<Entity> getTransferableEntities() {
		return MiscUtils.castList(subList(getEntityMetadata(), Entity::isTransferable));
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
	public boolean hasTransferContent() {
		return notEmpty(mapTransferContent);
	}
	
	@Override
	public byte[] getTransferContent(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		return mapTransferContent != null
				? mapTransferContent.get(entity.getUid())
				: null;
	}
	
	@Override
	public boolean containsModule(Module module) { // search recursively
		Assert.notNull(module, C.MODULE);
		
		return anyMatch(getNesteds(), 
						nested -> module.equals(nested.getNestedModule()) ||
								  nested.containsModule(module));
	}
	
	@Override
	public boolean containsNestedModule(Module module) {
		Assert.notNull(module, C.MODULE);
		
		return anyMatch(getNesteds(), nested -> module.equals(nested.getNestedModule()));
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
	public NestedModule getNestedByUid(String nestedUid) {
		Assert.notNull(nestedUid, "nestedUid");
		
		return getObjectByUid(getNesteds(), nestedUid);
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
	
	@Override
	protected void setOrderIndexes() {
		Order.setOrderIndexes(getNesteds());
	}
	
}
