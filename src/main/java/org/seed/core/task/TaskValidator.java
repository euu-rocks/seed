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
package org.seed.core.task;

import org.quartz.CronExpression;

import org.seed.C;
import org.seed.core.codegen.CodeManager;
import org.seed.core.codegen.CodeUtils;
import org.seed.core.codegen.compile.CompilerException;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.ValidationErrors;
import org.seed.core.data.ValidationException;
import org.seed.core.task.codegen.TaskCodeProvider;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskValidator extends AbstractSystemEntityValidator<Task> {
	
	@Autowired
	private CodeManager codeManager;
	
	@Autowired
	private TaskCodeProvider taskCodeProvider;
	
	@Override
	public void validateSave(Task task) throws ValidationException {
		Assert.notNull(task, C.TASK);
		final var errors = createValidationErrors(task);
		
		validateName(task, errors);
		
		if (!isEmpty(task.getCronExpression())) {
			if (task.getCronExpression().length() > getMaxStringLength()) {
				errors.addOverlongField("label.cronexpression", getMaxStringLength());
			}
			else if (!CronExpression.isValidExpression(task.getCronExpression())) {
				errors.addError("val.illegal.cronexpression");	
			}
		}
		if (task.isActive()) {
			if (isEmpty(task.getContent())) {
				errors.addEmptyField("label.sourcecode");
			}
			else {
				final String className = CodeUtils.extractClassName(
						CodeUtils.extractQualifiedName(task.getContent()));
				if (!task.getGeneratedClass().equals(className)) {
					errors.addError("val.illegal.jobclassname", task.getName(), task.getGeneratedClass());
				}
				else {
					validateCode(task, errors);
				}
			}
			validateTrigger(task, errors);
		}
		if (task.hasParameters()) {
			validateParameters(task, errors);
		}
		if (task.hasNotifications()) {
			validateNotifications(task, errors);
		}
		validate(errors);
	}
	
	private void validateName(Task task, ValidationErrors errors) {
		if (isEmpty(task.getName())) {
			errors.addEmptyName();
		}
		else if (!isNameLengthAllowed(task.getName())) {
			errors.addOverlongName(getMaxNameLength());
		}
		else if (!isNameAllowed(task.getInternalName())) {
			errors.addIllegalName(task.getName());
		}
	}
	
	// check whether a cron expression or interval properties exist but not both
	private void validateTrigger(Task task, ValidationErrors errors) {
		if (isEmpty(task.getCronExpression())) {
			if (isEmpty(task.getRepeatInterval()) && !isEmpty(task.getRepeatIntervalUnit())) {
				errors.addEmptyField("label.interval");
			}
			if (isEmpty(task.getRepeatIntervalUnit()) && !isEmpty(task.getRepeatInterval())) {
				errors.addEmptyField("label.intervalunit");
			}
		}
		else if (!isEmpty(task.getRepeatInterval()) ||
				 !isEmpty(task.getRepeatIntervalUnit())) {
			errors.addError("val.ambiguous.tasktrigger");
		}
	}
	
	@SuppressWarnings("unchecked")
	private void validateParameters(Task task, ValidationErrors errors) {
		for (TaskParameter parameter : task.getParameters()) {
			if (isEmpty(parameter.getName())) {
				errors.addEmptyField("label.paramname");
			}
			else if (!isNameUnique(parameter.getName(), task.getParameters())) {
				errors.addError("val.ambiguous.param", parameter.getName());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void validateNotifications(Task task, ValidationErrors errors) {
		for (TaskNotification notification : task.getNotifications()) {
			if (isEmpty(notification.getUser())) {
				errors.addError("val.empty.notificationfield", "label.user");
			}
			else if (!isUnique(notification.getUser(), C.USER, task.getNotifications())) {
				errors.addError("val.ambiguous.notificationuser", notification.getUser().getName());
			}
			if (isEmpty(notification.getResult())) {
				errors.addError("val.empty.notificationfield", "label.result");
			}
		}
	}
	
	private void validateCode(Task task, ValidationErrors errors) {
		try {
			codeManager.testCompile(taskCodeProvider.getTaskSource(task));
		}
		catch (CompilerException cex) {
			errors.addError("val.illegal.sourcecode");
		}
	}
	
}
