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

import org.seed.C;
import org.seed.Seed;
import org.seed.core.config.DatabaseInfo;
import org.seed.core.config.Limits;
import org.seed.core.config.changelog.AbstractChangeLogBuilder;
import org.seed.core.config.changelog.ChangeLog;
import org.seed.core.data.FieldType;
import org.seed.core.data.FileObject;
import org.seed.core.data.SystemField;
import org.seed.core.util.Assert;
import org.seed.core.util.TinyId;

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

class EntityChangeLogBuilder extends AbstractChangeLogBuilder<Entity> {
	
	private static final String PREFIX_PRIMARY_KEY = "pk_";
	private static final String PREFIX_FOREIGN_KEY = "fk_";
	private static final String PREFIX_UNIQUE_KEY  = "uni_";
	private static final String PREFIX_INDEX       = "idx_";
	private static final String SUFFIX_STATUS      = "_status";
	private static final String SUFFIX_AUDIT       = "_aud";
	
	private final EntityUsage entityUsage;
	
	private final DatabaseInfo databaseInfo;
	
	private final Limits limits;
	
	private boolean existValueObjects = false;
	
	private List<Entity> descendants; // entities that implements a generic entity
	
	private List<Entity> inverseRelateds;
	
	EntityChangeLogBuilder(DatabaseInfo databaseInfo, Limits limits) {
		entityUsage = Seed.getBean(EntityUsage.class);
		Assert.stateAvailable(entityUsage, "entity usage");
		Assert.notNull(databaseInfo, "databaseInfo");
		Assert.notNull(limits, "limits");
		
		this.databaseInfo = databaseInfo;
		this.limits = limits;
	}
	
	void setDescendants(List<Entity> descendants) {
		this.descendants = descendants;
	}
	
	void setInverseRelateds(List<Entity> inverseRelateds) {
		this.inverseRelateds = inverseRelateds;
	}

	@Override
	public ChangeLog build() {
		checkValid();
		if (!isGeneric()) {
			if (isUpdateChange()) {
				existValueObjects = entityUsage.existObjects(currentVersionObject);
			}
			buildEntityChanges();
		}
		else if (currentVersionObject != null && nextVersionObject != null) { // only on update
			buildGenericEntityChanges();
		}
		return super.build();
	}
	
	private void buildGenericEntityChanges() {
		if (currentVersionObject.hasAllFields() || nextVersionObject.hasAllFields()) {
			buildFieldChanges();
		}
		if (currentVersionObject.hasAllRelations() || nextVersionObject.hasAllRelations()) {
			buildRelationChanges();
		}
	}
	
	private void buildEntityChanges() {
		// create table
		if (currentVersionObject == null) {
			createEntityTables();
		}
		// drop table
		else if (nextVersionObject == null) {
			dropEntityTables();
		}
		else {
			// rename table
			if (!currentVersionObject.getEffectiveTableName().equals(nextVersionObject.getEffectiveTableName())) {
				renameEntity();
			}
			
			// audit state changee
			if (currentVersionObject.isAudited() != nextVersionObject.isAudited()) {
				buildAuditTableChange();
			}
			
			// status field change
			if (currentVersionObject.hasStatus() != nextVersionObject.hasStatus()) {
				buildStatusFieldChange();
			}
			
			// field changes
			if (currentVersionObject.hasAllFields() || nextVersionObject.hasAllFields()) {
				buildFieldChanges();
			}
			
			// relation changes
			if (currentVersionObject.hasAllRelations() || nextVersionObject.hasAllRelations()) {
				buildRelationChanges();
			}
		}
	}
	
	private void createEntityTables() {
		// entity table
		addCreateTableChangeSet(nextVersionObject, false);
		
		// audit table
		if (nextVersionObject.isAudited()) {
			addCreateTableChangeSet(nextVersionObject, true);
		}
		
		// relation tables
		if (nextVersionObject.hasAllRelations()) {
			for (EntityRelation relation : nextVersionObject.getAllRelations()) {
				addCreateTableChangeSet(relation);
			}
		}
	}
	
	private void dropEntityTables() {
		// drop relation tables
		if (currentVersionObject.hasAllRelations()) {
			for (EntityRelation relation : currentVersionObject.getAllRelations()) {
				addDropTableChangeSet(relation);
			}
		}
		addDropTableChangeSet(currentVersionObject, false);
		if (currentVersionObject.isAudited()) {
			addDropTableChangeSet(currentVersionObject, true);
		}
	}
	
	private boolean isGeneric() {
		return (currentVersionObject != null && currentVersionObject.isGeneric()) ||
			   (nextVersionObject != null && nextVersionObject.isGeneric());
	}
	
	private void renameEntity() {
		// rename table
		addRenameTableChange(currentVersionObject, nextVersionObject, false);
		// rename audit table
		if (nextVersionObject.isAudited()) {
			addRenameTableChange(currentVersionObject, nextVersionObject, true);
		}
		
		// rename relation tables
		if (nextVersionObject.hasAllRelations()) {
			for (EntityRelation relation : nextVersionObject.getAllRelations()) {
				final EntityRelation currentVersionRelation = 
						currentVersionObject.getRelationByUid(relation.getUid());
				if (currentVersionRelation != null) {
					renameRelation(currentVersionRelation, relation);
				}
			}
		}
		
		// rename inverse relation tables
		if (inverseRelateds != null) {
			for (Entity inverseRelated : inverseRelateds) {
				for (EntityRelation inverseRelation : inverseRelated.getRelations()) {
					renameRelation(inverseRelation, inverseRelation.createInverseRelation(nextVersionObject));
				}
			}
		}
	}
	
	private void renameRelation(EntityRelation oldRelation, EntityRelation newRelation) {
		Assert.notNull(oldRelation, "oldRelation");
		Assert.notNull(newRelation, "newRelation");
		
		// rename relation table
		final RenameTableChange renameTableChange = new RenameTableChange();
		renameTableChange.setOldTableName(oldRelation.getJoinTableName());
		renameTableChange.setNewTableName(newRelation.getJoinTableName());
		addChange(renameTableChange);
		
		// rename join column
		if (!oldRelation.getJoinColumnName().equals(newRelation.getJoinColumnName())) {
			final RenameColumnChange renameColumnChange = new RenameColumnChange();
			renameColumnChange.setTableName(newRelation.getJoinTableName());
			renameColumnChange.setOldColumnName(oldRelation.getJoinColumnName());
			renameColumnChange.setNewColumnName(newRelation.getJoinColumnName());
			addChange(renameColumnChange);
		}
		
		// rename inverse join column
		if (!oldRelation.getInverseJoinColumnName().equals(newRelation.getInverseJoinColumnName())) {
			final RenameColumnChange renameColumnChange = new RenameColumnChange();
			renameColumnChange.setTableName(newRelation.getJoinTableName());
			renameColumnChange.setOldColumnName(oldRelation.getInverseJoinColumnName());
			renameColumnChange.setNewColumnName(newRelation.getInverseJoinColumnName());
			addChange(renameColumnChange);
		}
	}
	
	private void addCreateTableChangeSet(Entity entity, boolean isAuditTable) {
		Assert.notNull(entity, C.ENTITY);
		
		final ConstraintsConfig pkConfig = new ConstraintsConfig()
				.setPrimaryKey(Boolean.TRUE)
				.setPrimaryKeyName(getPrimaryKeyConstraintName(entity, isAuditTable));
		
		final CreateTableChange createTableChange = new CreateTableChange();
		// table name
		createTableChange.setTableName(isAuditTable 
										? entity.getEffectiveTableName().concat(SUFFIX_AUDIT)
										: entity.getEffectiveTableName());
		// system fields
		createTableChange.addColumn(createColumn(SystemField.ID)
										.setConstraints(pkConfig));
		if (isAuditTable) {
			createTableChange.addColumn(createColumn("rev", FieldType.LONG, null)
											.setConstraints(pkConfig));
			createTableChange.addColumn(createColumn("revtype", FieldType.INTEGER, null)
											.setConstraints(new ConstraintsConfig()
											.setNullable(Boolean.FALSE)));
		}
		else {
			createTableChange.addColumn(createColumn(SystemField.VERSION)
											.setConstraints(new ConstraintsConfig()
											.setNullable(Boolean.FALSE)));
			createTableChange.addColumn(createColumn(SystemField.CREATEDON));
			createTableChange.addColumn(createColumn(SystemField.CREATEDBY, getLimit(Limits.LIMIT_USER_LENGTH)));
			createTableChange.addColumn(createColumn(SystemField.MODIFIEDON));
			createTableChange.addColumn(createColumn(SystemField.MODIFIEDBY, getLimit(Limits.LIMIT_USER_LENGTH)));
		}
		// uid
		if (entity.isTransferable()) {
			createTableChange.addColumn(createColumn(SystemField.UID, getLimit(Limits.LIMIT_UID_LENGTH))
											.setConstraints(new ConstraintsConfig()
												.setNullable(Boolean.FALSE)
												.setUnique(Boolean.TRUE)));
		}
		// status
		if (entity.hasStatus()) {
			createTableChange.addColumn(createColumn(SystemField.ENTITYSTATUS)
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
		
		addChange(createTableChange);
		
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
	
	private void addCreateTableChangeSet(EntityRelation relation) {
		Assert.notNull(relation, "relation");
		
		final ConstraintsConfig pkConfig = new ConstraintsConfig()
				.setPrimaryKey(Boolean.TRUE)
				.setPrimaryKeyName(getPrimaryKeyConstraintName(relation));
		
		final CreateTableChange createTableChange = new CreateTableChange();
		createTableChange.setTableName(relation.getJoinTableName());
		
		// join column
		createTableChange.addColumn(createJoinColumn(relation.getJoinColumnName())
							.setConstraints(pkConfig));
		
		// inverse join column 
		createTableChange.addColumn(createJoinColumn(relation.getInverseJoinColumnName())
							.setConstraints(pkConfig));
		addChange(createTableChange);
		addRelationConstraints(relation);
	}
	
	private void addDropTableChangeSet(Entity entity, boolean isAuditTable) {
		Assert.notNull(entity, C.ENTITY);
		
		final DropTableChange dropTableChange = new DropTableChange();
		dropTableChange.setTableName(isAuditTable 
										? entity.getEffectiveTableName().concat(SUFFIX_AUDIT)
										: entity.getEffectiveTableName());
		addChange(dropTableChange);
	}
	
	private void addDropTableChangeSet(EntityRelation relation) {
		Assert.notNull(relation, C.RELATION);
		
		final DropTableChange dropTableChange = new DropTableChange();
		dropTableChange.setTableName(relation.getJoinTableName());
		addChange(dropTableChange);
	}
	
	private void addRenameTableChange(Entity oldEntity, Entity newEntity, boolean isAuditTable) {
		Assert.notNull(oldEntity, "oldEntity");
		Assert.notNull(newEntity, "newEntity");
		
		final RenameTableChange renameTableChange = new RenameTableChange();
		renameTableChange.setOldTableName(isAuditTable 
											? oldEntity.getEffectiveTableName().concat(SUFFIX_AUDIT)
											: oldEntity.getEffectiveTableName());
		renameTableChange.setNewTableName(isAuditTable 
											? newEntity.getEffectiveTableName().concat(SUFFIX_AUDIT)
											: newEntity.getEffectiveTableName());
		addChange(renameTableChange);
	}
	
	private void addDropColumChangeSet(Entity entity, EntityField field, boolean isAuditTable) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(field, C.FIELD);
		
		final DropColumnChange dropColumnChange = new DropColumnChange();
		dropColumnChange.setTableName(isAuditTable 
										? entity.getEffectiveTableName().concat(SUFFIX_AUDIT)
										: entity.getEffectiveTableName());
		dropColumnChange.setColumnName(field.getEffectiveColumnName());
		addChange(dropColumnChange);
	}
	
	private void addRenameColumnChangeSet(Entity entity, EntityField oldField, EntityField newField, boolean isAuditTable) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(oldField, "oldField");
		Assert.notNull(newField, "newField");
		
		final RenameColumnChange renameColumnChange = new RenameColumnChange();
		renameColumnChange.setTableName(isAuditTable 
										? entity.getEffectiveTableName().concat(SUFFIX_AUDIT)
										: entity.getEffectiveTableName());
		renameColumnChange.setOldColumnName(oldField.getEffectiveColumnName());
		renameColumnChange.setNewColumnName(newField.getEffectiveColumnName());
		addChange(renameColumnChange);
	}
	
	private void addModifyDataTypeChangeSet(Entity entity, EntityField field, boolean isAuditTable) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(field, C.FIELD);
		
		final ModifyDataTypeChange modifyDataTypeChange = new ModifyDataTypeChange();
		modifyDataTypeChange.setTableName(isAuditTable 
											? entity.getEffectiveTableName().concat(SUFFIX_AUDIT)
											: entity.getEffectiveTableName());
		modifyDataTypeChange.setColumnName(field.getEffectiveColumnName());
		modifyDataTypeChange.setNewDataType(getDBFieldType(field));
		addChange(modifyDataTypeChange);
	}
	
	private void addAddMandatoryConstraintChangeSet(Entity entity, EntityField field, boolean isAuditTable) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(field, C.FIELD);
		
		final AddNotNullConstraintChange addNotNullConstraintChange = new AddNotNullConstraintChange();
		addNotNullConstraintChange.setTableName(isAuditTable 
												? entity.getEffectiveTableName().concat(SUFFIX_AUDIT)
												: entity.getEffectiveTableName());
		addNotNullConstraintChange.setColumnName(field.getEffectiveColumnName());
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
		addChange(addNotNullConstraintChange);
	}
	
	private void addDropMandatoryConstraintChangeSet(Entity entity, EntityField field, boolean isAuditTable) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(field, C.FIELD);
		
		final DropNotNullConstraintChange dropNotNullConstraintChange = new DropNotNullConstraintChange();
		dropNotNullConstraintChange.setTableName(isAuditTable 
													? entity.getEffectiveTableName().concat(SUFFIX_AUDIT)
													: entity.getEffectiveTableName());
		dropNotNullConstraintChange.setColumnName(field.getEffectiveColumnName());
		dropNotNullConstraintChange.setColumnDataType(getDBFieldType(field));
		addChange(dropNotNullConstraintChange);
	}
	
	private void addAddUniqueConstraintChangeSet(Entity entity, boolean isAuditTable, EntityField ...fields) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(fields, "fields");
		
		final AddUniqueConstraintChange addUniqueConstraintChange = new AddUniqueConstraintChange();
		addUniqueConstraintChange.setTableName(isAuditTable 
												? entity.getEffectiveTableName().concat(SUFFIX_AUDIT)
												: entity.getEffectiveTableName());
		addUniqueConstraintChange.setConstraintName(getUniqueConstraintName(entity, fields[0]));
		addUniqueConstraintChange.setColumnNames(createColumnNameList(fields));
		addChange(addUniqueConstraintChange);
	}
	
	private void addDropUniqueConstraintChangeSet(Entity entity, boolean isAuditTable, EntityField ...fields) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(fields, "fields");
	
		final DropUniqueConstraintChange dropUniqueConstraintChange = new DropUniqueConstraintChange();
		dropUniqueConstraintChange.setTableName(isAuditTable 
												? entity.getEffectiveTableName().concat(SUFFIX_AUDIT)
												: entity.getEffectiveTableName());
		dropUniqueConstraintChange.setConstraintName(getUniqueConstraintName(entity, fields[0]));
		addChange(dropUniqueConstraintChange);
	}
	
	private void addDropIndexChangeSet(Entity entity, EntityField field) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(field, C.FIELD);
		
		final DropIndexChange dropIndexChange = new DropIndexChange();
		dropIndexChange.setTableName(entity.getEffectiveTableName());
		dropIndexChange.setIndexName(getIndexName(entity, field));
		addChange(dropIndexChange);
	}
	
	private void buildStatusConstraintAndIndex(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		final AddForeignKeyConstraintChange addFKConstraintChange = new AddForeignKeyConstraintChange();
		addFKConstraintChange.setBaseTableName(entity.getEffectiveTableName());
		addFKConstraintChange.setConstraintName(getStatusForeignKeyConstraintName(entity));
		addFKConstraintChange.setBaseColumnNames(SystemField.ENTITYSTATUS.columName);
		addFKConstraintChange.setReferencedTableName(EntityStatus.class.getAnnotation(Table.class).name());
		addFKConstraintChange.setReferencedColumnNames(SystemField.ID.columName);
		addChange(addFKConstraintChange);
		
		final CreateIndexChange createIndexChange = new CreateIndexChange();
		createIndexChange.setTableName(entity.getEffectiveTableName());
		createIndexChange.setIndexName(getStatusIndexName(entity));
		
		final AddColumnConfig column = new AddColumnConfig();
		column.setName(SystemField.ENTITYSTATUS.columName);
		column.setType(FieldType.LONG.dataType.name());
		createIndexChange.addColumn(column);
		addChange(createIndexChange);
	}
		
	private void addAddColumChangeSet(Entity entity, EntityField field) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(field, C.FIELD);
		
		final AddColumnConfig columnConfig = new AddColumnConfig();
		initColumn(columnConfig, entity, field);
		addChange(createAddColumnChange(entity, columnConfig));
		
		if (field.getType().isReference() || field.getType().isFile() || field.isIndexed()) {
			addFieldConstraintsAndIndex(entity, field);
		}
	}
	
	private void buildRelationChanges() {
		if (currentVersionObject.hasAllRelations()) {
			for (EntityRelation relation : currentVersionObject.getAllRelations()) {
				if (nextVersionObject.getRelationByUid(relation.getUid()) == null) {
					buildRelationChangesCurrentVersion(relation);
				}
			}
		}
		if (nextVersionObject.hasAllRelations()) {
			for (EntityRelation relation : nextVersionObject.getAllRelations()) {
				if (relation.isNew() || currentVersionObject.getRelationByUid(relation.getUid()) == null) {
					buildRelationChangesNextVersion(relation);
				}
			}
		}
	}
	
	private void buildRelationChangesCurrentVersion(EntityRelation relation) {
		if (isGeneric()) {
			if (descendants != null) { // build changes for all implementing entities
				for (Entity descendant : descendants) {
					addDropTableChangeSet(relation.createDescendantRelation(descendant));
				}
			}
		}
		else {
			addDropTableChangeSet(relation);
		}
	}
	
	private void buildRelationChangesNextVersion(EntityRelation relation) {
		if (isGeneric()) {
			if (descendants != null) { // build changes for all implementing entities
				for (Entity descendant : descendants) {
					addCreateTableChangeSet(relation.createDescendantRelation(descendant));
				}
			}
		}
		else {
			addCreateTableChangeSet(relation);
		}
	}
	
	private void buildFieldChanges() {
		if (currentVersionObject.hasAllFields()) {
			currentVersionObject.getAllFields().forEach(this::buildFieldChangesCurrentVersion);
		}
		if (nextVersionObject.hasAllFields()) {
			if (isGeneric()) {
				if (descendants != null) { // build changes for all implementing entities
					descendants.forEach(this::buildFieldChangesNextVersion);
				}
			}
			else {
				buildFieldChangesNextVersion(nextVersionObject);
			}
		}
	}
	
	private void buildFieldChangesCurrentVersion(EntityField field) {
		// ignore calculated fields
		if (field.isCalculated()) {
			return;
		}
		
		final EntityField nextVersionField = nextVersionObject.getFieldById(field.getId());
		// generic
		if (isGeneric()) {
			if (descendants != null) { // build changes for all implementing entities
				descendants.forEach(descendant -> buildFieldChanges(descendant, field, nextVersionField));
			}
		}
		else {
			buildFieldChanges(nextVersionObject, field, nextVersionField);
		}
	}
	
	private void buildFieldChangesNextVersion(Entity entity) {
		for (EntityField field : nextVersionObject.getAllFields()) {
			// ignore calculated fields
			if (field.isCalculated()) {
				continue;
			}
			
			// add column
			if (field.isNew() || currentVersionObject.getFieldById(field.getId()) == null) {
				addAddColumChangeSet(entity, field);
			}
		}
	}
	
	private void buildFieldChanges(Entity entity, EntityField field, EntityField nextVersionField) {
		//  drop column
		if (nextVersionField == null) {
			addDropColumChangeSet(entity, field, false);
			if (entity.isAudited()) {
				addDropColumChangeSet(entity, field, true);
			}
			return;
		}
		
		// change data type
		if (field.getType() != nextVersionField.getType() ||
		    !ObjectUtils.nullSafeEquals(field.getLength(), nextVersionField.getLength())) {
			addModifyDataTypeChangeSet(entity, nextVersionField, false);
			if (entity.isAudited()) {
				addModifyDataTypeChangeSet(entity, nextVersionField, true);
			}
		}
		
		// rename column
		if (!field.getEffectiveColumnName().equals(nextVersionField.getEffectiveColumnName())) {
			addRenameColumnChangeSet(entity, field, nextVersionField, false);
			if (entity.isAudited()) {
				addRenameColumnChangeSet(entity, field, nextVersionField, true);
			}
		}
		
		// mandatory state changed
		if (!field.isMandatory() && nextVersionField.isMandatory()) {
			addAddMandatoryConstraintChangeSet(entity, nextVersionField, false);
			if (entity.isAudited()) {
				addAddMandatoryConstraintChangeSet(entity, nextVersionField, true);
			}
		}
		else if (field.isMandatory() && !nextVersionField.isMandatory()) {
			addDropMandatoryConstraintChangeSet(entity, nextVersionField, false);
			if (entity.isAudited()) {
				addDropMandatoryConstraintChangeSet(entity, nextVersionField, true);
			}
		}
		
		// unique state changed
		if (!field.isUnique() && nextVersionField.isUnique()) {
			addAddUniqueConstraintChangeSet(entity, false, nextVersionField);
			if (entity.isAudited()) {
				addAddUniqueConstraintChangeSet(entity, true, nextVersionField);
			}
		}
		else if (field.isUnique() && !nextVersionField.isUnique()) {
			addDropUniqueConstraintChangeSet(entity, false, nextVersionField);
			if (entity.isAudited()) {
				addDropUniqueConstraintChangeSet(entity, true, nextVersionField);
			}
		}
		
		buildFieldIndex(entity, field, nextVersionField);
	}
	
	private void buildFieldIndex(Entity entity, EntityField field, EntityField nextVersionField) {
		// index state change
		if (!field.isIndexed() && nextVersionField.isIndexed()) {
			addCreateIndexChangeSet(entity, nextVersionField);
		}
		else if (field.isIndexed() && !nextVersionField.isIndexed()) {
			addDropIndexChangeSet(entity, nextVersionField);
		}
	}
	
	private void buildStatusFieldChange() {
		// add status field
		if (nextVersionObject.hasStatus()) {
			final AddColumnConfig columnConfig = new AddColumnConfig();
			columnConfig.setName(SystemField.ENTITYSTATUS.columName);
			columnConfig.setType(FieldType.LONG.dataType.name());
			columnConfig.setConstraints(new ConstraintsConfig().setNullable(Boolean.FALSE));
			columnConfig.setDefaultValueNumeric(nextVersionObject.getInitialStatus().getId());
			addChange(createAddColumnChange(nextVersionObject, columnConfig));
			buildStatusConstraintAndIndex(nextVersionObject);
		}
		else { // remove status field
			final DropColumnChange dropColumnChange = new DropColumnChange();
			dropColumnChange.setTableName(nextVersionObject.getEffectiveTableName());
			dropColumnChange.setColumnName(SystemField.ENTITYSTATUS.columName);
			addChange(dropColumnChange);
		}
	}
	
	private void buildAuditTableChange() {
		if (nextVersionObject.isAudited()) {
			addCreateTableChangeSet(nextVersionObject, true);
		}
		else {
			addDropTableChangeSet(currentVersionObject, true);
		}
	}
	
	private ColumnConfig createColumn(SystemField systemField) {
		return createColumn(systemField, null);
	}
	
	private ColumnConfig createColumn(SystemField systemField, Integer length) {
		Assert.notNull(systemField, "system field");
		
		return createColumn(systemField.columName, systemField.type, length);
	}
	
	private ColumnConfig createColumn(String name, FieldType fieldType, Integer length) {
		Assert.notNull(name, C.NAME);
		Assert.notNull(fieldType, C.FIELDTYPE);
		
		return new ColumnConfig().setName(name).setType(getDBFieldType(fieldType, length));
	}
	
	private ColumnConfig createJoinColumn(String name) {
		Assert.notNull(name, C.NAME);
		
		return new ColumnConfig().setName(name).setType(getDBFieldType(FieldType.REFERENCE, null));
	}
	
	private String getDBFieldType(EntityField field) {
		Assert.notNull(field, C.FIELD);
		
		return getDBFieldType(field.getType(), field.getLength());
	}
	
	private String getDBFieldType(FieldType fieldType, Integer length) {
		Assert.notNull(fieldType, C.FIELDTYPE);
		if (length != null) {
			Assert.state(fieldType.isText(), "only text fields can have length");
		}
		
		// force bytea instead of oid on postgres
		if (fieldType.isBinary() && databaseInfo.isPostgres()) {
			return "bytea";
		}
		
		return (fieldType.isText() || fieldType.isAutonum())
				? fieldType.dataType.name() + '(' + getFieldLength(length) + ')' 
				: fieldType.dataType.name();
	}
	
	private int getFieldLength(Integer length) {
		return length != null ? length : getLimit(Limits.LIMIT_TEXT_LENGTH);
	}
	
	private ColumnConfig initColumn(ColumnConfig column, Entity entity, EntityField field) {
		Assert.notNull(column, "column");
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(field, C.FIELD);
		
		column.setName(field.getEffectiveColumnName());
		column.setType(getDBFieldType(field));
		// constraints
		if (field.isMandatory() || field.isUnique() || field.getType().isBoolean()) {
			final ConstraintsConfig constraints = new ConstraintsConfig();
			if (field.isMandatory() || field.getType().isBoolean()) {
				constraints.setNullable(Boolean.FALSE);
				if (currentVersionObject != null && existValueObjects) {
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
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(field, C.FIELD);
		
		final CreateIndexChange createIndexChange = new CreateIndexChange();
		createIndexChange.setTableName(entity.getEffectiveTableName());
		createIndexChange.setIndexName(getIndexName(entity, field));
		
		final AddColumnConfig column = new AddColumnConfig();
		column.setName(field.getEffectiveColumnName());
		column.setType(getDBFieldType(field));
		createIndexChange.addColumn(column);
		addChange(createIndexChange);
	}
	
	private void addFieldConstraintsAndIndex(Entity entity, EntityField field) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(field, C.FIELD);
		
		// reference / file field
		if (field.getType().isReference() || field.getType().isFile()) {
			final AddForeignKeyConstraintChange addFKConstraintChange = new AddForeignKeyConstraintChange();
			addFKConstraintChange.setConstraintName(getForeignKeyConstraintName(entity, field));
			addFKConstraintChange.setBaseTableName(entity.getEffectiveTableName());
			addFKConstraintChange.setBaseColumnNames(field.getEffectiveColumnName());
			addFKConstraintChange.setReferencedColumnNames(SystemField.ID.columName);
			if (field.getType().isReference()) {
				addFKConstraintChange.setReferencedTableName(field.getReferenceEntity().getEffectiveTableName());
			}
			else if (field.getType().isFile()) {
				addFKConstraintChange.setReferencedTableName(FileObject.class.getAnnotation(Table.class).name());
			}
			addChange(addFKConstraintChange);
		}
		
		// index
		if (field.isIndexed()) {
			addCreateIndexChangeSet(entity, field);
		}
	}
	
	private void addRelationConstraints(EntityRelation relation) {
		Assert.notNull(relation, C.RELATION);
		
		// join column fk
		final AddForeignKeyConstraintChange addJoinConstraintChange = new AddForeignKeyConstraintChange();
		addJoinConstraintChange.setConstraintName(getJoinColumnForeignKeyConstraintName(relation));
		addJoinConstraintChange.setBaseTableName(relation.getJoinTableName());
		addJoinConstraintChange.setBaseColumnNames(relation.getJoinColumnName());
		addJoinConstraintChange.setReferencedColumnNames(SystemField.ID.columName);
		addJoinConstraintChange.setReferencedTableName(relation.getEntity().getEffectiveTableName());
		addChange(addJoinConstraintChange);
		
		// inverse join column fk
		final AddForeignKeyConstraintChange addInverseJoinConstraintChange = new AddForeignKeyConstraintChange();
		addInverseJoinConstraintChange.setConstraintName(getInverseJoinColumnForeignKeyConstraintName(relation));
		addInverseJoinConstraintChange.setBaseTableName(relation.getJoinTableName());
		addInverseJoinConstraintChange.setBaseColumnNames(relation.getInverseJoinColumnName());
		addInverseJoinConstraintChange.setReferencedColumnNames(SystemField.ID.columName);
		addInverseJoinConstraintChange.setReferencedTableName(relation.getRelatedEntity().getEffectiveTableName());
		addChange(addInverseJoinConstraintChange);
	}
	
	private int getLimit(String limitName) {
		return limits.getLimit(limitName);
	}
	
	private static String getPrimaryKeyConstraintName(Entity entity, boolean isAudit) {
		return PREFIX_PRIMARY_KEY + TinyId.get(entity.getId()) + (isAudit ? SUFFIX_AUDIT : "");
	}
	
	private static String getPrimaryKeyConstraintName(EntityRelation relation) {
		return PREFIX_PRIMARY_KEY + TinyId.get(relation.getEntity().getId()) + '_' + TinyId.get(relation.getRelatedEntity().getId());
	}
	
	private static String getUniqueConstraintName(Entity entity, EntityField field) {
		return PREFIX_UNIQUE_KEY + getConstraintKey(entity, field);
	}
	
	private static String getForeignKeyConstraintName(Entity entity, EntityField field) {
		return PREFIX_FOREIGN_KEY + getConstraintKey(entity, field);
	}
	
	private static String getJoinColumnForeignKeyConstraintName(EntityRelation relation) {
		return PREFIX_FOREIGN_KEY + getConstraintKey(relation, relation.getEntity());
	}
	
	private static String getInverseJoinColumnForeignKeyConstraintName(EntityRelation relation) {
		return PREFIX_FOREIGN_KEY + getConstraintKey(relation, relation.getRelatedEntity());
	}
	
	private static String getStatusForeignKeyConstraintName(Entity entity) {
		return PREFIX_FOREIGN_KEY + TinyId.get(entity.getId()) + SUFFIX_STATUS;
	}
	
	private static String getIndexName(Entity entity, EntityField field) {
		return PREFIX_INDEX + getConstraintKey(entity, field);
	}
	
	private static String getStatusIndexName(Entity entity) {
		return PREFIX_INDEX + TinyId.get(entity.getId()) + SUFFIX_STATUS;
	}
	
	private static String getConstraintKey(Entity entity, EntityField field) {
		return TinyId.get(entity.getId()) + '_' + TinyId.get(field.getId());
	}
	
	private static String getConstraintKey(EntityRelation relation, Entity entity) {
		return TinyId.get(relation.getEntity().getId()) + '_' + 
			   TinyId.get(relation.getRelatedEntity().getId()) + '_' +  
			   TinyId.get(entity.getId());
	}
	
	private static AddColumnChange createAddColumnChange(Entity entity, AddColumnConfig columnConfig) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(columnConfig, "columnConfig");
		
		final AddColumnChange addColumnChange = new AddColumnChange();
		addColumnChange.setTableName(entity.getEffectiveTableName());
		addColumnChange.addColumn(columnConfig);
		return addColumnChange;
	}
	
	private static String createColumnNameList(EntityField ...fields) {
		final StringBuilder buf = new StringBuilder();
		for (EntityField field : fields) {
			if (buf.length() > 0) {
				buf.append(',');
			}
			buf.append(field.getEffectiveColumnName());
		}
		return buf.toString();
	}
	
}
