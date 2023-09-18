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

import static org.seed.core.util.CollectionUtils.*;

import java.util.ArrayList;
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
import org.seed.core.data.revision.RevisionEntity;
import org.seed.core.data.revision.RevisionField;
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
	private static final String SUFFIX_REVISION    = "_rev";
	
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
			// audit state change
			if (currentVersionObject.isAudited() != nextVersionObject.isAudited()) {
				buildAuditTableChange();
			}
			
			// rename table
			if (!currentVersionObject.getEffectiveTableName().equals(nextVersionObject.getEffectiveTableName())) {
				renameEntity();
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
				addCreateTableChangeSet(relation, false);
				// relation audit table
				if (nextVersionObject.isAudited()) {
					addCreateTableChangeSet(relation, true);
				}
			}
		}
	}
	
	private void dropEntityTables() {
		// drop relation tables
		if (currentVersionObject.hasAllRelations()) {
			for (EntityRelation relation : currentVersionObject.getAllRelations()) {
				addDropTableChangeSet(relation, false);
				// drop relation audit table
				if (currentVersionObject.isAudited()) {
					addDropTableChangeSet(relation, true);
				}
			}
		}
		// drop audit table
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
					renameRelation(null, currentVersionRelation, relation);
				}
			}
		}
		
		// rename inverse relation tables
		if (inverseRelateds != null) {
			for (Entity inverseRelated : inverseRelateds) {
				inverseRelated.getRelations().forEach(inverseRelation -> 
					renameRelation(currentVersionObject, inverseRelation, 
								   inverseRelation.createInverseRelation(nextVersionObject)));
			}
		}
	}
	
	private void renameRelation(Entity related, EntityRelation oldRelation, EntityRelation newRelation) {
		addRenameRelationTableChanges(related, oldRelation, newRelation, false);
		if (newRelation.getEntity().isAudited()) {
			addRenameRelationTableChanges(related, oldRelation, newRelation, true);
		}
	}
	
	private void addCreateTableChangeSet(Entity entity, boolean isAuditTable) {
		Assert.notNull(entity, C.ENTITY);
		
		final var createTableChange = new CreateTableChange();
		// table name
		createTableChange.setTableName(getTableName(entity, isAuditTable));
		// system fields
		createSystemFields(entity, isAuditTable).forEach(createTableChange::addColumn);
		
		// uid
		if (entity.isTransferable() && !isAuditTable) {
			createTableChange.addColumn(createColumn(SystemField.UID, getLimit(Limits.LIMIT_UID_LENGTH))
											.setConstraints(notNullConstraint().setUnique(Boolean.TRUE)));
		}
		// status
		if (entity.hasStatus()) {
			final ColumnConfig columnStatus = createColumn(SystemField.ENTITYSTATUS);
			if (!isAuditTable) {
				columnStatus.setConstraints(notNullConstraint());
			}
			createTableChange.addColumn(columnStatus);
		}
		// fields
		if (entity.hasAllFields()) {
			entity.getAllFields().forEach(field -> 
				createTableChange.addColumn(initColumn(new ColumnConfig(), entity, field, isAuditTable)));
		}
		
		addChange(createTableChange);
		
		if (isAuditTable) {
			buildRevisionFieldConstraint(entity);
		}
		else {
			buildFieldConstraintsAndIndexChanges(entity);
		}
	}
	
	private List<ColumnConfig> createSystemFields(Entity entity, boolean isAuditTable) {
		final var columns = new ArrayList<ColumnConfig>(8);
		final var pkConfig = new ConstraintsConfig()
					.setPrimaryKey(Boolean.TRUE)
					.setPrimaryKeyName(getPrimaryKeyConstraintName(entity, isAuditTable));
		
		columns.add(createColumn(SystemField.ID).setConstraints(pkConfig));
		if (isAuditTable) {
			columns.add(createColumn(RevisionField.REV)
							.setConstraints(pkConfig));
			columns.add(createColumn(RevisionField.REVTYPE)
							.setConstraints(notNullConstraint()));
		}
		else {
			columns.add(createColumn(SystemField.VERSION)
							.setConstraints(notNullConstraint()));
			columns.add(createColumn(SystemField.CREATEDON));
			columns.add(createColumn(SystemField.CREATEDBY, getLimit(Limits.LIMIT_USER_LENGTH)));
			columns.add(createColumn(SystemField.MODIFIEDON));
			columns.add(createColumn(SystemField.MODIFIEDBY, getLimit(Limits.LIMIT_USER_LENGTH)));
		}
		return columns;
	}
	
	private void addCreateTableChangeSet(EntityRelation relation, boolean isAuditTable) {
		Assert.notNull(relation, "relation");
		
		final var pkConfig = new ConstraintsConfig()
					.setPrimaryKey(Boolean.TRUE)
					.setPrimaryKeyName(getPrimaryKeyConstraintName(relation, isAuditTable));
		
		final var createTableChange = new CreateTableChange();
		createTableChange.setTableName(getTableName(relation, isAuditTable));
		if (isAuditTable) {
			createTableChange.addColumn(createColumn(RevisionField.REV)
										.setConstraints(pkConfig));
			createTableChange.addColumn(createColumn(RevisionField.REVTYPE)
										.setConstraints(notNullConstraint()));
		}
		
		// join column
		createTableChange.addColumn(createJoinColumn(relation.getJoinColumnName())
							.setConstraints(pkConfig));
		
		// inverse join column 
		createTableChange.addColumn(createJoinColumn(relation.getInverseJoinColumnName())
							.setConstraints(pkConfig));
		addChange(createTableChange);
		if (!isAuditTable) {
			addRelationConstraints(relation);
		}
	}
	
	private void addDropTableChangeSet(Entity entity, boolean isAuditTable) {
		Assert.notNull(entity, C.ENTITY);
		
		final var dropTableChange = new DropTableChange();
		dropTableChange.setTableName(getTableName(entity, isAuditTable));
		addChange(dropTableChange);
	}
	
	private void addDropTableChangeSet(EntityRelation relation, boolean isAuditTable) {
		Assert.notNull(relation, C.RELATION);
		
		final var dropTableChange = new DropTableChange();
		dropTableChange.setTableName(getTableName(relation, isAuditTable));
		addChange(dropTableChange);
	}
	
	private void addRenameTableChange(Entity oldEntity, Entity newEntity, boolean isAuditTable) {
		Assert.notNull(oldEntity, "oldEntity");
		Assert.notNull(newEntity, "newEntity");
		
		final var renameTableChange = new RenameTableChange();
		renameTableChange.setOldTableName(getTableName(oldEntity, isAuditTable));
		renameTableChange.setNewTableName(getTableName(newEntity, isAuditTable));
		addChange(renameTableChange);
	}
	
	private void addRenameRelationTableChanges(Entity related, EntityRelation oldRelation, EntityRelation newRelation, boolean isAuditTable) {
		Assert.notNull(oldRelation, "oldRelation");
		Assert.notNull(newRelation, "newRelation");
		
		// rename table 
		final var renameTableChange = new RenameTableChange();
		renameTableChange.setOldTableName(getTableName(related, oldRelation, isAuditTable));
		renameTableChange.setNewTableName(getTableName(newRelation, isAuditTable));
		addChange(renameTableChange);
		
		// rename join column
		if (!oldRelation.getJoinColumnName().equals(newRelation.getJoinColumnName())) {
			final var renameColumnChange = new RenameColumnChange();
			renameColumnChange.setTableName(getTableName(newRelation, isAuditTable));
			renameColumnChange.setOldColumnName(oldRelation.getJoinColumnName());
			renameColumnChange.setNewColumnName(newRelation.getJoinColumnName());
			addChange(renameColumnChange);
		}
		
		// rename inverse join column
		if (!oldRelation.getInverseJoinColumnName().equals(newRelation.getInverseJoinColumnName())) {
			final var renameColumnChange = new RenameColumnChange();
			renameColumnChange.setTableName(getTableName(newRelation, isAuditTable));
			renameColumnChange.setOldColumnName(oldRelation.getInverseJoinColumnName());
			renameColumnChange.setNewColumnName(newRelation.getInverseJoinColumnName());
			addChange(renameColumnChange);
		}
	}
	
	private void addDropColumChangeSet(Entity entity, EntityField field, boolean isAuditTable) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(field, C.FIELD);
		
		final var dropColumnChange = new DropColumnChange();
		dropColumnChange.setTableName(getTableName(entity, isAuditTable));
		dropColumnChange.setColumnName(field.getEffectiveColumnName());
		addChange(dropColumnChange);
	}
	
	private void addRenameColumnChangeSet(Entity entity, EntityField oldField, EntityField newField, boolean isAuditTable) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(oldField, "oldField");
		Assert.notNull(newField, "newField");
		
		final var renameColumnChange = new RenameColumnChange();
		renameColumnChange.setTableName(getTableName(entity, isAuditTable));
		renameColumnChange.setOldColumnName(oldField.getEffectiveColumnName());
		renameColumnChange.setNewColumnName(newField.getEffectiveColumnName());
		addChange(renameColumnChange);
	}
	
	private void addModifyDataTypeChangeSet(Entity entity, EntityField field, boolean isAuditTable) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(field, C.FIELD);
		
		final var modifyDataTypeChange = new ModifyDataTypeChange();
		modifyDataTypeChange.setTableName(getTableName(entity, isAuditTable));
		modifyDataTypeChange.setColumnName(field.getEffectiveColumnName());
		modifyDataTypeChange.setNewDataType(getDBFieldType(field));
		addChange(modifyDataTypeChange);
	}
	
	private void addAddMandatoryConstraintChangeSet(Entity entity, EntityField field) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(field, C.FIELD);
		
		final var addNotNullConstraintChange = new AddNotNullConstraintChange();
		addNotNullConstraintChange.setTableName(entity.getEffectiveTableName());
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
	
	private void addDropMandatoryConstraintChangeSet(Entity entity, EntityField field) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(field, C.FIELD);
		
		final var dropNotNullConstraintChange = new DropNotNullConstraintChange();
		dropNotNullConstraintChange.setTableName(entity.getEffectiveTableName());
		dropNotNullConstraintChange.setColumnName(field.getEffectiveColumnName());
		dropNotNullConstraintChange.setColumnDataType(getDBFieldType(field));
		addChange(dropNotNullConstraintChange);
	}
	
	private void addAddUniqueConstraintChangeSet(Entity entity, EntityField ...fields) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(fields, "fields");
		
		final var addUniqueConstraintChange = new AddUniqueConstraintChange();
		addUniqueConstraintChange.setTableName(entity.getEffectiveTableName());
		addUniqueConstraintChange.setConstraintName(getUniqueConstraintName(entity, fields[0]));
		addUniqueConstraintChange.setColumnNames(createColumnNameList(fields));
		addChange(addUniqueConstraintChange);
	}
	
	private void addDropUniqueConstraintChangeSet(Entity entity, EntityField ...fields) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(fields, "fields");
	
		final var dropUniqueConstraintChange = new DropUniqueConstraintChange();
		dropUniqueConstraintChange.setTableName(entity.getEffectiveTableName());
		dropUniqueConstraintChange.setConstraintName(getUniqueConstraintName(entity, fields[0]));
		addChange(dropUniqueConstraintChange);
	}
	
	private void addDropIndexChangeSet(Entity entity, EntityField field) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(field, C.FIELD);
		
		final var dropIndexChange = new DropIndexChange();
		dropIndexChange.setTableName(entity.getEffectiveTableName());
		dropIndexChange.setIndexName(getIndexName(entity, field));
		addChange(dropIndexChange);
	}
	
	private void buildStatusConstraintAndIndex(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		final var addFKConstraintChange = new AddForeignKeyConstraintChange();
		addFKConstraintChange.setBaseTableName(entity.getEffectiveTableName());
		addFKConstraintChange.setConstraintName(getStatusForeignKeyConstraintName(entity));
		addFKConstraintChange.setBaseColumnNames(SystemField.ENTITYSTATUS.columName);
		addFKConstraintChange.setReferencedTableName(EntityStatus.class.getAnnotation(Table.class).name());
		addFKConstraintChange.setReferencedColumnNames(SystemField.ID.columName);
		addChange(addFKConstraintChange);
		
		final var createIndexChange = new CreateIndexChange();
		createIndexChange.setTableName(entity.getEffectiveTableName());
		createIndexChange.setIndexName(getStatusIndexName(entity));
		
		final var column = new AddColumnConfig();
		column.setName(SystemField.ENTITYSTATUS.columName);
		column.setType(FieldType.LONG.dataType.name());
		createIndexChange.addColumn(column);
		addChange(createIndexChange);
	}
		
	private void addAddColumChangeSet(Entity entity, EntityField field, boolean isAuditTable) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(field, C.FIELD);
		
		final var columnConfig = new AddColumnConfig();
		initColumn(columnConfig, entity, field, isAuditTable);
		addChange(createAddColumnChange(entity, columnConfig, isAuditTable));
		if (!isAuditTable && 
			(field.isReferenceField() || field.getType().isFile() || field.isIndexed())) {
			addFieldConstraintsAndIndex(entity, field);
		}
	}
	
	private void buildRelationChanges() {
		filterAndForEach(currentVersionObject.getAllRelations(), 
						 rel -> nextVersionObject.getRelationByUid(rel.getUid()) == null, 
						 this::buildRelationChangesCurrentVersion);
		
		filterAndForEach(nextVersionObject.getAllRelations(), 
						 rel ->  rel.isNew() || currentVersionObject.getRelationByUid(rel.getUid()) == null,
						 this::buildRelationChangesNextVersion);
	}
	
	private void buildRelationChangesCurrentVersion(EntityRelation relation) {
		if (isGeneric()) {
			if (descendants != null) { // build changes for all implementing entities
				for (Entity descendant : descendants) {
					addDropTableChangeSet(relation.createDescendantRelation(descendant), false);
					if (relation.getEntity().isAudited()) {
						addDropTableChangeSet(relation.createDescendantRelation(descendant), true);
					}
				}
			}
		}
		else {
			addDropTableChangeSet(relation, false);
			if (relation.getEntity().isAudited()) {
				addDropTableChangeSet(relation, true);
			}
		}
	}
	
	private void buildRelationChangesNextVersion(EntityRelation relation) {
		if (isGeneric()) {
			if (descendants != null) { // build changes for all implementing entities
				for (Entity descendant : descendants) {
					addCreateTableChangeSet(relation.createDescendantRelation(descendant), false);
					if (relation.getEntity().isAudited()) {
						addCreateTableChangeSet(relation.createDescendantRelation(descendant), true);
					}
				}
			}
		}
		else {
			addCreateTableChangeSet(relation, false);
			if (relation.getEntity().isAudited()) {
				addCreateTableChangeSet(relation, true);
			}
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
		for (EntityField field : subList(nextVersionObject.getAllFields(), not(EntityField::isCalculated))) {
			// add column
			if (field.isNew() || currentVersionObject.getFieldById(field.getId()) == null) {
				addAddColumChangeSet(entity, field, false);
				if (entity.isAudited()) {
					addAddColumChangeSet(entity, field, true);
				}
			}
		}
	}
	
	private void buildFieldChanges(Entity entity, EntityField field, EntityField nextVersionField) {
		//  drop column
		if (nextVersionField == null) {
			buildFieldDropChanges(entity, field);
			return;
		}
		// rename column
		if (!field.getEffectiveColumnName().equals(nextVersionField.getEffectiveColumnName())) {
			buildFieldNameChanges(entity, field, nextVersionField);
		}
		// change data type
		if (field.getType() != nextVersionField.getType() ||
		    !ObjectUtils.nullSafeEquals(field.getLength(), nextVersionField.getLength())) {
			buildFieldTypeChanges(entity, nextVersionField);
		}
		// mandatory state changed
		if (!field.isMandatory() && nextVersionField.isMandatory()) {
			addAddMandatoryConstraintChangeSet(entity, nextVersionField);
		}
		else if (field.isMandatory() && !nextVersionField.isMandatory()) {
			addDropMandatoryConstraintChangeSet(entity, nextVersionField);
		}
		// unique state changed
		if (!field.isUnique() && nextVersionField.isUnique()) {
			addAddUniqueConstraintChangeSet(entity, nextVersionField);
		}
		else if (field.isUnique() && !nextVersionField.isUnique()) {
			addDropUniqueConstraintChangeSet(entity, nextVersionField);
		}
		// index state changes
		buildFieldIndex(entity, field, nextVersionField);
	}
	
	private void buildFieldDropChanges(Entity entity, EntityField field) {
		addDropColumChangeSet(entity, field, false);
		if (entity.isAudited()) {
			addDropColumChangeSet(entity, field, true);
		}
	}
	
	private void buildFieldTypeChanges(Entity entity, EntityField nextVersionField) {
		addModifyDataTypeChangeSet(entity, nextVersionField, false);
		if (entity.isAudited()) {
			addModifyDataTypeChangeSet(entity, nextVersionField, true);
		}
	}
	
	private void buildFieldNameChanges(Entity entity, EntityField field, EntityField nextVersionField) {
		addRenameColumnChangeSet(entity, field, nextVersionField, false);
		if (entity.isAudited()) {
			addRenameColumnChangeSet(entity, field, nextVersionField, true);
		}
	}
	
	private void buildFieldIndex(Entity entity, EntityField field, EntityField nextVersionField) {
		if (!field.isIndexed() && nextVersionField.isIndexed()) {
			addCreateIndexChangeSet(entity, nextVersionField);
		}
		else if (field.isIndexed() && !nextVersionField.isIndexed()) {
			addDropIndexChangeSet(entity, nextVersionField);
		}
	}
	
	private void buildRevisionFieldConstraint(Entity entity) {
		final var addFKConstraintChange = new AddForeignKeyConstraintChange();
		addFKConstraintChange.setConstraintName(getRevisionForeignKeyConstraintName(entity));
		addFKConstraintChange.setBaseTableName(entity.getEffectiveTableName().concat(SUFFIX_AUDIT));
		addFKConstraintChange.setBaseColumnNames(RevisionField.REV.columName);
		addFKConstraintChange.setReferencedTableName(RevisionEntity.class.getAnnotation(Table.class).name());
		addFKConstraintChange.setReferencedColumnNames(SystemField.ID.columName);
		addChange(addFKConstraintChange);
	}
	
	private void buildFieldConstraintsAndIndexChanges(Entity entity) {
		filterAndForEach(entity.getAllFields(), 
						 field -> field.isReferenceField() || field.getType().isFile() || field.isIndexed(), 
						 field -> addFieldConstraintsAndIndex(entity, field));
		if (entity.hasStatus()) {
			buildStatusConstraintAndIndex(entity);
		}
	}
	
	private void buildStatusFieldChange() {
		// add status field
		if (nextVersionObject.hasStatus()) {
			final var columnConfig = new AddColumnConfig();
			columnConfig.setName(SystemField.ENTITYSTATUS.columName)
						.setType(getDBFieldType(SystemField.ENTITYSTATUS.type, null))
						.setConstraints(notNullConstraint())
						.setDefaultValueNumeric(nextVersionObject.getInitialStatus().getId());
			addChange(createAddColumnChange(nextVersionObject, columnConfig, false));
			if (nextVersionObject.isAudited()) {
				final var auditColumnConfig = new AddColumnConfig();
				auditColumnConfig.setName(SystemField.ENTITYSTATUS.columName)
								 .setType(getDBFieldType(SystemField.ENTITYSTATUS.type, null));
				addChange(createAddColumnChange(nextVersionObject, auditColumnConfig, true));
			}
			buildStatusConstraintAndIndex(nextVersionObject);
		}
		else { // remove status field
			addChange(createDropColumnChange(nextVersionObject, SystemField.ENTITYSTATUS, false));
			if (nextVersionObject.isAudited()) {
				addChange(createDropColumnChange(nextVersionObject, SystemField.ENTITYSTATUS, true));
			}
		}
	}
	
	private void buildAuditTableChange() {
		if (nextVersionObject.isAudited()) {
			addCreateTableChangeSet(currentVersionObject, true);
			currentVersionObject.getAllRelations().forEach(
				relation -> addCreateTableChangeSet(relation, true));
		}
		else {
			addDropTableChangeSet(currentVersionObject, true);
			currentVersionObject.getAllRelations().forEach(
				relation -> addDropTableChangeSet(relation, true));
		}
	}
	
	private ColumnConfig createColumn(SystemField systemField) {
		return createColumn(systemField, null);
	}
	
	private ColumnConfig createColumn(SystemField systemField, Integer length) {
		Assert.notNull(systemField, "system field");
		
		return createColumn(systemField.columName, systemField.type, length);
	}
	
	private ColumnConfig createColumn(RevisionField revisionField) {
		Assert.notNull(revisionField, "revision field");
		
		return createColumn(revisionField.columName, revisionField.type, null);
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
	
	private ColumnConfig initColumn(ColumnConfig column, Entity entity, EntityField field, boolean isAuditTable) {
		Assert.notNull(column, "column");
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(field, C.FIELD);
		
		column.setName(field.getEffectiveColumnName());
		column.setType(getDBFieldType(field));
		
		// constraints
		if (!isAuditTable && (field.isMandatory() || field.isUnique() || field.getType().isBoolean())) {
			initColumnConstraints(column, entity, field);
		}
		return column;
	}
	
	private void initColumnConstraints(ColumnConfig column, Entity entity,EntityField field) {
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
	
	private void addCreateIndexChangeSet(Entity entity, EntityField field) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(field, C.FIELD);
		
		final var createIndexChange = new CreateIndexChange();
		createIndexChange.setTableName(entity.getEffectiveTableName());
		createIndexChange.setIndexName(getIndexName(entity, field));
		
		final var column = new AddColumnConfig();
		column.setName(field.getEffectiveColumnName());
		column.setType(getDBFieldType(field));
		createIndexChange.addColumn(column);
		addChange(createIndexChange);
	}
	
	private void addFieldConstraintsAndIndex(Entity entity, EntityField field) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(field, C.FIELD);
		
		// reference / file field
		if (field.isReferenceField() || field.getType().isFile()) {
			final var addFKConstraintChange = new AddForeignKeyConstraintChange();
			addFKConstraintChange.setConstraintName(getForeignKeyConstraintName(entity, field));
			addFKConstraintChange.setBaseTableName(entity.getEffectiveTableName());
			addFKConstraintChange.setBaseColumnNames(field.getEffectiveColumnName());
			addFKConstraintChange.setReferencedColumnNames(SystemField.ID.columName);
			if (field.isReferenceField()) {
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
		final var addJoinConstraintChange = new AddForeignKeyConstraintChange();
		addJoinConstraintChange.setConstraintName(getJoinColumnForeignKeyConstraintName(relation));
		addJoinConstraintChange.setBaseTableName(getTableName(relation, false));
		addJoinConstraintChange.setBaseColumnNames(relation.getJoinColumnName());
		addJoinConstraintChange.setReferencedColumnNames(SystemField.ID.columName);
		addJoinConstraintChange.setReferencedTableName(relation.getEntity().getEffectiveTableName());
		addChange(addJoinConstraintChange);
		
		// inverse join column fk
		final var addInverseJoinConstraintChange = new AddForeignKeyConstraintChange();
		addInverseJoinConstraintChange.setConstraintName(getInverseJoinColumnForeignKeyConstraintName(relation));
		addInverseJoinConstraintChange.setBaseTableName(getTableName(relation, false));
		addInverseJoinConstraintChange.setBaseColumnNames(relation.getInverseJoinColumnName());
		addInverseJoinConstraintChange.setReferencedColumnNames(SystemField.ID.columName);
		addInverseJoinConstraintChange.setReferencedTableName(relation.getRelatedEntity().getEffectiveTableName());
		addChange(addInverseJoinConstraintChange);
	}
	
	private int getLimit(String limitName) {
		return limits.getLimit(limitName);
	}
	
	private static String getTableName(Entity entity, boolean isAudit) {
		final String tableName = entity.getEffectiveTableName();
		return isAudit ? tableName.concat(SUFFIX_AUDIT) : tableName;
	}
	
	private static String getTableName(EntityRelation relation, boolean isAudit) {
		return getTableName(null, relation, isAudit);
	}
	
	private static String getTableName(Entity related, EntityRelation relation, boolean isAudit) {
		final String tableName = related != null ? relation.getJoinTableName(related) : relation.getJoinTableName();
		return isAudit ? tableName.concat(SUFFIX_AUDIT) : tableName;
	}
	
	private static String getPrimaryKeyConstraintName(Entity entity, boolean isAudit) {
		final String constraintName = PREFIX_PRIMARY_KEY.concat(TinyId.get(entity.getId()));
		return isAudit ? constraintName.concat(SUFFIX_AUDIT) : constraintName;
	}
	
	private static String getPrimaryKeyConstraintName(EntityRelation relation, boolean isAudit) {
		final String pkName = PREFIX_PRIMARY_KEY + TinyId.get(relation.getEntity().getId()) + '_' + 
												   TinyId.get(relation.getRelatedEntity().getId());
		return isAudit ? pkName.concat(SUFFIX_AUDIT) : pkName;
	}
	
	private static String getUniqueConstraintName(Entity entity, EntityField field) {
		return PREFIX_UNIQUE_KEY.concat(getConstraintKey(entity, field));
	}
	
	private static String getForeignKeyConstraintName(Entity entity, EntityField field) {
		return PREFIX_FOREIGN_KEY.concat(getConstraintKey(entity, field));
	}
	
	private static String getRevisionForeignKeyConstraintName(Entity entity) {
		return PREFIX_FOREIGN_KEY + TinyId.get(entity.getId()) + SUFFIX_REVISION;
	}
	
	private static String getJoinColumnForeignKeyConstraintName(EntityRelation relation) {
		return PREFIX_FOREIGN_KEY.concat(getConstraintKey(relation, relation.getEntity()));
	}
	
	private static String getInverseJoinColumnForeignKeyConstraintName(EntityRelation relation) {
		return PREFIX_FOREIGN_KEY.concat(getConstraintKey(relation, relation.getRelatedEntity()));
	}
	
	private static String getStatusForeignKeyConstraintName(Entity entity) {
		return PREFIX_FOREIGN_KEY + TinyId.get(entity.getId()) + SUFFIX_STATUS;
	}
	
	private static String getIndexName(Entity entity, EntityField field) {
		return PREFIX_INDEX.concat(getConstraintKey(entity, field));
	}
	
	private static String getStatusIndexName(Entity entity) {
		return PREFIX_INDEX + TinyId.get(entity.getId()) + SUFFIX_STATUS;
	}
	
	private static ConstraintsConfig notNullConstraint() {
		return new ConstraintsConfig().setNullable(Boolean.FALSE);
	}
	
	private static String getConstraintKey(Entity entity, EntityField field) {
		return TinyId.get(entity.getId()) + '_' + TinyId.get(field.getId());
	}
	
	private static String getConstraintKey(EntityRelation relation, Entity entity) {
		return TinyId.get(relation.getEntity().getId()) + '_' + 
			   TinyId.get(relation.getRelatedEntity().getId()) + '_' +  
			   TinyId.get(entity.getId());
	}
	
	private static AddColumnChange createAddColumnChange(Entity entity, AddColumnConfig columnConfig, boolean isAuditTable) {
		final var addColumnChange = new AddColumnChange();
		addColumnChange.setTableName(getTableName(entity, isAuditTable));
		addColumnChange.addColumn(columnConfig);
		return addColumnChange;
	}
	
	private static DropColumnChange createDropColumnChange(Entity entity, SystemField systemField, boolean isAuditTable) {
		final var dropColumnChange = new DropColumnChange();
		dropColumnChange.setTableName(getTableName(entity, isAuditTable));
		dropColumnChange.setColumnName(systemField.columName);
		return dropColumnChange;
	}
	
	private static String createColumnNameList(EntityField ...fields) {
		final var buf = new StringBuilder();
		for (EntityField field : fields) {
			if (buf.length() > 0) {
				buf.append(',');
			}
			buf.append(field.getEffectiveColumnName());
		}
		return buf.toString();
	}
	
}
