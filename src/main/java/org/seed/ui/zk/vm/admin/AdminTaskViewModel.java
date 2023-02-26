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
package org.seed.ui.zk.vm.admin;

import java.util.List;
import java.util.Objects;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import org.seed.C;
import org.seed.core.api.Job;
import org.seed.core.application.ContentObject;
import org.seed.core.codegen.SourceCode;
import org.seed.core.data.SystemObject;
import org.seed.core.task.IntervalUnit;
import org.seed.core.task.Task;
import org.seed.core.task.TaskMetadata;
import org.seed.core.task.TaskNotification;
import org.seed.core.task.TaskParameter;
import org.seed.core.task.TaskPermission;
import org.seed.core.task.TaskResult;
import org.seed.core.task.TaskRun;
import org.seed.core.task.TaskService;
import org.seed.core.task.codegen.TaskCodeProvider;
import org.seed.core.task.job.AbstractJob;
import org.seed.core.task.job.JobScheduler;
import org.seed.core.user.Authorisation;
import org.seed.core.user.User;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;
import org.seed.core.util.NameUtils;

import org.springframework.util.ObjectUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Destroy;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.WireVariable;

public class AdminTaskViewModel extends AbstractAdminViewModel<Task>
	implements JobListener {
	
	private static final String PARAMETERS = "parameters";
	private static final String PERMISSIONS = "permissions";
	private static final String NOTIFICATIONS = "notifications";
	
	@WireVariable(value="taskServiceImpl")
	private TaskService taskService;
	
	@WireVariable(value="taskCodeProvider")
	private TaskCodeProvider taskCodeProvider;
	
	@WireVariable(value="defaultJobScheduler")
	private JobScheduler jobScheduler;
	
	private String listenerName;
	
	private TaskParameter parameter;
	
	private TaskPermission permission;
	
	private TaskNotification notification;
	
	private TaskRun run;
	
	// JobListener changes
	private boolean jobStatusChanged;
	
	// initial task values
	private String initialContent;
	private boolean initialActive;
	private int initialInterval;
	
	public AdminTaskViewModel() {
		super(Authorisation.ADMIN_JOB, C.TASK,
			  "/admin/task/tasklist.zul", 
			  "/admin/task/task.zul");
	}
	
	public TaskParameter getParameter() {
		return parameter;
	}

	public void setParameter(TaskParameter parameter) {
		this.parameter = parameter;
	}

	public TaskPermission getPermission() {
		return permission;
	}

	public void setPermission(TaskPermission permission) {
		this.permission = permission;
	}

	public TaskNotification getNotification() {
		return notification;
	}

	public void setNotification(TaskNotification notification) {
		this.notification = notification;
	}

	public TaskRun getRun() {
		return run;
	}

	public void setRun(TaskRun run) {
		this.run = run;
	}
	
	public boolean isRunning(Task task) {
		return jobScheduler.isRunning(task);
	}

	public TaskResult[] getTaskResults() {
		return TaskResult.values();
	}
	
	public IntervalUnit[] getIntervalUnits() {
		return IntervalUnit.values();
	}
	
	public List<User> getUsers() {
		return userService.getObjects(currentSession());
	}
	
	@Init
	public void init(@ContextParam(ContextType.VIEW) Component view,
					 @ExecutionArgParam(C.PARAM) Object object) {
		super.init(object, view);
		jobScheduler.addJobListener(this);
	}
	
	@Override
	protected void initObject(Task task) {
		super.initObject(task);
		initialContent = task.getContent();
		initialActive = task.isActive();
		initialInterval = Objects.hash(task.getStartTime(),
									   task.getRepeatInterval(),
									   task.getRepeatIntervalUnit(),
									   task.getCronExpression());
	}
	
	@Override
	protected void initFilters() {
		getFilter(FILTERGROUP_LIST, "active").setBooleanFilter(true);
	}
	
	@Command
	@SmartNotifyChange(C.PERMISSION)
	public void insertToPermissionList(@BindingParam(C.BASE) TaskPermission base,
									   @BindingParam(C.ITEM) TaskPermission item,
									   @BindingParam(C.LIST) int listNum) {
		insertToList(PERMISSIONS, listNum, base, item);
		if (listNum == LIST_AVAILABLE && item == permission) {
			this.permission = null;
		}
	}
	
	@Command
	@SmartNotifyChange(C.PERMISSION)
	public void dropToPermissionList(@BindingParam(C.ITEM) TaskPermission item,
									 @BindingParam(C.LIST) int listNum) {
		dropToList(PERMISSIONS, listNum, item);
		if (listNum == LIST_AVAILABLE && item == permission) {
			this.permission = null;
		}
	}
	
	@Destroy
	public void destroy() {
		jobScheduler.removeJobListener(this);
	}
	
	@Command
	public void startTask() {
		jobScheduler.startJob(getObject());
	}
	
	@Command
	public void checkJobStatus() {
		if (jobStatusChanged) {
			switch (getViewMode()) {
				case LIST:
					refreshList();
					break;
					
				case DETAIL:
					refreshObject();
					break;
				
				default:
					throw new UnsupportedOperationException(getViewMode().name());
			}
			jobStatusChanged = false;
		}
	}
	
	@Command
	public void back() {
		cmdBack();
	}
	
	@Command
	public void newTask() {
		cmdNewObject();
	}
	
	@Command
	@NotifyChange(C.PARAMETER)
	public void newParameter() {
		parameter = taskService.createParameter(getObject());
		notifyObjectChange(PARAMETERS);
		flagDirty();
	}
	
	@Command
	@NotifyChange(C.PARAMETER)
	public void removeParameter() {
		getObject().removeParameter(parameter);
		parameter = null;
		notifyObjectChange(PARAMETERS);
		flagDirty();
	}
	
	@Command
	@NotifyChange("notification")
	public void newNotification() {
		notification = taskService.createNotification(getObject());
		notifyObjectChange(NOTIFICATIONS);
		flagDirty();
	}
	
	@Command
	@NotifyChange("notification")
	public void removeNotification() {
		getObject().removeNotification(notification);
		notification = null;
		notifyObjectChange(NOTIFICATIONS);
		flagDirty();
	}
 	
	@Command
	public void editTask() {
		cmdEditObject();
	}
	
	@Command
	public void editCode() {
		if (getObject().getContent() == null) {
			getObject().setContent(taskCodeProvider.getTaskTemplate(getObject()));
		}
		showCodeDialog(new CodeDialogParameter(this, getObject()));
	}
	
	@Command
	public void refreshTask(@BindingParam(C.ELEM) Component component) {
		cmdRefresh();
	}
	
	@Command
	public void deleteTask(@BindingParam(C.ELEM) Component component) {
		cmdDeleteObject(component);
	}
	
	@Override
	protected void afterObjectDeleted(Task task) {
		resetCurrentSession();
	}
	
	@Command
	public void saveTask(@BindingParam(C.ELEM) Component elem) {
		// detect content change
		final boolean contentChanged = !ObjectUtils.nullSafeEquals(initialContent, 
																   getObject().getContent());
		((TaskMetadata) getObject()).setContentChanged(contentChanged);
		adjustLists(getObject().getPermissions(), getListManagerList(PERMISSIONS, LIST_SELECTED));
		
		if (!cmdSaveObject(elem)) {
			return;
		}
		resetCurrentSession();
		refreshMenu();
		
		// configuration update takes place anyway
		if (contentChanged) {
			return; 
		}
		
		// at this point only metadata may have changed
		final boolean reschedule = initialInterval != Objects.hash(
														getObject().getStartTime(),
														getObject().getRepeatInterval(),
														getObject().getRepeatIntervalUnit(),
														getObject().getCronExpression());
		// deactivate
		if (reschedule || (initialActive && !getObject().isActive())) {
			jobScheduler.unscheduleTask(getObject());
		}
		// activate
		if (reschedule || (!initialActive && getObject().isActive())) {
			jobScheduler.scheduleTask(getObject());
		}
		
		refreshObject();
	}
	
	@Command
	public void flagDirty(@BindingParam("notify") String notify, 
						  @BindingParam("notifyObject") String notifyObject) {
		super.flagDirty(notify, null, notifyObject);
	}
	
	@GlobalCommand
	public void globalRefreshObject(@BindingParam(C.PARAM) Long objectId) {
		refreshObject(objectId);
	}
	
	@Override
	protected TaskService getObjectService() {
		return taskService;
	}

	@Override
	protected List<SystemObject> getListManagerSource(String key, int listNum) {
		if (PERMISSIONS.equals(key)) {
			return MiscUtils.castList(listNum == LIST_AVAILABLE 
					? taskService.getAvailablePermissions(getObject(), currentSession()) 
					: getObject().getPermissions());
		}
		else {
			throw new UnsupportedOperationException(key);
		}
	}

	@Override
	protected void resetProperties() {
		run = null;
		parameter = null;
		permission = null;
		notification = null;
		initialContent = null;
		initialActive = false;
		initialInterval = 0;
	}

	@Override
	public String getName() {
		if (listenerName == null) {
			listenerName = NameUtils.getRandomName();
		}
		return listenerName;
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		notifyJobStatusChange(context);
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {
		notifyJobStatusChange(context);
	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		notifyJobStatusChange(context);
	}
	
	private void notifyJobStatusChange(JobExecutionContext context) {
		if (context.getJobInstance() instanceof Job) {
			switch (getViewMode()) {
				case LIST:
					jobStatusChanged = true;
					break;
				
				case DETAIL:
					final AbstractJob job = (AbstractJob) context.getJobInstance();
					if (taskService.getTask(job).equals(getObject())) {
						jobStatusChanged = true;
					}
					break;
				
				default:
					throw new UnsupportedOperationException(getViewMode().name());
			}
		}
	}

	@Override
	protected SourceCode getSourceCode(ContentObject contentObject) {
		Assert.notNull(contentObject, "contentObject");
		final Task task = (Task) contentObject;
		return taskCodeProvider.getTaskSource(task);
	}

}
