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

import static org.seed.core.util.CollectionUtils.*;

import java.util.ArrayList;
import java.util.List;

import org.seed.C;
import org.seed.core.customcode.CustomCode;
import org.seed.core.customcode.CustomLib;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.datasource.IDataSource;
import org.seed.core.data.dbobject.DBObject;
import org.seed.core.entity.Entity;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.transfer.Transfer;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.form.Form;
import org.seed.core.form.navigation.Menu;
import org.seed.core.report.Report;
import org.seed.core.rest.Rest;
import org.seed.core.task.Task;
import org.seed.core.user.UserGroup;
import org.seed.core.util.Assert;

public class ImportAnalysis {
	
	private final List<Change> changes = new ArrayList<>();
	
	private final Module module;
	
	ImportAnalysis(Module module) {
		Assert.notNull(module, C.MODULE);
		
		this.module = module;
	}

	public Module getModule() {
		return module;
	}

	public void addChangeNew(SystemEntity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		addChange(ChangeType.NEW, entity);
	}
	
	public void addChangeModify(SystemEntity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		addChange(ChangeType.MODIFY, entity);
	}
	
	public void addChangeDelete(SystemEntity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		addChange(ChangeType.DELETE, entity);
	}
	
	public boolean hasChanges() {
		return notEmpty(changes);
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
	
	public List<Change> getParameterChanges() {
		return getChanges(ModuleParameter.class);
	}
	
	public List<Change> getDBObjectChanges() {
		return getChanges(DBObject.class);
	}
	
	public List<Change> getDataSourceChanges() {
		return getChanges(IDataSource.class);
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
	
	public List<Change> getRestChanges() {
		return getChanges(Rest.class);
	}
	
	public List<Change> getCustomCodeChanges() {
		return getChanges(CustomCode.class);
	}
	
	public List<Change> getCustomLibChanges() {
		return getChanges(CustomLib.class);
	}
	
	public List<Change> getUserGroupChanges() {
		return getChanges(UserGroup.class);
	}
	
	private void addChange(ChangeType type, SystemEntity entity) {
		changes.add(new Change(type, entity));
	}
	
	private long countChanges(ChangeType type) {
		return filterAndCount(changes, change -> change.type == type);
	}
	
	private List<Change> getChanges(Class<? extends SystemEntity> entityClass) {
		return subList(changes, change -> entityClass.isAssignableFrom(change.getEntity().getClass()));
	}
	
	public enum ChangeType {
		
		NEW,
		MODIFY,
		DELETE
		
	}
	
	public class Change {
		
		private final ChangeType type;
		
		private final SystemEntity entity;

		private Change(ChangeType type, SystemEntity entity) {
			this.type = type;
			this.entity = entity;
		}

		public ChangeType getType() {
			return type;
		}

		public SystemEntity getEntity() {
			return entity;
		}
		
	}

}
