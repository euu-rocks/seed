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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.ClassOrderer;

import org.seed.Seed;
import org.seed.test.integration.customcode.CreateCustomCodeTest;
import org.seed.test.integration.customcode.DeleteCustomCodeTest;
import org.seed.test.integration.customcode.EditCustomCodeTest;
import org.seed.test.integration.data.CreateDBObjectTest;
import org.seed.test.integration.data.CreateDataSourceTest;
import org.seed.test.integration.data.CreateHQLDataSourceTest;
import org.seed.test.integration.data.DeleteDBObjectTest;
import org.seed.test.integration.data.DeleteDataSourcesTest;
import org.seed.test.integration.data.EditDBObjectTest;
import org.seed.test.integration.data.EditDataSourceTest;
import org.seed.test.integration.entity.CreateDerivedEntityTest;
import org.seed.test.integration.entity.CreateEntityTest;
import org.seed.test.integration.entity.CreateGenericEntityTest;
import org.seed.test.integration.entity.CreateNestedEntityTest;
import org.seed.test.integration.entity.CreateTestobjectTest;
import org.seed.test.integration.entity.CreateTransferableEntityTest;
import org.seed.test.integration.entity.DeleteEntityTest;
import org.seed.test.integration.entity.DeleteTestobjectTest;
import org.seed.test.integration.entity.EditEntityTest;
import org.seed.test.integration.filter.CreateFilterTest;
import org.seed.test.integration.filter.CreateHQLFilterTest;
import org.seed.test.integration.filter.DeleteFilterTest;
import org.seed.test.integration.filter.EditFilterTest;
import org.seed.test.integration.form.CreateFormTest;
import org.seed.test.integration.form.DeleteFormTest;
import org.seed.test.integration.form.EditFormTest;
import org.seed.test.integration.form.TestobjectFormTest;
import org.seed.test.integration.menu.CreateMenuTest;
import org.seed.test.integration.menu.DeleteMenuTest;
import org.seed.test.integration.menu.EditMenuTest;
import org.seed.test.integration.module.CreateModuleTest;
import org.seed.test.integration.module.DeleteModuleTest;
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
import org.seed.test.integration.transfer.CreateJsonTransferTest;
import org.seed.test.integration.transfer.CreateTransferTest;
import org.seed.test.integration.transfer.DeleteJsonTransferTest;
import org.seed.test.integration.transfer.DeleteTransferTest;
import org.seed.test.integration.transfer.EditJsonTransferTest;
import org.seed.test.integration.transfer.EditTransferTest;
import org.seed.test.integration.transformer.CreateTransformerTest;
import org.seed.test.integration.transformer.DeleteTransformerTest;
import org.seed.test.integration.transformer.EditTransformerTest;
import org.seed.test.integration.user.CreateUserGroupTest;
import org.seed.test.integration.user.CreateUserTest;
import org.seed.test.integration.user.DeleteUserGroupTest;
import org.seed.test.integration.user.DeleteUserTest;
import org.seed.test.integration.user.EditUserGroupTest;
import org.seed.test.integration.user.EditUserTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@Testcontainers
@SpringBootTest(classes = Seed.class, webEnvironment = WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = {TestcontainersIntegrationTestSuite.Initializer.class})
public class TestcontainersIntegrationTestSuite {
	
	@Container
	static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:14.2"));

	static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			TestPropertyValues.of(
					"spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
					"spring.datasource.username=" + postgreSQLContainer.getUsername(),
					"spring.datasource.password=" + postgreSQLContainer.getPassword()
					).applyTo(configurableApplicationContext.getEnvironment());
		}
	}

    @Nested @Order(101) class NavigationTestImpl extends NavigationTest { }
    
    @Nested @Order(201) class CreateUserGroupTestImpl extends CreateUserGroupTest { }
    
    @Nested @Order(202) class CreateUserTestImpl extends CreateUserTest { }
    @Nested @Order(203) class CreateModuleTestImpl extends CreateModuleTest { }
    @Nested @Order(204) class CreateMenuTestImpl extends CreateMenuTest { }
    @Nested @Order(205) class CreateTransferableEntityTestImpl extends CreateTransferableEntityTest { }
    @Nested @Order(206) class CreateGenericEntityTestImpl extends CreateGenericEntityTest { }
    @Nested @Order(207) class CreateDerivedEntityTestImpl extends CreateDerivedEntityTest { }
    @Nested @Order(208) class CreateEntityTestImpl extends CreateEntityTest { }
    @Nested @Order(209) class CreateNestedEntityTestImpl extends CreateNestedEntityTest { }
    @Nested @Order(210) class CreateFilterTestImpl extends CreateFilterTest { }
    @Nested @Order(211) class CreateHQLFilterTestImpl extends CreateHQLFilterTest { }
    @Nested @Order(212) class CreateTransferTestImpl extends CreateTransferTest { }
    @Nested @Order(213) class CreateJsonTransferTestImpl extends CreateJsonTransferTest { }
    @Nested @Order(214) class CreateTransformerTestImpl extends CreateTransformerTest { }
    @Nested @Order(215) class CreateFormTestImpl extends CreateFormTest { }
    @Nested @Order(216) class CreateDBObjectTestImpl extends CreateDBObjectTest { }
    @Nested @Order(217) class CreateDataSourceTestImpl extends CreateDataSourceTest { }
    @Nested @Order(218) class CreateHQLDataSourceTestImpl extends CreateHQLDataSourceTest { }
    @Nested @Order(219) class CreateReportTestImpl extends CreateReportTest { }
    @Nested @Order(220) class CreateCustomCodeTestImpl extends CreateCustomCodeTest { }
    @Nested @Order(221) class CreateRestTestImpl extends CreateRestTest { }
    @Nested @Order(222) class CreateJobTestImpl extends CreateJobTest { }
    @Nested @Order(223) class CreateTestobjectTestImpl extends CreateTestobjectTest { }
    
    @Nested @Order(301) class TestobjectFormTestImpl extends TestobjectFormTest { }
    
    @Nested @Order(401) class EditEntityTestImpl extends EditEntityTest { }
    @Nested @Order(402) class EditFilterTestImpl extends EditFilterTest { }
    @Nested @Order(403) class EditDBObjectTestImpl extends EditDBObjectTest { }
    @Nested @Order(404) class EditDataSourceTestImpl extends EditDataSourceTest { }
    @Nested @Order(405) class EditTransferTestImpl extends EditTransferTest { }
    @Nested @Order(406) class EditJsonTransferTestImpl extends EditJsonTransferTest { }
    @Nested @Order(407) class EditTransformerTestImpl extends EditTransformerTest { }
    @Nested @Order(408) class EditFormTestImpl extends EditFormTest { }
    @Nested @Order(409) class EditMenuTestImpl extends EditMenuTest { }
    @Nested @Order(410) class EditJobTestImpl extends EditJobTest { }
    @Nested @Order(411) class EditModuleTestImpl extends EditModuleTest { }
    @Nested @Order(412) class EditReportTestImpl extends EditReportTest { }
    @Nested @Order(413) class EditCustomCodeTestImpl extends EditCustomCodeTest { }
    @Nested @Order(414) class EditRestTestImpl extends EditRestTest { }
    @Nested @Order(415) class EditUserTestImpl extends EditUserTest { }
    @Nested @Order(416) class EditUserGroupTestImpl extends EditUserGroupTest { }
    
    @Nested @Order(501) class DeleteRestTestImpl extends DeleteRestTest { }    
    @Nested @Order(502) class DeleteCustomCodeTestImpl extends DeleteCustomCodeTest { }
    @Nested @Order(503) class DeleteReportTestImpl extends DeleteReportTest { }
    @Nested @Order(504) class DeleteJobTestImpl extends DeleteJobTest { }
    @Nested @Order(505) class DeleteDataSourcesTestImpl extends DeleteDataSourcesTest { }
    @Nested @Order(506) class DeleteDBObjectTestImpl extends DeleteDBObjectTest { }
    @Nested @Order(507) class DeleteFormTestImpl extends DeleteFormTest { }
    @Nested @Order(508) class DeleteMenuTestImpl extends DeleteMenuTest { }
    @Nested @Order(509) class DeleteTransformerTestImpl extends DeleteTransformerTest { }
    @Nested @Order(510) class DeleteTransferTestImpl extends DeleteTransferTest { }
    @Nested @Order(511) class DeleteJsonTransferTestImpl extends DeleteJsonTransferTest { }
    @Nested @Order(512) class DeleteFilterTestImpl extends DeleteFilterTest { }
    @Nested @Order(513) class DeleteTestobjectTestImpl extends DeleteTestobjectTest { }
    @Nested @Order(514) class DeleteEntityTestImpl extends DeleteEntityTest { }
    @Nested @Order(515) class DeleteUserGroupTestImpl extends DeleteUserGroupTest { }
    @Nested @Order(516) class DeleteUserTestImpl extends DeleteUserTest { }
    @Nested @Order(517) class DeleteModuleTestImpl extends DeleteModuleTest { }

}
