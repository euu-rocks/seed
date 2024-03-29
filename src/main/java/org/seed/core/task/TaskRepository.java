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

import org.seed.C;
import org.seed.core.data.AbstractSystemEntityRepository;
import org.seed.core.util.Assert;

import org.springframework.stereotype.Repository;

@Repository
public class TaskRepository extends AbstractSystemEntityRepository<Task> {
	
	private static final String QUERY_SYSTEMTASK_RUNS = 
		"from SystemTaskRun r where r.systemTask = :systemTask order by r.createdOn desc";
	
	protected TaskRepository() {
		super(TaskMetadata.class);
	}
	
	@Override
	public Session getSession() {
		return super.getSession();
	}
	
	protected SystemTaskRun getLastSystemTaskRun(SystemTask systemTask, Session session) {
		Assert.notNull(systemTask, C.SYSTEMTASK);
		Assert.notNull(session, C.SESSION);
		
		final List<SystemTaskRun> result = 
				session.createQuery(QUERY_SYSTEMTASK_RUNS, SystemTaskRun.class)
					   .setParameter(C.SYSTEMTASK, systemTask)
					   .setMaxResults(1)
					   .list();
		return result.isEmpty() ? null : result.get(0);
	}
	
	protected List<SystemTaskRun> getSystemTaskRuns(SystemTask systemTask, Session session) {
		Assert.notNull(systemTask, C.SYSTEMTASK);
		Assert.notNull(session, C.SESSION);
		
		return session.createQuery(QUERY_SYSTEMTASK_RUNS, SystemTaskRun.class)
					  .setParameter(C.SYSTEMTASK, systemTask)
					  .list();
	}
	
}
