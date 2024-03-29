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
package org.seed.core.task.job;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.seed.C;
import org.seed.core.api.Job;
import org.seed.core.util.Assert;

public abstract class AbstractJob implements Job, org.quartz.Job {
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		final Session session = (Session) context.get(DefaultJobContext.RUN_SESSION);
		Assert.stateAvailable(session, C.SESSION);
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			execute(new DefaultJobContext(context));
			tx.commit();
		}
		catch (Exception ex) {
			if (tx != null) {
				tx.rollback();
			}
			throw new JobExecutionException(ex);
		}
	}
	
}
