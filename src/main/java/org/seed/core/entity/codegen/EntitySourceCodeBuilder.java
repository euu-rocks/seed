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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Formula;

import org.seed.core.codegen.AbstractSourceCodeBuilder;
import org.seed.core.codegen.AnnotationMetadata;
import org.seed.core.codegen.ParameterMetadata;
import org.seed.core.codegen.SourceCode;
import org.seed.core.codegen.TypeClass;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.NestedEntity;
import org.seed.core.entity.value.AbstractValueObject;
import org.seed.core.entity.value.ValueEntity;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

class EntitySourceCodeBuilder extends AbstractSourceCodeBuilder<Entity> {
	
	private final Entity entity;
	
	EntitySourceCodeBuilder(Entity entity) {
		super (entity,			
			   entity.isGeneric(),					// abstract
			   entity.getGenericEntity() != null 	// super class
			   	? newTypeClass(entity.getGenericEntity()) 
			   	: newTypeClass(AbstractValueObject.class), 
			   entity.isGeneric() 					// interface classes
			    ? null 
			    : new TypeClass[]{ newTypeClass(ValueEntity.class) },
			   getEntityAnnotations(entity));
		this.entity = entity;
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
		}
		return timestamp;
	}
	
	@Override
	public SourceCode<Entity> build(BuildMode buildMode) {
		Assert.state(buildMode == BuildMode.COMPLETE, "unsupported build mode: " + buildMode.name());
		
		// status field
		if (entity.hasStatus()) {
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
		
		// entityId
		buildEntityIdGetter();
		
		// getters / setters
		if (entity.hasStatus()) {
			addGetterAndSetter("entityStatus");
		}
		if (entity.hasFields()) {
			for (EntityField field : entity.getFields()) {
				addGetterAndSetter(field.getInternalName());
			}
		}
		if (entity.hasNesteds()) {
			for (NestedEntity nested : entity.getNesteds()) {
				addGetterAndSetter(nested.getInternalName());
			}
		}
		
		// nested methods
		if (entity.hasNesteds()) {
			buildNestedMethods();
		}
		
		// member functions
		if (entity.hasFunctions()) {
			for (EntityFunction function : entity.getMemberFunctions()) {
				addFunction(function.getContent(), newAnnotation(JsonIgnore.class));
			}
		}
		
		return super.build(true);
	}
	
	private void addFunction(String code, AnnotationMetadata ...annotations) {
		Assert.notNull(code, "code is null");
		
		if (annotations != null) {
			for (AnnotationMetadata annotation : annotations) {
				addAnnotation(annotation);
			}
		}
		try (Scanner scanner = new Scanner(code)) {
			boolean comment = false;
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine().trim();
				// ignore comments
				if (line.endsWith("*/")) {
					comment = false;
					continue;
				}
				if (comment || line.isEmpty() || line.startsWith("//")) {
					continue;
				}
				if (line.startsWith("/*")) {
					comment = true;
				}
				// imports
				else if (line.startsWith("import ")) {
					addImport(newTypeClass(line.substring(7).replace(";","").trim()));
				}
				// code
				else {
					addCode(code.substring(scanner.match().start()));
					break;
				}
			}
		}
	}
	
	private void buildStatusField() {
		addMember("entityStatus", newTypeClass(EntityStatus.class), 
				  newAnnotation(JoinColumn.class, "name", quote("status_id")),
				  newAnnotation(ManyToOne.class, "fetch", FetchType.LAZY));
	}
	
	private void buildEntityIdGetter() {
		addMethod(newTypeClass(Long.class), "getEntityId", null, 
				  "return " + (entity.isNew() ? -1 : entity.getId()) + "L;" + LF);
	}
	
	private void buildFields() {
		for (EntityField field : entity.getFields()) {
			TypeClass typeClass = newTypeClass(field.getType().typeClass); 
			final List<AnnotationMetadata> annotations = new ArrayList<>(4);
			if (field.getColumnName() != null) {
				annotations.add(newAnnotation(Column.class, "name", quote(field.getColumnName().toLowerCase())));
			}
			if (field.isCalculated()) {
				annotations.add(newAnnotation(Formula.class, field.getFormula()));
			}
			else if (field.getType().isReference() || field.getType().isFile()) {
				final Map<String, Object> annotationParamMap = new HashMap<>(4);
				annotationParamMap.put("fetch", FetchType.LAZY);
				if (field.getType().isFile()) {
					annotationParamMap.put("cascade", CascadeType.ALL);
				}
				annotations.add(newAnnotation(ManyToOne.class, annotationParamMap));
				annotations.add(newAnnotation(JoinColumn.class, "name", quote(field.getInternalName())));
				if (field.getType().isReference()) {
					typeClass = newTypeClass(field.getReferenceEntity());
				}
			}
			addMember(field.getInternalName(), typeClass, 
					  annotations.isEmpty() 
					  	? null 
					  	: annotations.toArray(new AnnotationMetadata[annotations.size()]));
		}
	}
	
	private void buildNesteds() {
		for (NestedEntity nested : entity.getNesteds()) {
			final Map<String, Object> annotationParamMap = new HashMap<>(8);
			annotationParamMap.put("mappedBy", quote(nested.getReferenceField().getInternalName()));
			annotationParamMap.put("cascade", CascadeType.ALL);
			annotationParamMap.put("fetch", FetchType.LAZY);
			annotationParamMap.put("orphanRemoval", true);
			addMember(nested.getInternalName(), 
					  newTypeClass(nested.getNestedEntity(), List.class), 
					  newAnnotation(OneToMany.class, annotationParamMap));
		}
	}
	
	private void buildNestedMethods() {
		addImport(ArrayList.class);
		for (NestedEntity nested : entity.getNesteds()) {
			final String nestedName = nested.getInternalName();
			final ParameterMetadata[] parameters = new ParameterMetadata[] { 
				newParameter(nestedName, newTypeClass(nested.getNestedEntity())) 
			}; 
			
			// add 
			final StringBuilder buf = new StringBuilder().append(nestedName).append(".set")
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
	
	private static AnnotationMetadata[] getEntityAnnotations(Entity entity) {
		final List<AnnotationMetadata> annotations = new ArrayList<>(2);
		if (entity.isGeneric()) {
			annotations.add(newAnnotation(MappedSuperclass.class));
		}
		else {
			if (entity.getTableName() != null) {
				annotations.add(newAnnotation(Table.class, "name", quote(entity.getTableName())));
			}
			annotations.add(newAnnotation(javax.persistence.Entity.class));
		}
		return annotations.toArray(new AnnotationMetadata[annotations.size()]);
	}

}
