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
package org.seed.core.entity.doc;

import java.util.List;

import org.seed.core.data.FieldType;
import org.seed.core.data.SystemField;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityRelation;
import org.seed.core.util.Assert;

class PlantUMLBuilder {
	
	private final List<Entity> entities;
	
	PlantUMLBuilder(List<Entity> entities) {
		Assert.notNull(entities, "entities");
		
		this.entities = entities;
	}

	String build() {
		final StringBuilder buf = new StringBuilder();
		buildHeader(buf);
		entities.forEach(entity -> buildEntity(buf, entity));
		entities.forEach(entity -> buildRelationEntities(buf, entity));
		entities.forEach(entity -> buildReferences(buf, entity));
		entities.forEach(entity -> buildRelations(buf, entity));
		buildFooter(buf);
		return buf.toString();
	}
	
	private static void buildEntity(StringBuilder buf, Entity entity) {
		buf.append("entity ").append(entity.getEffectiveTableName()).append(" {\n")
		   .append(SystemField.ID.columName).append(" : number <<generated>>\n")
		   .append("--\n");
		if (entity.hasAllFields()) {
			entity.getAllFields().forEach(field -> buildField(buf, field));
		}
		buildSystemField(buf, SystemField.CREATEDBY);
		buildSystemField(buf, SystemField.CREATEDON);
		buildSystemField(buf, SystemField.MODIFIEDBY);
		buildSystemField(buf, SystemField.MODIFIEDON);
		buildSystemField(buf, SystemField.VERSION);
		buf.append("}\n\n");
	}
	
	private static void buildRelationEntities(StringBuilder buf, Entity entity) {
		if (entity.hasAllRelations()) {
			entity.getAllRelations().forEach(relation -> buildRelationEntity(buf, relation));
		}
	}
	
	private static void buildRelationEntity(StringBuilder buf, EntityRelation relation) {
		buf.append("entity ").append(relation.getJoinTableName()).append(" {\n")
		   .append('*').append(relation.getJoinColumnName()).append(" : number <<FK>>\n")
		   .append('*').append(relation.getInverseJoinColumnName()).append(" : number <<FK>>\n")
		   .append("}\n\n");
	}
	
	private static void buildReferences(StringBuilder buf, Entity entity) {
		if (entity.hasAllFields()) {
			entity.getAllFields().stream()
				  .filter(field -> field.getType().isReference())
				  .forEach(field -> buildReference(buf, field));
		}
	}
	
	private static void buildReference(StringBuilder buf, EntityField field) {
		buf.append(field.getEntity().getEffectiveTableName()).append(" }o..")
		.append(field.isMandatory() ? "||" : "o|")
		.append(field.getReferenceEntity().getEffectiveTableName()).append('\n');
	}
	
	private static void buildRelations(StringBuilder buf, Entity entity) {
		if (entity.hasAllRelations()) {
			entity.getAllRelations().forEach(relation -> buildRelation(buf, relation));
		}
	}
	
	private static void buildRelation(StringBuilder buf, EntityRelation relation) {
		buf.append(relation.getEntity().getEffectiveTableName()).append(" ||..o{ ")
		   .append(relation.getJoinTableName()).append('\n')
		   .append(relation.getJoinTableName()).append(" }o..|| ")
		   .append(relation.getRelatedEntity().getEffectiveTableName()).append('\n');
	}
	
	private static void buildField(StringBuilder buf, EntityField field) {
		if (field.isMandatory()) {
			buf.append('*');
		}
		buf.append(field.getEffectiveColumnName()).append(" : ")
		   .append(getType(field.getType()));
		if (field.getType().isAutonum()) {
			buf.append(" <<generated>>");
		}
		else if (field.getType().isReference() || field.getType().isFile()) {
			buf.append(" <<FK>>");
		}
		buf.append('\n');
	}
	
	private static void buildSystemField(StringBuilder buf, SystemField field) {
		buf.append(field.columName).append(" : ")
		   .append(getType(field.type)).append('\n');
	}
	
	private static String getType(FieldType type) {
		switch (type) {
			case AUTONUM:
			case TEXT:
			case TEXTLONG:
				return "text";
			case BINARY:
				return "blob";
			case BOOLEAN:
				return "boolean";
			case DATE:
			case DATETIME:
				return "date";
			case DECIMAL:
			case DOUBLE:
			case FILE:
			case INTEGER:
			case LONG:
			case REFERENCE:
				return "number";
			default:
				throw new UnsupportedOperationException(type.name());
		}
	}
	
	private static void buildHeader(StringBuilder buf) {
		buf.append("@startuml\n\n");
	}
	
	private static void buildFooter(StringBuilder buf) {
		buf.append("\n@enduml\n");
	}
	
}
