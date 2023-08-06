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
package org.seed.test.unit.module;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.seed.core.application.module.Module;
import org.seed.core.application.module.ModuleMetadata;
import org.seed.core.application.module.ModuleParameter;
import org.seed.core.application.module.NestedModule;
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

class ModuleTest {
	
	@Test
	void testAddParameter() {
		final Module module = new ModuleMetadata();
		final ModuleParameter param = new ModuleParameter();
		assertFalse(module.hasParameters());
		
		module.addParameter(param);
		
		assertTrue(module.hasParameters());
		assertSame(1, module.getParameters().size());
		assertSame(param, module.getParameters().get(0));
	}
	
	@Test
	void testAddNested() {
		final Module module = new ModuleMetadata();
		final NestedModule nested = new NestedModule();
		assertFalse(module.hasNesteds());
		
		module.addNested(nested);
		
		assertTrue(module.hasNesteds());
		assertSame(1, module.getNesteds().size());
		assertSame(nested, module.getNesteds().get(0));
	}
	
	@Test
	void testAddTransferContent() {
		final Module module = new ModuleMetadata();
		final Entity entity = new EntityMetadata();
		((EntityMetadata) entity).setUid("test");
		assertFalse(module.hasTransferContent());
		
		module.addTransferContent(entity, "text".getBytes());
		
		assertTrue(module.hasTransferContent());
	}
	
	@Test
	void testContainsNestedModule() {
		final Module module = new ModuleMetadata();
		final NestedModule nested = new NestedModule();
		final Module nestedModule = new ModuleMetadata();
		nested.setNestedModule(nestedModule);
		assertFalse(module.containsNestedModule(nestedModule));
		module.addNested(nested);
		assertTrue(module.containsNestedModule(nestedModule));
	}
	
	@Test
	void testGetTransferContent() {
		final Module module = new ModuleMetadata();
		final Entity entity = new EntityMetadata();
		final byte[] content = "text".getBytes();
		((EntityMetadata) entity).setUid("test");
		assertNull(module.getTransferContent(entity));
		
		module.addTransferContent(entity, content);
		
		assertSame(content, module.getTransferContent(entity));
	}
	
	@Test
	void testGetCustomCodeByUid() {
		final Module module = new ModuleMetadata();
		final CustomCodeMetadata customCode = new CustomCodeMetadata();
		final List<CustomCodeMetadata> customCodes = new ArrayList<>();
		((ModuleMetadata) module).setCustomCodeMetadata(customCodes);
		customCodes.add(customCode);
		customCode.setUid("other");
		assertNull(module.getCustomCodeByUid("test"));
		
		customCode.setUid("test");
		
		assertSame(customCode, module.getCustomCodeByUid("test"));
	}
	
	@Test
	void testGetCustomLibByUid() {
		final Module module = new ModuleMetadata();
		final CustomLibMetadata customLib = new CustomLibMetadata();
		final List<CustomLibMetadata> customLibs = new ArrayList<>();
		((ModuleMetadata) module).setCustomLibMetadata(customLibs);
		customLibs.add(customLib);
		customLib.setUid("other");
		assertNull(module.getCustomLibByUid("test"));
		
		customLib.setUid("test");
		
		assertSame(customLib, module.getCustomLibByUid("test"));
	}
	
	@Test
	void testGetDataSourceByUid() {
		final Module module = new ModuleMetadata();
		final DataSourceMetadata dataSource = new DataSourceMetadata();
		final List<DataSourceMetadata> dataSources = new ArrayList<>();
		((ModuleMetadata) module).setDataSourceMetadata(dataSources);
		dataSources.add(dataSource);
		dataSource.setUid("other");
		assertNull(module.getDataSourceByUid("test"));
		
		dataSource.setUid("test");
		
		assertSame(dataSource, module.getDataSourceByUid("test"));
	}
	
	@Test
	void testGetDBObjectByUid() {
		final Module module = new ModuleMetadata();
		final DBObjectMetadata dbObject = new DBObjectMetadata();
		final List<DBObjectMetadata> dbObjects = new ArrayList<>();
		((ModuleMetadata) module).setDbObjectMetadata(dbObjects);
		dbObjects.add(dbObject);
		dbObject.setUid("other");
		assertNull(module.getDBObjectByUid("test"));
		
		dbObject.setUid("test");
		
		assertSame(dbObject, module.getDBObjectByUid("test"));
	}
	
	@Test
	void testGetEntityByUid() {
		final Module module = new ModuleMetadata();
		final EntityMetadata entity = new EntityMetadata();
		final List<EntityMetadata> entities = new ArrayList<>();
		((ModuleMetadata) module).setEntityMetadata(entities);
		entities.add(entity);
		entity.setUid("other");
		assertNull(module.getEntityByUid("test"));
		
		entity.setUid("test");
		
		assertSame(entity, module.getEntityByUid("test"));
	}
	
	@Test
	void testGetFilterByUid() {
		final Module module = new ModuleMetadata();
		final FilterMetadata filter = new FilterMetadata();
		final List<FilterMetadata> filters = new ArrayList<>();
		((ModuleMetadata) module).setFilterMetadata(filters);
		filters.add(filter);
		filter.setUid("other");
		assertNull(module.getFilterByUid("test"));
		
		filter.setUid("test");
		
		assertSame(filter, module.getFilterByUid("test"));
	}
	
	@Test
	void testGetFormByUid() {
		final Module module = new ModuleMetadata();
		final FormMetadata form = new FormMetadata();
		final List<FormMetadata> forms = new ArrayList<>();
		((ModuleMetadata) module).setFormMetadata(forms);
		forms.add(form);
		form.setUid("other");
		assertNull(module.getFormByUid("test"));
		
		form.setUid("test");
		
		assertSame(form, module.getFormByUid("test"));
	}
	
	@Test
	void testGetMenuByUid() {
		final Module module = new ModuleMetadata();
		final MenuMetadata menu = new MenuMetadata();
		final List<MenuMetadata> menus = new ArrayList<>();
		((ModuleMetadata) module).setMenuMetadata(menus);
		menus.add(menu);
		menu.setUid("other");
		assertNull(module.getMenuByUid("test"));
		
		menu.setUid("test");
		
		assertSame(menu, module.getMenuByUid("test"));
	}
	
	@Test
	void testGetParameterByUid() {
		final Module module = new ModuleMetadata();
		final ModuleParameter param = new ModuleParameter();
		module.addParameter(param);
		param.setUid("other");
		assertNull(module.getParameterByUid("test"));
		
		param.setUid("test");
		
		assertSame(param, module.getParameterByUid("test"));
	}
	
	@Test
	void testGetNestedByUid() {
		final Module module = new ModuleMetadata();
		final NestedModule nested = new NestedModule();
		module.addNested(nested);
		nested.setUid("other");
		assertNull(module.getNestedByUid("test"));
		
		nested.setUid("test");
		
		assertSame(nested, module.getNestedByUid("test"));
	}
	
	@Test
	void testGetReportByUid() {
		final Module module = new ModuleMetadata();
		final ReportMetadata report = new ReportMetadata();
		final List<ReportMetadata> reports = new ArrayList<>();
		((ModuleMetadata) module).setReportMetadata(reports);
		reports.add(report);
		report.setUid("other");
		assertNull(module.getReportByUid("test"));
		
		report.setUid("test");
		
		assertSame(report, module.getReportByUid("test"));
	}
	
	@Test
	void testGetRestByUid() {
		final Module module = new ModuleMetadata();
		final RestMetadata rest = new RestMetadata();
		final List<RestMetadata> rests = new ArrayList<>();
		((ModuleMetadata) module).setRestMetadata(rests);
		rests.add(rest);
		rest.setUid("other");
		assertNull(module.getRestByUid("test"));
		
		rest.setUid("test");
		
		assertSame(rest, module.getRestByUid("test"));
	}
	
	@Test
	void testGetTaskByUid() {
		final Module module = new ModuleMetadata();
		final TaskMetadata task = new TaskMetadata();
		final List<TaskMetadata> tasks = new ArrayList<>();
		((ModuleMetadata) module).setTaskMetadata(tasks);
		tasks.add(task);
		task.setUid("other");
		assertNull(module.getTaskByUid("test"));
		
		task.setUid("test");
		
		assertSame(task, module.getTaskByUid("test"));
	}
	
	@Test
	void testGetTransferByUid() {
		final Module module = new ModuleMetadata();
		final TransferMetadata transfer = new TransferMetadata();
		final List<TransferMetadata> transfers = new ArrayList<>();
		((ModuleMetadata) module).setTransferMetadata(transfers);
		transfers.add(transfer);
		transfer.setUid("other");
		assertNull(module.getTransferByUid("test"));
		
		transfer.setUid("test");
		
		assertSame(transfer, module.getTransferByUid("test"));
	}
	
	@Test
	void testGetTransformerByUid() {
		final Module module = new ModuleMetadata();
		final TransformerMetadata transformer = new TransformerMetadata();
		final List<TransformerMetadata> transformers = new ArrayList<>();
		((ModuleMetadata) module).setTransformerMetadata(transformers);
		transformers.add(transformer);
		transformer.setUid("other");
		assertNull(module.getTransformerByUid("test"));
		
		transformer.setUid("test");
		
		assertSame(transformer, module.getTransformerByUid("test"));
	}
	
	@Test
	void testGetUserGroupByUid() {
		final Module module = new ModuleMetadata();
		final UserGroupMetadata group = new UserGroupMetadata();
		final List<UserGroupMetadata> groups = new ArrayList<>();
		((ModuleMetadata) module).setUserGroupMetadata(groups);
		groups.add(group);
		group.setUid("other");
		assertNull(module.getUserGroupByUid("test"));
		
		group.setUid("test");
		
		assertSame(group, module.getUserGroupByUid("test"));
	}
	
	@Test
	void testHasParameters() {
		final Module module = new ModuleMetadata();
		final ModuleParameter param = new ModuleParameter();
		assertFalse(module.hasParameters());
		module.addParameter(param);
		assertTrue(module.hasParameters());
	}
	
	@Test
	void testHasNesteds() {
		final Module module = new ModuleMetadata();
		final NestedModule nested = new NestedModule();
		assertFalse(module.hasNesteds());
		module.addNested(nested);
		assertTrue(module.hasNesteds());
	}
	
	@Test
	void testHasTransferContent() {
		final Module module = new ModuleMetadata();
		final Entity entity = new EntityMetadata();
		assertFalse(module.hasTransferContent());
		module.addTransferContent(entity, "text".getBytes());
		assertTrue(module.hasTransferContent());
	}
	
	@Test
	void testRemoveParameter() {
		final Module module = new ModuleMetadata();
		final ModuleParameter param = new ModuleParameter();
		module.addParameter(param);
		assertSame(1, module.getParameters().size());
		
		module.removeParameter(param);
		
		assertFalse(module.hasParameters());
		assertSame(0, module.getParameters().size());
	}
	
	@Test
	void testRemoveNested() {
		final Module module = new ModuleMetadata();
		final NestedModule nested = new NestedModule();
		module.addNested(nested);
		assertSame(1, module.getNesteds().size());
		
		module.removeNested(nested);
		
		assertFalse(module.hasNesteds());
		assertSame(0, module.getNesteds().size());
	}

}
