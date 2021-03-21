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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CommonParams;
import org.hibernate.Session;

import org.seed.core.config.FullTextSearchProvider;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityChangeAware;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.NestedEntity;
import org.seed.core.util.MiscUtils;
import org.seed.core.util.Tupel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

@Component
public class FullTextSearch implements EntityChangeAware, ValueObjectChangeAware {
	
	private final static Logger log = LoggerFactory.getLogger(FullTextSearch.class);
	
	private final static String FIELD_ID = "id";
	
	private final static String FIELD_ENTITY_ID = "entity_id";
	
	private final static String FIELD_TEXT = "text";
	
	@Autowired
	private FullTextSearchProvider provider;
	
	@Autowired
	private ValueObjectRepository repository;
	
	@Autowired
	private ValueObjectAccess objectAccess;
	
	@Override
	public void notifyCreate(ValueObject object, Session session) {
		if (provider.isFullTextSearchAvailable()) {
			index(object);
		}
	}

	@Override
	public void notifyChange(ValueObject object, Session session) {
		if (provider.isFullTextSearchAvailable()) {
			index(object);
		}
	}

	@Override
	public void notifyDelete(ValueObject object, Session session) {
		if (provider.isFullTextSearchAvailable()) {
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
	public void notifyDelete(Entity entity, Session session) {
		if (provider.isFullTextSearchAvailable()) {
			delete(entity);
		}
	}
	
	// tupel.x = entityId
	// tupel.y = valueObjectId
	List<Tupel<Long, Long>> query(String queryString, @Nullable Entity entity) {
		Assert.notNull(queryString, "queryString is null");
		
		final SolrClient solrClient = provider.getSolrClient();
		final SolrQuery query = new SolrQuery(queryString);
		query.setParam(CommonParams.DF, FIELD_TEXT); 
		if (entity != null) {
			query.setParam(CommonParams.FQ, createEntityFilter(entity)); 
		}
		query.addField(FIELD_ID);
		query.addField(FIELD_ENTITY_ID);
		log.debug("Querying solr: " + query);
		try {
			return solrClient.query(query).getResults().stream()
						   	 .map(doc -> createResultEntry(doc))
						     .collect(Collectors.toList());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	Map<Long, String> getTextMap(List<ValueObject> objectList, String fullTextQuery) {
		Assert.notNull(objectList, "objectList is null");
		Assert.notNull(fullTextQuery, "fullTextQuery is null");
		// collect object ids
		final List<String> listIds = objectList.stream()
											   .map(o -> o.getId().toString())
											   .collect(Collectors.toList());
		// remove all non letter or digit chars
		final String fullTextKey = MiscUtils.filterString(fullTextQuery, Character::isLetterOrDigit);
		final SolrClient solrClient = provider.getSolrClient();
		try {
			return solrClient.getById(listIds).stream()
							 .collect(Collectors.toMap(doc -> Long.valueOf((String) doc.getFirstValue(FIELD_ID)),
													   doc -> decorateResultText((String) doc.getFirstValue(FIELD_TEXT), 
															   					 fullTextKey)));
		} 
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void index(ValueObject object) {
		Assert.notNull(object, "object is null");
		
		final Entity entity = repository.getEntity(object);
		if (entity.hasFullTextSearchFields()) {
			final SolrClient solrClient = provider.getSolrClient();
			final SolrInputDocument solrDocument = buildDocument(entity, object);
			log.debug("Indexing solr document: " + solrDocument);
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
	
	private void delete(ValueObject object) {
		Assert.notNull(object, "object is null");
		
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
		Assert.notNull(entity, "entity is null");
		
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
		// fields
		for (EntityField field : entity.getFullTextSearchFields()) {
			Object value = objectAccess.getValue(object, field); 
			if (field.getType().isReference()) {
				value = repository.getIdentifier((ValueObject) value);
			}
			if (value != null) {
				buf.append(value).append(' ');
			}
		}
		// nesteds
		if (entity.hasNesteds()) {
			for (NestedEntity nested : entity.getNesteds()) {
				if (!nested.getNestedEntity().hasFullTextSearchFields()) {
					continue;
				}
				final List<ValueObject> nestedObjects = objectAccess.getNestedObjects(object, nested);
				if (ObjectUtils.isEmpty(nestedObjects)) {
					continue;
				}
				for (ValueObject nestedObject : nestedObjects) {
					buf.append('\n');
					for (EntityField entityField : nested.getNestedEntity().getFullTextSearchFields()) {
						Object nestedFieldValue = objectAccess.getValue(nestedObject, entityField);
						if (entityField.getType().isReference()) {
							nestedFieldValue = repository.getIdentifier((ValueObject) nestedFieldValue);
						}
						if (nestedFieldValue != null) {
							buf.append(nestedFieldValue).append('|');
						}
					}
				}
				buf.append('\n');
			}
		}
		// solr document
		final SolrInputDocument solrDoc = new SolrInputDocument();
		solrDoc.addField(FIELD_ID, object.getId());
		solrDoc.addField(FIELD_ENTITY_ID, object.getEntityId());
		solrDoc.addField(FIELD_TEXT, buf.toString().trim());
		return solrDoc;
	}
	
	private static String decorateResultText(String text, String fullTextKey) {
		return MiscUtils.replaceAllIgnoreCase(text, fullTextKey, "<b>" + fullTextKey + "</b>");
	}
	
	private static Tupel<Long,Long> createResultEntry(SolrDocument document) {
		return new Tupel<>((Long) document.getFirstValue(FIELD_ENTITY_ID), 
							Long.valueOf((String) document.getFirstValue(FIELD_ID)));
	}
	
	private static String createEntityFilter(Entity entity) {
		return FIELD_ENTITY_ID + ':' + entity.getId();
	}
	
}
