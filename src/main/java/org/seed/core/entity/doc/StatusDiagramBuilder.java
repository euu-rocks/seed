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

import org.seed.C;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.EntityStatusTransition;
import org.seed.core.util.Assert;

import org.springframework.util.StringUtils;

class StatusDiagramBuilder extends AbstractPlantUMLBuilder {
	
	private final Entity entity;
	
	StatusDiagramBuilder(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		this.entity = entity;
	}

	@Override
	protected void build(StringBuilder buf) {
		if (entity.hasStatus()) {
			entity.getStatusList().forEach(status -> buildStatus(buf, status));
		}
		if (entity.hasStatusTransitions()) {
			entity.getStatusTransitions().forEach(transition -> buildStatusTransition(buf, transition));
		}
	}
	
	private static void buildStatus(StringBuilder buf, EntityStatus status) {
		buf.append("state ");
		buildStatusName(buf, status);
		buf.append('\n');
		if (StringUtils.hasText(status.getDescription())) {
			buildStatusName(buf, status);
			buf.append(COLON).append(status.getDescription()).append('\n');
		}
		if (status.isInitial()) {
			buf.append("[*] --> ");
			buildStatusName(buf, status);
			buf.append('\n');
		}
		buf.append('\n');
	}
	
	private static void buildStatusTransition(StringBuilder buf, EntityStatusTransition transition) {
		buildStatusName(buf, transition.getSourceStatus());
		buf.append(" --> ");
		buildStatusName(buf, transition.getTargetStatus());
		buf.append('\n');
		if (StringUtils.hasText(transition.getDescription())) {
			buf.append("note on link\n").append(transition.getDescription())
			   .append("\nend note\n");
		}
		buf.append('\n');
	}
	
	private static void buildStatusName(StringBuilder buf, EntityStatus status) {
		buf.append(status.getStatusNumber()).append('_').append(status.getName().replace(' ', '_'));
	}

}
