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

import java.util.List;

import org.seed.C;
import org.seed.core.application.AbstractRestController;
import org.seed.core.task.job.JobScheduler;
import org.seed.core.user.Authorisation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/seed/rest/job")
public class TaskRestController extends AbstractRestController<Task> {
	
	@Autowired
	private TaskService taskService;
	
	@Autowired
	private JobScheduler jobScheduler;
	
	@Override
	protected TaskService getService() {
		return taskService;
	}
	
	@Override
	@ApiOperation(value = "getAllTasks", notes="returns a list of all authorized tasks")
	@GetMapping
	public List<Task> getAll() {
		return getAll(this::checkPermissions);
	}
	
	@Override
	@ApiOperation(value = "getTaskById", notes="returns the task with the given id")
	@GetMapping(value = "/{id}")
	public Task get(@PathVariable(C.ID) Long id) {
		final Task task = super.get(id);
		if (!checkPermissions(task)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		return task;
	}
	
	@ApiOperation(value = "startJob", notes="starts job for task with specified id")
	@PostMapping(value = "/{id}/run")
	public Task run(@PathVariable(C.ID) Long id) {
		final Task task = get(id);
		if (!isAuthorised(Authorisation.RUN_JOBS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		jobScheduler.startJob(task);
		return task;
	}

}
