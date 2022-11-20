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
package org.seed.core.data.datasource;

import static org.seed.core.util.CollectionUtils.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.C;
import org.seed.core.application.AbstractApplicationEntity;
import org.seed.core.application.ContentObject;
import org.seed.core.util.Assert;
import org.seed.core.util.CDATAXmlAdapter;

@Entity
@Table(name = "sys_datasource")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DataSourceMetadata extends AbstractApplicationEntity
	implements IDataSource, ContentObject {
	
	/*  \\{: escape character '{'
	    (  : start match group
	    [  : one of the following characters
	    ^  : not the following character
	    +  : one or more
	    )  : stop match group
	    \\}: escape character '}'  
	*/
	private static final Pattern PARAM_PATTERN = Pattern.compile("\\{([^}]+)\\}");
	
	private DataSourceType type;
	
	private String content;
	
	@OneToMany(mappedBy = "dataSource",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<DataSourceParameter> parameters;
	
	@Override
	@XmlJavaTypeAdapter(CDATAXmlAdapter.class)
	public String getContent() {
		return content;
	}
	
	@Override
	public void setContent(String content) {
		this.content = content;
	}
	
	@Override
	public DataSourceType getType() {
		return type;
	}

	public void setType(DataSourceType type) {
		this.type = type;
	}

	@Override
	public boolean hasParameters() {
		return notEmpty(getParameters());
	}
	
	@Override
	public DataSourceParameter getParameterByUid(String uid) {
		return getObjectByUid(getParameters(), uid);
	}
	
	@Override
	public DataSourceParameter getParameterByName(String name) {
		return getObjectByName(getParameters(), name, true);
	}
	
	@Override
	@XmlElement(name="parameter")
	@XmlElementWrapper(name="parameters")
	public List<DataSourceParameter> getParameters() {
		return parameters;
	}

	@Override
	public void addParameter(DataSourceParameter parameter) {
		Assert.notNull(parameter, C.PARAMETER);
		
		if (parameters == null) {
			parameters = new ArrayList<>();
		}
		parameter.setDataSource(this);
		parameters.add(parameter);
	}
	
	@Override
	public void removeParameter(DataSourceParameter parameter) {
		Assert.notNull(parameter, C.PARAMETER);
		
		getParameters().remove(parameter);
	}
	
	public void setParameters(List<DataSourceParameter> parameters) {
		this.parameters = parameters;
	}
	
	@Override
	public Set<String> getContentParameterSet() {
		final Set<String> result = new HashSet<>();
		if (content != null) {
			final Matcher matcher = PARAM_PATTERN.matcher(content);
			while (matcher.find()) {
				result.add(matcher.group(1));
			}
		}
		return result;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final IDataSource otherDataSource = (IDataSource) other;
		if (!new EqualsBuilder()
			.append(getName(), otherDataSource.getName())
			.append(content, otherDataSource.getContent())
			.append(type, otherDataSource.getType())
			.isEquals()) {
			return false;
		}
		return isEqualParameters(otherDataSource);
	}
	
	private boolean isEqualParameters(IDataSource otherDataSource) {
		return !(anyMatch(parameters, param -> !param.isEqual(otherDataSource.getParameterByUid(param.getUid()))) ||
			     anyMatch(otherDataSource.getParameters(), param -> getParameterByUid(param.getUid()) == null));
	}
	
	@Override
	public void removeNewObjects() {
		removeNewObjects(getParameters());
	}
	
	@Override
	public void initUid() {
		super.initUid();
		initUids(getParameters());
	}
	
}
