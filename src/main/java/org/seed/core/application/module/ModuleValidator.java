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

import java.util.Set;

import org.seed.core.application.ApplicationEntity;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.ValidationError;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

@Component
public class ModuleValidator extends AbstractSystemEntityValidator<Module> {
	
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
	
	public void validateImport(Module module, Module existingModule) throws ValidationException {
		Assert.notNull(module, "module is null");
		final Set<ValidationError> errors = createErrorList();
		// entities
		if (module.getEntities() != null) {
			for (Entity entity : module.getEntities()) {
				final Entity existingEntity = entityService.findByName(entity.getName());
				if (existingEntity != null && !checkModule(existingEntity, existingModule)) {
					errors.add(new ValidationError("val.transfer.illegalentity", entity.getName(), module.getName()));
				}
			}
		}
		// filters
		if (module.getFilters() != null) {
			for (Filter filter : module.getFilters()) {
				final Filter existingFilter = filterService.findByName(filter.getName());
				if (existingFilter != null && !checkModule(existingFilter, existingModule)) {
					errors.add(new ValidationError("val.transfer.illegalfilter", filter.getName(), module.getName()));
				}
			}
		}
		// transformers
		if (module.getTransformers() != null) {
			for (Transformer transformer : module.getTransformers()) {
				final Transformer existingTransformer = transformerService.findByName(transformer.getName());
				if (existingTransformer != null && !checkModule(existingTransformer, existingModule)) {
					errors.add(new ValidationError("val.transfer.illegaltransformer", transformer.getName(), module.getName()));
				}
			}
		}
		// transfers
		if (module.getTransfers() != null) {
			for (Transfer transfer : module.getTransfers()) {
				final Transfer existingTransfer = transferService.findByName(transfer.getName());
				if (existingTransfer != null && !checkModule(existingTransfer, existingModule)) {
					errors.add(new ValidationError("val.transfer.illegaltransfer", transfer.getName(), module.getName()));
				}
			}
		}
		// forms
		if (module.getForms() != null) {
			for (Form form : module.getForms()) {
				final Form existingForm = formService.findByName(form.getName());
				if (existingForm != null && !checkModule(existingForm, existingModule)) {
					errors.add(new ValidationError("val.transfer.illegalform", form.getName(), module.getName()));
				}
			}
		}
		// menus
		if (module.getMenus() != null) {
			for (Menu menu : module.getMenus()) {
				final Menu existingMenu = menuService.findByName(menu.getName());
				if (existingMenu != null && !checkModule(existingMenu, existingModule)) {
					errors.add(new ValidationError("val.transfer.illegalmenu", menu.getName(), module.getName()));
				}
			}
		}
		// tasks
		if (module.getTasks() != null) {
			for (Task task : module.getTasks()) {
				final Task existingTask = taskService.findByName(task.getName());
				if (existingTask != null && !checkModule(existingTask, existingModule)) {
					errors.add(new ValidationError("val.transfer.illegaltask", task.getName(), module.getName()));
				}
			}
		}
		// user groups
		if (module.getUserGroups() != null) {
			for (UserGroup group : module.getUserGroups()) {
				final UserGroup existingGroup = userGroupService.findByName(group.getName());
				if (existingGroup != null && !checkModule(existingGroup, existingModule)) {
					errors.add(new ValidationError("val.transfer.illegalusergroup", group.getName(), module.getName()));
				}
			}
		}
		validate(errors);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void validateSave(Module module) throws ValidationException {
		Assert.notNull(module, "module is null");
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(module.getName())) {
			errors.add(new ValidationError("val.empty.field", "label.name"));
		}
		else if (!isNameLengthAllowed(module.getName())) {
			errors.add(new ValidationError("val.toolong.name", String.valueOf(getMaxNameLength())));
		}
		
		if (module.hasParameters()) {
			for (ModuleParameter parameter : module.getParameters()) {
				if (isEmpty(parameter.getName())) {
					errors.add(new ValidationError("val.empty.field", "label.paramname"));
				}
				else if (!isNameUnique(parameter.getName(), module.getParameters())) {
					errors.add(new ValidationError("val.ambiguous.param", parameter.getName()));
				}
			}
		}
		
		validate(errors);
	}
	
	private static boolean checkModule(ApplicationEntity entity, Module module) {
		return ObjectUtils.nullSafeEquals(entity.getModule(), module);
	}
	
}
