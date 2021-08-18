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

import org.seed.core.application.AbstractRestController;
import org.seed.core.config.JobScheduler;
import org.seed.core.user.Authorisation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
	public List<Task> findAll() {
		return findAll(this::checkPermissions);
	}
	
	@Override
	public Task get(@PathVariable("id") Long id) {
		final Task task = super.get(id);
		if (!checkPermissions(task)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		return task;
	}
	
	@PostMapping(value = "/{id}/run")
	public Task run(@PathVariable("id") Long id) {
		final Task task = get(id);
		if (!isAuthorised(Authorisation.RUN_JOBS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		jobScheduler.startJob(task);
		return task;
	}

}
