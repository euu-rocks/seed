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
package org.seed.core.form.printout;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.seed.C;
import org.seed.InternalException;
import org.seed.LabelProvider;
import org.seed.core.config.SystemLog;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityService;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.form.FormPrintout;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrintoutServiceImpl implements PrintoutService {
	
	private static Map<String, Class<? extends PrintoutProcessor>> mapProcessors = new HashMap<>();
	
	/*
	 * Extension MIME Type
		.doc      application/msword
		.dot      application/msword
		
		.docx     application/vnd.openxmlformats-officedocument.wordprocessingml.document
		.dotx     application/vnd.openxmlformats-officedocument.wordprocessingml.template
		.docm     application/vnd.ms-word.document.macroEnabled.12
		.dotm     application/vnd.ms-word.template.macroEnabled.12
		
		.xls      application/vnd.ms-excel
		.xlt      application/vnd.ms-excel
		.xla      application/vnd.ms-excel
		
		.xlsx     application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
		.xltx     application/vnd.openxmlformats-officedocument.spreadsheetml.template
		.xlsm     application/vnd.ms-excel.sheet.macroEnabled.12
		.xltm     application/vnd.ms-excel.template.macroEnabled.12
		.xlam     application/vnd.ms-excel.addin.macroEnabled.12
		.xlsb     application/vnd.ms-excel.sheet.binary.macroEnabled.12
		
		.ppt      application/vnd.ms-powerpoint
		.pot      application/vnd.ms-powerpoint
		.pps      application/vnd.ms-powerpoint
		.ppa      application/vnd.ms-powerpoint
		
		.pptx     application/vnd.openxmlformats-officedocument.presentationml.presentation
		.potx     application/vnd.openxmlformats-officedocument.presentationml.template
		.ppsx     application/vnd.openxmlformats-officedocument.presentationml.slideshow
		.ppam     application/vnd.ms-powerpoint.addin.macroEnabled.12
		.pptm     application/vnd.ms-powerpoint.presentation.macroEnabled.12
		.potm     application/vnd.ms-powerpoint.template.macroEnabled.12
		.ppsm     application/vnd.ms-powerpoint.slideshow.macroEnabled.12
		
		.mdb      application/vnd.ms-access
	 */
	
	static {
		mapProcessors.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", DOCXPrintoutProcessor.class);
	}
	
	@Autowired
	private EntityService entityService;
	
	@Autowired
	private LabelProvider labelProvider;

	@Override
	public byte[] print(FormPrintout printout, ValueObject valueObject) {
		Assert.notNull(printout, C.PRINTOUT);
		Assert.notNull(valueObject, "valueObject");
		
		try {
			final Class<? extends PrintoutProcessor> processorClass = mapProcessors.get(printout.getContentType());
			if (processorClass == null) {
				throw new IllegalStateException("no processor available for content type: " + printout.getContentType());
			}
			
			final Entity entity = entityService.getObject(valueObject.getEntityId());
			final PrintoutProcessor processor = processorClass.getDeclaredConstructor(Entity.class, LabelProvider.class)
															  .newInstance(entity, labelProvider);
			return processor.process(printout, valueObject);
		
		} 
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | 
				NoSuchMethodException | SecurityException ex) {
			SystemLog.logError(ex);
			throw new InternalException(ex);
		}
	}

}
