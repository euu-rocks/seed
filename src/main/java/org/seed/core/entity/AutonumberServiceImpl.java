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
package org.seed.core.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Nullable;

import org.hibernate.Session;

import org.seed.core.data.AbstractSystemEntityService;
import org.seed.core.data.SystemEntityValidator;
import org.seed.core.data.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

@Service
public class AutonumberServiceImpl extends AbstractSystemEntityService<Autonumber> 
	implements AutonumberService {
	
	@Autowired
	private AutonumberRepository repository;
	
	@Override
	public String getNextValue(EntityField entityField, @Nullable Session session) {
		Assert.notNull(entityField, "entityField is null");
		Assert.state(entityField.getType().isAutonum(), "field is not autonumber");
		
		final String pattern = entityField.getAutonumPattern() != null 
								? resolvePattern(entityField.getAutonumPattern())
								: null;
		Autonumber autonum = getAutonumber(entityField, session);
		if (autonum != null) {
			if (ObjectUtils.nullSafeEquals(pattern, autonum.getPattern())) {
				autonum.setValue(autonum.getValue() + 1);
			}
			else {
				autonum.setPattern(pattern);
				autonum.setValue(entityField.getAutonumStart() != null
									? entityField.getAutonumStart() 
									: 1L);
			}
		}
		else {
			autonum = new Autonumber();
			autonum.setField(entityField);
			autonum.setPattern(pattern);
			autonum.setValue(entityField.getAutonumStart() != null
								? entityField.getAutonumStart() 
								: 1L);
		}
		try {
			super.saveObject(autonum, session);
		} 
		catch (ValidationException e) {
			throw new RuntimeException(e);
		}
		return pattern != null 
				? pattern + autonum.getValue()
				: autonum.getValue().toString();
	}
	
	@Override
	public void deleteAutonumber(EntityField entityField, Session session) {
		Assert.notNull(entityField, "entityField is null");
		Assert.notNull(session, "session is null");
		Assert.state(entityField.getType().isAutonum(), "field is not autonumber");
		
		final Autonumber autonum = getAutonumber(entityField, session);
		if (autonum != null) {
			session.delete(autonum);
		}
	}
	
	@Override
	protected AutonumberRepository getRepository() {
		return repository;
	}

	@Override
	protected SystemEntityValidator<Autonumber> getValidator() {
		return null;
	}
	
	private Autonumber getAutonumber(EntityField entityField, @Nullable Session session) {
		return session != null 
				? repository.findUnique(session, queryParam("field", entityField)) 
				: repository.findUnique(queryParam("field", entityField));
	}
	
	private static String resolvePattern(String pattern) {
		final int idxStart = pattern.indexOf('{');
		if (idxStart < 0) {
			return pattern;
		}
		final int idxEnd = pattern.indexOf('}', idxStart);
		if (idxEnd < 0) {
			return pattern;
		}
		return pattern.substring(0, idxStart) + 
				new SimpleDateFormat(pattern.substring(idxStart + 1, idxEnd)).format(new Date()) + 
			    pattern.substring(idxEnd + 1);
 	}
	
}
