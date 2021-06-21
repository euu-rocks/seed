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

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.core.application.AbstractOrderedTransferableObject;

@javax.persistence.Entity
@Table(name = "sys_entity_statustran_func")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EntityStatusTransitionFunction extends AbstractOrderedTransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transition_id")
	private EntityStatusTransition statusTransition;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id")
	private EntityFunction function;
	
	private boolean isActiveBeforeTransition;
	
	private boolean isActiveAfterTransition;
	
	@Transient
	private String functionUid;
	
	@XmlTransient
	public EntityStatusTransition getStatusTransition() {
		return statusTransition;
	}
	
	public void setStatusTransition(EntityStatusTransition statusTransition) {
		this.statusTransition = statusTransition;
	}

	@XmlTransient
	public EntityFunction getFunction() {
		return function;
	}
	
	@XmlAttribute
	public String getFunctionUid() {
		return function != null ? function.getUid() : functionUid;
	}

	public void setFunctionUid(String functionUid) {
		this.functionUid = functionUid;
	}

	public void setFunction(EntityFunction function) {
		this.function = function;
	}
	
	@XmlAttribute
	public boolean isActiveBeforeTransition() {
		return isActiveBeforeTransition;
	}

	public void setActiveBeforeTransition(boolean isActiveBeforeTransition) {
		this.isActiveBeforeTransition = isActiveBeforeTransition;
	}
	
	@XmlAttribute
	public boolean isActiveAfterTransition() {
		return isActiveAfterTransition;
	}

	public void setActiveAfterTransition(boolean isActiveAfterTransition) {
		this.isActiveAfterTransition = isActiveAfterTransition;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !EntityStatusTransitionFunction.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final EntityStatusTransitionFunction otherFunction = (EntityStatusTransitionFunction) other;
		return new EqualsBuilder()
			.append(functionUid, otherFunction.getFunctionUid())
			.append(isActiveBeforeTransition, otherFunction.isActiveBeforeTransition)
			.append(isActiveAfterTransition, otherFunction.isActiveAfterTransition)
			.isEquals();
	}
	
}
