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
package org.seed.core.application.module;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.seed.core.application.ApplicationEntity;
import org.seed.core.customcode.CustomCode;
import org.seed.core.data.datasource.DataSource;
import org.seed.core.data.dbobject.DBObject;
import org.seed.core.entity.Entity;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.transfer.Transfer;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.form.Form;
import org.seed.core.form.navigation.Menu;
import org.seed.core.report.Report;
import org.seed.core.task.Task;
import org.seed.core.user.UserGroup;

import org.springframework.util.Assert;

public class ImportAnalysis {
	
	private final List<Change> changes = new ArrayList<>();
	
	private final Module module;
	
	ImportAnalysis(Module module) {
		Assert.notNull(module, "module is null");
		
		this.module = module;
	}

	public Module getModule() {
		return module;
	}

	public void addChangeNew(ApplicationEntity entity) {
		Assert.notNull(entity, "entity is null");
		
		addChange(ChangeType.NEW, entity);
	}
	
	public void addChangeModify(ApplicationEntity entity) {
		Assert.notNull(entity, "entity is null");
		
		addChange(ChangeType.MODIFY, entity);
	}
	
	public void addChangeDelete(ApplicationEntity entity) {
		Assert.notNull(entity, "entity is null");
		
		addChange(ChangeType.DELETE, entity);
	}
	
	public boolean hasChanges() {
		return !changes.isEmpty();
	}
	
	public int getNumChanges() {
		return changes.size();
	}
	
	public long getNumNewChanges() {
		return countChanges(ChangeType.NEW);
	}
	
	public long getNumModifyChanges() {
		return countChanges(ChangeType.MODIFY);
	}
	
	public long getNumDeleteChanges() {
		return countChanges(ChangeType.DELETE);
	}
	
	public List<Change> getDBObjectChanges() {
		return getChanges(DBObject.class);
	}
	
	public List<Change> getDataSourceChanges() {
		return getChanges(DataSource.class);
	}
	
	public List<Change> getEntityChanges() {
		return getChanges(Entity.class);
	}
	
	public List<Change> getFilterChanges() {
		return getChanges(Filter.class);
	}
	
	public List<Change> getTransformerChanges() {
		return getChanges(Transformer.class);
	}
	
	public List<Change> getTransferChanges() {
		return getChanges(Transfer.class);
	}
	
	public List<Change> getFormChanges() {
		return getChanges(Form.class);
	}
	
	public List<Change> getMenuChanges() {
		return getChanges(Menu.class);
	}
	
	public List<Change> getTaskChanges() {
		return getChanges(Task.class);
	}
	
	public List<Change> getReportChanges() {
		return getChanges(Report.class);
	}
	
	public List<Change> getCustomCodeChanges() {
		return getChanges(CustomCode.class);
	}
	
	public List<Change> getUserGroupChanges() {
		return getChanges(UserGroup.class);
	}
	
	private void addChange(ChangeType type, ApplicationEntity entity) {
		changes.add(new Change(type, entity));
	}
	
	private long countChanges(ChangeType type) {
		return changes.stream()
				      .filter(change -> change.type == type)
				      .count();
	}
	
	private List<Change> getChanges(Class<? extends ApplicationEntity> entityClass) {
		return changes.stream()
					  .filter(change -> entityClass.isAssignableFrom(change.getEntity().getClass()))
					  .collect(Collectors.toList());
	}
	
	public static enum ChangeType {
		
		NEW,
		MODIFY,
		DELETE
		
	}
	
	public class Change {
		
		private final ChangeType type;
		
		private final ApplicationEntity entity;

		private Change(ChangeType type, ApplicationEntity entity) {
			this.type = type;
			this.entity = entity;
		}

		public ChangeType getType() {
			return type;
		}

		public ApplicationEntity getEntity() {
			return entity;
		}
		
	}

}
