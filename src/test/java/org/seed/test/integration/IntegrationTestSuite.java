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
import org.seed.test.integration.data.CreateDataSourceTest;
import org.seed.test.integration.data.CreateHQLDataSourceTest;
import org.seed.test.integration.data.CreateViewTest;
import org.seed.test.integration.entity.CreateEntityTest;
import org.seed.test.integration.filter.CreateFilterTest;
import org.seed.test.integration.filter.CreateHQLFilter;
import org.seed.test.integration.form.CreateFormTest;
import org.seed.test.integration.menu.CreateMenuTest;
import org.seed.test.integration.module.CreateModuleTest;
import org.seed.test.integration.report.CreateReportTest;
import org.seed.test.integration.rest.CreateRestTest;
import org.seed.test.integration.task.CreateJobTest;
import org.seed.test.integration.transfer.CreateTransferTest;
import org.seed.test.integration.transformer.CreateTransformerTest;
import org.seed.test.integration.user.CreateUserGroupTest;
import org.seed.test.integration.user.CreateUserTest;

@Suite
@SelectClasses({ MainViewTest.class, CreateModuleTest.class, CreateMenuTest.class,
				 CreateUserTest.class, CreateUserGroupTest.class, CreateEntityTest.class, 
				 CreateFilterTest.class, CreateHQLFilter.class, CreateTransferTest.class, 
				 CreateTransformerTest.class, CreateFormTest.class, CreateJobTest.class, 
				 CreateViewTest.class, CreateDataSourceTest.class, CreateHQLDataSourceTest.class, 
				 CreateReportTest.class, CreateCustomCodeTest.class, CreateRestTest.class })
public class IntegrationTestSuite { }
