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
package org.seed.test.unit.form;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.seed.core.form.FormAction;
import org.seed.core.form.FormActionType;

class FormActionTest {
	
	@Test
	void testIsEqual() {
		final FormAction action1 = new FormAction();
		final FormAction action2 = new FormAction();
		assertTrue(action1.isEqual(action2));
		
		action1.setTargetFormUid("targetForm");
		action1.setEntityFunctionUid("entityFunction");
		action1.setType(FormActionType.NEWOBJECT);
		action1.setLabel("label");
		assertFalse(action1.isEqual(action2));
		
		action2.setTargetFormUid("targetForm");
		action2.setEntityFunctionUid("entityFunction");
		action2.setType(FormActionType.NEWOBJECT);
		action2.setLabel("label");
		assertTrue(action1.isEqual(action2));
	}
}
