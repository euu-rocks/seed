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

import static org.seed.core.util.CollectionUtils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.ListUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.C;
import org.seed.InternalException;
import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.data.AbstractSystemObject;
import org.seed.core.data.SystemField;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityChangeAware;
import org.seed.core.entity.EntityDependent;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFieldGroup;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityRelation;
import org.seed.core.entity.EntityService;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.NestedEntity;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.filter.FilterDependent;
import org.seed.core.entity.filter.FilterService;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerDependent;
import org.seed.core.entity.transform.TransformerService;
import org.seed.core.form.layout.LayoutService;
import org.seed.core.user.User;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class FormServiceImpl extends AbstractApplicationEntityService<Form> 
	implements FormService, EntityChangeAware, EntityDependent<Form>,  
			   FormDependent<Form>, FilterDependent<Form>, TransformerDependent<Form> {
	
	@Autowired
	private EntityService entityService;
	
	@Autowired
	private FilterService filterService;
	
	@Autowired
	private TransformerService transformerService;
	
	@Autowired
	private FormValidator formValidator;
	
	@Autowired
	private FormRepository formRepository;
	
	private List<FormChangeAware> changeAwareObjects;
	
	@Override
	protected FormRepository getRepository() {
		return formRepository;
	}

	@Override
	protected FormValidator getValidator() {
		return formValidator;
	}
	
	@Override
	public void initObject(Form form) throws ValidationException {
		Assert.notNull(form, C.FORM);
		
		super.initObject(form);
		final FormMetadata formMeta = (FormMetadata) form;
		formMeta.createLists();
		
		final FormOptions formOptions = form.getOptions();
		if (formOptions != null) {
			formMeta.setModule(formOptions.getModule());
			if (formOptions.isAutoLayout()) {
				formMeta.setAutoLayout(true);
				formMeta.setLayoutContent(getLayoutService().buildAutoLayout(form.getEntity(), form));
			}
		}
		// list form fields
		formMeta.setFields(createFormFields(form));
		// actions
		filterAndForEach(FormActionType.values(), 
						 actionType -> !actionType.isDefault && actionType.isDefaultSelected, 
						 actionType -> form.addAction(createAction(form, actionType)));
	}
	
	@Override
	public List<Filter> getFilters(Form form, Session session) {
		Assert.notNull(form, C.FORM);
		
		return filterService.findFilters(form.getEntity(), session);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public SubForm addSubForm(Form form, NestedEntity nested) throws ValidationException {
		Assert.notNull(form, C.FORM);
		
		formValidator.validateAddSubForm(form, nested);
		final SubForm subForm = new SubForm();
		subForm.setNestedEntity(nested);
		form.addSubForm(subForm);
		// sub form fields
		nested.getFields(true).forEach(entityField -> 
				subForm.addField(createSubFormField(subForm, entityField)));
		// actions
		filterAndForEach(FormActionType.values(), 
						 actionType -> !actionType.isDefault && actionType.isDefaultSelected && 
						 			   actionType.isVisibleAtSubform, 
						 actionType -> subForm.addAction(createAction(subForm, actionType)));
		return subForm;
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void addRelationForm(Form form, EntityRelation relation) throws ValidationException {
		Assert.notNull(form, C.FORM);
		
		formValidator.validateAddRelationForm(form, relation);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public FormPrintout createPrintout(Form form) {
		Assert.notNull(form, C.FORM);
		
		final FormPrintout printout = new FormPrintout();
		form.addPrintout(printout);
		return printout;
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public FormAction createCustomAction(Form form) {
		Assert.notNull(form, C.FORM);
		
		final FormAction action = createAction(form, FormActionType.CUSTOM);
		form.addAction(action);
		return action;
	}
	
	@Override
	public List<Form> findForms(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		return formRepository.find(queryParam(C.ENTITY, entity));
	}
	
	@Override
	public List<Form> findForms(Entity entity, Session session) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(session, C.SESSION);
		
		return formRepository.find(session, queryParam(C.ENTITY, entity));
	}
	
	@Override
	public List<Form> findUsage(Entity entity, Session session) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(session, C.SESSION);
		
		return entity.isGeneric()
				? Collections.emptyList()
				: formRepository.find(session, queryParam(C.ENTITY, entity), 
											   queryParam("autoLayout", false)); // ignore auto layout forms
	}
	
	@Override
	public List<Form> findUsage(EntityField entityField, Session session) {
		Assert.notNull(entityField, C.ENTITYFIELD);
		Assert.notNull(session, C.SESSION);
		
		return subList(getObjects(session), form -> !form.isAutoLayout() && 
						(form.containsEntityField(entityField) || 
						(form.getLayout() != null && getLayoutService().containsField(form.getLayout(), entityField)) ||
						anyMatch(form.getSubForms(), subForm -> subForm.containsEntityField(entityField))));
	}
	
	@Override
	public List<Form> findUsage(EntityRelation entityRelation, Session session) {
		Assert.notNull(entityRelation, C.RELATION);
		Assert.notNull(session, C.SESSION);
		
		return subList(getObjects(session), form -> !form.isAutoLayout() && 
													getLayoutService().containsRelation(form.getLayout(), entityRelation));
	}
	
	@Override
	public List<Form> findUsage(EntityFieldGroup fieldGroup) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Form> findUsage(EntityStatus entityStatus, Session session) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Form> findUsage(NestedEntity nestedEntity, Session session) {
		Assert.notNull(nestedEntity, C.NESTEDENTITY);
		Assert.notNull(session, C.SESSION);
		
		return subList(getObjects(session), form -> !form.isAutoLayout() && 
											 		anyMatch(form.getSubForms(), 
											 				 subForm -> nestedEntity.equals(subForm.getNestedEntity())));
	}
	
	@Override
	public List<Form> findUsage(EntityFunction entityFunction, Session session) {
		Assert.notNull(entityFunction, C.FUNCTION);
		Assert.notNull(session, C.SESSION);
		
		return subList(getObjects(session), form -> form.containsEntityFunction(entityFunction) || 
											 		anyMatch(form.getSubForms(), 
											 				 subForm -> subForm.containsEntityFunction(entityFunction)));
	}
 	
	@Override
	public List<Form> findUsage(Form form, Session session) {
		Assert.notNull(form, C.FORM);
		Assert.notNull(session, C.SESSION);
		
		return subList(getObjects(session), other -> !form.equals(other) && 
													 (other.containsForm(form) || 
													 anyMatch(other.getSubForms(), subForm -> subForm.containsForm(form))));
	}
	
	@Override
	public List<Form> findUsage(Transformer transformer, Session session) {
		Assert.notNull(transformer, C.TRANSFORMER);
		Assert.notNull(session, C.SESSION);
		
		return subList(getObjects(session), form -> form.containsTransformer(transformer) ||
													anyMatch(form.getSubForms(), subForm -> subForm.containsTransformer(transformer)));
	}
	
	@Override
	public List<Form> findUsage(Filter filter, Session session) {
		Assert.notNull(filter, C.FILTER);
		Assert.notNull(session, C.SESSION);
		
		return subList(getObjects(session), form -> form.containsFilter(filter) ||
													anyMatch(form.getSubForms(), subForm -> subForm.containsFilter(filter)));
	}
	
	@Override
	public List<FormTransformer> getFormTransformers(Form form, User user, EntityStatus status) {
		Assert.notNull(form, C.FORM);
		Assert.notNull(user, C.USER);
		
		return subList(form.getTransformers(), trans -> trans.getTransformer().checkPermissions(user) &&
														(status == null || trans.getTransformer().containsStatus(status)));
	}
	
	@Override
	public List<FormTransformer> getAvailableTransformers(Form form, Session session) {
		Assert.notNull(form, C.FORM);
		
		return filterAndConvert(transformerService.findTransformers(form.getEntity(), session), 
								trans -> noneMatch(form.getTransformers(), formTrans -> trans.equals(formTrans.getTransformer())), 
								trans -> createTransformer(form, trans));
	}
	
	@Override
	public List<FormAction> getAvailableActions(Form form) {
		Assert.notNull(form, C.FORM);
		
		return filterAndConvert(FormActionType.values(), 
								type -> !type.isDefault && type != FormActionType.CUSTOM &&
										form.getActionByType(type) == null, 
								type -> createAction(form, type));
	}
	
	@Override
	public List<FormAction> getListFormActions(Form form) {
		return getFormActions(form, true);
	}
	
	@Override
	public List<FormAction> getDetailFormActions(Form form) {
		return getFormActions(form, false);
	}
	
	@Override
	protected void analyzeNextVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		if (analysis.getModule().getForms() != null) {
			for (Form form : analysis.getModule().getForms()) {
				if (currentVersionModule == null) {
					analysis.addChangeNew(form);
				}
				else {
					final Form currentVersionForm = 
						currentVersionModule.getFormByUid(form.getUid());
					if (currentVersionForm == null) {
						analysis.addChangeNew(form);
					}
					else if (!form.isEqual(currentVersionForm)) {
						analysis.addChangeModify(form);
					}
				}
			}
		}
	}
	
	@Override
	protected void analyzeCurrentVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		filterAndForEach(currentVersionModule.getForms(), 
						 form -> analysis.getModule().getFormByUid(form.getUid()) == null, 
						 analysis::addChangeDelete);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends ApplicationEntityService<ApplicationEntity>>[] getImportDependencies() {
		return new Class[] { FilterService.class, TransformerService.class };
	}
	
	@Override
	public void importObjects(TransferContext context, Session session) throws ValidationException {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(session, C.SESSION);
		
		if (context.getModule().getForms() != null) {
			for (Form form : context.getModule().getForms()) {
				final Form currentVersionForm = findByUid(session, form.getUid());
				final Entity entity = entityService.findByUid(session, form.getEntityUid());
				((FormMetadata) form).setModule(context.getModule());
				((FormMetadata) form).setEntity(entity);
				if (currentVersionForm != null) {
					((FormMetadata) currentVersionForm).copySystemFieldsTo(form);
					session.detach(currentVersionForm);
				}
				initForm(form, currentVersionForm, session);
				formRepository.save(form, session);
			}
			
			// set references to other forms
			for (Form form : context.getModule().getForms()) {
				initFormReferences(form, session);
				initSubFormReferences(form, session);
				// validate and save
				saveObject(form, session);
			}
		}
	}
	
	@Override
	public void deleteObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, C.MODULE);
		Assert.notNull(currentVersionModule, "currentVersionModule");
		Assert.notNull(session, C.SESSION);
		
		filterAndForEach(currentVersionModule.getForms(), 
						 form -> module.getFormByUid(form.getUid()) == null, 
						 session::delete);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void deleteObject(Form form) throws ValidationException {
		Assert.notNull(form, C.FORM);
		
		try (Session session = formRepository.openSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				deleteObject(form, session);
				tx.commit();
			}
			catch (Exception ex) {
				handleException(tx, ex);
			}
		}
	}
	
	@Override
	public void deleteObject(Form form, Session session) throws ValidationException {
		Assert.notNull(form, C.FORM);
		Assert.notNull(session, C.SESSION);
		
		for (FormChangeAware changeAware : getChangeAwareObjects()) {
			changeAware.notifyDelete(form, session);
		}
		
		super.deleteObject(form, session);
		
		if (form.getLayout() != null) {
			session.delete(form.getLayout());
		}
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void saveObject(Form form) throws ValidationException {
		Assert.notNull(form, C.FORM);
		
		cleanupForm(form);
		final boolean isInsert = form.isNew();
		try (Session session = formRepository.openSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				saveObject(form, session);
				tx.commit();
			}
			catch (Exception ex) {
				if (isInsert) {
					// reset id because its assigned even if insert fails
					((AbstractSystemObject) form).resetId();
				}
				handleException(tx, ex);
			}
		}
	}
	
	@Override
	public void saveObject(Form form, Session session) throws ValidationException {
		Assert.notNull(form, C.FORM);
		Assert.notNull(session, C.SESSION);
		
		final boolean isInsert = form.isNew();
		if (form.getName() == null) {
			form.setName(form.getEntity().getName());
		}
		super.saveObject(form, session);
		
		if (form.getLayout() != null) {
			session.saveOrUpdate(form.getLayout());
		}
		
		for (FormChangeAware changeAware : getChangeAwareObjects()) {
			if (isInsert) {
				changeAware.notifyCreate(form, session);
			}
			else {
				changeAware.notifyChange(form, session);
			}
		}
	}
	
	@Override
	public void notifyCreate(Entity entity, Session session) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(session, C.SESSION);
		
		final FormOptions formOptions = entity.getOptions();
		if (formOptions != null && formOptions.isAutoLayout()) {
			// create auto form
			final Form form = createInstance(formOptions);
			form.setName(entity.getName());
			((FormMetadata) form).setEntity(entity);
			((FormMetadata) form).setAutoLayout(true);
			try {
				initObject(form);
				getLayoutService().rebuildLayout(form);
				saveObject(form, session);
			} 
			catch (ValidationException vex) {
				throw new InternalException(vex);
			}
		}
	}
	
	@Override
	public void notifyDelete(Entity entity, Session session) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(session, C.SESSION);
		
		// delete auto layout forms
		for (Form form : formRepository.find(queryParam(C.ENTITY, entity), 
								   			 queryParam("autoLayout", true))) {
			try {
				deleteObject(form, session);
			} 
			catch (ValidationException vex) {
				throw new InternalException(vex);
			}
		}
	}
	
	@Override
	public void notifyBeforeChange(Entity entity, Session session) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(session, C.SESSION);
		
		for (Form form : formRepository.find(session, queryParam(C.ENTITY, entity))) {
			if (form.hasSubForms()) {
				notifyBeforeChangeSubForms(form, entity, session);
			}
			if (form.hasFieldExtras()) {
				notifyBeforeChangeFieldExtras(form, entity, session);
			}
		}
	}
	
	private void notifyBeforeChangeSubForms(Form form, Entity entity, Session session) {
		// delete sub form if nested no longer exist
		for (SubForm subForm : new ArrayList<>(form.getSubForms())) {
			if (!entity.containsNested(subForm.getNestedEntity())) {
				form.removeSubForm(subForm);
				session.delete(subForm);
			}
		}
	}
	
	private void notifyBeforeChangeFieldExtras(Form form, Entity entity, Session session) {
		// delete field extra if entity field no longer exist
		for (FormFieldExtra fieldExtra : new ArrayList<>(form.getFieldExtras())) {
			if (!entity.containsField(fieldExtra.getEntityField())) {
				form.removeFieldExtra(fieldExtra);
				session.delete(fieldExtra);
			}
		}
	}
	
	@Override
	public void notifyChange(Entity entity, Session session) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(session, C.SESSION);
		
		if (!entity.isGeneric()) {
			for (Form form : findForms(entity, session)) {
				// remove field if entity field no longer exist
				form.getFields().removeIf(field -> field.getEntityField() != null && 
												   !entity.containsAllField(field.getEntityField()));
				updateFormLayout(form, entity, session);
			}
			// update parent entity forms (entity is used as nested entity)
			for (Entity parentEntity : entityService.findParentEntities(entity, session)) {
				findForms(parentEntity, session).forEach(parentForm -> updateFormLayout(parentForm, parentEntity, session));
			}
		}
	}
	
	private void updateFormLayout(Form form, Entity entity, Session session) {
		boolean layoutChanged = false;
		if (form.isAutoLayout()) {
			updateAutoLayout(entity, form);
			layoutChanged = true;
		}
		else if (form.getLayout() != null) {
			getLayoutService().rebuildLayout(form);
			layoutChanged = true;
		}
		if (layoutChanged) {
			session.save(form);
		}
	}
	
	private void updateAutoLayout(Entity entity, Form form) {
		if (entity.hasAllFields()) {
			for (EntityField entityField : entity.getAllFields()) {
				if (!form.containsEntityField(entityField)) {
					final FormField formField = createFormField(form, entityField); 
					if (entityField.getType().isAutonum() || 
						entityField.getType().isText()) {
						formField.setSelected(true);
					}
					form.addField(formField);
				}
			}
		}
		final FormMetadata formMeta = (FormMetadata) form;
		formMeta.setLayoutContent(getLayoutService().buildAutoLayout(entity, form));
		formMeta.setOrderIndexes();
		formMeta.initUid();
		getLayoutService().rebuildLayout(form);
	}
	
	private List<FormAction> getFormActions(Form form, boolean isList) {
		Assert.notNull(form, C.FORM);
		
		final List<FormAction> result = new ArrayList<>();
		// default actions that comes first
		for (FormActionType actionType : FormActionType.defaultActionTypes(isList, true)) {
			result.add(createAction(form, actionType));
		}
		
		// form actions
		result.addAll(form.getActions(isList));
		
		// default actions that comes last
		for (FormActionType actionType : FormActionType.defaultActionTypes(isList, false)) {
			switch (actionType) {
				case SELECTCOLS:
					result.add(createAction(form, actionType));
					break;
					
				case STATUS:
					if (form.getEntity().hasStatus()) {
						result.add(createAction(form, actionType));
					}
					break;
					
				case TRANSFORM:
					if (form.hasTransformers()) {
						result.add(createAction(form, actionType));
					}
					break;
					
				default:
					throw new UnsupportedOperationException(actionType.name());
			}
		}
		return result;
	}
	
	private void initForm(Form form, Form currentVersionForm, Session session) {
		if (form.hasFields()) {
			initFields(form, currentVersionForm);
		}
		if (form.hasFieldExtras()) {
			initFieldExtras(session, form, currentVersionForm);
		}
		if (form.hasActions()) {
			initActions(form, currentVersionForm);
		}
		if (form.hasTransformers()) {
			initTransformers(session, form, currentVersionForm);
		}
		if (form.hasPrintouts()) {
			initPrintouts(form, currentVersionForm);
		}
		if (form.hasSubForms()) {
			initSubForms(session, form, currentVersionForm);
		}
		if (form.getFilterUid() != null) {
			((FormMetadata) form).setFilter(filterService.findByUid(session, form.getFilterUid()));
		}
		if (form.getLayout() != null) {
			final FormLayout currentVersionLayout =
				currentVersionForm != null ? currentVersionForm.getLayout() : null;
			if (currentVersionLayout != null) {
				currentVersionLayout.copySystemFieldsTo(form.getLayout());
				session.detach(currentVersionLayout);
			}
			getLayoutService().rebuildLayout(form);
			session.saveOrUpdate(form.getLayout());
		}
	}
	
	private void initFormReferences(Form form, Session session) {
		if (form.hasFieldExtras()) {
			initReferenceFieldExtras(form, session);
		}
		if (form.hasActions()) {
			initReferenceActions(form, session);
		}
		if (form.hasTransformers()) {
			initReferenceTransformers(form, session);
		}
	}
	
	private void initReferenceFieldExtras(Form form, Session session) {
		filterAndForEach(form.getFieldExtras(), 
						 extra -> extra.getDetailFormUid() != null, 
						 extra -> extra.setDetailForm(findByUid(session, extra.getDetailFormUid())));
	}
	
	private void initReferenceActions(Form form, Session session) {
		filterAndForEach(form.getActions(), 
						 action -> action.getTargetFormUid() != null, 
						 action -> action.setTargetForm(findByUid(session, action.getTargetFormUid())));
	}
	
	private void initReferenceTransformers(Form form, Session session) {
		filterAndForEach(form.getTransformers(), 
						 trans -> trans.getTargetFormUid() != null, 
						 trans -> trans.setTargetForm(findByUid(session, trans.getTargetFormUid())));
	}
	
	private void initSubFormReferences(Form form, Session session) {
		filterAndForEach(form.getSubForms(), 
						 SubForm::hasFields, 
						 sub -> filterAndForEach(sub.getFields(), 
								 				 field -> field.getDetailFormUid() != null, 
								 				 field -> field.setDetailForm(findByUid(session, field.getDetailFormUid()))));
	}
	
	private void initFields(Form form, Form currentVersionForm) {
		for (FormField formField : form.getFields()) {
			formField.setForm(form);
			if (formField.getEntityFieldUid() != null) {
				formField.setEntityField(form.getEntity().findFieldByUid(formField.getEntityFieldUid()));
			}
			if (currentVersionForm != null) {
				final FormField currentVersionField =
					currentVersionForm.getFieldByUid(formField.getUid());
				if (currentVersionField != null) {
					currentVersionField.copySystemFieldsTo(formField);
				}
			}
		}
	}
	
	private void initFieldExtras(Session session, Form form, Form currentVersionForm) {
		for (FormFieldExtra fieldExtra : form.getFieldExtras()) {
			fieldExtra.setForm(form);
			fieldExtra.setEntityField(form.getEntity().findFieldByUid(fieldExtra.getEntityFieldUid()));
			if (fieldExtra.getTransformerUid() != null) {
				fieldExtra.setTransformer(transformerService.findByUid(session, fieldExtra.getTransformerUid()));
			}
			if (fieldExtra.getFilterUid() != null) {
				fieldExtra.setFilter(filterService.findByUid(session, fieldExtra.getFilterUid()));
			}
			if (currentVersionForm != null) {
				final FormFieldExtra currentVersionExtra =
					currentVersionForm.getFieldExtraByUid(fieldExtra.getUid());
				if (currentVersionExtra != null) {
					currentVersionExtra.copySystemFieldsTo(fieldExtra);
				}
			}
		}
	}
	
	private void initActions(Form form, Form currentVersionForm) {
		for (FormAction action : form.getActions()) {
			action.setForm(form);
			if (action.getEntityFunctionUid() != null) {
				action.setEntityFunction(form.getEntity().getFunctionByUid(action.getEntityFunctionUid()));
			}
			if (currentVersionForm != null) {
				final FormAction currentVersionAction =
					currentVersionForm.getActionByUid(action.getUid());
				if (currentVersionAction != null) {
					currentVersionAction.copySystemFieldsTo(action);
				}
			}
		}
	}
	
	private void initTransformers(Session session, Form form, Form currentVersionForm) {
		for (FormTransformer transformer : form.getTransformers()) {
			transformer.setForm(form);
			if (transformer.getTransformerUid() != null) {
				transformer.setTransformer(transformerService.findByUid(session, transformer.getTransformerUid()));
			}
			if (currentVersionForm != null) {
				final FormTransformer currentVersionTransformer =
					currentVersionForm.getTransformerByUid(transformer.getUid());
				if (currentVersionTransformer != null) {
					currentVersionTransformer.copySystemFieldsTo(transformer);
				}
			}
		}
	}
	
	private void initPrintouts(Form form, Form currentVersionForm) {
		for (FormPrintout printout : form.getPrintouts()) {
			printout.setForm(form);
			if (currentVersionForm != null) {
				final FormPrintout currentVersionPrintout =
					currentVersionForm.getPrintoutByUid(printout.getUid());
				if (currentVersionPrintout != null) {
					currentVersionPrintout.copySystemFieldsTo(printout);
				}
			}
		}
	}
	
	private void initSubForms(Session session, Form form, Form currentVersionForm) {
		for (SubForm subForm : form.getSubForms()) {
			SubForm currentVersionSubForm = null;
			final NestedEntity nested = form.getEntity().getNestedByUid(subForm.getNestedEntityUid());
			subForm.setForm(form);
			subForm.setNestedEntity(nested);
			if (currentVersionForm != null) {
				currentVersionSubForm = currentVersionForm.getSubFormByUid(subForm.getUid());
				if (currentVersionSubForm != null) {
					currentVersionSubForm.copySystemFieldsTo(subForm);
				}
			}
			initSubFormElements(session, nested, subForm, currentVersionSubForm);
		}
	}
	
	private void initSubFormElements(Session session, NestedEntity nested, 
									 SubForm subForm, SubForm currentVersionSubForm) {
		if (subForm.hasActions()) {
			for (SubFormAction subFormAction : subForm.getActions()) {
				initSubFormAction(nested, subFormAction, subForm, currentVersionSubForm);
			}
		}
		if (subForm.hasFields()) {
			for (SubFormField subFormField : subForm.getFields()) {
				initSubFormField(session, nested, subFormField, subForm, currentVersionSubForm);
			}
		}
	}
	
	private void initSubFormAction(NestedEntity nested, SubFormAction subFormAction, 
								   SubForm subForm, SubForm currentVersionSubForm) {
		subFormAction.setSubForm(subForm);
		if (subFormAction.getEntityFunctionUid() != null) {
			subFormAction.setEntityFunction(nested.getFunctionByUid(subFormAction.getEntityFunctionUid()));
		}
		if (currentVersionSubForm != null) {
			final SubFormAction currentVersionAction =
				currentVersionSubForm.getActionByUid(subFormAction.getUid());
			if (currentVersionAction != null) {
				currentVersionAction.copySystemFieldsTo(subFormAction);
			}
		}
	}
	
	private void initSubFormField(Session session, NestedEntity nested, SubFormField subFormField, 
								  SubForm subForm, SubForm currentVersionSubForm) {
		subFormField.setSubForm(subForm);
		subFormField.setEntityField(nested.getFieldByUid(subFormField.getEntityFieldUid()));
		if (subFormField.getTransformerUid() != null) {
			subFormField.setTransformer(transformerService.findByUid(session, subFormField.getTransformerUid()));
		}
		if (subFormField.getFilterUid() != null) {
			subFormField.setFilter(filterService.findByUid(session, subFormField.getFilterUid()));
		}
		if (currentVersionSubForm != null) {
			final SubFormField currentVersionField = 
					currentVersionSubForm.getFieldByUid(subFormField.getUid());
			if (currentVersionField != null) {
				currentVersionField.copySystemFieldsTo(subFormField);
			}
		}
	}
	
	private void cleanupForm(Form form) {
		if (!form.isAutoLayout()) {
			((FormMetadata) form).setAutolayoutType(null);
		}
		if (form.hasFields()) {
			cleanupFields(form);
		}
		if (form.hasActions()) {
			cleanupActions(form);
		}
		if (form.hasFieldExtras()) {
			cleanupFieldExtras(form);
		}
		if (form.hasSubForms()) {
			cleanupSubForms(form);
		}
	}
	
	private void cleanupFields(Form form) {
		filterAndForEach(form.getFields(), 
						 field -> field.getThumbnailWidth() != null && !field.getEntityField().getType().isBinary(), 
						 field -> field.setThumbnailWidth(null));
	}
	
	private LayoutService getLayoutService() {
		return getBean(LayoutService.class);
	}
	
	private List<FormChangeAware> getChangeAwareObjects() {
		if (changeAwareObjects == null) {
			changeAwareObjects = getBeans(FormChangeAware.class);
		}
		return changeAwareObjects;
	}
	
	private void cleanupFieldExtras(Form form) {
		final Set<String> fieldIds = getLayoutService().getIdSet(form.getLayout());
		form.getFieldExtras().removeIf(extra -> !fieldIds.contains(extra.getEntityField().getUid()));
	}
	
	private void cleanupActions(Form form) {
		filterAndForEach(form.getActions(), 
						 action -> action.getEntityFunction() != null && !action.isCustom(), 
						 action -> action.setEntityFunction(null));
	}
	
	private void cleanupSubForms(Form form) {
		filterAndForEach(form.getSubForms(), 
						 SubForm::hasActions, 
						 sub -> filterAndForEach(sub.getActions(), 
								 				 action -> action.getEntityFunction() != null && !action.isCustom(), 
								 				 action -> action.setEntityFunction(null)));
	}
	
	private static List<FormField> createFormFields(Form form) {
		Assert.notNull(form, C.FORM);
		
		return MiscUtils.castList(
				ListUtils.union(createEntityFields(form), 
								createSystemFields(form)));
	}
	
	private static List<FormField> createEntityFields(Form form) {
		final var result = new ArrayList<FormField>();
		final Entity entity = form.getEntity();
		if (entity.hasAllFields()) {
			for (EntityField entityField : entity.getAllFields()) {
				if (!form.containsEntityField(entityField)) {
					final FormField formField = createFormField(form, entityField); 
					if (entityField.getType().isAutonum() || 
						entityField.getType().isText()) {
						formField.setSelected(true);
					}
					result.add(formField);
				}
			}
		}
		return result;
	}
	
	private static List<FormField> createSystemFields(Form form) {
		return filterAndConvert(SystemField.publicSystemFields(), 
								field -> (field != SystemField.ENTITYSTATUS || form.getEntity().hasStatus()) && 
										 !form.containsSystemField(field), 
								field-> createSystemFormField(form, field));
	}
	
	private static FormAction createAction(Form form, FormActionType actionType) {
		final var action = new FormAction();
		action.setForm(form);
		action.setType(actionType);
		return action;
	}
	
	private static SubFormAction createAction(SubForm subForm, FormActionType actionType) {
		final var action = new SubFormAction();
		action.setSubForm(subForm);
		action.setType(actionType);
		return action;
	}
	
	private static FormField createFormField(Form form, EntityField entityField) {
		final var formField = new FormField();
		formField.setForm(form);
		formField.setEntityField(entityField);
		return formField;
	}
	
	private static FormField createSystemFormField(Form form, SystemField systemField) {
		final var formField = new FormField();
		formField.setForm(form);
		formField.setSystemField(systemField);
		return formField;
	}
	
	private static FormTransformer createTransformer(Form form, Transformer transformer) {
		final var formTransformer = new FormTransformer();
		formTransformer.setForm(form);
		formTransformer.setTransformer(transformer);
		return formTransformer;
	}
	
	private static SubFormField createSubFormField(SubForm subForm, EntityField entityField) {
		final var subFormField = new SubFormField();
		subFormField.setSubForm(subForm);
		subFormField.setEntityField(entityField);
		
		if (entityField.getType().isBinary()) {
			subFormField.setWidth("100");
			subFormField.setHeight("25");
		}
		return subFormField;
	}

}
