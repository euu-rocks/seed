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
package org.seed.core.entity.autonum;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Nullable;

import org.hibernate.Session;
import org.seed.C;
import org.seed.InternalException;
import org.seed.core.config.SystemLog;
import org.seed.core.data.AbstractSystemEntityService;
import org.seed.core.data.SystemEntityValidator;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.EntityField;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class AutonumberServiceImpl extends AbstractSystemEntityService<Autonumber> 
	implements AutonumberService {
	
	@Autowired
	private AutonumberRepository repository;
	
	@Override
	public String getNextValue(EntityField entityField, @Nullable Session session) {
		Assert.notNull(entityField, C.ENTITYFIELD);
		Assert.state(entityField.getType().isAutonum(), "no auto number");
		
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
				autonum.setValue(getStartValue(entityField));
			}
		}
		else {
			autonum = new Autonumber();
			autonum.setField(entityField);
			autonum.setPattern(pattern);
			autonum.setValue(getStartValue(entityField));
		}
		try {
			super.saveObject(autonum, session);
		} 
		catch (ValidationException e) {
			SystemLog.logError(e);
			throw new InternalException(e);
		}
		return pattern != null 
				? pattern + autonum.getValue()
				: autonum.getValue().toString();
	}
	
	@Override
	public void deleteAutonumber(EntityField entityField, Session session) {
		Assert.notNull(entityField, C.ENTITYFIELD);
		Assert.notNull(session, C.SESSION);
		Assert.state(entityField.getType().isAutonum(), "field is not an autonumber");
		
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
				? repository.findUnique(session, queryParam(C.FIELD, entityField)) 
				: repository.findUnique(queryParam(C.FIELD, entityField));
	}
	
	private static Long getStartValue(EntityField entityField) {
		return entityField.getAutonumStart() != null
				? entityField.getAutonumStart() 
				: 1L;
	}
	
	private static String resolvePattern(String pattern) {
		final Date now = new Date();
		final StringBuilder buf = new StringBuilder();
		int idx = 0;
		boolean run = true;
		while (run && idx < pattern.length()) {
			// find next '{'
			final int idxStart = pattern.indexOf('{', idx);
			if (idxStart >= 0) {
				final int idxEnd = pattern.indexOf('}', idxStart);
				if (idxEnd >= 0) {
					if (idx < idxStart) {
						// all chars before '{'
						buf.append(pattern.substring(idx, idxStart));
					}
					// formated date value
					buf.append(new SimpleDateFormat(pattern.substring(idxStart + 1, idxEnd)).format(now));
					idx = idxEnd + 1;
				}
				else { // '}' not found
					run = false;
				}
			}
			else { // no (further) '{' found
				run = false;
			}
		}
		if (idx > 0) {
			// all chars after last '}'
			buf.append(pattern.substring(idx));
		}
		return buf.length() > 0 ? buf.toString() : pattern;
 	}
	
}
