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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "sys_subform_action")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SubFormAction extends AbstractFormAction {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subform_id")
	private SubForm subForm;
	
	@XmlTransient
	public SubForm getSubForm() {
		return subForm;
	}

	public void setSubForm(SubForm subForm) {
		this.subForm = subForm;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !SubFormAction.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final SubFormAction otherAction = (SubFormAction) other;
		return new EqualsBuilder()
				.append(getEntityFunctionUid(), otherAction.getEntityFunctionUid())
				.append(getType(), otherAction.getType())
				.append(getLabel(), otherAction.getLabel())
				.isEquals();
	}
	
}