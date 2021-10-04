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
package org.seed;

import org.seed.config.ZKCEApplication;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@EnableAsync
@EnableScheduling
@ZKCEApplication
@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class})
public class Seed {
	
	private static ApplicationContext applicationContext;
	
	@Autowired
	private BuildProperties buildProperties;
	
	public static void main(String[] args) {
		applicationContext = SpringApplication.run(Seed.class, args);
	}
	
	@GetMapping("/seed")
	public String seed() {
		return C.SEED;
	}
	
	public String getVersion() {
		return buildProperties != null
				? buildProperties.getVersion()
				: null;
	}
	
	public static <T> T getBean(Class<T> typeClass) {
		Assert.notNull(typeClass, C.TYPECLASS);
    	// context only available after startup completed
		Assert.stateAvailable(applicationContext, "applicationContext");
		
    	return applicationContext.getBean(typeClass);
	}
	
}
