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

import java.util.Set;

import org.quartz.CronExpression;
import org.seed.C;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;

import org.springframework.stereotype.Component;

@Component
public class TaskValidator extends AbstractSystemEntityValidator<Task> {
	
	@Override
	public void validateSave(Task task) throws ValidationException {
		Assert.notNull(task, C.TASK);
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(task.getName())) {
			errors.add(ValidationError.emptyName());
		}
		else if (!isNameLengthAllowed(task.getName())) {
			errors.add(ValidationError.overlongName(getMaxNameLength()));
		}
		else if (!isNameAllowed(task.getInternalName())) {
			errors.add(ValidationError.illegalField("label.name", task.getName()));
		}
		if (!isEmpty(task.getCronExpression())) {
			if (task.getCronExpression().length() > getMaxStringLength()) {
				errors.add(ValidationError.overlongField("label.cronexpression", getMaxStringLength()));
			}
			else if (!CronExpression.isValidExpression(task.getCronExpression())) {
				errors.add(new ValidationError("val.illegal.cronexpression"));	
			}
		}
		if (task.isActive()) {
			if (isEmpty(task.getContent())) {
				errors.add(ValidationError.emptyField("label.sourcecode"));
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
	
	// check whether a cron expression or interval properties exist but not both
	private void validateTrigger(Task task, Set<ValidationError> errors) {
		if (isEmpty(task.getCronExpression())) {
			if (isEmpty(task.getRepeatInterval()) && !isEmpty(task.getRepeatIntervalUnit())) {
				errors.add(ValidationError.emptyField("label.interval"));
			}
			if (isEmpty(task.getRepeatIntervalUnit()) && !isEmpty(task.getRepeatInterval())) {
				errors.add(ValidationError.emptyField("label.intervalunit"));
			}
		}
		else if (!isEmpty(task.getRepeatInterval()) ||
				 !isEmpty(task.getRepeatIntervalUnit())) {
			errors.add(new ValidationError("val.ambiguous.tasktrigger"));
		}
	}
	
	@SuppressWarnings("unchecked")
	private void validateParameters(Task task, Set<ValidationError> errors) {
		for (TaskParameter parameter : task.getParameters()) {
			if (isEmpty(parameter.getName())) {
				errors.add(ValidationError.emptyField("label.paramname"));
			}
			else if (!isNameUnique(parameter.getName(), task.getParameters())) {
				errors.add(new ValidationError("val.ambiguous.param", parameter.getName()));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void validateNotifications(Task task, Set<ValidationError> errors) {
		for (TaskNotification notification : task.getNotifications()) {
			if (isEmpty(notification.getUser())) {
				errors.add(new ValidationError("val.empty.notificationfield", "label.user"));
			}
			else if (!isUnique(notification.getUser(), "user", task.getNotifications())) {
				errors.add(new ValidationError("val.ambiguous.notificationuser", 
											   notification.getUser().getName()));
			}
			if (isEmpty(notification.getResult())) {
				errors.add(new ValidationError("val.empty.notificationfield", "label.result"));
			}
		}
	}
	
}
