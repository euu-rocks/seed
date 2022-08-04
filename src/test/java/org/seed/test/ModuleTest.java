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
package org.seed.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.seed.core.application.module.Module;
import org.seed.core.application.module.ModuleMetadata;
import org.seed.core.application.module.ModuleParameter;
import org.seed.core.customcode.CustomCodeMetadata;
import org.seed.core.customcode.CustomLibMetadata;
import org.seed.core.data.datasource.DataSourceMetadata;
import org.seed.core.data.dbobject.DBObjectMetadata;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityMetadata;
import org.seed.core.entity.filter.FilterMetadata;
import org.seed.core.entity.transfer.TransferMetadata;
import org.seed.core.entity.transform.TransformerMetadata;
import org.seed.core.form.FormMetadata;
import org.seed.core.form.navigation.MenuMetadata;
import org.seed.core.report.ReportMetadata;
import org.seed.core.rest.RestMetadata;
import org.seed.core.task.TaskMetadata;
import org.seed.core.user.UserGroupMetadata;

public class ModuleTest {
	
	@Test
	public void testAddParameter() {
		final Module module = new ModuleMetadata();
		final ModuleParameter param = new ModuleParameter();
		assertFalse(module.hasParameters());
		
		module.addParameter(param);
		
		assertTrue(module.hasParameters());
		assertSame(module.getParameters().size(), 1);
		assertSame(module.getParameters().get(0), param);
	}
	
	@Test
	public void testAddTransferContent() {
		final Module module = new ModuleMetadata();
		final Entity entity = new EntityMetadata();
		((EntityMetadata) entity).setUid("test");
		assertFalse(module.hasTransferContent());
		
		module.addTransferContent(entity, "text".getBytes());
		
		assertTrue(module.hasTransferContent());
	}
	
	@Test
	public void testGetTransferContent() {
		final Module module = new ModuleMetadata();
		final Entity entity = new EntityMetadata();
		final byte[] content = "text".getBytes();
		((EntityMetadata) entity).setUid("test");
		assertNull(module.getTransferContent(entity));
		
		module.addTransferContent(entity, content);
		
		assertSame(module.getTransferContent(entity), content);
	}
	
	@Test
	public void testGetCustomCodeByUid() {
		final Module module = new ModuleMetadata();
		final CustomCodeMetadata customCode = new CustomCodeMetadata();
		final List<CustomCodeMetadata> customCodes = new ArrayList<>();
		((ModuleMetadata) module).setCustomCodeMetadata(customCodes);
		customCodes.add(customCode);
		customCode.setUid("other");
		assertNull(module.getCustomCodeByUid("test"));
		
		customCode.setUid("test");
		
		assertSame(module.getCustomCodeByUid("test"), customCode);
	}
	
	@Test
	public void testGetCustomLibByUid() {
		final Module module = new ModuleMetadata();
		final CustomLibMetadata customLib = new CustomLibMetadata();
		final List<CustomLibMetadata> customLibs = new ArrayList<>();
		((ModuleMetadata) module).setCustomLibMetadata(customLibs);
		customLibs.add(customLib);
		customLib.setUid("other");
		assertNull(module.getCustomLibByUid("test"));
		
		customLib.setUid("test");
		
		assertSame(module.getCustomLibByUid("test"), customLib);
	}
	
	@Test
	public void testGetDataSourceByUid() {
		final Module module = new ModuleMetadata();
		final DataSourceMetadata dataSource = new DataSourceMetadata();
		final List<DataSourceMetadata> dataSources = new ArrayList<>();
		((ModuleMetadata) module).setDataSourceMetadata(dataSources);
		dataSources.add(dataSource);
		dataSource.setUid("other");
		assertNull(module.getDataSourceByUid("test"));
		
		dataSource.setUid("test");
		
		assertSame(module.getDataSourceByUid("test"), dataSource);
	}
	
	@Test
	public void testGetDBObjectByUid() {
		final Module module = new ModuleMetadata();
		final DBObjectMetadata dbObject = new DBObjectMetadata();
		final List<DBObjectMetadata> dbObjects = new ArrayList<>();
		((ModuleMetadata) module).setDbObjectMetadata(dbObjects);
		dbObjects.add(dbObject);
		dbObject.setUid("other");
		assertNull(module.getDBObjectByUid("test"));
		
		dbObject.setUid("test");
		
		assertSame(module.getDBObjectByUid("test"), dbObject);
	}
	
	@Test
	public void testGetEntityByUid() {
		final Module module = new ModuleMetadata();
		final EntityMetadata entity = new EntityMetadata();
		final List<EntityMetadata> entities = new ArrayList<>();
		((ModuleMetadata) module).setEntityMetadata(entities);
		entities.add(entity);
		entity.setUid("other");
		assertNull(module.getEntityByUid("test"));
		
		entity.setUid("test");
		
		assertSame(module.getEntityByUid("test"), entity);
	}
	
	@Test
	public void testGetFilterByUid() {
		final Module module = new ModuleMetadata();
		final FilterMetadata filter = new FilterMetadata();
		final List<FilterMetadata> filters = new ArrayList<>();
		((ModuleMetadata) module).setFilterMetadata(filters);
		filters.add(filter);
		filter.setUid("other");
		assertNull(module.getFilterByUid("test"));
		
		filter.setUid("test");
		
		assertSame(module.getFilterByUid("test"), filter);
	}
	
	@Test
	public void testGetFormByUid() {
		final Module module = new ModuleMetadata();
		final FormMetadata form = new FormMetadata();
		final List<FormMetadata> forms = new ArrayList<>();
		((ModuleMetadata) module).setFormMetadata(forms);
		forms.add(form);
		form.setUid("other");
		assertNull(module.getFormByUid("test"));
		
		form.setUid("test");
		
		assertSame(module.getFormByUid("test"), form);
	}
	
	@Test
	public void testGetMenuByUid() {
		final Module module = new ModuleMetadata();
		final MenuMetadata menu = new MenuMetadata();
		final List<MenuMetadata> menus = new ArrayList<>();
		((ModuleMetadata) module).setMenuMetadata(menus);
		menus.add(menu);
		menu.setUid("other");
		assertNull(module.getMenuByUid("test"));
		
		menu.setUid("test");
		
		assertSame(module.getMenuByUid("test"), menu);
	}
	
	@Test
	public void testGetParameterByUid() {
		final Module module = new ModuleMetadata();
		final ModuleParameter param = new ModuleParameter();
		module.addParameter(param);
		param.setUid("other");
		assertNull(module.getParameterByUid("test"));
		
		param.setUid("test");
		
		assertSame(module.getParameterByUid("test"), param);
	}
	
	@Test
	public void testGetReportByUid() {
		final Module module = new ModuleMetadata();
		final ReportMetadata report = new ReportMetadata();
		final List<ReportMetadata> reports = new ArrayList<>();
		((ModuleMetadata) module).setReportMetadata(reports);
		reports.add(report);
		report.setUid("other");
		assertNull(module.getReportByUid("test"));
		
		report.setUid("test");
		
		assertSame(module.getReportByUid("test"), report);
	}
	
	@Test
	public void testGetRestByUid() {
		final Module module = new ModuleMetadata();
		final RestMetadata rest = new RestMetadata();
		final List<RestMetadata> rests = new ArrayList<>();
		((ModuleMetadata) module).setRestMetadata(rests);
		rests.add(rest);
		rest.setUid("other");
		assertNull(module.getRestByUid("test"));
		
		rest.setUid("test");
		
		assertSame(module.getRestByUid("test"), rest);
	}
	
	@Test
	public void testGetTaskByUid() {
		final Module module = new ModuleMetadata();
		final TaskMetadata task = new TaskMetadata();
		final List<TaskMetadata> tasks = new ArrayList<>();
		((ModuleMetadata) module).setTaskMetadata(tasks);
		tasks.add(task);
		task.setUid("other");
		assertNull(module.getTaskByUid("test"));
		
		task.setUid("test");
		
		assertSame(module.getTaskByUid("test"), task);
	}
	
	@Test
	public void testGetTransferByUid() {
		final Module module = new ModuleMetadata();
		final TransferMetadata transfer = new TransferMetadata();
		final List<TransferMetadata> transfers = new ArrayList<>();
		((ModuleMetadata) module).setTransferMetadata(transfers);
		transfers.add(transfer);
		transfer.setUid("other");
		assertNull(module.getTransferByUid("test"));
		
		transfer.setUid("test");
		
		assertSame(module.getTransferByUid("test"), transfer);
	}
	
	@Test
	public void testGetTransformerByUid() {
		final Module module = new ModuleMetadata();
		final TransformerMetadata transformer = new TransformerMetadata();
		final List<TransformerMetadata> transformers = new ArrayList<>();
		((ModuleMetadata) module).setTransformerMetadata(transformers);
		transformers.add(transformer);
		transformer.setUid("other");
		assertNull(module.getTransformerByUid("test"));
		
		transformer.setUid("test");
		
		assertSame(module.getTransformerByUid("test"), transformer);
	}
	
	@Test
	public void testgetUserGroupByUid() {
		final Module module = new ModuleMetadata();
		final UserGroupMetadata group = new UserGroupMetadata();
		final List<UserGroupMetadata> groups = new ArrayList<>();
		((ModuleMetadata) module).setUserGroupMetadata(groups);
		groups.add(group);
		group.setUid("other");
		assertNull(module.getUserGroupByUid("test"));
		
		group.setUid("test");
		
		assertSame(module.getUserGroupByUid("test"), group);
	}
	
	@Test
	public void testRemoveParameter() {
		final Module module = new ModuleMetadata();
		final ModuleParameter param = new ModuleParameter();
		module.addParameter(param);
		assertSame(module.getParameters().size(), 1);
		
		module.removeParameter(param);
		
		assertFalse(module.hasParameters());
		assertSame(module.getParameters().size(), 0);
	}

}
