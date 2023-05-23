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

/**
 * A <code>CallbackFunctionContext</code> is the context in which a {@link CallbackFunction} is executed.
 * 
 * @author seed-master
 *
 */
public interface CallbackFunctionContext extends FunctionContext {
	
	/**
	 * Returns the type of the triggering event.
	 * @return the type of the triggering event
	 */
	CallbackEventType getEventType();
	
	/**
	 * Returns a service that provides client-side functionalities.
	 * @return a service that provides client-side functionalities
	 */
	ClientProvider getClientProvider();
	
	/**
	 * Returns a service that provides access to parameters.
	 * @return a service that provides access to parameters
	 */
	ParameterProvider getParameterProvider();
	
	/**
	 * Returns a service that provides {@link Mail} functionalities.
	 * @return a service that provides mail functionalities
	 */
	MailProvider getMailProvider();
	
	/**
	 * Returns a service that provides access to {@link EntityObject} instances.
	 * @return a service that provides access to entity objects
	 */
	EntityObjectProvider getObjectProvider();
	
	/**
	 * Returns a service that provides access to {@link DataSource} objects. 
	 * @return a service that provides access to data sources
	 */
	DataSourceProvider getDataSourceProvider();
	
	/**
	 * Returns a service that provides access to stored procedures. 
	 * See {@link StoredProcedureCall}
	 * @return a service that provides access to stored procedures
	 */
	StoredProcedureProvider getStoredProcedureProvider();
	
	/**
	 * Returns a service that provides {@link RestClient} functionalities.
	 * @return a service that provides REST client functionalities
	 */
	RestProvider getRestProvider();
	
	/**
	 * Returns the source {@link Status} or null if no status exists
	 * @return the source status or null if no status exists
	 */
	Status getSourceStatus();
	
	/**
	 * Returns the target {@link Status} or null if no status exists
	 * @return the target status or null if no status exists
	 */
	Status getTargetStatus();
	
}
