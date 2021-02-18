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
package org.seed.core.data.dbobject;

import java.util.Set;

import javax.persistence.PersistenceException;

import org.hibernate.Session;

import org.seed.core.config.SessionFactoryProvider;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.DataException;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class DBObjectValidator extends AbstractSystemEntityValidator<DBObject> {
	
	@Autowired
	private SessionFactoryProvider sessionFactoryProvider;
	
	@Override
	public void validateCreate(DBObject dbObject) throws ValidationException {
		Assert.notNull(dbObject, "dbObject is null");
		
		if (isEmpty(dbObject.getType())) {
			throw new ValidationException(new ValidationError("val.empty.field", "label.type"));
		}
	}
	
	public void validateSave(DBObject dbObject) throws ValidationException {
		Assert.notNull(dbObject, "dbObject is null");
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(dbObject.getName())) {
			errors.add(new ValidationError("val.empty.field", "label.name"));
		}
		else if (!isNameLengthAllowed(dbObject.getName())) {
			errors.add(new ValidationError("val.toolong.name", String.valueOf(getMaxNameLength())));
		}
		if (isEmpty(dbObject.getType())) {
			errors.add(new ValidationError("val.empty.field", "label.type"));
		}
		if (isEmpty(dbObject.getContent())) {
			errors.add(new ValidationError("val.empty.field", "label.sqlstatement"));
		}
		else { // test sql
			try (Session session = sessionFactoryProvider.getSessionFactory().openSession()) {
				switch (dbObject.getType()) {
					case VIEW:
						// test query
						session.createSQLQuery(dbObject.getContent())
									.setMaxResults(1).list();
						break;
				}
			}
			catch (PersistenceException pex) {
				throw new ValidationException(new ValidationError("val.illegal.sqlstatement"), 
											  new DataException(pex));
			}
		}
		
		validate(errors);
	}
	
}
