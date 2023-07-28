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
package org.seed.core.form;

import java.util.List;

import org.hibernate.Session;

import org.seed.core.application.ApplicationEntityService;
import org.seed.core.codegen.GeneratedCode;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityRelation;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.NestedEntity;
import org.seed.core.entity.filter.Filter;
import org.seed.core.user.User;

public interface FormService extends ApplicationEntityService<Form> {
	
	SubForm addSubForm(Form form, NestedEntity nested) throws ValidationException;
	
	void addRelationForm(Form form, EntityRelation relation) throws ValidationException;
	
	FormFunction createFunction(Form form);
	
	FormPrintout createPrintout(Form form);
	
	FormAction createCustomAction(Form form);
	
	List<Filter> getFilters(Form form, Session sesion);
	
	List<Form> findForms(Entity entity);
	
	List<Form> findForms(Entity entity, Session session);
	
	List<FormTransformer> getAvailableTransformers(Form form, Session session);
	
	List<FormTransformer> getFormTransformers(Form form, User user, EntityStatus status);
	
	List<FormAction> getAvailableActions(Form form);
	
	List<FormAction> getListFormActions(Form form);
	
	List<FormAction> getDetailFormActions(Form form);
	
	Class<GeneratedCode> getFunctionClass(Form form, String functionName);
	
}
