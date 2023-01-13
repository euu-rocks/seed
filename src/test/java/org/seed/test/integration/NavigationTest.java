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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NavigationTest extends AbstractIntegrationTest {
	
	@Test
	void testLogin() {
		// do nothing more
	}
	
	@Test
	void testMenu() {
		clickMenu("administration-entitaeten");
		assertEquals("Entitäten", findTab("entitaeten").getText());
		
		openMenu("administration-entitaeten");
		clickMenu("administration-entitaeten-filter");
		assertEquals("Filter", findTab("filter").getText());
		
		clickMenu("administration-entitaeten-import--export");
		assertEquals("Import / Export", findTab("import--export").getText());
		
		clickMenu("administration-entitaeten-transformationen");
		assertEquals("Transformationen", findTab("transformationen").getText());
		
		clickMenu("administration-formulare");
		assertEquals("Formulare", findTab("formulare").getText());
		
		clickMenu("administration-menues");
		assertEquals("Menüs", findTab("menues").getText());
		
		clickMenu("administration-jobs");
		assertEquals("Jobs", findTab("jobs").getText());
		
		clickMenu("administration-datenbankelemente");
		findTab("datenbankelemente");
		assertEquals("Datenbankelemente", findTab("datenbankelemente").getText());
		
		clickMenu("administration-abfragen");
		assertEquals("Abfragen", findTab("abfragen").getText());
		
		clickMenu("administration-reporte");
		assertEquals("Reporte", findTab("reporte").getText());
		
		clickMenu("administration-quellcode");
		findTab("quellcode");
		
		openMenu("administration-quellcode");
		clickMenu("administration-quellcode-bibliotheken");
		findTab("bibliotheken");
		
		clickMenu("administration-rest-services");
		findTab("rest-services");
		
		clickMenu("administration-module");
		findTab("module");
		
		clickMenu("administration-einstellungen");
		findTab("einstellungen");
		
		clickMenu("administration-system---informationen");
		findTab("system---informationen");
		
		clickMenu("administration-system---funktionen");
		findTab("system---funktionen");
		
		clickMenu("administration-benutzer");
		findTab("benutzer");
		
		openMenu("administration-benutzer");
		clickMenu("administration-benutzer-rollen");
		findTab("rollen");
	}
	
}
