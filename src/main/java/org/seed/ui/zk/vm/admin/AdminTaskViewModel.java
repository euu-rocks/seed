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
import java.util.UUID;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import org.seed.core.config.JobScheduler;
import org.seed.core.data.SystemObject;
import org.seed.core.task.AbstractJob;
import org.seed.core.task.IntervalUnit;
import org.seed.core.task.Task;
import org.seed.core.task.TaskMetadata;
import org.seed.core.task.TaskNotification;
import org.seed.core.task.TaskParameter;
import org.seed.core.task.TaskPermission;
import org.seed.core.task.TaskResult;
import org.seed.core.task.TaskRun;
import org.seed.core.task.TaskService;
import org.seed.core.user.Authorisation;
import org.seed.core.user.User;

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
	
	private final static String PARAMETERS = "parameters";
	private final static String PERMISSIONS = "permissions";
	private final static String NOTIFICATIONS = "notifications";
	
	private final String listenerName = UUID.randomUUID().toString();
	
	@WireVariable(value="taskServiceImpl")
	private TaskService taskService;
	
	@WireVariable(value="jobScheduler")
	private JobScheduler jobScheduler;
	
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
		super(Authorisation.ADMIN_JOB, "task",
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
		return userService.findAllObjects();
	}
	
	@Init
	public void init(@ContextParam(ContextType.VIEW) Component view,
					 @ExecutionArgParam("param") Object object) {
		super.init(object, view);
		jobScheduler.addJobListener(this);
	}
	
	@Override
	protected void initObject(Task task) {
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
	@SmartNotifyChange("permission")
	public void insertToPermissionList(@BindingParam("base") TaskPermission base,
									   @BindingParam("item") TaskPermission item,
									   @BindingParam("list") int listNum) {
		insertToList(PERMISSIONS, listNum, base, item);
		if (listNum == LIST_AVAILABLE && item == permission) {
			this.permission = null;
		}
	}
	
	@Command
	@SmartNotifyChange("permission")
	public void dropToPermissionList(@BindingParam("item") TaskPermission item,
									 @BindingParam("list") int listNum) {
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
					notifyChange("objectList");
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
	@NotifyChange("parameter")
	public void newParameter() {
		parameter = taskService.createParameter(getObject());
		notifyObjectChange(PARAMETERS);
		flagDirty();
	}
	
	@Command
	@NotifyChange("parameter")
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
		showCodeDialog(new CodeDialogParameter(this, getObject()));
	}
	
	@Command
	public void refreshTask(@BindingParam("elem") Component component) {
		cmdRefresh();
	}
	
	@Command
	public void deleteTask(@BindingParam("elem") Component component) {
		cmdDeleteObject(component);
	}
	
	@Command
	public void saveTask(@BindingParam("elem") Component elem) {
		// detect content change
		final boolean contentChanged = !ObjectUtils.nullSafeEquals(initialContent, 
																   getObject().getContent());
		((TaskMetadata) getObject()).setContentChanged(contentChanged);
		adjustLists(getObject().getPermissions(), getListManagerList(PERMISSIONS, LIST_SELECTED));
		
		if (!cmdSaveObject(elem)) {
			return;
		}
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
		super.flagDirty(notify, notifyObject);
	}
	
	@GlobalCommand
	public void _refreshObject(@BindingParam("param") Long objectId) {
		refreshObject(objectId);
	}
	
	@Override
	protected TaskService getObjectService() {
		return taskService;
	}

	@Override
	protected List<? extends SystemObject> getListManagerSource(String key, int listNum) {
		switch (key) {
			case PERMISSIONS:
				return listNum == LIST_AVAILABLE 
				? taskService.getAvailablePermissions(getObject()) 
				: getObject().getPermissions();
		}
		return null;
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
