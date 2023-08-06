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

import java.util.List;

import org.seed.core.application.TransferableObject;
import org.seed.core.config.SchemaVersion;
import org.seed.core.customcode.CustomCode;
import org.seed.core.customcode.CustomLib;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.datasource.IDataSource;
import org.seed.core.data.dbobject.DBObject;
import org.seed.core.entity.Entity;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.transfer.Transfer;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.form.Form;
import org.seed.core.form.navigation.Menu;
import org.seed.core.report.Report;
import org.seed.core.rest.Rest;
import org.seed.core.task.Task;
import org.seed.core.user.UserGroup;

public interface Module extends SystemEntity, TransferableObject {
	
	SchemaVersion getSchemaVersion();
	
	List<DBObject> getDBObjects();
	
	List<IDataSource> getDataSources();
	
	List<Entity> getEntities();
	
	List<Entity> getTransferableEntities();
	
	List<Filter> getFilters();
	
	List<Transformer> getTransformers();
	
	List<Transfer> getTransfers();
	
	List<Form> getForms();
	
	List<Menu> getMenus();
	
	List<Task> getTasks();
	
	List<NestedModule> getNesteds();
	
	List<Report> getReports();
	
	List<CustomCode> getCustomCodes();
	
	List<CustomLib> getCustomLibs();
	
	List<Rest> getRests();
	
	List<UserGroup> getUserGroups();
	
	List<ModuleParameter> getParameters();
	
	boolean hasParameters();
	
	boolean hasNesteds();
	
	boolean containsNestedModule(Module module);
	
	ModuleParameter getParameter(String name);
	
	void addParameter(ModuleParameter parameter);
	
	void addNested(NestedModule nested);
	
	void removeParameter(ModuleParameter parameter);
	
	void removeNested(NestedModule nested);
	
	String getFileName();
	
	ModuleParameter getParameterByUid(String parameterUid);
	
	NestedModule getNestedByUid(String nestedUid);
	
	Entity getEntityByUid(String entityUid);
	
	Filter getFilterByUid(String filterUid);
	
	Transformer getTransformerByUid(String transformerUid);
	
	Transfer getTransferByUid(String transferUid);
	
	Form getFormByUid(String formUid);
	
	Menu getMenuByUid(String menuUid);
	
	Task getTaskByUid(String taskUid);
	
	UserGroup getUserGroupByUid(String groupUid);
	
	DBObject getDBObjectByUid(String objectUid);
	
	IDataSource getDataSourceByUid(String dataSourceUid);
	
	CustomCode getCustomCodeByUid(String customCodeUid);
	
	CustomLib getCustomLibByUid(String customLibUid);
	
	Report getReportByUid(String reportUid);
	
	Rest getRestByUid(String restUid);
	
	void addTransferContent(Entity entity, byte[] content);
	
	boolean hasTransferContent();
	
	byte[] getTransferContent(Entity entity);
	
}
