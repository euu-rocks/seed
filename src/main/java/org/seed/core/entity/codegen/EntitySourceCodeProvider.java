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
package org.seed.core.entity.codegen;

import static org.seed.core.util.CollectionUtils.convertedList;

import java.util.List;
import java.util.TimeZone;

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.application.setting.ApplicationSettingService;
import org.seed.core.application.setting.Setting;
import org.seed.core.codegen.SourceCode;
import org.seed.core.codegen.SourceCodeBuilder;
import org.seed.core.codegen.SourceCodeProvider;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityRepository;
import org.seed.core.rest.RestHelper;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntitySourceCodeProvider implements SourceCodeProvider {
	
	@Autowired
	private EntityRepository entityRepository;
	
	@Autowired
	private ApplicationSettingService settingService;
	
	@Override
	public List<SourceCodeBuilder> getSourceCodeBuilders(Session session) {
		return convertedList(entityRepository.find(session), this::createBuilder);
	}
	
	public SourceCode getEntitySource(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		return createBuilder(entity).build();
	}
	
	private EntitySourceCodeBuilder createBuilder(Entity entity) {
		final var builder = new EntitySourceCodeBuilder(entity);
		builder.setTimeZone(getTimeZone());
		builder.setFormatRestDate(RestHelper.getRestDateFormat());
		builder.setFormatRestDateTime(RestHelper.getRestDateTimeFormat());
		return builder;
	}
	
	private String getTimeZone() {
		if (settingService.hasSetting(Setting.APPLICATION_TIMEZONE)) {
			return settingService.getSetting(Setting.APPLICATION_TIMEZONE);
		}
		return TimeZone.getDefault().getID();
	}
	
}
