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
package org.seed.core.task.codegen;

import java.util.Date;

import org.seed.C;
import org.seed.core.api.JobContext;
import org.seed.core.codegen.AbstractSourceCodeBuilder;
import org.seed.core.codegen.ParameterMetadata;
import org.seed.core.codegen.SourceCode;
import org.seed.core.task.AbstractJob;
import org.seed.core.task.Task;
import org.seed.core.util.Assert;

class TaskCodeBuilder extends AbstractSourceCodeBuilder {
	
	private final Task task;
	
	TaskCodeBuilder(Task task) {
		super(task,
			  false,
			  newTypeClass(AbstractJob.class),
			  null);
		this.task = task;
	}

	@Override
	public Date getLastModified() {
		return task.getLastModified();
	}
	
	@Override
	public SourceCode build(BuildMode buildMode) {
		Assert.notNull(buildMode, "buildMode");
		
		switch (buildMode) {
			case TEMPLATE:
				addMethod(null, "execute", 
						  new ParameterMetadata[] {
							newParameter(C.CONTEXT, newTypeClass(JobContext.class))
						  }, 
						  CODE_PLACEHOLDER, newAnnotation(Override.class));
				return super.build(false);
				
			case COMPLETE:
				return createSourceCode(task.getContent());
				
			default:
				throw new UnsupportedOperationException(buildMode.name());
		}
		
	}
	
}
