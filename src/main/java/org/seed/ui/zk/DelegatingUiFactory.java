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
package org.seed.ui.zk;

import org.seed.Seed;
import org.seed.core.form.layout.LayoutProvider;
import org.seed.core.util.Assert;

import org.zkoss.zk.ui.http.SimpleUiFactory;
import org.zkoss.zk.ui.metainfo.PageDefinition;
import org.zkoss.zk.ui.sys.RequestInfo;

public class DelegatingUiFactory extends SimpleUiFactory {
	
	private static final String PATH_GENERATED = "/generated";	//NOSONAR
	
	private LayoutProvider layoutProvider;
	
	@Override
	public PageDefinition getPageDefinition(RequestInfo requestInfo, String path) {
		if (path.startsWith(PATH_GENERATED)) {
			final String pageContent = getLayoutProvider()
											.getLayout(path.substring(PATH_GENERATED.length()), 
													   ViewUtils.getSettings(requestInfo.getSession()));
			return getPageDefinitionDirectly(requestInfo, 
											 pageContent != null 
												? pageContent 
												: "<zk/>", 
											 "zul");
		}
		return super.getPageDefinition(requestInfo, path);
	}
	
	private LayoutProvider getLayoutProvider() {
		if (layoutProvider == null) {
			layoutProvider = Seed.getBean(LayoutProvider.class);
			Assert.stateAvailable(layoutProvider, "layout provider");
		}
		return layoutProvider;
	}
	
}
