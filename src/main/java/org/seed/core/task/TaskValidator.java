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

import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationException;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class TaskValidator extends AbstractSystemEntityValidator<Task> {
	
	@SuppressWarnings("unchecked")
	@Override
	public void validateSave(Task task) throws ValidationException {
		Assert.notNull(task, "task is null");
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(task.getName())) {
			errors.add(new ValidationError("val.empty.field", "label.name"));
		}
		else if (!isNameLengthAllowed(task.getName())) {
			errors.add(new ValidationError("val.toolong.name", String.valueOf(getMaxNameLength())));
		}
		else if (!isNameAllowed(task.getInternalName())) {
			errors.add(new ValidationError("val.illegal.field", "label.name", task.getName()));
		}
		if (task.getCronExpression() != null) {
			if (task.getCronExpression().length() > getLimit("entity.stringfield.length")) {
				errors.add(new ValidationError("val.toolong.fieldvalue", "label.cronexpression", 
						   					   String.valueOf(getLimit("entity.stringfield.length"))));
			}
			else if (!CronExpression.isValidExpression(task.getCronExpression())) {
				errors.add(new ValidationError("val.illegal.cronexpression"));	
			}
		}
		if (task.isActive()) {
			if (isEmpty(task.getContent())) {
				errors.add(new ValidationError("val.empty.field", "label.sourcecode"));
			}
			if (!isEmpty(task.getCronExpression()) && 
					 (!isEmpty(task.getRepeatInterval()) ||
					  !isEmpty(task.getRepeatIntervalUnit()))) {
				errors.add(new ValidationError("val.ambiguous.tasktrigger"));
			}
			else if (isEmpty(task.getCronExpression())) {
				if (isEmpty(task.getRepeatInterval()) && !isEmpty(task.getRepeatIntervalUnit())) {
					errors.add(new ValidationError("val.empty.field", "label.interval"));
				}
				if (isEmpty(task.getRepeatIntervalUnit()) && !isEmpty(task.getRepeatInterval())) {
					errors.add(new ValidationError("val.empty.field", "label.intervalunit"));
				}
			}
		}
		if (task.hasParameters()) {
			for (TaskParameter parameter : task.getParameters()) {
				if (isEmpty(parameter.getName())) {
					errors.add(new ValidationError("val.empty.field", "label.paramname"));
				}
				else if (!isNameUnique(parameter.getName(), task.getParameters())) {
					errors.add(new ValidationError("val.ambiguous.param", parameter.getName()));
				}
			}
		}
		if (task.hasNotifications()) {
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
		validate(errors);
	}
	
}
