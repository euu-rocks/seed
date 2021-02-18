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
package org.seed.core.customcode.codegen;

import java.util.Date;

import org.seed.core.codegen.SourceCode;
import org.seed.core.codegen.SourceCodeBuilder;
import org.seed.core.codegen.SourceCodeImpl;
import org.seed.core.customcode.CustomCode;

import org.springframework.util.Assert;

class CustomCodeBuilder implements SourceCodeBuilder<CustomCode> {
	
	private final CustomCode customCode;
	
	CustomCodeBuilder(CustomCode customCode) {
		Assert.notNull(customCode, "customCode is null");
		
		this.customCode = customCode;
	}

	@Override
	public Date getLastModified() {
		return customCode.getLastModified();
	}
	
	@Override
	public SourceCode<CustomCode> build() {
		return build(BuildMode.COMPLETE);
	}

	@Override
	public SourceCode<CustomCode> build(BuildMode buildMode) {
		Assert.state(buildMode == BuildMode.COMPLETE, "unsupported build mode: " + buildMode.name());
		
		return new SourceCodeImpl<>(customCode.getQualifiedName(), 
								    customCode.getContent());
	}

}
