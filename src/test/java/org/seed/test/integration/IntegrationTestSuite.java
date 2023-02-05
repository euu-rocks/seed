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
package org.seed.test.integration;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import org.seed.test.integration.customcode.CreateCustomCodeTest;
import org.seed.test.integration.customcode.DeleteCustomCodeTest;
import org.seed.test.integration.customcode.EditCustomCodeTest;
import org.seed.test.integration.data.CreateDataSourceTest;
import org.seed.test.integration.data.CreateHQLDataSourceTest;
import org.seed.test.integration.data.DeleteDBObjectTest;
import org.seed.test.integration.data.DeleteDataSourcesTest;
import org.seed.test.integration.data.EditDBObjectTest;
import org.seed.test.integration.data.EditDataSourceTest;
import org.seed.test.integration.data.CreateDBObjectTest;
import org.seed.test.integration.entity.CreateDerivedEntityTest;
import org.seed.test.integration.entity.CreateEntityTest;
import org.seed.test.integration.entity.CreateGenericEntityTest;
import org.seed.test.integration.entity.CreateNestedEntityTest;
import org.seed.test.integration.entity.CreateTransferableEntityTest;
import org.seed.test.integration.entity.EditEntityTest;
import org.seed.test.integration.filter.CreateFilterTest;
import org.seed.test.integration.filter.CreateHQLFilterTest;
import org.seed.test.integration.filter.EditFilterTest;
import org.seed.test.integration.form.CreateFormTest;
import org.seed.test.integration.form.EditFormTest;
import org.seed.test.integration.menu.CreateMenuTest;
import org.seed.test.integration.menu.EditMenuTest;
import org.seed.test.integration.module.CreateModuleTest;
import org.seed.test.integration.module.EditModuleTest;
import org.seed.test.integration.report.CreateReportTest;
import org.seed.test.integration.report.DeleteReportTest;
import org.seed.test.integration.report.EditReportTest;
import org.seed.test.integration.rest.CreateRestTest;
import org.seed.test.integration.rest.DeleteRestTest;
import org.seed.test.integration.rest.EditRestTest;
import org.seed.test.integration.task.CreateJobTest;
import org.seed.test.integration.task.DeleteJobTest;
import org.seed.test.integration.task.EditJobTest;
import org.seed.test.integration.transfer.CreateTransferTest;
import org.seed.test.integration.transfer.EditTransferTest;
import org.seed.test.integration.transformer.CreateTransformerTest;
import org.seed.test.integration.transformer.EditTransformerTest;
import org.seed.test.integration.user.CreateUserGroupTest;
import org.seed.test.integration.user.CreateUserTest;
import org.seed.test.integration.user.EditUserGroupTest;
import org.seed.test.integration.user.EditUserTest;

@Suite
@SelectClasses({ NavigationTest.class, CreateUserTest.class, CreateUserGroupTest.class, 
				 CreateModuleTest.class, CreateMenuTest.class, CreateTransferableEntityTest.class,
				 CreateGenericEntityTest.class, CreateDerivedEntityTest.class, CreateEntityTest.class, 
				 CreateNestedEntityTest.class, CreateFilterTest.class, CreateHQLFilterTest.class, 
				 CreateTransferTest.class, CreateTransformerTest.class, CreateFormTest.class, 
				 CreateDBObjectTest.class, CreateDataSourceTest.class, CreateHQLDataSourceTest.class, 
				 CreateReportTest.class, CreateCustomCodeTest.class, CreateRestTest.class,
				 CreateJobTest.class, EditEntityTest.class, EditFilterTest.class, EditDBObjectTest.class, 
				 EditDataSourceTest.class, EditTransferTest.class, EditTransformerTest.class, 
				 EditFormTest.class, EditMenuTest.class, EditJobTest.class, EditModuleTest.class, 
				 EditReportTest.class, EditCustomCodeTest.class, EditRestTest.class, EditUserTest.class,
				 EditUserGroupTest.class, DeleteRestTest.class, DeleteCustomCodeTest.class,
				 DeleteReportTest.class, DeleteJobTest.class, DeleteDataSourcesTest.class,
				 DeleteDBObjectTest.class})
public class IntegrationTestSuite { }
