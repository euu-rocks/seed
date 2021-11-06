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

import org.seed.core.application.ApplicationEntityService;
import org.seed.core.data.FileObject;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;

public interface TransferService extends ApplicationEntityService<Transfer> {
	
	ImportOptions createImportOptions(Transfer transfer);
	
	List<Transfer> findTransfers(Entity entity);
	
	List<TransferElement> getAvailableElements(Transfer transfer);
	
	byte[] doExport(Transfer transfer);
	
	byte[] doExport(Entity transferableEntity);
	
	TransferResult doImport(Transfer transfer, ImportOptions options, FileObject importFile) throws ValidationException;
	
	TransferResult doImport(Entity transferableEntity, byte[] content) throws ValidationException;
	
}
