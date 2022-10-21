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
package org.seed.core.util;

import java.io.IOException;

import org.seed.core.data.SystemObject;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class ReferenceJsonSerializer extends StdSerializer<SystemObject> {
	
	private static final long serialVersionUID = -597156653430418805L;

	protected ReferenceJsonSerializer() {
		this(null);
	}
	
	protected ReferenceJsonSerializer(Class<SystemObject> typeClass) {
		super(typeClass);
	}

	@Override
	public void serialize(SystemObject value, JsonGenerator generator, 
						  SerializerProvider provider) throws IOException {
		if (value != null) {
			// create dummy object that only contains id field
			generator.writeObject(new Object() { 
				
				@SuppressWarnings("unused") // used in json
				public Long getId() {
					return value.getId();
				}
				
			});
		}
		else {
			generator.writeNull();
		}
	}
	
}
