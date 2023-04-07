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
package org.seed.core.api;

import org.seed.core.codegen.GeneratedCode;

/**
 * <code>TransformationFunction</code> is the base interface of all transformation functions.
 * It is triggered when a transformation is performed.
 * 
 * @author seed-master
 *
 * @param <S> the type of the source {@link EntityObject}
 * @param <T> the type of the target {@link EntityObject}
 */
public interface TransformationFunction<S extends EntityObject, T extends EntityObject> extends GeneratedCode {
	
	/**
	 * Excecutes the function.
	 * @param sourceObject the source {@link EntityObject}
	 * @param targetObject the target {@link EntityObject}
	 * @param context the context of the function
	 */
	void transform(S sourceObject, T targetObject, CallbackFunctionContext context);
	
}
