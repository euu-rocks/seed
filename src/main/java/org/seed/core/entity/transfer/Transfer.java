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
package org.seed.core.entity.transfer;

import java.util.List;

import org.seed.core.application.ApplicationEntity;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;

public interface Transfer extends ApplicationEntity {
	
	Entity getEntity();
	
	TransferFormat getFormat();
	
	String getSeparatorChar();
	
	String getQuoteChar();
	
	String getEscapeChar();
	
	CharEncoding getEncoding();
	
	Newline getNewline();
	
	boolean isQuoteAll();
	
	boolean isHeader();
	
	boolean hasElements();
	
	List<TransferElement> getElements();
	
	List<EntityField> getElementFields();
	
	EntityField getIdentifierField();
	
	String getEntityUid();
	
	TransferElement getElementByUid(String uid);
	
}
