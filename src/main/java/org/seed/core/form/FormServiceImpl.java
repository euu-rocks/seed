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
import org.seed.Seed;
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
import org.seed.core.util.BeanUtils;
import org.seed.core.util.MiscUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class FormServiceImpl extends AbstractApplicationEntityService<Form> 
	implements FormService, EntityChangeAware, EntityDependent<Form>,  
			   FormDependent<Form>, FilterDependent<Form>, TransformerDependent<Form>,
			   ApplicationContextAware {
	
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
	
	private ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
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
		for (FormActionType actionType : FormActionType.values()) {
			if (!actionType.isDefault && 
				actionType.isDefaultSelected) {
				form.addAction(createAction(form, actionType));
			}
		}
	}
	
	@Override
	public List<Filter> getFilters(Form form) {
		Assert.notNull(form, C.FORM);
		
		return filterService.findFilters(form.getEntity());
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public SubForm addSubForm(Form form, NestedEntity nested) throws ValidationException {
		Assert.notNull(form, C.FORM);
		
		formValidator.validateAddSubForm(nested);
		final SubForm subForm = new SubForm();
		subForm.setNestedEntity(nested);
		form.addSubForm(subForm);
		for (EntityField entityField : nested.getFields(true)) {
			subForm.addField(createSubFormField(subForm, entityField));
		}
		for (FormActionType actionType : FormActionType.values()) {
			if (!actionType.isDefault &&
				actionType.isDefaultSelected && 
				actionType.isVisibleAtSubform) {
					subForm.addAction(createAction(subForm, actionType));
			}
		}
		return subForm;
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void addRelationForm(Form form, EntityRelation relation) throws ValidationException {
		Assert.notNull(form, C.FORM);
		
		formValidator.validateAddRelationForm(relation);
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
	public List<Form> findUsage(Entity entity) {
		if (!entity.isGeneric()) {
			return formRepository.find(queryParam(C.ENTITY, entity), 
									   queryParam("autoLayout", false)); // ignore auto layout forms
		}
		return Collections.emptyList();
	}
	
	@Override
	public List<Form> findUsage(EntityField entityField) {
		Assert.notNull(entityField, C.ENTITYFIELD);
		
		final List<Form> result = new ArrayList<>();
		for (Form form : getObjects()) {
			if (form.isAutoLayout()) {
				continue;
			}
			if (form.containsEntityField(entityField) || 
				(form.getLayout() != null && 
				getLayoutService().containsField(form.getLayout(), entityField))) {
				result.add(form);
			}
			// check subforms
			else if (form.hasSubForms()) {
				for (SubForm subForm : form.getSubForms()) {
					if (subForm.containsEntityField(entityField)) {
						result.add(form);
						break; // skip other subforms
					}
				}
			}
		}
		return result;
	}
	
	@Override
	public List<Form> findUsage(EntityRelation entityRelation) {
		Assert.notNull(entityRelation, C.RELATION);
		
		return subList(getObjects(), form -> !form.isAutoLayout() && 
											 getLayoutService().containsRelation(form.getLayout(), entityRelation));
	}
	
	@Override
	public List<Form> findUsage(EntityFieldGroup fieldGroup) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Form> findUsage(EntityStatus entityStatus) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Form> findUsage(NestedEntity nestedEntity) {
		Assert.notNull(nestedEntity, C.NESTEDENTITY);
		
		return subList(getObjects(), form -> !form.isAutoLayout() && 
											 anyMatch(form.getSubForms(), 
													  subForm -> nestedEntity.equals(subForm.getNestedEntity())));
	}
	
	@Override
	public List<Form> findUsage(EntityFunction entityFunction) {
		Assert.notNull(entityFunction, "entityFunction");
		
		return subList(getObjects(), form -> form.containsEntityFunction(entityFunction) || 
											 anyMatch(form.getSubForms(), 
													  subForm -> subForm.containsEntityFunction(entityFunction)));
	}
 	
	@Override
	public List<Form> findUsage(Form form) {
		Assert.notNull(form, C.FORM);
		
		return subList(getObjects(), other -> !form.equals(other) && 
											  (form.containsForm(other) || 
											   anyMatch(other.getSubForms(), subForm -> subForm.containsForm(form))));
	}
	
	@Override
	public List<Form> findUsage(Transformer transformer) {
		Assert.notNull(transformer, C.TRANSFORMER);
		
		return subList(getObjects(), form -> form.containsTransformer(transformer) ||
											 anyMatch(form.getSubForms(), subForm -> subForm.containsTransformer(transformer)));
	}
	
	@Override
	public List<Form> findUsage(Filter filter) {
		Assert.notNull(filter, C.FILTER);
		
		return subList(getObjects(), form -> form.containsFilter(filter) ||
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
	public List<FormField> getAvailableFields(Form form) {
		Assert.notNull(form, C.FORM);
		
		final Entity entity = form.getEntity();
		final List<FormField> result = new ArrayList<>();
		// entity fields
		if (entity.hasAllFields()) {
			for (EntityField entityField : entity.getAllFields()) {
				if (!form.containsEntityField(entityField)) {
					result.add(createFormField(form, entityField));
				}
			}
		}
		// system fields
		for (SystemField systemField : SystemField.publicSystemFields()) {
			if ((systemField != SystemField.ENTITYSTATUS || entity.hasStatus()) && 
				!form.containsSystemField(systemField)) {
				result.add(createSystemFormField(form, systemField));
			}
		}
		return result;
	}
	
	@Override
	public List<FormTransformer> getAvailableTransformers(Form form) {
		Assert.notNull(form, C.FORM);
		
		final List<FormTransformer> result = new ArrayList<>();
		for (Transformer transformer : transformerService.findTransformers(form.getEntity())) {
			boolean found = false;
			if (form.hasTransformers()) {
				for (FormTransformer formTransformer : form.getTransformers()) {
					if (transformer.equals(formTransformer.getTransformer())) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				final FormTransformer formTransformer = new FormTransformer();
				formTransformer.setForm(form);
				formTransformer.setTransformer(transformer);
				result.add(formTransformer);
			}
		}
		return result;
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
		if (currentVersionModule.getForms() != null) {
			for (Form currentVersionForm : currentVersionModule.getForms()) {
				if (analysis.getModule().getFormByUid(currentVersionForm.getUid()) == null) {
					analysis.addChangeDelete(currentVersionForm);
				}
			}
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends ApplicationEntityService<ApplicationEntity>>[] getImportDependencies() {
		return new Class[] { FilterService.class, TransformerService.class };
	}
	
	@Override
	public void importObjects(TransferContext context, Session session) {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(session, C.SESSION);
		try {
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
		catch (ValidationException vex) {
			throw new InternalException(vex);
		}
	}
	
	@Override
	public void deleteObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, C.MODULE);
		Assert.notNull(currentVersionModule, "currentVersionModule");
		Assert.notNull(session, C.SESSION);
		
		if (currentVersionModule.getForms() != null) {
			for (Form currentVersionForm : currentVersionModule.getForms()) {
				if (module.getFormByUid(currentVersionForm.getUid()) == null) {
					session.delete(currentVersionForm);
				}
			}
		}
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
		
		for (Form form : formRepository.find(session, queryParam(C.ENTITY, entity))) {
			// remove field if entity field no longer exist
			form.getFields().removeIf(field -> field.getEntityField() != null && 
											   !entity.containsAllField(field.getEntityField()));
			if (form.isAutoLayout()) {
				updateAutoLayout(entity, form);
			}
			else if (form.getLayout() != null) {
				getLayoutService().rebuildLayout(form);
			}
			session.save(form.getLayout());
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
		for (FormFieldExtra fieldExtra : form.getFieldExtras()) {
			if (fieldExtra.getDetailFormUid() != null) {
				fieldExtra.setDetailForm(findByUid(session, fieldExtra.getDetailFormUid()));
			}
		}
	}
	
	private void initReferenceActions(Form form, Session session) {
		for (FormAction action : form.getActions()) {
			if (action.getTargetFormUid() != null) {
				action.setTargetForm(findByUid(session, action.getTargetFormUid()));
			}
		}
	}
	
	private void initReferenceTransformers(Form form, Session session) {
		for (FormTransformer transformer : form.getTransformers()) {
			if (transformer.getTargetFormUid() != null) {
				transformer.setTargetForm(findByUid(session, transformer.getTargetFormUid()));
			}
		}
	}
	
	private void initSubFormReferences(Form form, Session session) {
		if (form.hasSubForms()) {
			for (SubForm subForm : form.getSubForms()) {
				if (subForm.hasFields()) {
					for (SubFormField subFormField : subForm.getFields()) {
						if (subFormField.getDetailFormUid() != null) {
							subFormField.setDetailForm(findByUid(session, subFormField.getDetailFormUid()));
						}
					}
				}
			}
		}
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
		return Seed.getBean(LayoutService.class);
	}
	
	private List<FormChangeAware> getChangeAwareObjects() {
		if (changeAwareObjects == null) {
			changeAwareObjects = BeanUtils.getBeans(applicationContext, FormChangeAware.class);
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
		for (SubForm subForm : form.getSubForms()) {
			if (subForm.hasActions()) {
				for (SubFormAction action : subForm.getActions()) {
					if (action.getEntityFunction() != null &&
						!action.isCustom()) {
						action.setEntityFunction(null);
					}
				}
			}
		}
	}
	
	private static List<FormField> createFormFields(Form form) {
		Assert.notNull(form, C.FORM);
		
		return MiscUtils.castList(
				ListUtils.union(createEntityFields(form), 
								createSystemFields(form)));
	}
	
	private static List<FormField> createEntityFields(Form form) {
		final List<FormField> result = new ArrayList<>();
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
		final Entity entity = form.getEntity();
		return filterAndConvert(SystemField.publicSystemFields(), 
								field -> (field != SystemField.ENTITYSTATUS || entity.hasStatus()) && 
										 !form.containsSystemField(field), 
								field-> createSystemFormField(form, field));
	}
	
	private static FormAction createAction(Form form, FormActionType actionType) {
		final FormAction action = new FormAction();
		action.setForm(form);
		action.setType(actionType);
		return action;
	}
	
	private static SubFormAction createAction(SubForm subForm, FormActionType actionType) {
		final SubFormAction action = new SubFormAction();
		action.setSubForm(subForm);
		action.setType(actionType);
		return action;
	}
	
	private static FormField createFormField(Form form, EntityField entityField) {
		final FormField formField = new FormField();
		formField.setForm(form);
		formField.setEntityField(entityField);
		return formField;
	}
	
	private static FormField createSystemFormField(Form form, SystemField systemField) {
		final FormField formField = new FormField();
		formField.setForm(form);
		formField.setSystemField(systemField);
		return formField;
	}
	
	private static SubFormField createSubFormField(SubForm subForm, EntityField entityField) {
		final SubFormField subFormField = new SubFormField();
		subFormField.setSubForm(subForm);
		subFormField.setEntityField(entityField);
		
		if (entityField.getType().isBinary()) {
			subFormField.setWidth("100");
			subFormField.setHeight("25");
		}
		return subFormField;
	}

}
