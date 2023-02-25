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

import static org.seed.core.util.CollectionUtils.*;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.C;
import org.seed.LabelProvider;
import org.seed.core.api.Job;
import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.codegen.CodeChangeAware;
import org.seed.core.codegen.CodeManager;
import org.seed.core.codegen.CodeManagerImpl;
import org.seed.core.codegen.CodeUtils;
import org.seed.core.codegen.SourceCode;
import org.seed.core.data.Options;
import org.seed.core.data.ValidationException;
import org.seed.core.mail.MailBuilder;
import org.seed.core.mail.MailService;
import org.seed.core.task.job.AbstractSystemJob;
import org.seed.core.user.User;
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserGroupDependent;
import org.seed.core.user.UserGroupService;
import org.seed.core.util.Assert;
import org.seed.core.util.BeanUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class TaskServiceImpl extends AbstractApplicationEntityService<Task> 
	implements TaskService, UserGroupDependent<Task>, CodeChangeAware {
	
	@Autowired
	private TaskRepository taskRepository;
	
	@Autowired
	private TaskValidator taskValidator;
	
	@Autowired
	private CodeManager codeManager;
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private LabelProvider labelProvider;
	
	@Autowired
	private UserGroupService userGroupService;
	
	@Override
	public Task createInstance(@Nullable Options options) {
		final TaskMetadata instance = (TaskMetadata) super.createInstance(options);
		instance.createLists();
		instance.setActive(true);
		return instance;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends AbstractSystemJob> Class<T> getSystemJobClass(SystemTask systemTask) {
		Assert.notNull(systemTask, "system task");
		
		return (Class<T>) firstMatch(BeanUtils.getImplementingClasses(AbstractSystemJob.class), 
									 jobClass -> systemTask == BeanUtils.instantiate(jobClass).getSytemTask());
	}
	
	@Override
	public Task getTask(Job job) {
		try (Session session = taskRepository.getSession()) {
			return getTask(session, job);
		}
	}
	
	@Override
	public Task getTask(Session session, Job job) {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(job, "job");
		
		final String jobName = job.getClass().getSimpleName();
		return firstMatch(getObjects(session), task -> jobName.equalsIgnoreCase(task.getInternalName()));
	}
	
	@Override
	public List<Task> getTasks(User user, Session session) {
		Assert.notNull(user, C.USER);
		
		return subList(getObjects(session), task -> task.checkPermissions(user));
	}
	
	@Override
	public TaskRun createRun(Task task) {
		Assert.notNull(task, C.TASK);
		
		final TaskRun run = new TaskRun();
		run.setStartTime(new Date());
		task.addRun(run);
		return run;
	}
	
	@Override
	public SystemTaskRun createRun(SystemTask systemTask) {
		Assert.notNull(systemTask, "system task");
		
		final SystemTaskRun run = new SystemTaskRun();
		run.setSystemTask(systemTask);
		run.setStartTime(new Date());
		return run;
	}
	
	@Override
	@Secured("ROLE_ADMIN_JOB")
	public TaskParameter createParameter(Task task) {
		Assert.notNull(task, C.TASK);
		
		final TaskParameter param = new TaskParameter();
		task.addParameter(param);
		return param;
	}
	
	@Override
	@Secured("ROLE_ADMIN_JOB")
	public TaskNotification createNotification(Task task) {
		Assert.notNull(task, C.TASK);
		
		final TaskNotification notification = new TaskNotification();
		notification.setResult(TaskResult.SUCCESS);
		task.addNotification(notification);
		return notification;
	}
	
	@Override
	@Secured("ROLE_SYSTEMTASK")
	public SystemTaskRun getLastSystemTaskRun(SystemTask systemTask, Session session) {
		return taskRepository.getLastSystemTaskRun(systemTask, session);
	}
	
	@Override
	@Secured("ROLE_SYSTEMTASK")
	public List<SystemTaskRun> getSystemTaskRuns(SystemTask systemTask, Session session) {
		return taskRepository.getSystemTaskRuns(systemTask, session);
	}
	
	@Override
	public List<Task> findUsage(UserGroup userGroup, Session session) {
		Assert.notNull(userGroup, C.USERGROUP);
		Assert.notNull(session, C.SESSION);
		
		return subList(getObjects(session), 
					   task -> anyMatch(task.getPermissions(), 
							   			perm -> userGroup.equals(perm.getUserGroup())));
	}
	
	@Override
	public List<TaskPermission> getAvailablePermissions(Task task, Session session) {
		Assert.notNull(task, C.TASK);
		
		return filterAndConvert(userGroupService.findNonSystemGroups(session), 
								group -> !task.containsPermission(group), 
								group -> createPermission(task, group));
	}
	
	@Override
	protected void analyzeNextVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
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
	}
	
	@Override
	protected void analyzeCurrentVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		filterAndForEach(currentVersionModule.getTasks(), 
						 task -> analysis.getModule().getTaskByUid(task.getUid()) == null, 
						 analysis::addChangeDelete);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends ApplicationEntityService<ApplicationEntity>>[] getImportDependencies() {
		return new Class[] { UserGroupService.class };
	}
	
	@Override
	public void importObjects(TransferContext context, Session session) throws ValidationException {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(session, C.SESSION);
		
		if (context.getModule().getTasks() != null) {
			for (Task task : context.getModule().getTasks()) {
				final Task currentVersionTask = findByUid(session, task.getUid());
				((TaskMetadata) task).setModule(context.getModule());
				if (currentVersionTask != null) {
					((TaskMetadata) currentVersionTask).copySystemFieldsTo(task);
					session.detach(currentVersionTask);
				}
				initTask(task, currentVersionTask, session);
				getRepository().save(task, session);
			}
		}
	}
	
	private void initTask(Task task, Task currentVersionTask, Session session) {
		if (task.hasParameters()) {
			for (TaskParameter parameter : task.getParameters()) {
				initTaskParameter(parameter, task, currentVersionTask);
			}
		}
		if (task.hasPermissions()) {
			for (TaskPermission permission : task.getPermissions()) {
				initTaskPermission(permission, task, currentVersionTask, session);
			}
		}
	}
	
	private void initTaskParameter(TaskParameter parameter, Task task, Task currentVersionTask) {
		parameter.setTask(task);
		final TaskParameter currentVersionParameter =
			currentVersionTask != null 
				? currentVersionTask.getParameterByUid(parameter.getUid()) 
				: null;
		if (currentVersionParameter != null) {
			currentVersionParameter.copySystemFieldsTo(parameter);
		}
	}
	
	private void initTaskPermission(TaskPermission permission, Task task, Task currentVersionTask, Session session) {
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
	
	@Override
	public void deleteObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, C.MODULE);
		Assert.notNull(currentVersionModule, "currentVersionModule");
		Assert.notNull(session, C.SESSION);
		
		if (currentVersionModule.getTasks() != null) {
			for (Task task : currentVersionModule.getTasks()) {
				if (module.getTaskByUid(task.getUid()) == null) {
					session.delete(task);
					removeTaskClass(task);
				}
			}
		}
	}
	
	@Override
	@Secured("ROLE_ADMIN_JOB")
	public void saveObject(Task task) throws ValidationException {
		Assert.notNull(task, C.TASK);
		
		final boolean isNew = task.isNew();
		final boolean contentChanged = ((TaskMetadata) task).isContentChanged();
		final Task currentVersionTask = !isNew ? getObject(task.getId()) : null;
		super.saveObject(task);
		
		if (!isNew && !task.getInternalName().equals(currentVersionTask.getInternalName())) {
			removeTaskClass(currentVersionTask);
		}
		if (isNew || contentChanged) {
			updateConfiguration();
		}
	}
	
	@Override
	public void saveTaskDirectly(Task task, Session session) {
		saveObjectDirectly(task, session);
	}
	
	@Override
	public void saveSystemTaskRun(SystemTaskRun run) {
		Assert.notNull(run, "run");
		
		try (Session session = taskRepository.getSession()) {
			Transaction tx = session.beginTransaction();
			session.saveOrUpdate(run);
			tx.commit();
		}
	}
	
	@Override
	@Secured("ROLE_ADMIN_JOB")
	public void deleteObject(Task task) throws ValidationException {
		super.deleteObject(task);
		removeTaskClass(task);
	}
	
	@Override
	public void removeNotifications(User user, Session session) {
		Assert.notNull(user, C.USER);
		Assert.notNull(session, C.SESSION);
		
		filterAndForEach(getObjects(session), 
						 task -> task.removeNotifications(user), 
						 session::saveOrUpdate);
	}
	
	@Override
	public boolean processCodeChange(SourceCode sourceCode, Session session) {
		Assert.notNull(sourceCode, "sourceCode");
		Assert.notNull(session, C.SESSION);
		
		if (sourceCode.getPackageName().equals(CodeManagerImpl.GENERATED_TASK_PACKAGE)) {
			for (Task task : getObjects(session)) {
				if (task.getInternalName().equalsIgnoreCase(sourceCode.getClassName())) {
					if (!task.getContent().equals(sourceCode.getContent())) {
						task.setContent(sourceCode.getContent());
						taskRepository.save(task, session);
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
		Assert.notNull(task, C.TASK);
		Assert.notNull(run, "run");
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
	
	private void removeTaskClass(Task task) {
		codeManager.removeClass(CodeUtils.getQualifiedName(task));
	}
	
	private String getRunLogText(TaskRun run) {
		final StringBuilder buf = new StringBuilder();
		for (TaskRunLog log : run.getLogs()) {
			buf.append(labelProvider.formatDate(log.getCreatedOn())).append(' ')
			   .append(log.getLevel()).append(' ').append(log.getContent()).append("\n");
		}
		return buf.toString();
	}
	
	private static TaskPermission createPermission(Task task, UserGroup group) {
		final TaskPermission permission = new TaskPermission();
		permission.setTask(task);
		permission.setUserGroup(group);
		return permission;
	}
	
}
