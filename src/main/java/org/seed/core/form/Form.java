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

import org.seed.core.application.ApplicationEntity;
import org.seed.core.data.SystemField;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.user.User;

public interface Form extends ApplicationEntity {
	
	Entity getEntity();
	
	String getEntityUid();
	
	FormLayout getLayout();
	
	Filter getFilter();
	
	String getFilterUid();
	
	boolean isAutoLayout();
	
	AutolayoutType getAutolayoutType();
	
	boolean hasFields();
	
	boolean containsEntityField(EntityField entityField);
	
	boolean containsSystemField(SystemField systemField);
	
	boolean containsEntityFunction(EntityFunction entityFunction);
	
	boolean containsTransformer(Transformer transformer);
	
	boolean containsFilter(Filter filter);
	
	boolean containsForm(Form form);
	
	List<FormField> getFields();
	
	List<FormField> getSelectedFields(boolean selected);
	
	FormField getFieldById(Long fieldId);
	
	FormField getFieldByUid(String fieldUid);
	
	FormFieldExtra getFieldExtraByUid(String fieldExtraUid);
	
	FormAction getActionByUid(String actionUid);
	
	FormTransformer getTransformerByUid(String transformerUid);
	
	FormPrintout getPrintoutByUid(String printoutUid);
	
	SubForm getSubFormByUid(String subFormUid);
	
	SubForm getSubFormByNestedEntityUid(String nestedEntityUid);
	
	RelationForm getRelationFormByUid(String relationFormUid);
	
	boolean isFieldVisible(EntityField entityField, EntityStatus status, User user);
	
	boolean isFieldReadonly(EntityField entityField, EntityStatus status, User user);
	
	boolean isFieldMandatory(EntityField entityField);
	
	boolean hasFieldExtras();
	
	List<FormFieldExtra> getFieldExtras();
	
	void addField(FormField field);
	
	void removeField(FormField field);
	
	boolean hasActions();
	
	List<FormAction> getActions();
	
	List<FormAction> getActions(boolean isList);
	
	boolean isActionEnabled(FormAction action, User user);
	
	boolean isActionEnabled(SubFormAction action, User user);
	
	void addAction(FormAction action);
	
	FormAction getActionByType(FormActionType actionType);
	
	void removeAction(FormAction action);
	
	boolean hasTransformers();
	
	List<FormTransformer> getTransformers();
	
	void addTransformer(FormTransformer transformer);
	
	void removeTransformer(FormTransformer transformer);
	
	List<FormPrintout> getPrintouts();
	
	boolean hasPrintouts();
	
	void addPrintout(FormPrintout printout);
	
	void removePrintout(FormPrintout printout);
	
	List<SubForm> getSubForms();
	
	boolean hasSubForms();
	
	boolean isSubFormVisible(String nestedEntityUid, User user);
	
	boolean isRelationFormVisible(String relationEntityUid, User user);
	
	void addSubForm(SubForm subForm);
	
	SubForm getSubFormByEntityId(Long entityId);
	
	SubForm getSubFormByNestedEntityId(Long nestedEntityId);
	
	SubFormField getSubFormField(EntityField entityField);
	
	void removeSubForm(SubForm subForm);
	
	FormFieldExtra getFieldExtra(EntityField entityField);
	
	void addFieldExtra(FormFieldExtra fieldExtra);
	
	void removeFieldExtra(FormFieldExtra fieldExtra);
	
	void removeRelationForm(RelationForm relationForm);
	
}
