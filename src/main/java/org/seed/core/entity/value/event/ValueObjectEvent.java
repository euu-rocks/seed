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
package org.seed.core.entity.value.event;

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.api.CallbackEventType;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityStatusTransition;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.util.Assert;

public class ValueObjectEvent {
	
	final ValueObject object;
	
	final CallbackEventType type;
	
	final ValueObjectFunctionContext functionContext;
	
	final EntityStatusTransition statusTransition;
	
	final EntityFunction entityFunction;
	
	final Session session;
	
	public ValueObjectEvent(ValueObject object, CallbackEventType type, Session session) {
		this(object, type, null, session, null, null);
	}
	
	public ValueObjectEvent(ValueObject object, EntityFunction entityFunction, Session session) {
		this(object, CallbackEventType.USERACTION, null, session, null, entityFunction);
	}
	
	public ValueObjectEvent(ValueObject object, CallbackEventType type, 
			EntityStatusTransition statusTransition, 
			Session session, ValueObjectFunctionContext functionContext) {
		this(object, type, statusTransition, session, functionContext, null);
	}
	
	private ValueObjectEvent(ValueObject object, CallbackEventType type, 
							EntityStatusTransition statusTransition, 
							Session session, ValueObjectFunctionContext functionContext, 
							EntityFunction entityFunction) {
		Assert.notNull(object, C.OBJECT);
		Assert.notNull(type, C.TYPE);
		Assert.state(!(session == null && functionContext == null), "no session or functionContext provided");
		Assert.state(!(session != null && functionContext != null), "only session or functionContext allowed");
		
		this.object = object;
		this.type = type;
		this.session = session;
		this.functionContext = functionContext;
		this.statusTransition = statusTransition;
		this.entityFunction = entityFunction;
	}
	
	Session getSession() {
		return session != null ? session : functionContext.getSession();
	}
	
}
