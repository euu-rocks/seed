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
import java.util.List;

import org.seed.InternalException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityService;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;
import org.seed.core.util.StreamUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextpdf.svg.converter.SvgConverter;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

@Service
public class DocumentationServiceImpl implements DocumentationService {
	
	@Autowired
	private EntityService entityService;
	
	@Override
	public String createERDiagramPlantUML() {
		return new ERDiagramBuilder(getEntities()).build();
	}
	
	@Override
	public String createStatusDiagramPlantUML(Entity entity) {
		return new StatusDiagramBuilder(entity).build();
	}
	
	@Override
	public String createSVG(String plantUML) {
		Assert.notNull(plantUML, "plantUML");
		
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			new SourceStringReader(plantUML)
				.outputImage(baos, new FileFormatOption(FileFormat.SVG));
			return MiscUtils.toString(baos.toByteArray());
		} 
		catch (IOException ioex) {
			throw new InternalException(ioex);
		}
	}
	
	@Override
	public byte[] createPDF(String svg) {
		Assert.notNull(svg, "svg");
		
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			SvgConverter.createPdf(StreamUtils.getStringAsStream(svg), baos);
			return baos.toByteArray();
		} 
		catch (IOException ioex) {
			throw new InternalException(ioex);
		}
	}
	
	private List<Entity> getEntities() {
		return entityService.findNonGenericEntities();
	}
 	
}
