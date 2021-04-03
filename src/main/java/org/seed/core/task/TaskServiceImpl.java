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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.hibernate.Session;

import org.seed.core.api.Job;
import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.codegen.CodeChangeAware;
import org.seed.core.codegen.SourceCode;
import org.seed.core.config.UpdatableConfiguration;
import org.seed.core.data.Options;
import org.seed.core.data.ValidationException;
import org.seed.core.form.LabelProvider;
import org.seed.core.mail.MailBuilder;
import org.seed.core.mail.MailService;
import org.seed.core.user.User;
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserGroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class TaskServiceImpl extends AbstractApplicationEntityService<Task> 
	implements TaskService, CodeChangeAware {
	
	@Autowired
	private TaskRepository taskRepository;
	
	@Autowired
	private TaskValidator taskValidator;
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private LabelProvider labelProvider;
	
	@Autowired
	private UserGroupService userGroupService;
	
	@Autowired
	private UpdatableConfiguration configuration;
	
	@Override
	public Task createInstance(@Nullable Options options) {
		final TaskMetadata instance = (TaskMetadata) super.createInstance(options);
		instance.createLists();
		instance.setActive(true);
		return instance;
	}
	
	@Override
	public Task getTask(Job job) {
		Assert.notNull(job, "job is null");
		
		final String jobName = job.getClass().getSimpleName();
		for (Task task : findAllObjects()) {
			if (jobName.equalsIgnoreCase(task.getInternalName())) {
				return task;
			}
		}
		return null;
	}
	
	@Override
	public List<Task> getTasks(User user) {
		Assert.notNull(user, "user is null");
		
		final List<Task> result = new ArrayList<>();
		for (Task task : findAllObjects()) {
			if (task.checkPermissions(user, null)) {
				result.add(task);
			}
		}
		return result;
	}
	
	@Override
	@Secured("ROLE_ADMIN_JOB")
	public TaskParameter createParameter(Task task) {
		Assert.notNull(task, "task is null");
		
		final TaskParameter param = new TaskParameter();
		task.addParameter(param);
		return param;
	}
	
	@Override
	@Secured("ROLE_ADMIN_JOB")
	public TaskNotification createNotification(Task task) {
		Assert.notNull(task, "task is null");
		
		final TaskNotification notification = new TaskNotification();
		notification.setResult(TaskResult.SUCCESS);
		task.addNotification(notification);
		return notification;
	}
	
	@Override
	public List<TaskPermission> getAvailablePermissions(Task task) {
		Assert.notNull(task, "task is null");
		
		final List<TaskPermission> result = new ArrayList<>();
		for (UserGroup group : userGroupService.findAllObjects()) {
			boolean found = false;
			if (task.hasPermissions()) {
				for (TaskPermission permission : task.getPermissions()) {
					if (permission.getUserGroup().equals(group)) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				final TaskPermission permission = new TaskPermission();
				permission.setTask(task);
				permission.setUserGroup(group);
				result.add(permission);
			}
		}
		return result;
	}
	
	@Override
	public void analyzeObjects(ImportAnalysis analysis, Module currentVersionModule) {
		Assert.notNull(analysis, "analysis is null");
		
		if (analysis.getModule().getTasks() != null) {
			for (Task task : analysis.getModule().getTasks()) {
				if (currentVersionModule == null) {
					analysis.addChangeNew(task);
				}
				else {
					final Task currentVersionTask = 
						currentVersionModule.getTaskByUid(task.getUid());
					if (currentVersionTask == null) {
						analysis.addChangeNew(task);
					}
					else if (!task.isEqual(currentVersionTask)) {
						analysis.addChangeModify(task);
					}
				}
			}
		}
		if (currentVersionModule != null && currentVersionModule.getTasks() != null) {
			for (Task currentVersionTask : currentVersionModule.getTasks()) {
				if (analysis.getModule().getTaskByUid(currentVersionTask.getUid()) == null) {
					analysis.addChangeDelete(currentVersionTask);
				}
			}
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends ApplicationEntityService<? extends ApplicationEntity>>[] getImportDependencies() {
		return (Class<? extends ApplicationEntityService<? extends ApplicationEntity>>[]) 
				new Class[] { UserGroupService.class };
	}
	
	@Override
	public void importObjects(TransferContext context, Session session) {
		Assert.notNull(context, "context is null");
		Assert.notNull(session, "session is null");
		try {
			if (context.getModule().getTasks() != null) {
				for (Task task : context.getModule().getTasks()) {
					final Task currentVersionTask = findByUid(session, task.getUid());
					((TaskMetadata) task).setModule(context.getModule());
					if (currentVersionTask != null) {
						((TaskMetadata) currentVersionTask).copySystemFieldsTo(task);
						session.detach(currentVersionTask);
					}
					if (task.hasParameters()) {
						for (TaskParameter parameter : task.getParameters()) {
							parameter.setTask(task);
							final TaskParameter currentVersionParameter =
								currentVersionTask != null 
									? currentVersionTask.getParameterByUid(parameter.getUid()) 
									: null;
							if (currentVersionParameter != null) {
								currentVersionParameter.copySystemFieldsTo(parameter);
							}
						}
					}
					if (task.hasPermissions()) {
						for (TaskPermission permission : task.getPermissions()) {
							permission.setTask(task);
							permission.setUserGroup(userGroupService.findByUid(session, permission.getUserGroupUid()));
							final TaskPermission currentVersionPermission =
								currentVersionTask != null
									? currentVersionTask.getPermissionByUid(permission.getUid())
									: null;
							if (currentVersionPermission != null) {
								currentVersionPermission.copySystemFieldsTo(permission);
							}
						}
					}
					saveObject(task, session);
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
		
		if (currentVersionModule.getTasks() != null) {
			for (Task currentVersionTask : currentVersionModule.getTasks()) {
				if (module.getTaskByUid(currentVersionTask.getUid()) == null) {
					session.delete(currentVersionTask);
				}
			}
		}
	}
	
	@Override
	@Secured("ROLE_ADMIN_JOB")
	public void saveObject(Task task) throws ValidationException {
		Assert.notNull(task, "task is null");
		
		final boolean isNew = task.isNew();
		final boolean contentChanged = ((TaskMetadata) task).isContentChanged();
		super.saveObject(task);
		
		if (isNew || contentChanged) {
			configuration.updateConfiguration();
		}
	}
	
	@Override
	public void saveTaskDirectly(Task task) {
		getRepository().save(task);
	}
	
	@Override
	@Secured("ROLE_ADMIN_JOB")
	public void deleteObject(Task task) throws ValidationException {
		super.deleteObject(task);
	}
	
	@Override
	public boolean processCodeChange(SourceCode<?> sourceCode, Session session) {
		Assert.notNull(sourceCode, "sourceCode is null");
		Assert.notNull(session, "session is null");
		
		if (sourceCode.getPackageName().equals(Task.PACKAGE_NAME)) {
			for (Task task : findAllObjects()) {
				if (task.getInternalName().equalsIgnoreCase(sourceCode.getClassName())) {
					if (!task.getContent().equals(sourceCode.getContent())) {
						task.setContent(sourceCode.getContent());
						session.saveOrUpdate(task);
						return true;
					}
					break;
				}
			}
		}
		return false;
	}
	
	@Override
	public void sendNotifications(Task task, TaskRun run) {
		Assert.notNull(task, "task is null");
		Assert.notNull(run, "run is null");
		Assert.state(task.hasNotifications(), "task has no notifications");
		Assert.state(run.getResult() != null, "run has no result");
		
		if (!mailService.isMailingEnabled()) {
			return;
		}
		final TaskResult result = run.getResult();
		final MailBuilder mailBuilder = mailService.getMailBuilder()
			.setSubject(labelProvider.getLabel("label.jobrun") + ": " + task.getName() + ' ' + 
					labelProvider.formatDate(run.getStartTime()) + " - " + 
					labelProvider.formatDate(run.getEndTime()) + ' ' +
					labelProvider.getEnumLabel(result))
			.setText(getRunLogText(run));
		
		for (TaskNotification notification : task.getNotifications()) {
			if (result.ordinal() >= notification.getResult().ordinal()) {
				mailService.sendMail(mailBuilder.setToAddress(notification.getUser().getEmail())
												.build());
			}
		}
	}
	
	@Override
	protected TaskRepository getRepository() {
		return taskRepository;
	}

	@Override
	protected TaskValidator getValidator() {
		return taskValidator;
	}
	
	private String getRunLogText(TaskRun run) {
		final StringBuilder buf = new StringBuilder();
		for (TaskRunLog log : run.getLogs()) {
			buf.append(labelProvider.formatDate(log.getCreatedOn())).append(' ')
			   .append(log.getLevel()).append(' ').append(log.getContent()).append("\n");
		}
		return buf.toString();
	}

}
