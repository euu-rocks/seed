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

import static org.seed.core.util.CollectionUtils.*;

import javax.persistence.PersistenceException;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.C;
import org.seed.core.config.SchemaManager;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.DataException;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationErrors;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class DBObjectValidator extends AbstractSystemEntityValidator<DBObject> {
	
	@Autowired
	private DBObjectRepository repository;
	
	@Autowired
	private SchemaManager schemaManager;
	
	@Override
	public void validateCreate(DBObject dbObject) throws ValidationException {
		Assert.notNull(dbObject, C.DBOBJECT);
		
		if (isEmpty(dbObject.getType())) {
			throw new ValidationException(createValidationErrors(dbObject)
											.addEmptyField("label.type"));
		}
	}
	
	@Override
	public void validateDelete(DBObject dbObject) throws ValidationException {
		Assert.notNull(dbObject, C.DBOBJECT);
		final var errors = createValidationErrors(dbObject);
		final var currentVersionObject = repository.get(dbObject.getId());
		
		validateDependencies(currentVersionObject, "val.inuse.dbobjectdelete", errors);
		validate(errors);
	}
	
	@Override
	public void validateSave(DBObject dbObject) throws ValidationException {
		Assert.notNull(dbObject, C.DBOBJECT);
		final var errors = createValidationErrors(dbObject);
		
		if (isEmpty(dbObject.getName())) {
			errors.addEmptyName();
		}
		else if (!isNameLengthAllowed(dbObject.getName())) {
			errors.addOverlongName(getMaxNameLength());
		}
		if (isEmpty(dbObject.getType())) {
			errors.addEmptyField("label.type");
		}
		if (isEmpty(dbObject.getOrder())) {
			errors.addEmptyField("label.order");
		}
		else if (isBelowZero(dbObject.getOrder())) {
			errors.addError("val.neg.field", "label.order");
		}
		else if (dbObject.isEnabled()) {
			if (isEmpty(dbObject.getContent())) {
				errors.addEmptyField("label.sqlstatement");
			}
			else if (!isEmpty(dbObject.getName()) &&
					 !isEmpty(dbObject.getType()) && 
					 dbObject.getType() != DBObjectType.VIEW && 
					 !dbObject.contains(dbObject)) {
				errors.addNotContains(dbObject.getObjectName());
			}
		}
		if (errors.isEmpty()) {
			validateSaveExt(dbObject, errors);
		}
		validate(errors);
	}
	
	private void validateSaveExt(DBObject dbObject, ValidationErrors errors) 
		throws ValidationException {
		if (dbObject.isEnabled()) {
			validateObjectType(dbObject, errors);
		}
		if (errors.isEmpty() && dbObject.isEnabled() && 
			(dbObject.isNew() || dbObject.getType().isEditable())) {
			testSQL(dbObject);
		}
		validateSaveUsage(dbObject, errors);
	}
	
	private void validateSaveUsage(DBObject dbObject, ValidationErrors errors) {
		final var service = getBean(DBObjectService.class); 
		final var currentVersionObject = dbObject.isNew() 
													? null 
													: repository.get(dbObject.getId());
		// disabled
		if (currentVersionObject != null && 
			currentVersionObject.isEnabled() && !dbObject.isEnabled()) {
			
			validateDependencies(currentVersionObject, "val.inuse.dbobjectdisable", errors);
		}
		
		// content changed
		else if (currentVersionObject != null && 
				 !ObjectUtils.nullSafeEquals(currentVersionObject.getContent(), 
						 					 dbObject.getContent())) {
			validateDependencies(currentVersionObject, "val.inuse.dbobjectchanged", errors);
		}
		if (!errors.isEmpty() || !dbObject.isEnabled()) {
			return;
		}
		
		// check nesteds order
		filterAndForEach(repository.find(), 
						 object -> !dbObject.equals(object) &&
						 		   dbObject.contains(object) &&
						 		   !dbObject.isOrderHigherThan(object), 
						 object -> errors.addError("val.inuse.dbobjectnestedorder", 
								 				   object.getName(), 
								 				   String.valueOf(object.getOrder())));		
		// check dependents order
		if (!dbObject.isNew()) {
			filterAndForEach(service.findUsage(currentVersionObject), 
							 object -> !dbObject.equals(object) && 
							 		   !object.isOrderHigherThan(dbObject), 
							 object -> errors.addError("val.inuse.dbobjectdependentorder", 
									 				   object.getName(), 
									 				   String.valueOf(object.getOrder())));
		}
	}
	
	private void validateDependencies(DBObject dbObject, String errorKey, 
									  ValidationErrors errors) {
		final var service = getBean(DBObjectService.class); 
		try (Session session = repository.getSession()) {
			schemaManager.findDependencies(session, dbObject.getObjectName(), null)
				.forEach(view -> errors.addError(errorKey, view, 
		 				  						 getEnumLabel(DBObjectType.VIEW)));
		}
		if (errors.isEmpty()) {
			filterAndForEach(service.findUsage(dbObject), 
							 not(dbObject::equals), 
							 object -> errors.addError(errorKey, object.getName(), 
									 				   getEnumLabel(object.getType())));
		}
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
				
			case SEQUENCE:
				if (!dbObject.contains(" sequence ")) {
					errors.addNotContains("sequence");
				}
				break;
			
			default:
				throw new UnsupportedOperationException(dbObject.getType().name());
		}
	}
	
	private void testSQL(DBObject dbObject) throws ValidationException {
		try (Session session = repository.getSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				switch (dbObject.getType()) {
					case VIEW:
						session.createNativeQuery(dbObject.getContent())
							   .setMaxResults(0).list();
						break;
						
					case PROCEDURE:
					case FUNCTION:
					case TRIGGER:
					case SEQUENCE:
						session.createNativeQuery(dbObject.getContent())
							   .executeUpdate();
						break;
						
					default:
						throw new UnsupportedOperationException(
								dbObject.getType().name());
				}
			}
			finally {
				if (tx != null) {
					tx.rollback();
				}
			}
		}
		catch (PersistenceException pex) {
			throw new ValidationException(new ValidationError(dbObject, 
										  "val.illegal.sqlstatement"), 
										  new DataException(pex));
		}
	}
	
}
