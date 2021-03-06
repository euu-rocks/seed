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
import org.seed.core.customcode.CustomCode;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.datasource.DataSource;
import org.seed.core.data.dbobject.DBObject;
import org.seed.core.entity.Entity;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.transfer.Transfer;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.form.Form;
import org.seed.core.form.navigation.Menu;
import org.seed.core.report.Report;
import org.seed.core.task.Task;
import org.seed.core.user.UserGroup;

public interface Module extends SystemEntity, TransferableObject {
	
	List<DBObject> getDBObjects();
	
	List<DataSource> getDataSources();
	
	List<Entity> getEntities();
	
	List<Filter> getFilters();
	
	List<Transformer> getTransformers();
	
	List<Transfer> getTransfers();
	
	List<Form> getForms();
	
	List<Menu> getMenus();
	
	List<Task> getTasks();
	
	List<Report> getReports();
	
	List<CustomCode> getCustomCodes();
	
	List<UserGroup> getUserGroups();
	
	List<ModuleParameter> getParameters();
	
	boolean hasParameters();
	
	ModuleParameter getParameter(String name);
	
	void addParameter(ModuleParameter parameter);
	
	void removeParameter(ModuleParameter parameter);
	
	Entity getEntityByUid(String entityUid);
	
	Filter getFilterByUid(String filterUid);
	
	Transformer getTransformerByUid(String transformerUid);
	
	Transfer getTransferByUid(String transferUid);
	
	Form getFormByUid(String formUid);
	
	Menu getMenuByUid(String menuUid);
	
	Task getTaskByUid(String taskUid);
	
	UserGroup getUserGroupByUid(String groupUid);
	
	DBObject getDBObjectByUid(String objectUid);
	
	DataSource getDataSourceByUid(String dataSourceUid);
	
	CustomCode getCustomCodeByUid(String customCodeUid);
	
	Report getReportByUid(String reportUid);
	
}
