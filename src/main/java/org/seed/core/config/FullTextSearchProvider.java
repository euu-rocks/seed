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
package org.seed.core.config;

import javax.annotation.PostConstruct;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

import org.seed.core.util.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class FullTextSearchProvider {
	
	private static final Logger log = LoggerFactory.getLogger(FullTextSearchProvider.class);
	
	@Autowired
	private Environment environment;
	
	private SolrClient solrClient;
	
	@PostConstruct
	private void init() {
		final String propSolrUrl = environment.getProperty("search.solr.url");
		if (propSolrUrl != null) {
			try {
				solrClient = new HttpSolrClient.Builder(propSolrUrl).build();
				solrClient.ping();
				log.info("Full-text search enabled");
			}
			catch (Exception e) {
				solrClient = null;
				log.warn("Solr server not found: {}", propSolrUrl);
				log.warn("Full-text search not available");
			}
		}
	}
	
	public boolean isFullTextSearchAvailable() {
		return solrClient != null;
	}

	public SolrClient getSolrClient() {
		Assert.state(isFullTextSearchAvailable(), "full-text search not available");
		
		return solrClient;
	}
	
}
