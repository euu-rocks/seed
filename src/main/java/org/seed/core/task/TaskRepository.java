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

import org.hibernate.Session;

import org.seed.core.data.AbstractSystemEntityRepository;
import org.seed.core.util.Assert;

import org.springframework.stereotype.Repository;

@Repository
public class TaskRepository extends AbstractSystemEntityRepository<Task> {
	
	private static final String QUERY_SYSTEMTASKRUNS_SYSTEMTASK = 
		"from SystemTaskRun r where r.systemTask = :systemTask order by r.createdOn desc";
	
	protected TaskRepository() {
		super(TaskMetadata.class);
	}
	
	@Override
	public Session getSession() {
		return super.getSession();
	}
	
	protected List<SystemTaskRun> getSystemTaskRuns(SystemTask systemTask) {
		Assert.notNull(systemTask, "systemTask");
		
		try (Session session = getSession()) {
			return session.createQuery(QUERY_SYSTEMTASKRUNS_SYSTEMTASK, SystemTaskRun.class)
						  .setParameter("systemTask", systemTask)
						  .list();
		}
	}
	
}
