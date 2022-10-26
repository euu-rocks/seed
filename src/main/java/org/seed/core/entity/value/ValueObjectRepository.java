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
package org.seed.core.entity.value;

import static org.seed.core.util.CollectionUtils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import org.seed.C;
import org.seed.InternalException;
import org.seed.core.api.CallbackEventType;
import org.seed.core.codegen.CodeManager;
import org.seed.core.config.SessionProvider;
import org.seed.core.data.QueryCursor;
import org.seed.core.data.FieldType;
import org.seed.core.data.FileObject;
import org.seed.core.data.Sort;
import org.seed.core.data.SystemField;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityRelation;
import org.seed.core.entity.EntityRepository;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.EntityStatusTransition;
import org.seed.core.entity.NestedEntity;
import org.seed.core.entity.autonum.AutonumberService;
import org.seed.core.entity.filter.CriterionOperator;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.filter.FilterCriterion;
import org.seed.core.entity.value.event.ValueObjectEvent;
import org.seed.core.entity.value.event.ValueObjectEventHandler;
import org.seed.core.entity.value.event.ValueObjectFunctionContext;
import org.seed.core.util.Assert;
import org.seed.core.util.BeanUtils;
import org.seed.core.util.MiscUtils;
import org.seed.core.util.UID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@Repository
public class ValueObjectRepository {
	
	public static final int DEFAULT_CHUNK_SIZE = 50;
	
	@Autowired
	private SessionProvider sessionProvider;
	
	@Autowired
	private EntityRepository entityRepository;
	
	@Autowired
	private AutonumberService autonumService;
	
	@Autowired
	private ValueObjectEventHandler eventHandler;
	
	@Autowired
	private ValueObjectAccess objectAccess;
	
	@Autowired
	private CodeManager codeManager;
	
	ValueObject get(Session session, Entity entity, Long id) {
		Assert.notNull(entity, C.ENTITY);
		checkGeneric(entity);
		
		return get(session, getEntityClass(session, entity), id);
	}
	
	ValueObject get(Session session, Class<?> entityClass, Long id) {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(entityClass, C.ENTITYCLASS);
		Assert.notNull(id, C.ID);
		
		return (ValueObject) session.get(entityClass, id);
	}
	
	ValueObject createInstance(Entity entity, Session session, ValueObjectFunctionContext functionContext) {
		Assert.notNull(entity, C.ENTITY);
		checkGeneric(entity);
		checkSessionAndContext(session, functionContext);
		try {
			final Class<?> entityClass = getEntityClass(session != null ? session : functionContext.getSession(), entity);
			final AbstractValueObject object = (AbstractValueObject) BeanUtils.instantiate(entityClass);
			if (entity.hasStatus()) {
				object.setEntityStatus(entity.getInitialStatus());
			}
			fireEvent(CallbackEventType.CREATE, object, session, functionContext);
			return object;
		}
		catch (Exception ex) {
			throw new InternalException(ex);
		}
	}
	
	boolean notifyChange(ValueObject object) {
		Assert.notNull(object, C.OBJECT);
		
		try (Session session = getSession()) {
			if (anyMatch(getEntity(session, object).getFunctions(), EntityFunction::isActiveOnModify)) {
				Transaction tx = null;
				try {
					tx = session.beginTransaction();
					fireEvent(CallbackEventType.MODIFY, object, session);
					tx.commit();
					return true;
				}
				catch (Exception ex) {
					if (tx != null) {
						tx.rollback();
					}
					throw ex;
				}
			}
		}
		return false;
	}
	
	long count(Session session, Class<?> entityClass) {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(entityClass, C.ENTITYCLASS);
		
		final CriteriaBuilder builder = session.getCriteriaBuilder();
		final CriteriaQuery<Long> query = builder.createQuery(Long.class);
		query.select(builder.count(query.from(entityClass)));
		return session.createQuery(query).getSingleResult();
	}
	
	List<ValueObject> findAll(Session session, Entity entity) {
		return findAll(session, getEntityClass(session, entity));
	}
	
	@SuppressWarnings("unchecked")
	List<ValueObject> findAll(Session session, Class<?> entityClass) {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(entityClass, C.ENTITYCLASS);
		
		final CriteriaQuery<ValueObject> query = (CriteriaQuery<ValueObject>) 
				session.getCriteriaBuilder().createQuery(entityClass);
		return find(session, query.select((Selection<? extends ValueObject>) query.from(entityClass)));
	}
	
	@SuppressWarnings("unchecked")
	List<ValueObject> find(Session session, Entity entity, Filter filter, Sort ...sorts) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(filter, C.FILTER);
		checkFilter(entity, filter);
		
		if (filter.getHqlQuery() != null) {
			return session.createQuery(filter.getHqlQuery())
						  .setCacheable(true)
						  .list();
		}
		return find(session, buildQuery(session, entity, filter, sorts));
	}
	
	List<ValueObject> find(Session session, CriteriaQuery<ValueObject> query) {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(query, "query");
		
		return session.createQuery(query)
					  .setCacheable(true)
					  .getResultList();
	}
	
	List<ValueObject> findByIds(Session session, Class<?> entityClass, Long ...ids) {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(entityClass, C.ENTITYCLASS);
		Assert.notNull(ids, "ids");
		
		return MiscUtils.castList(session.byMultipleIds(entityClass).multiLoad(ids));
	}
	
	List<ValueObject> findByIds(Session session, Class<?> entityClass, List<Long> idList) {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(entityClass, C.ENTITYCLASS);
		Assert.notNull(idList, "id list");
		
		return MiscUtils.castList(session.byMultipleIds(entityClass).multiLoad(idList));
	}
	
	boolean exist(Entity entity, @Nullable Filter filter) {
		try (Session session = getSession()) {
			return exist(session, entity, filter);
		}
	}
	
	boolean exist(Session session, Entity entity, @Nullable Filter filter) {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(entity, C.ENTITY);
		if (filter != null) {
			checkFilter(entity, filter);
		}
		return !session.createQuery(buildQuery(session, entity, filter))
					   .setCacheable(true)
					   .setMaxResults(1)
					   .getResultList().isEmpty();
	}
	
	ValueObject findUnique(Entity entity, Filter filter) {
		try (Session session = getSession()) {
			return findUnique(session, entity, filter);
		}
	}
	
	ValueObject findUnique(Session session, Entity entity, Filter filter) {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(filter, C.FILTER);
		checkFilter(entity, filter);
		
		return findUnique(session, buildQuery(session, entity, filter));
	}
	
	ValueObject findUnique(Session session, CriteriaQuery<ValueObject> query) {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(query, "query");
		
		return session.createQuery(query)
					  .setCacheable(true)
					  .setMaxResults(2)
					  .uniqueResult();
	}
	
	void delete(ValueObject object, Session session, ValueObjectFunctionContext functionContext) {
		Assert.notNull(object, C.OBJECT);
		checkSessionAndContext(session, functionContext);
		
		// fire before-event
		fireEvent(CallbackEventType.BEFOREDELETE, object, session, functionContext);
		
		// delete object
		final Session localSession = functionContext != null ? functionContext.getSession() : session;
		if (localSession != null) {
			localSession.delete(object);
		}
		
		// delete orphan files
		for (EntityField fileField : getEntity(localSession, object).getAllFieldsByType(FieldType.FILE)) {
			final FileObject file = (FileObject) objectAccess.getValue(object, fileField);
			if (localSession != null && file != null && !file.isNew()) {
				localSession.delete(file);
			}
		}
		
		// fire after-event
		fireEvent(CallbackEventType.AFTERDELETE, object, session, functionContext);
	}
	
	void save(ValueObject object, Session session, ValueObjectFunctionContext functionContext) {
		Assert.notNull(object, C.OBJECT);
		checkSessionAndContext(session, functionContext);
		
		final Session localSession = functionContext != null ? functionContext.getSession() : session;
		final Entity entity = getEntity(localSession, object);
		final boolean isInsert = object.isNew();
		if (isInsert) {
			if (entity.isTransferable()) { 
				initUid(object);
			}
			setAutonumValuesInsert(entity, object, localSession);
		}
		else {
			setAutonumValuesUpdate(entity, object, localSession);
		}
		
		// fire before-event
		final CallbackEventType eventTypeBefore = isInsert 
				? CallbackEventType.BEFOREINSERT 
				: CallbackEventType.BEFOREUPDATE;
		fireEvent(eventTypeBefore, object, session, functionContext);
		
		// save object
		localSession.saveOrUpdate(object);
		
		// fire after-event
		final CallbackEventType eventTypeAfter = isInsert 
				? CallbackEventType.AFTERINSERT 
				: CallbackEventType.AFTERUPDATE;
		fireEvent(eventTypeAfter, object, session, functionContext);
	}
	
	protected CriteriaQuery<Long> buildCountQuery(Session session, Entity entity, @Nullable Filter filter) {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(entity, C.ENTITY);
		
		final CriteriaBuilder builder = session.getCriteriaBuilder();
		final CriteriaQuery<Long> query = createQuery(builder, entity, session);
		return query.select(builder.count(buildQuery(builder, entity, session, filter, query)));
	}
	
	protected CriteriaQuery<ValueObject> buildQuery(Session session, Entity entity, @Nullable Filter filter, Sort ...sorts) {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(entity, C.ENTITY);
		
		final CriteriaBuilder builder = session.getCriteriaBuilder();
		final CriteriaQuery<ValueObject> query = createQuery(builder, entity, session);
		return query.select(buildQuery(builder, entity, session, filter, query, sorts));
	}
	
	protected CriteriaQuery<Long> buildCountQuery(Session session, ValueObject searchObject, 
												  Map<Long, Map<String, CriterionOperator>> criteriaMap) {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(searchObject, "searchObject");
		
		final Entity entity = getEntity(searchObject.getEntityId(), session);
		final CriteriaBuilder builder = session.getCriteriaBuilder();
		final CriteriaQuery<Long> query = createQuery(builder, entity, session);
		return query.select(builder.count(buildQuery(builder, entity, session, searchObject, query, criteriaMap)));
	}
	
	protected CriteriaQuery<ValueObject> buildQuery(Session session, ValueObject searchObject, 
													Map<Long, Map<String, CriterionOperator>> criteriaMap, Sort ...sorts) {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(searchObject, "searchObject");
		
		final Entity entity = getEntity(searchObject.getEntityId(), session);
		final CriteriaBuilder builder = session.getCriteriaBuilder();
		final CriteriaQuery<ValueObject> query = createQuery(builder, entity, session);
		return query.select(buildQuery(builder, entity, session, searchObject, query, criteriaMap, sorts));
	}
	
	protected void changeStatus(ValueObject object, EntityStatus targetStatus, Session session) {
		Assert.notNull(object, C.OBJECT);
		Assert.notNull(session, C.SESSION);
		Assert.notNull(targetStatus, "targetStatus");
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			changeStatus(object, targetStatus, session, null);
			tx.commit();
		}
		catch (Exception ex) {
			if (tx != null) {
				tx.rollback();
			}
			throw ex;
		}
	}
	
	protected void changeStatus(ValueObject object, EntityStatus targetStatus,
							 	Session session, ValueObjectFunctionContext functionContext) {
		Assert.notNull(object, C.OBJECT);
		Assert.notNull(targetStatus, "targetStatus");
		checkSessionAndContext(session, functionContext);
		
		final Entity entity = getEntity(session != null ? session : functionContext.getSession(), object);
		final EntityStatusTransition statusTransition = entity.getStatusTransition(object.getEntityStatus(), targetStatus);
		if (statusTransition != null) {
			// fire before-event
			fireEvent(CallbackEventType.BEFORETRANSITION, object, statusTransition, session, functionContext);
			
			// change state
			((AbstractValueObject) object).setEntityStatus(targetStatus);
			save(object, session, functionContext);
			
			// fire after-event
			fireEvent(CallbackEventType.AFTERTRANSITION, object, statusTransition, session, functionContext);
		}
		else {
			throw new IllegalStateException("transition not found: " + 
						object.getEntityStatus().getStatusNumber() + 
						" - " + 
						targetStatus.getStatusNumber());
		}
	}
	
	protected Session getSession() {
		return sessionProvider.getSession();
	}
	
	protected <T> T querySingleResult(Session session, CriteriaQuery<T> query) {
		return session.createQuery(query)
					  .setCacheable(true)
					  .getSingleResult();
	}
	
	protected Entity getEntity(ValueObject object) {
		Assert.notNull(object, C.OBJECT);
		
		return getEntity(object.getEntityId());
	}
	
	protected Entity getEntity(Session session, ValueObject object) {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(object, C.OBJECT);
		
		return getEntity(object.getEntityId(), session);
	}
	
	protected Entity getEntity(Long entityId) {
		return getEntity(entityId, null);
	}
	
	protected Entity getEntity(Long entityId, @Nullable Session session) {
		Assert.notNull(entityId, "entityId");
		
		final Entity entity = session != null 
				? entityRepository.get(entityId, session)
				: entityRepository.get(entityId);
		Assert.state(entity != null, "entity not available id:" + entityId);
		return entity;
	}
	
	protected String getIdentifier(ValueObject object) {
		return getIdentifier(object, null);
	}
	
	protected String getIdentifier(ValueObject object, Session session) {
		Assert.notNull(object, C.OBJECT);
		
		final Entity entity = session != null 
								? getEntity(object.getEntityId(), session)
								: getEntity(object);
		if (entity.getIdentifierPattern() != null) {
			return resolveIdentifierPattern(entity, object);
		}
		
		// fallback if no pattern exist
		final EntityField defaultField = entity.findDefaultIdentifierField();
		Object value = null;
		if (defaultField != null) {
			value = objectAccess.getValue(object, defaultField);
		}
		if (value != null) {
			return value.toString();
		}
		else {
			return entity.getName() + " (" + object.getId() + ')';
		}
	}
	
	private String resolveIdentifierPattern(Entity entity, ValueObject object) {
		String pattern = entity.getIdentifierPattern();
		pattern = pattern.replace("{entity}", entity.getName());
		if (entity.hasAllFields()) {
			for (EntityField field : entity.getAllFields()) {
				final String key = '{' + field.getName() + '}';
				if (pattern.contains(key)) {
					Object value = objectAccess.getValue(object, field);
					if (value != null && field.getType().isReference()) {
						value = getIdentifier((ValueObject) value);
					}
					// replace key -> value
					pattern = pattern.replace(key, value != null ? value.toString() : "");
				}
			}
		}
		return pattern;
	}
	
	protected Class<?> getEntityClass(Session session, Entity entity) {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(entity, C.ENTITY);
		Assert.state(!entity.isNew(), "entity is new");
		
		// entity reload is necessary because entity could be renamed but not saved yet
		entity = getEntity(entity.getId(), session);
		final Class<?> entityClass = codeManager.getGeneratedClass(entity);
		Assert.stateAvailable(entityClass, "class for entity: " + entity.getName());
		return entityClass;
	}
	
	private static Set<NestedEntity> getNestedEntities(Filter filter) {
		Assert.notNull(filter, C.FILTER);
		
		if (filter.hasCriteria()) {
			return filter.getCriteria().stream()
						 .filter(criterion -> criterion.getEntityField() != null && 
								 !criterion.getEntityField().getEntity().equals(filter.getEntity()))
						 .map(criterion -> filter.getEntity().getNestedByEntityId(criterion.getEntityField().getEntity().getId()))
						 .collect(Collectors.toSet());
		}
		return Collections.emptySet();
	}
	
	private static <T> void applySorting(CriteriaBuilder builder, CriteriaQuery<T> query, Root<ValueObject> root, Sort ...sorts) {
		if (sorts.length == 1) {
			query.orderBy(createOrder(builder, root, sorts[0]));
		}
		else if (sorts.length > 1) {
			int idx = 0;
			final Order[] orders = new Order[sorts.length];
			for (Sort sort : sorts) {
				orders[idx++] = createOrder(builder, root, sort);
			}
			query.orderBy(orders);
		}
	}
	
	// set null-values for operators EMPTY and NOT_EMPTY
	private static void initNullValues(Map<String, Object> valueMap, Map<Long, Map<String, CriterionOperator>> criteriaMap, Long tmpId) {
		final Map<String, CriterionOperator> operatorMap = criteriaMap.get(tmpId);
		if (operatorMap != null) {
			for (String fieldUid : operatorMap.keySet()) {
				if (!valueMap.containsKey(fieldUid)) {
					valueMap.put(fieldUid, null);
				}
			}
		}
	}
	
	private static FilterCriterion createCriterion(Entity entity, String fieldUid, CriterionOperator operator, Object value) {
		final EntityField field = entity.getFieldByUid(fieldUid);
		Assert.stateAvailable(field, "field " + fieldUid);
		
		// value ends with * -> use LIKE
		if (operator == CriterionOperator.EQUAL && 
			value instanceof String && 
			((String) value).endsWith("*")) {
			value = StringUtils.trimTrailingCharacter((String) value, '*');
			operator = CriterionOperator.LIKE;
		}
		
		final FilterCriterion criterion = new FilterCriterion();
		criterion.setEntityField(field);
		criterion.setOperator(operator);
		if (value != null) {
			criterion.setValue(value);
		}
		return criterion;
	}

	private static CriterionOperator getOperator(Map<Long, Map<String, CriterionOperator>> criteriaMap, Long tmpId, String fieldUid) {
		final Map<String, CriterionOperator> operatorMap = criteriaMap.get(tmpId);
		final CriterionOperator operator = operatorMap != null ? operatorMap.get(fieldUid) : null;
		return operator != null ? operator : CriterionOperator.EQUAL;
	}
	
	private Map<String, Object> getValueMap(ValueObject object, NestedEntity nestedEntity) {
		final Map<String, Object> valueMap = getValueMap(object, nestedEntity.getNestedEntity());
		// remove ref to parent
		valueMap.remove(nestedEntity.getReferenceField().getUid());
		return valueMap;
	}
	
	private Map<String, Object> getValueMap(ValueObject object, Entity entity) {
		final Map<String, Object> valueMap = new HashMap<>();
		if (entity.hasAllFields()) {
			for (EntityField field : entity.getAllFields()) {
				final Object value = objectAccess.getValue(object, field);
				if (value != null) {
					valueMap.put(field.getUid(), value);
				}
			}
		}
		return valueMap;
	}
	
	QueryCursor<ValueObject> createCursor(Entity entity, int chuckSize) {
		try (Session session = getSession()) {
			final CriteriaQuery<Long> countQuery = buildCountQuery(session, entity, null); 
			final Long totalSize = querySingleResult(session, countQuery);
			final CriteriaQuery<ValueObject> query = buildQuery(session, entity, null);
			return new QueryCursor<>(query, totalSize.intValue(), chuckSize);
		}
	}
	
	QueryCursor<ValueObject> createCursor(Entity entity, @Nullable Filter filter, int chuckSize, Sort ...sort) {
		try (Session session = getSession()) {
			return createCursor(session, entity, filter, chuckSize, sort);
		}
	}
	
	@SuppressWarnings("unchecked")
	QueryCursor<ValueObject> createCursor(Session session, Entity entity, @Nullable Filter filter, int chuckSize, Sort ...sort) {
		if (filter != null && filter.getHqlQuery() != null) {
			final StringBuilder queryBuilder = new StringBuilder("select count(*) ").append(filter.getHqlQuery());
			final Query<Long> query = session.createQuery(queryBuilder.toString());
			final Long totalSize = query.uniqueResult();
			return new QueryCursor<>(filter.getHqlQuery(), totalSize.intValue(), chuckSize);
		}
		
		final CriteriaQuery<Long> countQuery = buildCountQuery(session, entity, filter); 
		final Long totalSize = querySingleResult(session, countQuery);
		final CriteriaQuery<ValueObject> query = buildQuery(session, entity, filter, sort);
		return new QueryCursor<>(query, totalSize.intValue(), chuckSize);
	}
	
	QueryCursor<ValueObject> createCursor(Session session, ValueObject searchObject, Map<Long, Map<String, CriterionOperator>> criteriaMap, Sort ...sort) {
		final CriteriaQuery<Long> countQuery = buildCountQuery(session, searchObject, criteriaMap);
		final Long totalSize = querySingleResult(session, countQuery);
		final CriteriaQuery<ValueObject> query = buildQuery(session, searchObject, criteriaMap, sort);
		return new QueryCursor<>(query, totalSize.intValue(), DEFAULT_CHUNK_SIZE);
	}
	
	@SuppressWarnings("unchecked")
	private <T> CriteriaQuery<T> createQuery(CriteriaBuilder builder, Entity entity, Session session) {
		return (CriteriaQuery<T>) builder.createQuery(getEntityClass(session, entity));
	}
	
	@SuppressWarnings("unchecked")
	private <T> Root<ValueObject> buildQuery(CriteriaBuilder builder, Entity entity, Session session, 
											 Filter filter, CriteriaQuery<T> query, Sort ...sorts) {
		final Root<ValueObject> root = (Root<ValueObject>) query.from(getEntityClass(session, entity));
		// criteria
		if (filter != null) {
			Assert.state(entity.equals(filter.getEntity()), "entity not match filter entity");
			if (filter.hasCriteria()) {
				final Map<Long, Join<Object, Object>> joins = buildJoinMap(filter, root);
				if (filter.getCriteria().size() == 1) {
					query.where(createRestriction(builder, root, filter.getCriteria().get(0), joins));
				}
				else {
					query.where(builder.and(buildCriteria(filter, builder, root, joins)));
				}
			}
		}
		// sort
		if (!ObjectUtils.isEmpty(sorts)) {
			applySorting(builder, query, root, sorts);
		}
		return root;
	}
	
	@SuppressWarnings("unchecked")
	private <T> Root<ValueObject> buildQuery(CriteriaBuilder builder, Entity entity, Session session,
											 ValueObject searchObject, CriteriaQuery<T> query, 
											 Map<Long, Map<String, CriterionOperator>> criteriaMap, Sort ...sorts) {
		final Root<ValueObject> root = (Root<ValueObject>) query.from(getEntityClass(session, entity));
		final List<Predicate> restrictions = new ArrayList<>();
		
		// main object
		final Map<String, Object> valueMap = getValueMap(searchObject, entity);
		initNullValues(valueMap, criteriaMap, 0L);
		for (Entry<String, Object> entry : valueMap.entrySet()) {
			final CriterionOperator operator = getOperator(criteriaMap, 0L, entry.getKey());
			final FilterCriterion criterion = createCriterion(entity, entry.getKey(), operator, entry.getValue());
			restrictions.add(createRestriction(builder, root, criterion, null));
		}
		
		// nesteds
		if (entity.hasNesteds()) {
			restrictions.addAll(createNestedsRestrictions(builder, entity, session, searchObject, criteriaMap, query, root));
		}
		
		// relations
		if (entity.hasAllRelations()) {
			restrictions.addAll(createRelationsRestrictions(builder, entity, searchObject, root));
		}
		
		if (restrictions.size() == 1) {
			query.where(restrictions.get(0));
		}
		else if (restrictions.size() > 1) {
			query.where(builder.and(restrictions.toArray(new Predicate[restrictions.size()])));
		}
		// sort
		if (!ObjectUtils.isEmpty(sorts)) {
			applySorting(builder, query, root, sorts);
		}
		return root;
	}
	
	private <T> List<Predicate> createNestedsRestrictions(CriteriaBuilder builder, Entity entity, Session session,
														  ValueObject searchObject, Map<Long, Map<String, CriterionOperator>> criteriaMap,
														  CriteriaQuery<T> query, Root<ValueObject> root) {
		final List<Predicate> restrictions = new ArrayList<>();
		for (NestedEntity nestedEntity : entity.getNesteds()) {
			final List<ValueObject> nesteds = objectAccess.getNestedObjects(searchObject, nestedEntity);
			if (!ObjectUtils.isEmpty(nesteds)) {
				restrictions.addAll(createNestedRestrictions(builder, nestedEntity, session, nesteds, criteriaMap, query, root));
			}
		}
		return restrictions;
	}
	
	@SuppressWarnings("unchecked")
	private <T> List<Predicate> createNestedRestrictions(CriteriaBuilder builder, NestedEntity nestedEntity, Session session, 
														 List<ValueObject> nesteds, Map<Long, Map<String, CriterionOperator>> criteriaMap,
														 CriteriaQuery<T> query, Root<ValueObject> root) {
		final List<Predicate> restrictions = new ArrayList<>();
		final Entity entityNested = nestedEntity.getNestedEntity();
		final Class<?> nestedEntityClass = getEntityClass(session, entityNested);
		for (ValueObject nestedObject : nesteds) {
			final Long tmpId = ((AbstractValueObject) nestedObject).getTmpId();
			final Map<String, Object> valueMap = getValueMap(nestedObject, nestedEntity);
			initNullValues(valueMap, criteriaMap, tmpId);
			if (valueMap.isEmpty()) {
				continue;
			}
			
			final Subquery<ValueObject> subQuery = (Subquery<ValueObject>) query.subquery(nestedEntityClass);
			final Root<ValueObject> subRoot = subQuery.correlate(root);
			final Join<Object, Object> join = subRoot.join(nestedEntity.getInternalName());
			final Map<Long, Join<Object, Object>> joins = Collections.singletonMap(entityNested.getId(), join);
			final Predicate[] subRestrictions = new Predicate[valueMap.size()];
			
			int idx = 0;
			for (Entry<String, Object> entry : valueMap.entrySet()) {
				final CriterionOperator operator = getOperator(criteriaMap, tmpId, entry.getKey());
				final FilterCriterion criterion = createCriterion(entityNested, entry.getKey(), operator, entry.getValue());
				subRestrictions[idx++] = createRestriction(builder, subRoot, criterion, joins);
			}
			subQuery.select(subRoot);
			if (subRestrictions.length == 1) {
				subQuery.where(subRestrictions[0]);
			} 
			else {
				subQuery.where(builder.and(subRestrictions));
			}
			restrictions.add(builder.exists(subQuery));
		}
		return restrictions;
	}
	
	private List<Predicate> createRelationsRestrictions(CriteriaBuilder builder, Entity entity, 
														ValueObject searchObject, Root<ValueObject> root) {
		final List<Predicate> restrictions = new ArrayList<>();
		for (EntityRelation relation : entity.getAllRelations()) {
			final Set<ValueObject> relatedObjects = objectAccess.getRelatedObjects(searchObject, relation);
			if (!ObjectUtils.isEmpty(relatedObjects)) {
				for (ValueObject relatedObject : relatedObjects) {
					restrictions.add(builder.equal(root.join(relation.getInternalName()), relatedObject));
				}
			}
		}
		return restrictions;
	}
	
	private void initUid(ValueObject object) {
		// imported transferable objects already have a uid
		if (objectAccess.getValue(object, SystemField.UID) == null) {
			objectAccess.setValue(object, SystemField.UID, UID.createUID());
		}
	}
	
	private void setAutonumValuesInsert(Entity entity, ValueObject object, Session session) {
		final EntityField autonumField = entity.findAutonumField();
		if (autonumField != null) {
			objectAccess.setValue(object, autonumField,
								  autonumService.getNextValue(autonumField, session));
		}
		if (entity.hasNesteds()) {
			for (NestedEntity nested : entity.getNesteds()) {
				final EntityField nestedAutonumField = nested.getNestedEntity().findAutonumField();
				if (nestedAutonumField != null && objectAccess.hasNestedObjects(object, nested)) {
					objectAccess.getNestedObjects(object, nested)
								.forEach(obj -> objectAccess.setValue(obj, nestedAutonumField, 
													autonumService.getNextValue(nestedAutonumField, session)));
				}
			}
		}
	}
	
	private void setAutonumValuesUpdate(Entity entity, ValueObject object, Session session) {
		if (entity.hasNesteds()) {
			for (NestedEntity nested : entity.getNesteds()) {
				final EntityField nestedAutonumField = nested.getNestedEntity().findAutonumField();
				if (nestedAutonumField != null && objectAccess.hasNestedObjects(object, nested)) {
					filterAndForEach(objectAccess.getNestedObjects(object, nested), 
									 ValueObject::isNew, 
									 obj -> objectAccess.setValue(obj, nestedAutonumField, 
												autonumService.getNextValue(nestedAutonumField, session)));
				}
			}
		}
	}
	
	private static Map<Long, Join<Object, Object>> buildJoinMap(Filter filter, Root<ValueObject> root) {
		return convertedMap(getNestedEntities(filter), 
							nested -> nested.getNestedEntity().getId(), 
							nested -> root.join(nested.getInternalName()));
	}

	private static Predicate[] buildCriteria(Filter filter, CriteriaBuilder builder, Root<ValueObject> root, 
											 Map<Long, Join<Object, Object>> joins) {
		final Predicate[] restrictions = new Predicate[filter.getCriteria().size()];
		for (int i = 0; i < restrictions.length; i++) {
			restrictions[i] = createRestriction(builder, root, filter.getCriteria().get(i), joins);
		}
		return restrictions;
	}
	
	private static Order createOrder(CriteriaBuilder builder, Root<ValueObject> root, Sort sort) {
		return sort.isAscending() 
						? builder.asc(root.get(sort.getColumnName())) 
						: builder.desc(root.get(sort.getColumnName()));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Predicate createRestriction(CriteriaBuilder builder, Root<ValueObject> table, 
											   FilterCriterion criterion, Map<Long, Join<Object, Object>> joins) {
		Path path;
		// system field
		if (criterion.getSystemField() != null) {
			path = table.get(criterion.getSystemField().property);
		}
		// entity field
		else {
			final EntityField field = criterion.getEntityField();
			final Join<Object, Object> join = joins != null 
										? joins.get(field.getEntity().getId()) 
										: null;
			path = join != null 
					? join.get(field.getInternalName())
					: table.get(field.getInternalName());
		}
		switch (criterion.getOperator()) {
			case EMPTY:
				return builder.isNull(path);
			case NOT_EMPTY:
				return builder.isNotNull(path);
			case EQUAL:
				return builder.equal(path, formatValue(criterion));
			case LIKE:
				return builder.like(path, criterion.getLike());
			case NOT_LIKE:
				return builder.notLike(path, criterion.getLike());
			case NOT_EQUAL:
				return builder.notEqual(path, formatValue(criterion));
			case GREATER:
				return builder.greaterThan(path, (Comparable) criterion.getValue());
			case GREATER_EQUAL:
				return builder.greaterThanOrEqualTo(path, (Comparable) criterion.getValue());
			case LESS:
				return builder.lessThan(path, (Comparable) criterion.getValue());
			case LESS_EQUAL:
				return builder.lessThanOrEqualTo(path, (Comparable) criterion.getValue());
			default:
				throw new UnsupportedOperationException(criterion.getOperator().name());
		}
	}
	
	private static Object formatValue(FilterCriterion criterion) {
		if ((criterion.getSystemField() == SystemField.CREATEDBY || 
			 criterion.getSystemField() == SystemField.MODIFIEDBY) &&
			 criterion.getValue() instanceof String) {
			return ((String) criterion.getValue()).replace("{username}", MiscUtils.geUserName());
		}
		return criterion.getValue();
	}
	
	private static void checkFilter(Entity entity, Filter filter) {
		Assert.state(entity.equals(filter.getEntity()), "filter is not suitable for  " + entity.getName());
	}
	
	private static void checkGeneric(Entity entity) {
		Assert.state(!entity.isGeneric(), "entity is generic");
	}
	
	private static void checkSessionAndContext(Session session, ValueObjectFunctionContext functionContext) {
		Assert.state(!(session == null && functionContext == null), "no session or functionContext provided");
		Assert.state(!(session != null && functionContext != null), "only session or functionContext allowed");
	}
	
	private boolean fireEvent(CallbackEventType eventType, ValueObject object, Session session) {
		return eventHandler.processEvent(new ValueObjectEvent(object, eventType, session));
	}
	
	private boolean fireEvent(CallbackEventType eventType, ValueObject object, Session session, ValueObjectFunctionContext functionContext) {
		return eventHandler.processEvent(new ValueObjectEvent(object, eventType, null, session, functionContext));
	}
	
	private boolean fireEvent(CallbackEventType eventType, ValueObject object, EntityStatusTransition statusTransition, Session session, ValueObjectFunctionContext functionContext) {
		return eventHandler.processEvent(new ValueObjectEvent(object, eventType, statusTransition, session, functionContext));
	}
	
}
