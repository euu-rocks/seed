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
package org.seed.core.application.module;

import java.util.List;

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.ValidationErrors;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityService;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.filter.FilterService;
import org.seed.core.entity.transfer.Transfer;
import org.seed.core.entity.transfer.TransferService;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerService;
import org.seed.core.form.Form;
import org.seed.core.form.FormService;
import org.seed.core.form.navigation.Menu;
import org.seed.core.form.navigation.MenuService;
import org.seed.core.task.Task;
import org.seed.core.task.TaskService;
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserGroupService;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class ModuleValidator extends AbstractSystemEntityValidator<Module> {
	
	@Autowired
	private ModuleRepository repository;
	
	@Autowired
	private EntityService entityService;
	
	@Autowired
	private FilterService filterService;
	
	@Autowired
	private TransformerService transformerService;
	
	@Autowired
	private TransferService transferService;
	
	@Autowired
	private FormService formService;
	
	@Autowired
	private MenuService menuService;
	
	@Autowired
	private TaskService taskService;
	
	@Autowired
	private UserGroupService userGroupService;
	
	private List<ModuleDependent<? extends SystemEntity>> moduleDependents;
	
	public void validateImport(Module module, Module existingModule) throws ValidationException {
		Assert.notNull(module, C.MODULE);
		final var errors = createValidationErrors(module);
		
		// entities
		if (module.getEntities() != null) {
			validateEntities(module, existingModule, errors);
		}
		// filters
		if (module.getFilters() != null) {
			validateFilters(module, existingModule, errors);
		}
		// transformers
		if (module.getTransformers() != null) {
			validateTransformers(module, existingModule, errors);
		}
		// transfers
		if (module.getTransfers() != null) {
			validateTransfers(module, existingModule, errors);
		}
		// forms
		if (module.getForms() != null) {
			validateForms(module, existingModule, errors);
		}
		// menus
		if (module.getMenus() != null) {
			validateMenus(module, existingModule, errors);
		}
		// tasks
		if (module.getTasks() != null) {
			validateTasks(module, existingModule, errors);
		}
		// user groups
		if (module.getUserGroups() != null) {
			validateUserGroups(module, existingModule, errors);
		}
		
		validate(errors);
	}
	
	@Override
	public void validateSave(Module module) throws ValidationException {
		Assert.notNull(module, C.MODULE);
		final var errors = createValidationErrors(module);
		
		if (isEmpty(module.getName())) {
			errors.addEmptyName();
		}
		else if (!isNameLengthAllowed(module.getName())) {
			errors.addOverlongName(getMaxNameLength());
		}
		
		if (module.hasParameters()) {
			validateParameters(module, errors);
		}
		if (module.hasNesteds()) {
			validateNesteds(module, errors);
		}
		
		validate(errors);
	}
	
	@Override
	public void validateDelete(Module module) throws ValidationException {
		Assert.notNull(module, C.MODULE);
		final var errors = createValidationErrors(module);
		
		try (Session session = repository.openSession()) {
			for (var dependent : getModuleDependents()) {
				validateDeleteModuleDependent(module, dependent, errors, session);
			}
		}
		
		validate(errors);
	}
	
	private void validateDeleteModuleDependent(Module module, ModuleDependent<? extends SystemEntity> dependent, 
			ValidationErrors errors, Session session) {
		for (SystemEntity systemEntity : dependent.findUsage(module, session)) {
			switch (getEntityType(systemEntity)) {
				case "customcode":
					errors.addError("val.inuse.modulecustomcode", systemEntity.getName());
					break;
			
				case "datasource":
					errors.addError("val.inuse.moduledatasource", systemEntity.getName());
					break;
					
				case "dbobject":
					errors.addError("val.inuse.moduledbobject", systemEntity.getName());
					break;
			
				case C.ENTITY:
					errors.addError("val.inuse.moduleentity", systemEntity.getName());
					break;
				
				case C.FILTER:
					errors.addError("val.inuse.modulefilter", systemEntity.getName());
					break;
					
				case C.FORM:
					errors.addError("val.inuse.moduleform", systemEntity.getName());
					break;
				
				case "navigation":
					errors.addError("val.inuse.modulemenu", systemEntity.getName());
					break;
				
				case C.MODULE:
					errors.addError("val.inuse.modulemodule", systemEntity.getName());
					break;
					
				case C.REPORT:
					errors.addError("val.inuse.modulereport", systemEntity.getName());
					break;
					
				case C.REST:
					errors.addError("val.inuse.modulerest", systemEntity.getName());
					break;
					
				case C.TASK:
					errors.addError("val.inuse.moduletask", systemEntity.getName());
					break;	
					
				case C.TRANSFER:
					errors.addError("val.inuse.moduletransfer", systemEntity.getName());
					break;
					
				case C.TRANSFORM:
					errors.addError("val.inuse.moduletransformer", systemEntity.getName());
					break;
				
				case C.USER:
					errors.addError("val.inuse.moduleusergroup", systemEntity.getName());
					break;
					
				default:
					unhandledEntity(systemEntity);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void validateParameters(Module module, ValidationErrors errors) {
		for (ModuleParameter parameter : module.getParameters()) {
			if (isEmpty(parameter.getName())) {
				errors.addEmptyField("label.paramname");
			}
			else if (!isNameUnique(parameter.getName(), module.getParameters())) {
				errors.addError("val.ambiguous.param", parameter.getName());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void validateNesteds(Module module, ValidationErrors errors) {
		for (NestedModule nested : module.getNesteds()) {
			if (isEmpty(nested.getNestedModule())) {
				errors.addEmptyField("label.submodule");
			}
			else if (!isUnique(nested.getNestedModule(), "nestedModule", module.getNesteds())) {
				errors.addError("val.ambiguous.nested", nested.getNestedModule().getName()); 
			}
		}
	}
	
	private void validateEntities(Module module, Module existingModule, ValidationErrors errors) {
		for (Entity entity : module.getEntities()) {
			final Entity existingEntity = entityService.findByName(entity.getName());
			if (existingEntity != null && !checkModule(existingEntity, existingModule)) {
				errors.addError("val.transfer.illegalentity", entity.getName(), module.getName());
			}
		}
	}
	
	private void validateFilters(Module module, Module existingModule, ValidationErrors errors) {
		for (Filter filter : module.getFilters()) {
			final Filter existingFilter = filterService.findByName(filter.getName());
			if (existingFilter != null && !checkModule(existingFilter, existingModule)) {
				errors.addError("val.transfer.illegalfilter", filter.getName(), module.getName());
			}
		}
	}
	
	private void validateTransformers(Module module, Module existingModule, ValidationErrors errors) {
		for (Transformer transformer : module.getTransformers()) {
			final Transformer existingTransformer = transformerService.findByName(transformer.getName());
			if (existingTransformer != null && !checkModule(existingTransformer, existingModule)) {
				errors.addError("val.transfer.illegaltransformer", transformer.getName(), module.getName());
			}
		}
	}
	
	private void validateTransfers(Module module, Module existingModule, ValidationErrors errors) {
		for (Transfer transfer : module.getTransfers()) {
			final Transfer existingTransfer = transferService.findByName(transfer.getName());
			if (existingTransfer != null && !checkModule(existingTransfer, existingModule)) {
				errors.addError("val.transfer.illegaltransfer", transfer.getName(), module.getName());
			}
		}
	}
	
	private void validateForms(Module module, Module existingModule, ValidationErrors errors) {
		for (Form form : module.getForms()) {
			final Form existingForm = formService.findByName(form.getName());
			if (existingForm != null && !checkModule(existingForm, existingModule)) {
				errors.addError("val.transfer.illegalform", form.getName(), module.getName());
			}
		}
	}
	
	private void validateMenus(Module module, Module existingModule, ValidationErrors errors) {
		for (Menu menu : module.getMenus()) {
			final Menu existingMenu = menuService.findByName(menu.getName());
			if (existingMenu != null && !checkModule(existingMenu, existingModule)) {
				errors.addError("val.transfer.illegalmenu", menu.getName(), module.getName());
			}
		}
	}
	
	private void validateTasks(Module module, Module existingModule, ValidationErrors errors) {
		for (Task task : module.getTasks()) {
			final Task existingTask = taskService.findByName(task.getName());
			if (existingTask != null && !checkModule(existingTask, existingModule)) {
				errors.addError("val.transfer.illegaltask", task.getName(), module.getName());
			}
		}
	}
	
	private void validateUserGroups(Module module, Module existingModule, ValidationErrors errors) {
		for (UserGroup group : module.getUserGroups()) {
			final UserGroup existingGroup = userGroupService.findByName(group.getName());
			if (existingGroup != null && !checkModule(existingGroup, existingModule)) {
				errors.addError("val.transfer.illegalusergroup", group.getName(), module.getName());
			}
		}
	}
	
	private List<ModuleDependent<? extends SystemEntity>> getModuleDependents() {
		if (moduleDependents == null) {
			moduleDependents = MiscUtils.castList(getBeans(ModuleDependent.class));
		}
		return moduleDependents;
	}
	
	private static boolean checkModule(ApplicationEntity entity, Module module) {
		return ObjectUtils.nullSafeEquals(entity.getModule(), module);
	}
	
}
