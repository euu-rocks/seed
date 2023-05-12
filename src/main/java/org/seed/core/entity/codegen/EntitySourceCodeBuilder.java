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
package org.seed.core.entity.codegen;

import static org.seed.core.util.CollectionUtils.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Formula;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import org.seed.C;
import org.seed.core.application.TransferableObject;
import org.seed.core.codegen.AbstractSourceCodeBuilder;
import org.seed.core.codegen.AnnotationMetadata;
import org.seed.core.codegen.ParameterMetadata;
import org.seed.core.codegen.SourceCode;
import org.seed.core.codegen.TypeClass;
import org.seed.core.data.SystemField;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityRelation;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.NestedEntity;
import org.seed.core.entity.value.AbstractValueObject;
import org.seed.core.entity.value.ValueEntity;
import org.seed.core.util.Assert;
import org.seed.core.util.ByteArrayJsonSerializer;
import org.seed.core.util.FileObjectJsonSerializer;
import org.seed.core.util.MiscUtils;
import org.seed.core.util.ReferenceJsonSerializer;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

class EntitySourceCodeBuilder extends AbstractSourceCodeBuilder {
	
	private final Entity entity;
	
	private String timeZone;
	
	private String formatRestDate;
	
	private String formatRestDateTime;
	
	EntitySourceCodeBuilder(Entity entity) {
		super (entity,			
			   entity.isGeneric(),					// abstract
			   getEntitySuperClass(entity), 		// super class
			   getEntityInterfaceTypes(entity),		// interfaces
			   getEntityAnnotations(entity));		// annotations
		this.entity = entity;
	}
	
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public void setFormatRestDate(String formatRestDate) {
		this.formatRestDate = formatRestDate;
	}

	public void setFormatRestDateTime(String formatRestDateTime) {
		this.formatRestDateTime = formatRestDateTime;
	}

	@Override
	public Date getLastModified() {
		Date timestamp = entity.getLastModified();
		
		// check referenced entities
		if (entity.hasFields()) {
			for (EntityField field : entity.getFields()) {
				if (field.getType().isReference() && 
					field.getReferenceEntity().getLastModified().after(timestamp)) {
					timestamp = field.getReferenceEntity().getLastModified();
				}
			}
		}
		
		// check nested entities
		if (entity.hasNesteds()) {
			timestamp = getNeestedsLastModified(timestamp);
		}
		
		// check related entities
		if (entity.hasAllRelations()) {
			timestamp = getRelationsLastModified(timestamp);
		}
		return timestamp;
	}
	
	@Override
	public SourceCode build(BuildMode buildMode) {
		Assert.state(buildMode == BuildMode.COMPLETE, "unsupported build mode: " + buildMode.name());
		
		// uid field
		if (entity.isTransferable()) {
			buildUidField();
		}
		
		// status field
		else if (entity.hasStatus()) {
			buildStatusField();
		}
		
		// fields
		if (entity.hasFields()) {
			buildFields();
		}
		
		// nesteds
		if (entity.hasNesteds()) {
			buildNesteds();
		}
		
		// relations
		if (!entity.isGeneric() && entity.hasAllRelations()) {
			buildRelations();
		}
		
		// entityId
		if (!entity.isGeneric()) {
			buildEntityIdGetter();
		}
		
		// getters / setters
		buildGetterAndSetter();

		// nested methods
		if (entity.hasNesteds()) {
			buildNestedMethods();
		}
		
		// relation methods
		if (!entity.isGeneric() && entity.hasAllRelations()) {
			buildRelationMethods();
		}
		
		// member functions
		if (entity.hasFunctions()) {
			entity.getMemberFunctions().forEach(function -> 
					addFunction(function.getContent(), newAnnotation(JsonIgnore.class)));
		}
		
		return super.build(true);
	}
	
	private void addFunction(String code, AnnotationMetadata ...annotations) {
		Assert.notNull(code, C.CODE);
		
		forEach(annotations, this::addAnnotation);
		try (Scanner scanner = new Scanner(code)) {
			boolean comment = false;
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine().trim();
				if (line.endsWith("*/")) {
					comment = false;
				}
				else if (!(comment || line.isEmpty() || line.startsWith("//"))) {
					if (line.startsWith("/*")) {
						comment = true;
					}	
					else if (line.startsWith("import ")) {
						addImport(newTypeClass(line.substring(7).replace(";","").trim()));
					}
					else {
						addCode(code.substring(scanner.match().start()));
						break;
					}
				}
			}
		}
	}
	
	private void buildStatusField() {
		addImport(ReferenceJsonSerializer.class);
		addMember(SystemField.ENTITYSTATUS.property, newTypeClass(EntityStatus.class), 
				  newAnnotation(JoinColumn.class, C.NAME, quote(SystemField.ENTITYSTATUS.columName)),
				  newAnnotation(ManyToOne.class, C.FETCH, FetchType.LAZY),
				  newAnnotation(JsonSerialize.class, "using", "ReferenceJsonSerializer.class"));
	}
	
	private void buildUidField() {
		if (entity.isAudited()) {
			addMember(SystemField.UID.property, newTypeClass(SystemField.UID.type.typeClass),
					  newAnnotation(NotAudited.class));
		}
		else {
			addMember(SystemField.UID.property, newTypeClass(SystemField.UID.type.typeClass));
		}
	}
	
	private void buildEntityIdGetter() {
		addMethod(newTypeClass(Long.class), "getEntityId", null, 
				  "return " + (entity.isNew() ? -1 : entity.getId()) + "L;" + LF,
				  newAnnotation(JsonIgnore.class));
	}
	
	private void buildFields() {
		for (EntityField field : entity.getFields()) {
			final TypeClass typeClass = field.getType().isReference()
											? newTypeClass(field.getReferenceEntity())
											: newTypeClass(field.getType().typeClass);
			final var annotations = getFieldAnnotations(field);
			addMember(field.getInternalName(), typeClass, 
					  annotations.isEmpty() 
					  	? null 
					  	: annotations.toArray(new AnnotationMetadata[annotations.size()]));
		}
	}
	
	private void buildGetterAndSetter() {
		if (entity.isTransferable()) {
			addGetterAndSetter(SystemField.UID.property);
		}
		else if (entity.hasStatus()) {
			addGetterAndSetter(SystemField.ENTITYSTATUS.property);
		}
		if (entity.hasFields()) {
			entity.getFields().forEach(field -> 
				addGetterAndSetter(field.getInternalName()));
		}
		if (entity.hasNesteds()) {
			entity.getNesteds().forEach(nested -> {
				if (nested.isReadonly()) {
					addGetter(nested.getInternalName());
				}
				else {
					addGetterAndSetter(nested.getInternalName());
				}
			});
		}
		if (!entity.isGeneric() && entity.hasAllRelations()) {
			entity.getAllRelations().forEach(relation -> 
				addGetterAndSetter(relation.getInternalName()));
		}
	}
	
	private void buildNesteds() {
		for (NestedEntity nested : entity.getNesteds()) {
			final var annotationParamMap = new HashMap<String, Object>(8);
			annotationParamMap.put("mappedBy", quote(nested.getReferenceField().getInternalName()));
			annotationParamMap.put(C.CASCADE, CascadeType.ALL);
			annotationParamMap.put(C.FETCH, FetchType.LAZY);
			annotationParamMap.put("orphanRemoval", true);
			addMember(nested.getInternalName(), 
					  newTypeClass(nested.getNestedEntity(), List.class), 
					  newAnnotation(OneToMany.class, annotationParamMap));
		}
	}
	
	private void buildRelations() {
		final var annotationParamMapM2M = new HashMap<String, Object>();
		annotationParamMapM2M.put(C.FETCH, FetchType.LAZY);
		annotationParamMapM2M.put(C.CASCADE, CascadeType.ALL);
		
		for (EntityRelation relation : entity.getAllRelations()) {
			final EntityRelation descendantRelation = relation.createDescendantRelation(entity);
			final var joinColumns = new AnnotationMetadata[] {
				createJoinColumnAnnotation(descendantRelation.getJoinColumnName())
			};
			final var inverseJoinColumns = new AnnotationMetadata[] {
				createJoinColumnAnnotation(relation.getInverseJoinColumnName())	
			};
			
			final var annotationParamMapJoin = new HashMap<String, Object>(8);
			annotationParamMapJoin.put(C.NAME, quote(descendantRelation.getJoinTableName()));
			annotationParamMapJoin.put("joinColumns", joinColumns);
			annotationParamMapJoin.put("inverseJoinColumns", inverseJoinColumns);
			addMember(relation.getInternalName(), 
					  newTypeClass(relation.getRelatedEntity(), Set.class), 
					  newAnnotation(ManyToMany.class, annotationParamMapM2M),
					  newAnnotation(JoinTable.class, annotationParamMapJoin));
		}
	}
	
	private void buildNestedMethods() {
		for (NestedEntity nested : subList(entity.getNesteds(), not(NestedEntity::isReadonly))) {
			addImport(ArrayList.class);
			final String nestedName = nested.getInternalName();
			final var parameters = new ParameterMetadata[] { 
				newParameter(nestedName, newTypeClass(nested.getNestedEntity())) 
			}; 
			
			// add 
			final var buf = new StringBuilder().append(nestedName).append(".set")
			   .append(StringUtils.capitalize(nested.getReferenceField().getInternalName()))
			   .append("(this);").append(LF) 
			   .append("\t\tif (this.").append(nestedName).append(" == null) {").append(LF)
			   .append("\t\t\tthis.").append(nestedName).append(" = new ArrayList<>();").append(LF)
			   .append("\t\t}").append(LF)
			   .append("\t\tthis.").append(nestedName).append(".add(").append(nestedName).append(");").append(LF);
			addMethod(null, "add" + StringUtils.capitalize(nestedName), parameters, buf.toString());
			
			// remove
			addMethod(null, "remove" + StringUtils.capitalize(nestedName), parameters,
					  "this." + nestedName + ".remove(" + nestedName + ");" + LF);
		}
	}
	
	private void buildRelationMethods() {
		for (EntityRelation relation : entity.getAllRelations()) {
			addImport(HashSet.class);
			final String relationName = relation.getInternalName();
			final var parameters = new ParameterMetadata[] { 
				newParameter(relationName, newTypeClass(relation.getRelatedEntity())) 
			};
			
			// add
			final var buf = new StringBuilder()
				.append("if (this.").append(relationName).append(" == null) {").append(LF)
				.append("\t\t\tthis.").append(relationName).append(" = new HashSet<>();").append(LF)
				.append("\t\t}").append(LF)
				.append("\t\tthis.").append(relationName).append(".add(").append(relationName).append(");").append(LF);
			addMethod(null, "add" + StringUtils.capitalize(relationName), parameters, buf.toString());
			
			// remove
			addMethod(null, "remove" + StringUtils.capitalize(relationName), parameters,
					  "this." + relationName + ".remove(" + relationName + ");" + LF);
		}
	}
	
	private Date getNeestedsLastModified(Date timestamp) {
		for (NestedEntity nested : entity.getNesteds()) {
			final Entity entityNested = nested.getNestedEntity();
			if (entityNested.getLastModified().after(timestamp)) {
				timestamp = entityNested.getLastModified();
			}
			// check nested referenced entities
			if (entityNested.hasFields()) {
				for (EntityField field : entityNested.getFields()) {
					if (field.getType().isReference() && 
						field.getReferenceEntity().getLastModified().after(timestamp)) {
						timestamp = field.getReferenceEntity().getLastModified();
					}
				}
			}
		}
		return timestamp;
	}
	
	private Date getRelationsLastModified(Date timestamp) {
		for (EntityRelation relation : entity.getAllRelations()) {
			final Entity relatedEntity = relation.getRelatedEntity();
			if (relatedEntity.getLastModified().after(timestamp)) {
				timestamp = relatedEntity.getLastModified();
			}
		}
		return timestamp;
	}
	
	private static AnnotationMetadata createJoinColumnAnnotation(String name) {
		final var annotationParamMap = new HashMap<String, Object>(4, 1f);
		annotationParamMap.put(C.NAME, quote(name));
		annotationParamMap.put("nullable", false);
		annotationParamMap.put("updatable", false);
		return newAnnotation(JoinColumn.class, annotationParamMap);
	}
	
	private static TypeClass getEntitySuperClass(Entity entity) {
		return entity.getGenericEntity() != null 
			   	? newTypeClass(entity.getGenericEntity()) 
			   	: newTypeClass(AbstractValueObject.class);
	}
	
	private static TypeClass[] getEntityInterfaceTypes(Entity entity) {
		if (entity.isGeneric()) {
			return MiscUtils.toArray();
		}
		final var interfaces = new ArrayList<TypeClass>(2);
		interfaces.add(newTypeClass(ValueEntity.class));
		if (entity.isTransferable()) {
			interfaces.add(newTypeClass(TransferableObject.class));
		}
		return interfaces.toArray(new TypeClass[interfaces.size()]);
	}
	
	private static AnnotationMetadata[] getEntityAnnotations(Entity entity) {
		final var annotations = new ArrayList<AnnotationMetadata>(4);
		if (entity.isAudited()) {
			annotations.add(newAnnotation(Audited.class, "targetAuditMode", RelationTargetAuditMode.NOT_AUDITED));
		}
		if (entity.isGeneric()) {
			annotations.add(newAnnotation(MappedSuperclass.class));
		}
		else {
			if (entity.isTransferable()) {
				annotations.add(newAnnotation(Cache.class, "usage", CacheConcurrencyStrategy.READ_WRITE));
			}
			if (entity.getTableName() != null) {
				annotations.add(newAnnotation(Table.class, C.NAME, quote(entity.getTableName())));
			}
			annotations.add(newAnnotation(javax.persistence.Entity.class));
		}
		return annotations.toArray(new AnnotationMetadata[annotations.size()]);
	}
	
	private List<AnnotationMetadata> getFieldAnnotations(EntityField field) {
		final var annotations = new ArrayList<AnnotationMetadata>(8);
		if (field.getColumnName() != null) {
			annotations.add(newAnnotation(Column.class, C.NAME, quote(field.getColumnName().toLowerCase())));
		}
		if (field.isCalculated()) {
			annotations.add(newAnnotation(Formula.class, field.getFormula()));
		}
		
		Map<String, Object> annotationParamMap;
		switch (field.getType()) {
			case BINARY:
				addImport(ByteArrayJsonSerializer.class);
				annotations.add(newAnnotation(JsonSerialize.class, "using", "ByteArrayJsonSerializer.class"));
				break;
				
			case DATE:
				annotationParamMap = new HashMap<>(4);
				annotationParamMap.put("pattern", quote(formatRestDate));
				annotationParamMap.put("timezone", quote(timeZone));
				annotations.add(newAnnotation(JsonFormat.class, annotationParamMap));
				break;
				
			case DATETIME:
				annotationParamMap = new HashMap<>(4);
				annotationParamMap.put("pattern", quote(formatRestDateTime));
				annotationParamMap.put("timezone", quote(timeZone));
				annotations.add(newAnnotation(JsonFormat.class, annotationParamMap));
				break;
				
			case FILE:
				addImport(FileObjectJsonSerializer.class);
				annotationParamMap = new HashMap<>(4);
				annotationParamMap.put(C.FETCH, FetchType.LAZY);
				annotationParamMap.put(C.CASCADE, CascadeType.ALL);
				annotations.add(newAnnotation(ManyToOne.class, annotationParamMap));
				annotations.add(newAnnotation(JoinColumn.class, C.NAME, quote(field.getInternalName())));
				annotations.add(newAnnotation(JsonSerialize.class, "using", "FileObjectJsonSerializer.class"));
				break;
				
			case REFERENCE:
				addImport(ReferenceJsonSerializer.class);
				annotations.add(newAnnotation(ManyToOne.class, C.FETCH, FetchType.LAZY));
				annotations.add(newAnnotation(JoinColumn.class, C.NAME, quote(field.getInternalName())));
				annotations.add(newAnnotation(JsonSerialize.class, "using", "ReferenceJsonSerializer.class"));
				break;
				
			default:	// no annotation
		}
		return annotations;
	}

}
