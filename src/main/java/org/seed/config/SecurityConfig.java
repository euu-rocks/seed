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
package org.seed.config;

import javax.sql.DataSource;

import org.seed.core.user.Authorisation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth,
    							PasswordEncoder passwordEncoder) throws Exception {
		auth.jdbcAuthentication()
			.dataSource(dataSource)
			.usersByUsernameQuery("select name, password, isenabled from sys_user where name = ?")
			.authoritiesByUsernameQuery("select distinct u.name, a.rolename from sys_user u join sys_user_group g on g.user_id = u.id join sys_usergroup_auth a on a.group_id = g.group_id where u.name = ?")
			.passwordEncoder(passwordEncoder);
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
			.antMatchers("/actuator/**").hasRole(Authorisation.ENDPOINTS.name())  
			.antMatchers(
					"/zkau*",
					"/login*", "/logout",
					"/js/**",
					"/css/**", 
					"/img/**", 
					"/static/**"
					)
			.permitAll()
			.anyRequest().authenticated().and()
			.headers().frameOptions().sameOrigin().and()
			.formLogin().permitAll().and()
			.logout().logoutSuccessUrl("/login").permitAll().and()
			.csrf().disable();
	}
	
}
