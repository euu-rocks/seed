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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CommonParams;
import org.hibernate.Session;

import org.seed.C;
import org.seed.InternalException;
import org.seed.core.config.FullTextSearchProvider;
import org.seed.core.config.SystemLog;
import org.seed.core.data.SystemField;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityChangeAware;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.NestedEntity;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;
import org.seed.core.util.Tupel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FullTextSearch implements EntityChangeAware, ValueObjectChangeAware {
	
	private static final Logger log = LoggerFactory.getLogger(FullTextSearch.class);
	
	private static final String FIELD_ENTITY_ID = "entity_id";
	
	private static final String FIELD_TEXT = "text";
	
	@Autowired
	private FullTextSearchProvider provider;
	
	@Autowired
	private ValueObjectRepository repository;
	
	@Autowired
	private ValueObjectAccess objectAccess;
	
	public final boolean isAvailable() {
		return provider.isFullTextSearchAvailable();
	}
	
	@Override
	public void notifyCreate(ValueObject object, Session session) {
		if (isAvailable()) {
			index(object);
		}
	}

	@Override
	public void notifyChange(ValueObject object, Session session) {
		if (isAvailable()) {
			index(object);
		}
	}

	@Override
	public void notifyDelete(ValueObject object, Session session) {
		if (isAvailable()) {
			delete(object);
		}
	}
	
	@Override
	public void notifyCreate(Entity object, Session session) {
		// do nothing
	}
	
	@Override
	public void notifyChange(Entity object, Session session) {
		// do nothing
	}
	
	@Override
	public void notifyBeforeChange(Entity entity, Session session) {
		// do nothing
	}
	
	@Override
	public void notifyDelete(Entity entity, Session session) {
		if (isAvailable()) {
			delete(entity);
		}
	}
	
	// tupel.x = entityId
	// tupel.y = valueObjectId
	List<Tupel<Long, Long>> query(String queryString, @Nullable Entity entity) {
		Assert.notNull(queryString, "queryString");
		
		final SolrClient solrClient = provider.getSolrClient();
		final SolrQuery query = new SolrQuery(queryString);
		query.setParam(CommonParams.DF, FIELD_TEXT); 
		if (entity != null) {
			query.setParam(CommonParams.FQ, createEntityFilter(entity)); 
		}
		query.addField(SystemField.ID.property);
		query.addField(FIELD_ENTITY_ID);
		log.debug("Querying solr: {}", query);
		try {
			return convertedList(solrClient.query(query).getResults(), 
								 FullTextSearch::createResultEntry);
		}
		catch (Exception ex) {
			SystemLog.logError(ex);
			throw new InternalException(ex);
		}
	}
	
	Map<Long, String> getTextMap(List<ValueObject> objectList, String fullTextQuery) {
		Assert.notNull(objectList, "objectList");
		Assert.notNull(fullTextQuery, "fullTextQuery");
		// collect object ids
		final List<String> listIds = convertedList(objectList, obj -> obj.getId().toString());
		// remove all non letter or digit chars
		final String fullTextKey = MiscUtils.filterString(fullTextQuery, Character::isLetterOrDigit);
		final SolrClient solrClient = provider.getSolrClient();
		try {
			return convertedMap(solrClient.getById(listIds), 
								doc -> Long.valueOf((String) doc.getFirstValue(SystemField.ID.property)), 
								doc -> decorateResultText((String) doc.getFirstValue(FIELD_TEXT), fullTextKey));
		} 
		catch (Exception ex) {
			SystemLog.logError(ex);
			throw new InternalException(ex);
		}
	}
	
	boolean indexChunk(Entity entity, List<ValueObject> objects) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(objects, "objects");
		
		boolean chunckIndexed = false;
		log.info("Indexing {} ({})", entity.getInternalName(), objects.size());
		final SolrClient solrClient = provider.getSolrClient();
		try {
			for (ValueObject object : objects) {
				solrClient.add(buildDocument(entity, object));
			}
			solrClient.commit();
			chunckIndexed = true;
		}
		catch (Exception ex) {
			// only warn
			log.warn("Error while full-text indexing", ex);
		}
		return chunckIndexed;
	}
	
	void index(ValueObject object) {
		Assert.notNull(object, C.OBJECT);
		
		final Entity entity = repository.getEntity(object);
		if (entity.hasFullTextSearchFields()) {
			final SolrClient solrClient = provider.getSolrClient();
			final SolrInputDocument solrDocument = buildDocument(entity, object);
			log.debug("Indexing solr document: {}", solrDocument);
			try {
				solrClient.add(solrDocument);
				solrClient.commit();
			} 
			catch (Exception e) {
				// only warn
				log.warn("Error while full-text indexing", e);
			}
		}
	}
	
	void deleteIndex() {
		final SolrClient solrClient = provider.getSolrClient();
		log.debug("Deleting index");
		try {
			solrClient.deleteByQuery("*:*");
			solrClient.commit();
		} 
		catch (SolrServerException | IOException ex) {
			SystemLog.logError(ex);
			throw new InternalException(ex);
		}
	}
	
	private void delete(ValueObject object) {
		Assert.notNull(object, C.OBJECT);
		
		final Entity entity = repository.getEntity(object);
		if (entity.hasFullTextSearchFields()) {
			final SolrClient solrClient = provider.getSolrClient();
			try {
				solrClient.deleteById(String.valueOf(object.getId()));
				solrClient.commit();
			} 
			catch (Exception e) {
				// only warn
				log.warn("Error while deleting from full-text index", e);
			}
		}
	}
	
	private void delete(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		if (entity.hasFullTextSearchFields()) {
			final SolrClient solrClient = provider.getSolrClient();
			try {
				solrClient.deleteByQuery(createEntityFilter(entity));
				solrClient.commit();
			} 
			catch (Exception e) {
				// only warn
				log.warn("Error while deleting from full-text index", e);
			}
		}
	}
	
	private SolrInputDocument buildDocument(Entity entity, ValueObject object) {
		final StringBuilder buf = new StringBuilder();
		buildFields(entity, object, buf);
		buildNesteds(entity, object, buf);
		
		final SolrInputDocument solrDoc = new SolrInputDocument();
		solrDoc.addField(SystemField.ID.property, object.getId());
		solrDoc.addField(FIELD_ENTITY_ID, entity.getId());
		solrDoc.addField(FIELD_TEXT, buf.toString().trim());
		return solrDoc;
	}
	
	private void buildFields(Entity entity, ValueObject object, StringBuilder buf) {
		for (EntityField field : entity.getFullTextSearchFields()) {
			Object value = objectAccess.getValue(object, field); 
			if (field.isReferenceField()) {
				value = repository.getIdentifier((ValueObject) value);
			}
			if (value != null) {
				buf.append(value).append(' ');
			}
		}
	}
	
	private void buildNesteds(Entity entity, ValueObject object, StringBuilder buf) {
		for (NestedEntity nested : entity.getNesteds()) {
			if (nested.getNestedEntity().hasFullTextSearchFields()) {
				final List<ValueObject> nestedObjects = objectAccess.getNestedObjects(object, nested);
				if (nestedObjects != null) {
					buildNestedObjects(nested, nestedObjects, buf);
					buf.append('\n');
				}
			}
		}
	}
	
	private void buildNestedObjects(NestedEntity nested, List<ValueObject> nestedObjects, StringBuilder buf) {
		for (ValueObject nestedObject : nestedObjects) {
			buf.append('\n');
			for (EntityField entityField : nested.getNestedEntity().getFullTextSearchFields()) {
				Object nestedFieldValue = objectAccess.getValue(nestedObject, entityField);
				if (entityField.isReferenceField()) {
					nestedFieldValue = repository.getIdentifier((ValueObject) nestedFieldValue);
				}
				if (nestedFieldValue != null) {
					buf.append(nestedFieldValue).append('|');
				}
			}
		}
	}
	
	private static String decorateResultText(String text, String fullTextKey) {
		return MiscUtils.replaceAllIgnoreCase(text, fullTextKey, "<b>" + fullTextKey + "</b>");
	}
	
	private static Tupel<Long, Long> createResultEntry(SolrDocument document) {
		return new Tupel<>((Long) document.getFirstValue(FIELD_ENTITY_ID), 
							Long.valueOf((String) document.getFirstValue(SystemField.ID.property)));
	}
	
	private static String createEntityFilter(Entity entity) {
		return FIELD_ENTITY_ID + ':' + entity.getId();
	}

}
