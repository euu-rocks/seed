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
package org.seed.core.application.module;

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.data.AbstractSystemEntityRepository;
import org.seed.core.data.QueryParameter;
import org.seed.core.util.Assert;

import org.springframework.stereotype.Repository;

@Repository
public class ModuleRepository extends AbstractSystemEntityRepository<Module> {

	protected ModuleRepository() {
		super(ModuleMetadata.class);
	}
	
	Session openSession() {
		return super.getSession();
	}
	
	public Module findByUid(String uid) {
		Assert.notNull(uid, C.UID);
		
		return findUnique(new QueryParameter(C.UID, uid));
	}

}
