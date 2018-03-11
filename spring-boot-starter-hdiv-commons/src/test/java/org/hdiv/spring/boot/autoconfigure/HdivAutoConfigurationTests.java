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

import static org.assertj.core.api.Assertions.assertThat;

import org.hdiv.config.HDIVConfig;
import org.hdiv.util.Method;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.MockServletWebServerFactory;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.server.WebServerFactoryCustomizerBeanPostProcessor;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tests for {@link HdivAutoConfiguration}.
 */
public class HdivAutoConfigurationTests {

	private static final MockServletWebServerFactory webServerFactory = new MockServletWebServerFactory();

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(WebMvcAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class,
					PropertyPlaceholderAutoConfiguration.class, HdivAutoConfiguration.class))
			.withUserConfiguration(Config.class);

	@Test
	public void defaultConfig() throws Exception {
		contextRunner.run((context) -> {
			assertThat(context).getBeans(HDIVConfig.class).hasSize(1);
			assertThat(context.getBean(HDIVConfig.class).isStartPage("/", Method.GET)).isTrue();
			assertThat(context.getBean(HDIVConfig.class).isStartPage("/example.js", Method.GET)).isTrue();
			assertThat(context.getBean(HDIVConfig.class).isStartParameter("_csrf")).isTrue();

			assertThat(context.getBean(HDIVConfig.class).getEditableDataValidationProvider()
					.validate("/", "paramName", new String[] { "paramValue" }, "text").isValid()).isTrue();
			assertThat(context.getBean(HDIVConfig.class).getEditableDataValidationProvider()
					.validate("/", "paramName", new String[] { "<script>XSS</script>" }, "text").isValid()).isFalse();
			assertThat(context.getBean(HDIVConfig.class).getEditableDataValidationProvider()
					.validate("/", "paramName", new String[] { "<script>XSS</script>" }, "text").getValidationId()).isEqualTo("simpleXSS");
		});
	}

	@Configuration
	public static class Config {

		@Bean
		public ServletWebServerFactory webServerFactory() {
			return webServerFactory;
		}

		@Bean
		public WebServerFactoryCustomizerBeanPostProcessor ServletWebServerCustomizerBeanPostProcessor() {
			return new WebServerFactoryCustomizerBeanPostProcessor();
		}

	}

}
