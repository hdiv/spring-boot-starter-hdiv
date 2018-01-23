/**
 * Copyright 2005-2018 hdiv.org
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.hdiv.config.HDIVConfig;
import org.hdiv.util.Method;
import org.hdiv.validator.EditableDataValidationResult;
import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizerBeanPostProcessor;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.MockEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tests for {@link HdivAutoConfiguration}.
 */
public class HdivAutoConfigurationTests {

	private static final MockEmbeddedServletContainerFactory containerFactory = new MockEmbeddedServletContainerFactory();

	private final AnnotationConfigEmbeddedWebApplicationContext context = new AnnotationConfigEmbeddedWebApplicationContext();

	@After
	public void close() {
		if (context != null) {
			context.close();
		}
	}

	@Test
	public void defaultConfig() throws Exception {
		context.register(Config.class, WebMvcAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class, HdivAutoConfiguration.class);
		context.refresh();
		HDIVConfig config = context.getBean(HDIVConfig.class);
		assertTrue(config.isStartPage("/", Method.GET));
		assertTrue(config.isStartPage("/example.js", Method.GET));

		assertTrue(config.isStartParameter("_csrf"));

		EditableDataValidationResult result = config.getEditableDataValidationProvider().validate("/", "paramName",
				new String[] { "paramValue" }, "text");
		assertTrue(result.isValid());
		result = config.getEditableDataValidationProvider().validate("/", "paramName", new String[] { "<script>XSS</script>" }, "text");
		assertFalse(result.isValid());
		assertEquals("simpleXSS", result.getValidationId());
	}

	@Configuration
	public static class Config {

		@Bean
		public EmbeddedServletContainerFactory containerFactory() {
			return containerFactory;
		}

		@Bean
		public EmbeddedServletContainerCustomizerBeanPostProcessor embeddedServletContainerCustomizerBeanPostProcessor() {
			return new EmbeddedServletContainerCustomizerBeanPostProcessor();
		}

	}

}
