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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.seed.C;
import org.seed.InternalException;
import org.seed.LabelProvider;
import org.seed.core.config.SystemLog;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.entity.value.ValueObjectService;
import org.seed.core.util.Assert;

import org.springframework.util.FastByteArrayOutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

class JsonProcessor extends AbstractTransferProcessor {

	JsonProcessor(TransferService transferService,
				  ValueObjectService valueObjectService, 
				  Class<? extends ValueObject> objectClass,
				  LabelProvider labelProvider, Transfer transfer) {
		super(transferService, valueObjectService, objectClass, labelProvider, transfer);
	}

	@Override
	public byte[] doExport() {
		try (var out = new FastByteArrayOutputStream()) {
			doExport(out);
			return out.toByteArray();
		}
		catch (IOException ex) {
			SystemLog.logError(ex);
			throw new InternalException(ex);
		}
	}
	
	private void doExport(OutputStream outputStream) throws IOException {
		Assert.notNull(outputStream, "outputStream");
		final var mapper = new ObjectMapper();
		final var list = new ArrayList<Map<String, Object>>();
		
		while (hasNextObject()) {
			list.add(exportObjectMap(getNextObject()));
		}
		mapper.writeValue(outputStream, list);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public TransferResult doImport(ImportOptions options, InputStream inputStream) throws ValidationException {
		Assert.notNull(options, C.OPTIONS);
		Assert.notNull(inputStream, "inputStream");
		final var result = new TransferResult(options);
		final var objects = new ArrayList<ValueObject>();
		final var mapper = new ObjectMapper();
		final var collectionType = mapper.getTypeFactory().constructCollectionType(List.class, Map.class);
		
		try {
			final var list = (List<Map<String, Object>>) mapper.readValue(inputStream, collectionType);
			if (list != null) {
				for (var map : list) {
					final ValueObject object = importObject(map, result);
					if (object != null) {
						objects.add(object);
					}
				}
				saveObjects(objects, options, result);
			}
		}
		catch (IOException ex) {
			SystemLog.logError(ex);
			throw new InternalException(ex);
		}
		return result;
	}
	
	private ValueObject importObject(Map<String, Object> map, TransferResult result) {
		try {
			return importObject(map);
		}
		catch (ParseException pex) {
			result.addError(pex);
			return null;
		}
	}

}
