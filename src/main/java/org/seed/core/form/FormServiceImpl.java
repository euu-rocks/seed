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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.data.SystemField;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityChangeAware;
import org.seed.core.entity.EntityDependent;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFieldGroup;
import org.seed.core.entity.EntityFunction;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class FormServiceImpl extends AbstractApplicationEntityService<Form> 
	implements FormService, EntityChangeAware, EntityDependent,  
			   FormDependent, FilterDependent, TransformerDependent {
	
	@Autowired
	private EntityService entityService;
	
	@Autowired
	private LayoutService layoutService;
	
	@Autowired
	private FilterService filterService;
	
	@Autowired
	private TransformerService transformerService;
	
	@Autowired
	private FormValidator formValidator;
	
	@Autowired
	private FormRepository formRepository;
	
	@Autowired
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
		Assert.notNull(form, "form is null");
		
		super.initObject(form);
		final FormMetadata formMeta = (FormMetadata) form;
		formMeta.createLists();
		// module
		final FormOptions formOptions = form.getOptions();
		formMeta.setModule(formOptions.getModule());
		// auto layout
		if (formOptions != null && formOptions.isAutoLayout()) {
			formMeta.setLayoutContent(layoutService.buildAutoLayout(form));
		}
		// list form
		if (form.getEntity().hasAllFields()) {
			for (EntityField entityField : form.getEntity().getAllFields()) {
				form.addField(createFormField(form, entityField));
			}
		}
		// actions
		for (FormActionType actionType : FormActionType.values()) {
			if (!actionType.isDefault && 
				actionType.isDefaultSelected) {
				form.addAction(createAction(form, actionType));
			}
		}
	}
	
	void initSubForm(SubForm subForm) {
		Assert.notNull(subForm, "subForm is null");
		
		// actions
		for (FormActionType actionType : FormActionType.values()) {
			if (!actionType.isDefault &&
				actionType.isDefaultSelected && 
				actionType.isVisibleAtSubform) {
					subForm.addAction(createAction(subForm, actionType));
			}
		}
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public SubForm addSubForm(Form form, NestedEntity nested) throws ValidationException {
		Assert.notNull(form, "form is null");
		
		formValidator.validateAddSubForm(nested);
		final SubForm subForm = new SubForm();
		subForm.setNestedEntity(nested);
		form.addSubForm(subForm);
		for (EntityField entityField : nested.getFields(true)) {
			subForm.addField(createSubFormField(subForm, entityField));
		}
		initSubForm(subForm);
		return subForm;
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public FormPrintout createPrintout(Form form) {
		Assert.notNull(form, "form is null");
		
		final FormPrintout printout = new FormPrintout();
		form.addPrintout(printout);
		return printout;
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public FormAction createCustomAction(Form form) {
		Assert.notNull(form, "form is null");
		
		final FormAction action = createAction(form, FormActionType.CUSTOM);
		form.addAction(action);
		return action;
	}
	
	@Override
	public List<EntityField> getListFormFields(Entity entity, int numMax) {
		Assert.notNull(entity, "entity is null");
		
		for (Form form : findForms(entity)) {
			if (form.hasFields()) {
				final List<EntityField> result = new ArrayList<>(numMax);
				for (FormField formField : form.getFields()) {
					if (result.size() < numMax) {
						result.add(formField.getEntityField());
					}
					else {
						break;
					}
				}
				return result;
			}
		}
		return Collections.emptyList();
	}
	
	@Override
	public List<Form> findForms(Entity entity) {
		Assert.notNull(entity, "entity is null");
		
		return formRepository.find(queryParam("entity", entity));
	}
	
	@Override
	public List<Form> findUsage(Entity entity) {
		return findForms(entity);
	}
	
	@Override
	public List<Form> findUsage(EntityField entityField) {
		Assert.notNull(entityField, "entityField is null");
		
		final List<Form> result = new ArrayList<>();
		for (Form form : findAllObjects()) {
			boolean found = false;
			// same entity
			if (form.getEntity().equals(entityField.getEntity())) {
			    // check fields
				if (form.hasFields()) {
					for (FormField formField : form.getFields()) {
						if (formField.getEntityField().equals(entityField)) {
							found = true;
							break;
						}
					}
			    }
				// check layout
			    if (!found && form.getLayout() != null && !result.contains(form) &&
					layoutService.containsField(form.getLayout(), entityField)) {
					found = true;
				}
			}
			// subforms
			if (!found && form.hasSubForms()) {
				for (SubForm subForm : form.getSubForms()) {
					if (!found && subForm.getNestedEntity().getNestedEntity().equals(entityField.getEntity()) &&
						subForm.hasFields()) {
						for (SubFormField subFormField : subForm.getFields()) {
							if (subFormField.getEntityField().equals(entityField)) {
								found = true;
								break;
							}
						}
					}
				}
			}
			if (found) {
				result.add(form);
			}
		}
		return result;
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
		Assert.notNull(nestedEntity, "nestedEntity is null");
		final List<Form> result = new ArrayList<>();
		
		for (Form form : findAllObjects()) {
			if (form.hasSubForms()) {
				for (SubForm subForm : form.getSubForms()) {
					if (nestedEntity.equals(subForm.getNestedEntity())) {
						result.add(form);
						break;
					}
				}
			}
		}
		return result;
	}
	
	@Override
	public List<Form> findUsage(EntityFunction entityFunction) {
		Assert.notNull(entityFunction, "entityFunction is null");
		final List<Form> result = new ArrayList<>();
		
		for (Form form : findAllObjects()) {
			boolean found = false;
			if (form.hasActions()) {
				for (FormAction action : form.getActions()) {
					if (entityFunction.equals(action.getEntityFunction())) {
						found = true;
						break;
					}
				}
			}
			if (!found && form.hasSubForms()) {
				for (SubForm subForm : form.getSubForms()) {
					if (!found && subForm.hasActions()) {
						for (SubFormAction action : subForm.getActions()) {
							if (entityFunction.equals(action.getEntityFunction())) {
								found = true;
								break;
							}
						}
					}
				}
			}
			if (found) {
				result.add(form);
			}
		}
		return result;
	}
 	
	@Override
	public List<Form> findUsage(Form form) {
		Assert.notNull(form, "form is null");
		final List<Form> result = new ArrayList<>();
		
		for (Form otherForm : findAllObjects()) {
			if (!form.equals(otherForm)) {
				boolean found = false;
				if (otherForm.hasFieldExtras()) {
					for (FormFieldExtra fieldExtra : otherForm.getFieldExtras()) {
						if (form.equals(fieldExtra.getDetailForm())) {
							found = true;
							break;
						}
					}
				}
				if (!found && otherForm.hasActions()) {
					for (FormAction action : otherForm.getActions()) {
						if (form.equals(action.getTargetForm())) {
							found = true;
							break;
						}
					}
				}
				if (!found && otherForm.hasTransformers()) {
					for (FormTransformer transformer : otherForm.getTransformers()) {
						if (form.equals(transformer.getTargetForm())) {
							found = true;
							break;
						}
					}
				}
				if (!found && otherForm.hasSubForms()) {
					for (SubForm subForm : otherForm.getSubForms()) {
						if (!found && subForm.hasFields()) {
							for (SubFormField subFormField : subForm.getFields()) {
								if (form.equals(subFormField.getDetailForm())) {
									found = true;
									break;
								}
							}
						}
					}
				}
				if (found) {
					result.add(form);
				}
			}
		}
		return result;
	}
	
	@Override
	public List<Form> findUsage(Transformer transformer) {
		Assert.notNull(transformer, "transformer is null");
		final List<Form> result = new ArrayList<>();
		
		for (Form form : findAllObjects()) {
			boolean found = false;
			if (form.hasTransformers()) {
				for (FormTransformer formTransformer : form.getTransformers()) {
					if (formTransformer.getTransformer().equals(transformer)) {
						found = true;
						break;
					}
				}
			}
			if (!found && form.getFieldExtras() != null) {
				for (FormFieldExtra fieldExtra : form.getFieldExtras()) {
					if (transformer.equals(fieldExtra.getTransformer())) {
						found = true;
						break;
					}
				}
			}
			if (!found && form.hasSubForms()) {
				for (SubForm subForm : form.getSubForms()) {
					if (!found && subForm.hasFields()) {
						for (SubFormField subFormField : subForm.getFields()) {
							if (transformer.equals(subFormField.getTransformer())) {
								found = true;
								break;
							}
						}
					}
				}
			}
			if (found) {
				result.add(form);
			}
		}
		return result;
	}
	
	@Override
	public List<Form> findUsage(Filter filter) {
		Assert.notNull(filter, "filter is null");
		final List<Form> result = new ArrayList<>();
		
		for (Form form : findAllObjects()) {
			boolean found = false;
			if (form.getFieldExtras() != null) {
				for (FormFieldExtra fieldExtra : form.getFieldExtras()) {
					if (filter.equals(fieldExtra.getFilter())) {
						found = true;
						break;
					}
				}
			}
			if (!found && form.hasSubForms()) {
				for (SubForm subForm : form.getSubForms()) {
					if (!found && subForm.hasFields()) {
						for (SubFormField subFormField : subForm.getFields()) {
							if (filter.equals(subFormField.getFilter())) {
								found = true;
								break;
							}
						}
					}
				}
			}
			if (found) {
				result.add(form);
			}
		}
		return result;
	}
	
	@Override
	public List<FormTransformer> getFormTransformers(Form form, User user, EntityStatus status) {
		Assert.notNull(form, "form is null");
		Assert.notNull(user, "user is null");
		
		final List<FormTransformer> result = new ArrayList<>();
		if (form.hasTransformers()) {
			for (FormTransformer transformer : form.getTransformers()) {
				if (transformer.getTransformer().isAuthorized(user) &&
					(status == null || transformer.getTransformer().isEnabled(status))) {
					result.add(transformer);
				}
			}
		}
		return result;
	}
	
	@Override
	public List<FormField> getAvailableFields(Form form) {
		Assert.notNull(form, "form is null");
		
		final Entity entity = form.getEntity();
		final List<FormField> result = new ArrayList<>();
		// entity fields
		if (entity.hasAllFields()) {
			for (EntityField entityField : entity.getAllFields()) {
				boolean found = false;
				if (form.hasFields()) {
					for (FormField formField : form.getFields()) {
						if (entityField.equals(formField.getEntityField())) {
							found = true;
							break;
						}
					}
				}
				if (!found) {
					result.add(createFormField(form, entityField));
				}
			}
		}
		// system fields
		for (SystemField systemField : SystemField.values()) {
			if (systemField != SystemField.ENTITYSTATUS || entity.hasStatus()) {
				boolean found = false;
				if (form.hasFields()) {
					for (FormField formField : form.getFields()) {
						if (formField.getSystemField() == systemField) {
							found = true;
							break;
						}
					}
				}
				if (!found) {
					result.add(createSystemFormField(form, systemField));
				}
			}
		}
		return result;
	}
	
	@Override
	public List<FormTransformer> getAvailableTransformers(Form form) {
		Assert.notNull(form, "form is null");
		
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
		Assert.notNull(form, "form is null");
		
		final List<FormAction> result = new ArrayList<>();
		for (FormActionType actionType : FormActionType.values()) {
			if (!actionType.isDefault && actionType != FormActionType.CUSTOM &&
				form.getActionByType(actionType) == null) {
					result.add(createAction(form, actionType));
			}
		}
		return result;
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
	public void analyzeObjects(ImportAnalysis analysis, Module currentVersionModule) {
		Assert.notNull(analysis, "analysis is null");
		
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
		if (currentVersionModule != null && currentVersionModule.getForms() != null) {
			for (Form currentVersionForm : currentVersionModule.getForms()) {
				if (analysis.getModule().getFormByUid(currentVersionForm.getUid()) == null) {
					analysis.addChangeDelete(currentVersionForm);
				}
			}
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends ApplicationEntityService<? extends ApplicationEntity>>[] getImportDependencies() {
		return (Class<? extends ApplicationEntityService<? extends ApplicationEntity>>[]) 
				new Class[] { FilterService.class, TransformerService.class };
	}
	
	@Override
	public void importObjects(TransferContext context, Session session) {
		Assert.notNull(context, "context is null");
		Assert.notNull(session, "session is null");
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
					if (form.hasFields()) {
						initFields(form, currentVersionForm);
					}
					if (form.hasFieldExtras()) {
						initFieldExtras(session, form, currentVersionForm);
					}
					if (form.hasActions()) {
						initActions(session, form, currentVersionForm);
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
					if (form.getLayout() != null) {
						final FormLayout currentVersionLayout =
							currentVersionForm != null ? currentVersionForm.getLayout() : null;
						if (currentVersionLayout != null) {
							currentVersionLayout.copySystemFieldsTo(form.getLayout());
							session.detach(currentVersionLayout);
						}
						session.saveOrUpdate(form.getLayout());
					}
					session.saveOrUpdate(form);
				}
				
				// set references to other forms
				for (Form form : context.getModule().getForms()) {
					if (form.hasFieldExtras()) {
						for (FormFieldExtra fieldExtra : form.getFieldExtras()) {
							if (fieldExtra.getDetailFormUid() != null) {
								fieldExtra.setDetailForm(findByUid(session, fieldExtra.getDetailFormUid()));
							}
						}
					}
					if (form.hasActions()) {
						for (FormAction action : form.getActions()) {
							if (action.getTargetFormUid() != null) {
								action.setTargetForm(findByUid(session, action.getTargetFormUid()));
							}
						}
					}
					if (form.hasTransformers()) {
						for (FormTransformer transformer : form.getTransformers()) {
							if (transformer.getTargetFormUid() != null) {
								transformer.setTargetForm(findByUid(session, transformer.getTargetFormUid()));
							}
						}
					}
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
					// validate and save
					saveObject(form, session);
				}
			}
		}
		catch (ValidationException vex) {
			throw new RuntimeException(vex);
		}
	}
	
	@Override
	public void deleteObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, "module is null");
		Assert.notNull(currentVersionModule, "currentVersionModule is null");
		Assert.notNull(session, "session is null");
		
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
		Assert.notNull(form, "form is null");
		
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
		Assert.notNull(form, "form is null");
		Assert.notNull(session, "session is null");
		
		for (FormChangeAware changeAware : changeAwareObjects) {
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
		Assert.notNull(form, "form is null");
		
		cleanupForm(form);
		try (Session session = formRepository.openSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				saveObject(form, session);
				tx.commit();
			}
			catch (Exception ex) {
				handleException(tx, ex);
			}
		}
	}
	
	@Override
	public void saveObject(Form form, Session session) throws ValidationException {
		Assert.notNull(form, "form is null");
		Assert.notNull(session, "session is null");
		
		final boolean isInsert = form.isNew();
		super.saveObject(form, session);
		
		if (form.getLayout() != null) {
			session.saveOrUpdate(form.getLayout());
		}
		
		for (FormChangeAware changeAware : changeAwareObjects) {
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
		Assert.notNull(entity, "entity is null");
		Assert.notNull(session, "session is null");
		
		final FormOptions formOptions = entity.getOptions();
		if (formOptions != null && formOptions.isAutoLayout()) {
			// create auto form
			final Form form = createInstance(formOptions);
			form.setName(entity.getName());
			((FormMetadata) form).setEntity(entity);
			try {
				initObject(form);
				layoutService.rebuildLayout(form);
				saveObject(form, session);
			} catch (ValidationException vex) {
				throw new RuntimeException(vex);
			}
		}
	}
	
	@Override
	public void notifyDelete(Entity entity, Session session) {
		// do nothing
	}
	
	@Override
	public void notifyChange(Entity entity, Session session) {
		Assert.notNull(entity, "entity is null");
		Assert.notNull(session, "session is null");
		
		for (Form form : formRepository.find(session, queryParam("entity", entity))) {
			if (form.getLayout() != null) {
				layoutService.rebuildLayout(form);
				session.save(form.getLayout());
			}
		}
	}
	
	private List<FormAction> getFormActions(Form form, boolean isList) {
		Assert.notNull(form, "form is null");
		
		final List<FormAction> result = new ArrayList<>();
		for (FormActionType actionType : FormActionType.values()) {
			if (actionType.isDefault && 
				actionType.comesFirst() &&
				((isList && actionType.isVisibleAtList) ||
				(!isList && actionType.isVisibleAtDetail))) {
				result.add(createAction(form, actionType));
			}
		}
		if (form.hasActions()) {
			for (FormAction action : form.getActions()) {
				if ((isList && action.getType().isVisibleAtList) ||
				   (!isList && action.getType().isVisibleAtDetail)) {
					result.add(action);
				}
			}
		}
		for (FormActionType actionType : FormActionType.values()) {
			if (actionType.isDefault && 
				!actionType.comesFirst() &&
				((isList && actionType.isVisibleAtList) ||
				(!isList && actionType.isVisibleAtDetail))) {
				
				boolean enabled = false;
				switch (actionType) {
					case SELECTCOLS:
						enabled = true;
						break;
					case STATUS:
						if (form.getEntity().hasStatus()) {
							enabled = true;
						}
						break;
					case TRANSFORM:
						if (form.hasTransformers()) {
							enabled = true;
						}
						break;
					default:
						throw new UnsupportedOperationException(actionType.name());
				}
				if (enabled) {
					result.add(createAction(form, actionType));
				}
 			}
		}
		return result;
	}
	
	private void initFields(Form form, Form currentVersionForm) {
		for (FormField formField : form.getFields()) {
			formField.setForm(form);
			formField.setEntityField(form.getEntity().findFieldByUid(formField.getEntityFieldUid()));
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
	
	private void initActions(Session session, Form form, Form currentVersionForm) {
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
			if (subForm.hasActions()) {
				for (SubFormAction subFormAction : subForm.getActions()) {
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
			}
			if (subForm.hasFields()) {
				for (SubFormField subFormField : subForm.getFields()) {
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
			}
		}
	}
	
	private void cleanupForm(Form form) {
		Assert.notNull(form, "form is null");
		
		if (form.hasFields()) {
			for (FormField field : form.getFields()) {
				if (field.getThumbnailWidth() != null && 
					!field.getEntityField().getType().isBinary()) {
					field.setThumbnailWidth(null);
				}
			}
		}
		if (form.hasActions()) {
			for (FormAction action : form.getActions()) {
				if (action.getEntityFunction() != null &&
					!action.isCustom()) {
					action.setEntityFunction(null);
				}
			}
		}
		if (form.hasFieldExtras()) {
			final List<String> fieldIds = layoutService.getFieldIdList(form.getLayout());
			form.getFieldExtras().removeIf(e -> !fieldIds.contains(e.getEntityField().getUid()));
		}
		if (form.hasSubForms()) {
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
	}
	
	private static FormAction createAction(Form form, FormActionType actionType) {
		Assert.notNull(form, "form is null");
		Assert.notNull(actionType, "actionType is null");
		
		final FormAction action = new FormAction();
		action.setForm(form);
		action.setType(actionType);
		return action;
	}
	
	private static SubFormAction createAction(SubForm subForm, FormActionType actionType) {
		Assert.notNull(subForm, "subForm is null");
		Assert.notNull(actionType, "actionType is null");
		
		final SubFormAction action = new SubFormAction();
		action.setSubForm(subForm);
		action.setType(actionType);
		return action;
	}
	
	private static FormField createFormField(Form form, EntityField entityField) {
		Assert.notNull(form, "form is null");
		Assert.notNull(entityField, "entityField is null");
		
		final FormField formField = new FormField();
		formField.setForm(form);
		formField.setEntityField(entityField);
		return formField;
	}
	
	private static FormField createSystemFormField(Form form, SystemField systemField) {
		Assert.notNull(form, "form is null");
		Assert.notNull(systemField, "systemField is null");
		
		final FormField formField = new FormField();
		formField.setForm(form);
		formField.setSystemField(systemField);
		return formField;
	}
	
	private static SubFormField createSubFormField(SubForm subForm, EntityField entityField) {
		Assert.notNull(subForm, "subForm is null");
		Assert.notNull(entityField, "entityField is null");
		
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
