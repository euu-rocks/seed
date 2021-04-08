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
package org.seed.core.entity;

import java.util.List;

import javax.persistence.Table;

import org.seed.core.config.AbstractChangeLogBuilder;
import org.seed.core.config.ChangeLog;
import org.seed.core.data.FieldType;
import org.seed.core.data.FileObject;
import org.seed.core.util.TinyId;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import liquibase.change.AddColumnConfig;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.AddForeignKeyConstraintChange;
import liquibase.change.core.AddNotNullConstraintChange;
import liquibase.change.core.AddUniqueConstraintChange;
import liquibase.change.core.CreateIndexChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropColumnChange;
import liquibase.change.core.DropIndexChange;
import liquibase.change.core.DropNotNullConstraintChange;
import liquibase.change.core.DropTableChange;
import liquibase.change.core.DropUniqueConstraintChange;
import liquibase.change.core.ModifyDataTypeChange;
import liquibase.change.core.RenameColumnChange;
import liquibase.change.core.RenameTableChange;

class EntityChangeLogBuilder extends AbstractChangeLogBuilder {
	
	private final static String FIELD_ID		 = "id";
	private final static String FIELD_STATUSID   = "status_id";
	private final static String FIELD_VERSION	 = "version";
	private final static String FIELD_CREATEDON  = "createdon";
	private final static String FIELD_CREATEDBY  = "createdby";
	private final static String FIELD_MODIFIEDON = "modifiedon";
	private final static String FIELD_MODIFIEDBY = "modifiedby";
	
	private Entity currentVersionEntity;
	
	private Entity nextVersionEntity;
	
	EntityChangeLogBuilder setCurrentVersionEntity(Entity currentVersionEntity) {
		this.currentVersionEntity = currentVersionEntity;
		return this;
	}
	
	EntityChangeLogBuilder setNextVersionEntity(Entity nextVersionEntity) {
		this.nextVersionEntity = nextVersionEntity;
		return this;
	}
	
	@Override
	public List<ChangeLog> build() {
		// create table
		if (currentVersionEntity == null) {
			addCreateTableChangeSet(nextVersionEntity);
		}
		// drop table
		else if (nextVersionEntity == null) {
			addDropTableChangeSet(currentVersionEntity);
		}
		else {
			// rename table
			if (!getTableName(currentVersionEntity).equals(getTableName(nextVersionEntity))) {
				addRenameTableChange(currentVersionEntity, nextVersionEntity);
			}
			// status field change
			if (currentVersionEntity.hasStatus() != nextVersionEntity.hasStatus()) {
				buildStatusFieldChange();
			}
			// field changes
			if (currentVersionEntity.hasAllFields() || nextVersionEntity.hasAllFields()) {
				buildFieldChanges();
			}
		}
		return super.build();
	}
	
	static String getTableName(Entity entity) {
		Assert.notNull(entity, "entity is null");
		
		return entity.getTableName() != null 
				? entity.getTableName() 
				: entity.getInternalName().toLowerCase();
	}
	
	static String getColumnName(EntityField entityField) {
		Assert.notNull(entityField, "entityField is null");
		
		return entityField.getColumnName() != null 
				? entityField.getColumnName()
				: entityField.getInternalName();
	}
	
	private void addCreateTableChangeSet(Entity entity) {
		Assert.notNull(entity, "entity is null");
		
		final CreateTableChange createTableChange = new CreateTableChange();
		createTableChange.setTableName(getTableName(entity));
		
		// system fields
		createTableChange.addColumn(createColumn(FIELD_ID, FieldType.LONG)
										.setConstraints(new ConstraintsConfig()
											.setPrimaryKey(Boolean.TRUE)
											.setPrimaryKeyName(getPrimaryKeyConstraintName(entity))));
		createTableChange.addColumn(createColumn(FIELD_VERSION, FieldType.INTEGER)
										.setConstraints(new ConstraintsConfig()
											.setNullable(Boolean.FALSE)));
		createTableChange.addColumn(createColumn(FIELD_CREATEDON, FieldType.DATETIME));
		createTableChange.addColumn(createColumn(FIELD_CREATEDBY, FieldType.TEXT, getLimit("user.name.length")));
		createTableChange.addColumn(createColumn(FIELD_MODIFIEDON, FieldType.DATETIME));
		createTableChange.addColumn(createColumn(FIELD_MODIFIEDBY, FieldType.TEXT, getLimit("user.name.length")));
		
		// status
		if (entity.hasStatus()) {
			createTableChange.addColumn(createColumn(FIELD_STATUSID, FieldType.LONG)
											.setConstraints(new ConstraintsConfig()
												.setNullable(Boolean.FALSE)));
		}
		
		// fields
		if (entity.hasAllFields()) {
			for (EntityField field : entity.getAllFields()) {
				final ColumnConfig column = initColumn(new ColumnConfig(), entity, field);
				createTableChange.addColumn(column);
			}
		}
		
		createChangeSet().addChange(createTableChange);
		
		// field constraints and index
		if (entity.hasAllFields()) {
			for (EntityField field : entity.getAllFields()) {
				if (field.getType().isReference() || field.getType().isFile() || field.isIndexed()) {
					addFieldConstraintsAndIndex(entity, field);
				}
			}
		}
		if (entity.hasStatus()) {
			buildStatusConstraintAndIndex(entity);
		}
	}
	
	private void addDropTableChangeSet(Entity entity) {
		Assert.notNull(entity, "entity is null");
		
		final DropTableChange dropTableChange = new DropTableChange();
		dropTableChange.setTableName(getTableName(entity));
		createChangeSet().addChange(dropTableChange);
	}
	
	private void addRenameTableChange(Entity oldEntity, Entity newEntity) {
		Assert.notNull(oldEntity, "oldEntity is null");
		Assert.notNull(newEntity, "newEntity is null");
		
		final RenameTableChange renameTableChange = new RenameTableChange();
		renameTableChange.setOldTableName(getTableName(oldEntity));
		renameTableChange.setNewTableName(getTableName(newEntity));
		createChangeSet().addChange(renameTableChange);
	}
	
	private void addDropColumChangeSet(Entity entity, EntityField field) {
		Assert.notNull(entity, "entity is null");
		Assert.notNull(field, "field is null");
		
		final DropColumnChange dropColumnChange = new DropColumnChange();
		dropColumnChange.setTableName(getTableName(entity));
		dropColumnChange.setColumnName(getColumnName(field));
		createChangeSet().addChange(dropColumnChange);
	}
	
	private void addRenameColumnChangeSet(Entity entity, EntityField oldField, EntityField newField) {
		Assert.notNull(entity, "entity is null");
		Assert.notNull(oldField, "oldField is null");
		Assert.notNull(newField, "newField is null");
		
		final RenameColumnChange renameColumnChange = new RenameColumnChange();
		renameColumnChange.setTableName(getTableName(entity));
		renameColumnChange.setOldColumnName(getColumnName(oldField));
		renameColumnChange.setNewColumnName(getColumnName(newField));
		createChangeSet().addChange(renameColumnChange);
	}
	
	private void addModifyDataTypeChangeSet(Entity entity, EntityField field) {
		Assert.notNull(entity, "entity is null");
		Assert.notNull(field, "field is null");
		
		final ModifyDataTypeChange modifyDataTypeChange = new ModifyDataTypeChange();
		modifyDataTypeChange.setTableName(getTableName(entity));
		modifyDataTypeChange.setColumnName(getColumnName(field));
		modifyDataTypeChange.setNewDataType(getDBFieldType(field));
		createChangeSet().addChange(modifyDataTypeChange);
	}
	
	private void addAddMandatoryConstraintChangeSet(Entity entity, EntityField field) {
		Assert.notNull(entity, "entity is null");
		Assert.notNull(field, "field is null");
		
		final AddNotNullConstraintChange addNotNullConstraintChange = new AddNotNullConstraintChange();
		addNotNullConstraintChange.setTableName(getTableName(entity));
		addNotNullConstraintChange.setColumnName(getColumnName(field));
		addNotNullConstraintChange.setColumnDataType(getDBFieldType(field));
		String defaultValue = null;
		if (field.getDefaultString() != null) {
			defaultValue = field.getDefaultString();
		}
		else if (field.getDefaultNumber() != null) {
			defaultValue = field.getDefaultNumber().toString();
		}
		else if (field.getDefaultDate() != null) {
			defaultValue = field.getDefaultDate().toString();
		}
		else if (field.getDefaultObject() != null) {
			defaultValue = field.getDefaultObject().getId().toString();
		}
		if (defaultValue != null) {
			addNotNullConstraintChange.setDefaultNullValue(defaultValue);
		}
		createChangeSet().addChange(addNotNullConstraintChange);
	}
	
	private void addDropMandatoryConstraintChangeSet(Entity entity, EntityField field) {
		Assert.notNull(entity, "entity is null");
		Assert.notNull(field, "field is null");
		
		final DropNotNullConstraintChange dropNotNullConstraintChange = new DropNotNullConstraintChange();
		dropNotNullConstraintChange.setTableName(getTableName(entity));
		dropNotNullConstraintChange.setColumnName(getColumnName(field));
		dropNotNullConstraintChange.setColumnDataType(getDBFieldType(field));
		createChangeSet().addChange(dropNotNullConstraintChange);
	}
	
	private void addAddUniqueConstraintChangeSet(Entity entity, EntityField ...fields) {
		Assert.notNull(entity, "entity is null");
		Assert.notNull(fields, "fields is null");
		
		final AddUniqueConstraintChange addUniqueConstraintChange = new AddUniqueConstraintChange();
		addUniqueConstraintChange.setTableName(getTableName(entity));
		addUniqueConstraintChange.setConstraintName(getUniqueConstraintName(entity, fields[0]));
		addUniqueConstraintChange.setColumnNames(createColumnNameList(fields));
		createChangeSet().addChange(addUniqueConstraintChange);
	}
	
	private void addDropUniqueConstraintChangeSet(Entity entity, EntityField ...fields) {
		Assert.notNull(entity, "entity is null");
		Assert.notNull(fields, "fields is null");
	
		final DropUniqueConstraintChange dropUniqueConstraintChange = new DropUniqueConstraintChange();
		dropUniqueConstraintChange.setTableName(getTableName(entity));
		dropUniqueConstraintChange.setConstraintName(getUniqueConstraintName(entity, fields[0]));
		createChangeSet().addChange(dropUniqueConstraintChange);
	}
	
	private void addDropIndexChangeSet(Entity entity, EntityField field) {
		Assert.notNull(entity, "entity is null");
		Assert.notNull(field, "field is null");
		
		final DropIndexChange dropIndexChange = new DropIndexChange();
		dropIndexChange.setTableName(getTableName(entity));
		dropIndexChange.setIndexName(getIndexName(entity, field));
		createChangeSet().addChange(dropIndexChange);
	}
	
		private void buildStatusConstraintAndIndex(Entity entity) {
		Assert.notNull(entity, "entity is null");
		
		final AddForeignKeyConstraintChange addFKConstraintChange = new AddForeignKeyConstraintChange();
		addFKConstraintChange.setBaseTableName(getTableName(entity));
		addFKConstraintChange.setConstraintName(getStatusForeignKeyConstraintName(entity));
		addFKConstraintChange.setBaseColumnNames(FIELD_STATUSID);
		addFKConstraintChange.setReferencedTableName(EntityStatus.class.getAnnotation(Table.class).name());
		addFKConstraintChange.setReferencedColumnNames(FIELD_ID);
		createChangeSet().addChange(addFKConstraintChange);
		
		final CreateIndexChange createIndexChange = new CreateIndexChange();
		createIndexChange.setTableName(getTableName(entity));
		createIndexChange.setIndexName(getStatusIndexName(entity));
		
		final AddColumnConfig column = new AddColumnConfig();
		column.setName(FIELD_STATUSID);
		column.setType(FieldType.LONG.dbType);
		createIndexChange.addColumn(column);
		createChangeSet().addChange(createIndexChange);
	}
		
	private void addAddColumChangeSet(Entity entity, EntityField field) {
		Assert.notNull(entity, "entity is null");
		Assert.notNull(field, "field is null");
		
		final AddColumnConfig columnConfig = new AddColumnConfig();
		initColumn(columnConfig, entity, field);
		createChangeSet().addChange(createAddColumnChange(entity, columnConfig));
		
		if (field.getType().isReference() || field.getType().isFile() || field.isIndexed()) {
			addFieldConstraintsAndIndex(entity, field);
		}
	}
	
	private void buildFieldChanges() {
		if (currentVersionEntity.hasAllFields()) {
			for (EntityField field : currentVersionEntity.getAllFields()) {
				// ignore calculated fields
				if (field.isCalculated()) {
					continue;
				}
				
				final EntityField nextVersionField = nextVersionEntity.getFieldById(field.getId());
				
				//  drop column
				if (nextVersionField == null) {
					addDropColumChangeSet(nextVersionEntity, field);
					continue;
				}
				
				// change data type
				if (field.getType() != nextVersionField.getType() ||
				    !ObjectUtils.nullSafeEquals(field.getLength(), nextVersionField.getLength())) {
					addModifyDataTypeChangeSet(nextVersionEntity, nextVersionField);
				}
				
				// rename column
				if (!getColumnName(field).equals(getColumnName(nextVersionField))) {
					addRenameColumnChangeSet(nextVersionEntity, field, nextVersionField);
				}
				
				// mandatory state changed
				if (!field.isMandatory() && nextVersionField.isMandatory()) {
					addAddMandatoryConstraintChangeSet(nextVersionEntity, nextVersionField);
				}
				else if (field.isMandatory() && !nextVersionField.isMandatory()) {
					addDropMandatoryConstraintChangeSet(nextVersionEntity, nextVersionField);
				}
				
				// unique state changed
				if (!field.isUnique() && nextVersionField.isUnique()) {
					addAddUniqueConstraintChangeSet(nextVersionEntity, nextVersionField);
				}
				else if (field.isUnique() && !nextVersionField.isUnique()) {
					addDropUniqueConstraintChangeSet(nextVersionEntity, nextVersionField);
				}
				
				// index state change
				if (!field.isIndexed() && nextVersionField.isIndexed()) {
					addCreateIndexChangeSet(nextVersionEntity, nextVersionField);
				}
				else if (field.isIndexed() && !nextVersionField.isIndexed()) {
					addDropIndexChangeSet(nextVersionEntity, nextVersionField);
				}
			}
		}
		if (nextVersionEntity.hasAllFields()) {
			for (EntityField field : nextVersionEntity.getAllFields()) {
				// ignore calculated fields
				if (field.isCalculated()) {
					continue;
				}
				
				// add column
				if (field.isNew() || currentVersionEntity.getFieldById(field.getId()) == null) {
					addAddColumChangeSet(nextVersionEntity, field);
				}
			}
		}
	}
	
	private void buildStatusFieldChange() {
		// add status field
		if (nextVersionEntity.hasStatus()) {
			final AddColumnConfig columnConfig = new AddColumnConfig();
			columnConfig.setName(FIELD_STATUSID);
			columnConfig.setType(FieldType.LONG.dbType);
			columnConfig.setConstraints(new ConstraintsConfig().setNullable(Boolean.FALSE));
			columnConfig.setDefaultValueNumeric(nextVersionEntity.getInitialStatus().getId());
			createChangeSet().addChange(createAddColumnChange(nextVersionEntity, columnConfig));
			buildStatusConstraintAndIndex(nextVersionEntity);
		}
		else { // remove status field
			final DropColumnChange dropColumnChange = new DropColumnChange();
			dropColumnChange.setTableName(getTableName(nextVersionEntity));
			dropColumnChange.setColumnName(FIELD_STATUSID);
			createChangeSet().addChange(dropColumnChange);
		}
	}
	
	private ColumnConfig createColumn(String name, FieldType fieldType) {
		return createColumn(name, fieldType, null);
	}
	
	private ColumnConfig createColumn(String name, FieldType fieldType, Integer length) {
		Assert.notNull(name, "name is null");
		Assert.notNull(fieldType, "fieldType is null");
		
		return new ColumnConfig().setName(name).setType(getDBFieldType(fieldType, length));
	}
	
	private String getDBFieldType(EntityField field) {
		Assert.notNull(field, "field is null");
		
		return getDBFieldType(field.getType(), field.getLength());
	}
	
	private String getDBFieldType(FieldType fieldType, Integer length) {
		Assert.notNull(fieldType, "fieldType is null");
		if (length != null) {
			Assert.state(fieldType.isText(), "only text fields can have length");
		}
		
		// force bytea instead of oid on postgres
		if (fieldType.isBinary() && isPostgres()) {
			return "bytea";
		}
		return (fieldType.isText() || fieldType.isAutonum())
				? fieldType.dbType + '(' + (length != null ? length : getLimit("entity.stringfield.length")) + ')' 
				: fieldType.dbType;
	}
	
	private ColumnConfig initColumn(ColumnConfig column, Entity entity, EntityField field) {
		Assert.notNull(column, "column is null");
		Assert.notNull(entity, "entity is null");
		Assert.notNull(field, "field is null");
		
		column.setName(getColumnName(field));
		column.setType(getDBFieldType(field));
		// constraints
		if (field.isMandatory() || field.isUnique() || field.getType().isBoolean()) {
			final ConstraintsConfig constraints = new ConstraintsConfig();
			if (field.isMandatory() || field.getType().isBoolean()) {
				constraints.setNullable(Boolean.FALSE);
				if (currentVersionEntity != null) {
					switch (field.getType()) {
						case TEXT:
						case TEXTLONG:
							column.setDefaultValue(field.getDefaultString());
							break;
						case DATE:
						case DATETIME:
							column.setDefaultValueDate(field.getDefaultDate());
							break;
						case REFERENCE:
							column.setDefaultValueNumeric(field.getDefaultObject().getId());
							break;
						case INTEGER:
						case LONG:
						case DECIMAL:
						case DOUBLE:
							column.setDefaultValueNumeric(field.getDefaultNumber());
							break;
						case BOOLEAN:
							column.setDefaultValueBoolean(false);
							break;
						case AUTONUM:
						case BINARY:
						case FILE:
							break;
						default:
							throw new UnsupportedOperationException(field.getType().name());
					}
				}
			}
			if (field.isUnique()) {
				constraints.setUnique(Boolean.TRUE);
				constraints.setUniqueConstraintName(getUniqueConstraintName(entity, field));
			}
			column.setConstraints(constraints);
		}
		return column;
	}
	
	private void addCreateIndexChangeSet(Entity entity, EntityField field) {
		Assert.notNull(entity, "entity is null");
		Assert.notNull(field, "field is null");
		
		final CreateIndexChange createIndexChange = new CreateIndexChange();
		createIndexChange.setTableName(getTableName(entity));
		createIndexChange.setIndexName(getIndexName(entity, field));
		
		final AddColumnConfig column = new AddColumnConfig();
		column.setName(getColumnName(field));
		column.setType(getDBFieldType(field));
		createIndexChange.addColumn(column);
		createChangeSet().addChange(createIndexChange);
	}
	
	private void addFieldConstraintsAndIndex(Entity entity, EntityField field) {
		Assert.notNull(entity, "entity is null");
		Assert.notNull(field, "field is null");
		
		// reference / file field
		if (field.getType().isReference() || field.getType().isFile()) {
			final AddForeignKeyConstraintChange addFKConstraintChange = new AddForeignKeyConstraintChange();
			addFKConstraintChange.setConstraintName(getForeignKeyConstraintName(entity, field));
			addFKConstraintChange.setBaseTableName(getTableName(entity));
			addFKConstraintChange.setBaseColumnNames(getColumnName(field));
			addFKConstraintChange.setReferencedColumnNames(FIELD_ID);
			if (field.getType().isReference()) {
				addFKConstraintChange.setReferencedTableName(getTableName(field.getReferenceEntity()));
			}
			else if (field.getType().isFile()) {
				addFKConstraintChange.setReferencedTableName(FileObject.class.getAnnotation(Table.class).name());
			}
			createChangeSet().addChange(addFKConstraintChange);
		}
		
		// index
		if (field.isIndexed()) {
			addCreateIndexChangeSet(entity, field);
		}
	}
	
	private static String getPrimaryKeyConstraintName(Entity entity) {
		return "pk_" + TinyId.get(entity.getId());
	}
	
	private static String getUniqueConstraintName(Entity entity, EntityField field) {
		return "uni_" + getConstraintKey(entity, field);
	}
	
	private static String getForeignKeyConstraintName(Entity entity, EntityField field) {
		return "fk_" + getConstraintKey(entity, field);
	}
	
	private static String getStatusForeignKeyConstraintName(Entity entity) {
		return "fk_" + TinyId.get(entity.getId()) + "_status";
	}
	
	private static String getIndexName(Entity entity, EntityField field) {
		return "idx_" + getConstraintKey(entity, field);
	}
	
	private static String getStatusIndexName(Entity entity) {
		return "idx_" + TinyId.get(entity.getId()) + "_status";
	}
	
	private static String getConstraintKey(Entity entity, EntityField field) {
		Assert.notNull(entity, "entity is null");
		Assert.notNull(field, "field is null");
		
		return TinyId.get(entity.getId()) + '_' + TinyId.get(field.getId());
	}
	
	private static AddColumnChange createAddColumnChange(Entity entity, AddColumnConfig columnConfig) {
		Assert.notNull(entity, "entity is null");
		Assert.notNull(columnConfig, "columnConfig is null");
		
		final AddColumnChange addColumnChange = new AddColumnChange();
		addColumnChange.setTableName(getTableName(entity));
		addColumnChange.addColumn(columnConfig);
		return addColumnChange;
	}
	
	private static String createColumnNameList(EntityField ...fields) {
		final StringBuilder buf = new StringBuilder();
		for (EntityField field : fields) {
			if (buf.length() > 0) {
				buf.append(',');
			}
			buf.append(getColumnName(field));
		}
		return buf.toString();
	}
	
}
