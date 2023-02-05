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

import static org.seed.core.util.CollectionUtils.subList;

import java.util.List;

import org.hibernate.Session;

import org.seed.core.data.AbstractSystemEntityRepository;

import org.springframework.stereotype.Repository;

@Repository
public class EntityRepository extends AbstractSystemEntityRepository<Entity> {

	public EntityRepository() {
		super(EntityMetadata.class);
	}
	
	@Override
	public Session getSession() {
		return super.getSession();
	}
	
	boolean areColumnValuesUnique(Entity entity, EntityField field, Session session) {
		return session.createSQLQuery(
				String.format("select %s from %s where %s is not null group by %s having count(*) > 1 limit 1",
							  field.getEffectiveColumnName(), entity.getEffectiveTableName(), 
							  field.getEffectiveColumnName(), field.getEffectiveColumnName()))
					  .list().isEmpty();
	}
	
	int countEmptyColumnValues(Entity entity, EntityField field, Session session) {
		return ((Number) session.createSQLQuery(
				String.format("select count(*) from %s where %s is null",
							  entity.getEffectiveTableName(), field.getEffectiveColumnName()))
								.getSingleResult()).intValue();
	}
	
	int countColumnValue(Entity entity, EntityField field, Object value, Session session) {
		return ((Number) session.createSQLQuery(
				String.format("select count(*) from %s where %s = (:value)",
							  entity.getEffectiveTableName(), field.getEffectiveColumnName()))
								.setParameter("value", value)
								.getSingleResult()).intValue();
	}
	
	boolean testFormula(Entity entity, String formula) {
		try (Session session = getSession()) {
			session.createSQLQuery(
				String.format("select %s from %s limit 1", 
							  formula, entity.getEffectiveTableName())).list();
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}
	
	List<Entity> findParentEntities(Entity entity) {
		return subList(find(), parent -> parent.isNestedEntity(entity));
	}
	
	List<Entity> findParentEntities(Entity entity, Session session) {
		return subList(find(session), parent -> parent.isNestedEntity(entity));
	}
	
}
