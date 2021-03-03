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

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.hibernate.Session;

import org.seed.core.config.FullTextSearchProvider;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityChangeAware;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.NestedEntity;
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
	
	public void notifyCreate(Entity object, Session session) {
		// do nothing
	}
	
	public void notifyChange(Entity object, Session session) {
		// do nothing
	}
	
	public void notifyDelete(Entity entity, Session session) {
		if (provider.isFullTextSearchAvailable()) {
			delete(entity);
		}
	}
	
	// tupel.x = entityId
	// tupel.y = valueObjectId
	List<Tupel<Long,Long>> query(Entity entity, String queryString) {
		Assert.notNull(entity, "entity is null");
		Assert.notNull(queryString, "queryString is null");
		Assert.state(entity.hasFullTextSearchFields(), "entity has no full-text serach fields");
		
		final List<Tupel<Long,Long>> result = new ArrayList<>();
		final SolrClient solrClient = provider.getSolrClient();
		final SolrQuery query = new SolrQuery(buildQuery(entity, queryString));
		query.addField(FIELD_ID);
		query.addField(FIELD_ENTITY_ID);
		try {
			final QueryResponse response = solrClient.query(query);
			for (SolrDocument document : response.getResults()) {
				result.add(new Tupel<>((Long) document.getFirstValue(FIELD_ENTITY_ID), 
						   			   Long.valueOf((String) document.getFirstValue(FIELD_ID))));
			}
			return result;
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
				solrClient.deleteByQuery(FIELD_ENTITY_ID + ':' + entity.getId());
				solrClient.commit();
			} 
			catch (Exception e) {
				// only warn
				log.warn("Error while deleting from full-text index", e);
			}
		}
	}
	
	private SolrInputDocument buildDocument(Entity entity, ValueObject object) {
		final SolrInputDocument solrDoc = new SolrInputDocument();
		// system fields
		solrDoc.addField(FIELD_ID, object.getId());
		solrDoc.addField(FIELD_ENTITY_ID, object.getEntityId());
		// fields
		for (EntityField field : entity.getFullTextSearchFields()) {
			final Object value = objectAccess.getValue(object, field);
			if (value != null) {
				solrDoc.addField(field.getInternalName(), value);
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
				final StringBuilder buf = new StringBuilder();
				for (ValueObject nestedObject : nestedObjects) {
					for (EntityField entityField : nested.getNestedEntity().getFullTextSearchFields()) {
						final Object nestedFieldObject = objectAccess.getValue(nestedObject, entityField);
						if (nestedFieldObject != null) {
							if (buf.length() > 0) {
								buf.append('|');
							}
							buf.append(nestedFieldObject);
						}
					}
					buf.append('\n');
				}
				solrDoc.addField(nested.getName(), buf.toString());
			}
		}
		return solrDoc;
	}
	
	private static String buildQuery(Entity entity, String queryString) {
		final StringBuilder buf = new StringBuilder();
		buf.append(FIELD_ENTITY_ID).append(':').append(entity.getId())
		   .append(" AND (");
		boolean first = true;
		for (EntityField field : entity.getFullTextSearchFields()) {
			if (first) {
				first = false;
			}
			else {
				buf.append(" OR ");
			}
			buf.append(field.getInternalName()).append(':').append(queryString);
		}
		if (entity.hasAllNesteds()) {
			for (NestedEntity nested : entity.getAllNesteds()) {
				if (nested.getNestedEntity().hasFullTextSearchFields()) {
					if (first) {
						first = false;
					}
					else {
						buf.append(" OR ");
					}
					buf.append(nested.getName()).append(':').append(queryString);
				}
			}
		}
		return buf.append(')').toString();
	}

}
