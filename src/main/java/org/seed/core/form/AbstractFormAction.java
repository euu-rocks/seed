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
package org.seed.core.form;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.seed.Seed;
import org.seed.core.application.AbstractOrderedTransferableObject;
import org.seed.core.entity.EntityFunction;
import org.seed.core.util.NameUtils;

@MappedSuperclass
public abstract class AbstractFormAction extends AbstractOrderedTransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_function_id")
	private EntityFunction entityFunction;
	
	private String label;
	
	private FormActionType type;
	
	@Transient
	private String entityFunctionUid;
	
	@XmlTransient
	public EntityFunction getEntityFunction() {
		return entityFunction;
	}

	public void setEntityFunction(EntityFunction entityFunction) {
		this.entityFunction = entityFunction;
	}
	
	@XmlAttribute
	public String getEntityFunctionUid() {
		return entityFunction != null ? entityFunction.getUid() : entityFunctionUid;
	}

	public void setEntityFunctionUid(String entityFunctionUid) {
		this.entityFunctionUid = entityFunctionUid;
	}
	
	@XmlAttribute
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	@XmlAttribute
	public FormActionType getType() {
		return type;
	}

	public void setType(FormActionType type) {
		this.type = type;
	}
	
	public String getInternalName() {
		return label != null 
				? NameUtils.getInternalName(label)
				: type.name().toLowerCase();
	}
	
	public String getTestClass() {
		return NameUtils.getInternalName(Seed.getLabel("button." + type.name().toLowerCase()))
				.replace('_','-').toLowerCase() + "-button";
	}
	
	public boolean isCustom() {
		return type == FormActionType.CUSTOM;
	}
	
}
