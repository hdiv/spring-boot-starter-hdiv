/**
 * Copyright 2005-2014 hdiv.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hdiv.spring.boot.autoconfigure;

import org.hdiv.config.annotation.EnableHdivWebSecurity;
import org.hdiv.config.annotation.ExclusionRegistry;
import org.hdiv.config.annotation.ValidationConfigurer;
import org.hdiv.config.annotation.configuration.HdivWebSecurityConfigurerAdapter;
import org.hdiv.filter.ValidatorFilter;
import org.hdiv.listener.InitListener;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration Auto-configuration} for HDIV Integration.
 */
@Configuration
@ConditionalOnClass(ValidatorFilter.class)
@ConditionalOnWebApplication
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
@Import(EditableValidationConfiguration.class)
public class HdivAutoConfiguration {

	@Configuration
	@EnableHdivWebSecurity
	protected static class HdivDefaultConfiguration extends HdivWebSecurityConfigurerAdapter {

		@Override
		public void addExclusions(final ExclusionRegistry registry) {

			// Static content
			registry.addUrlExclusions("/webjars/.*").method("GET");
			// It's not possible to exclude static by pattern, like /resources/.*
			registry.addUrlExclusions(".*.js").method("GET");
			registry.addUrlExclusions(".*.css").method("GET");
			registry.addUrlExclusions(".*.png").method("GET");
			registry.addUrlExclusions(".*.jpg").method("GET");
			registry.addUrlExclusions(".*.woff").method("GET");
			registry.addUrlExclusions(".*.ttf").method("GET");
			registry.addUrlExclusions(".*.svg").method("GET");
			registry.addUrlExclusions(".*.ico").method("GET");

			// Excluded URLs
			registry.addUrlExclusions("/").method("GET");

			// It's possible to autodetect actuator endpoints and configure as them as exclusion?
			// Actuator filters
			registry.addUrlExclusions("/health");
			registry.addUrlExclusions("/beans").method("GET");
			registry.addUrlExclusions("/trace").method("GET");
			registry.addUrlExclusions("/configprops").method("GET");
			registry.addUrlExclusions("/info").method("GET");
			registry.addUrlExclusions("/dump").method("GET");
			registry.addUrlExclusions("/autoconfig").method("GET");
			registry.addUrlExclusions("/metrics", "/metrics/.*").method("GET");
			registry.addUrlExclusions("/env", "/env/.*").method("GET");
			registry.addUrlExclusions("/mappings").method("GET");

			// Spring Security
			registry.addParamExclusions("_csrf");
		}

		@Override
		public void configureEditableValidation(final ValidationConfigurer validationConfigurer) {

			// Enable default rules for all URLs.
			validationConfigurer.addValidation(".*");
		}

	}

	@Bean
	public FilterRegistrationBean filterRegistrationBean() {
		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		ValidatorFilter validatorFilter = new ValidatorFilter();
		registrationBean.setFilter(validatorFilter);
		registrationBean.setOrder(0);
		return registrationBean;
	}

	@Bean
	public InitListener initListener() {
		return new InitListener();
	}

}