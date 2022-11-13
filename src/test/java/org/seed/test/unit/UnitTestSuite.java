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
package org.seed.test.unit;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({ "org.seed.test.unit.data", "org.seed.test.unit.entity",
				  "org.seed.test.unit.filter", "org.seed.test.unit.form",
				  "org.seed.test.unit.menu", "org.seed.test.unit.module",
				  "org.seed.test.unit.report", "org.seed.test.unit.rest",
				  "org.seed.test.unit.task", "org.seed.test.unit.transfer", 
				  "org.seed.test.unit.transformer", "org.seed.test.unit.user", 
				  "org.seed.test.unit.util", "org.seed.test.unit.value"
				})
public class UnitTestSuite { }
