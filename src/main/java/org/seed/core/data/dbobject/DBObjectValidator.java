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

import javax.persistence.PersistenceException;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.seed.C;
import org.seed.core.config.SessionFactoryProvider;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.DataException;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationErrors;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DBObjectValidator extends AbstractSystemEntityValidator<DBObject> {
	
	@Autowired
	private SessionFactoryProvider sessionFactoryProvider;
	
	@Override
	public void validateCreate(DBObject dbObject) throws ValidationException {
		Assert.notNull(dbObject, C.DBOBJECT);
		
		if (isEmpty(dbObject.getType())) {
			throw new ValidationException(new ValidationErrors().addEmptyField("label.type"));
		}
	}
	
	@Override
	public void validateSave(DBObject dbObject) throws ValidationException {
		Assert.notNull(dbObject, C.DBOBJECT);
		final ValidationErrors errors = new ValidationErrors();
		
		if (isEmpty(dbObject.getName())) {
			errors.addEmptyName();
		}
		else if (!isNameLengthAllowed(dbObject.getName())) {
			errors.addOverlongName(getMaxNameLength());
		}
		if (isEmpty(dbObject.getType())) {
			errors.addEmptyField("label.type");
		}
		if (isEmpty(dbObject.getContent())) {
			errors.addEmptyField("label.sqlstatement");
		}
		else if (!dbObject.contains(dbObject.getInternalName())) {
			errors.addNotContains(dbObject.getInternalName());
		}
		if (errors.isEmpty()) {
			validateObjectType(dbObject, errors);
		}
		if (errors.isEmpty()) {
			testSQL(dbObject);
		}
		validate(errors);
	}
	
	private void validateObjectType(DBObject dbObject, ValidationErrors errors) {
		switch (dbObject.getType()) {
			case VIEW:
				if (!dbObject.contains("select ")) {
					errors.addNotContains("select");
				}
				break;
			
			case PROCEDURE:
				if (!dbObject.contains(" procedure ")) {
					errors.addNotContains("procedure");
				}
				break;
				
			case FUNCTION:
				if (!dbObject.contains(" function ")) {
					errors.addNotContains("function");
				}
				break;
				
			case TRIGGER:
				if (!dbObject.contains(" trigger ")) {
					errors.addNotContains("trigger");
				}
				break;
			
			default:
				throw new UnsupportedOperationException(dbObject.getType().name());
		}
	}
	
	private void testSQL(DBObject dbObject) throws ValidationException {
		try (Session session = sessionFactoryProvider.getSessionFactory().openSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				switch (dbObject.getType()) {
					case VIEW:
						session.createSQLQuery(dbObject.getContent()).setMaxResults(0).list();
						break;
						
					case PROCEDURE:
					case FUNCTION:
					case TRIGGER:
						session.createSQLQuery(dbObject.getContent()).executeUpdate();
						break;
						
					default:
						throw new UnsupportedOperationException(dbObject.getType().name());
				}
			}
			finally {
				if (tx != null) {
					tx.rollback();
				}
			}
		}
		catch (PersistenceException pex) {
			throw new ValidationException(new ValidationError("val.illegal.sqlstatement"), 
										  new DataException(pex));
		}
	}
	
}
