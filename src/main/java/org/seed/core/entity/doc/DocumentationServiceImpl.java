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
package org.seed.core.entity.doc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.seed.InternalException;
import org.seed.core.entity.EntityService;
import org.seed.core.util.MiscUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

@Service
public class DocumentationServiceImpl implements DocumentationService {
	
	@Autowired
	private EntityService entityService;
	
	@Override
	public String createPlantUML() {
		return new PlantUMLBuilder(entityService.findNonGenericEntities())
					.build();
	}
	
	@Override
	public String createERDiagramSVG() {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			new SourceStringReader(createPlantUML())
				.outputImage(baos, new FileFormatOption(FileFormat.SVG));
			return MiscUtils.toString(baos.toByteArray());
		} 
		catch (IOException ioex) {
			throw new InternalException(ioex);
		}
	}
	
}
