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

import javax.annotation.PostConstruct;

import org.hdiv.config.xml.EditableValidationsBeanDefinitionParser;
import org.hdiv.web.validator.EditableParameterValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

@Configuration
public class EditableValidationConfiguration {

	@Autowired
	@Qualifier(EditableValidationsBeanDefinitionParser.EDITABLE_VALIDATOR_BEAN_NAME)
	private EditableParameterValidator editableParameterValidator;

	@Autowired
	private RequestMappingHandlerAdapter handlerAdapter;

	@PostConstruct
	public void initEditableValidation() {

		// Add HDIVs Validator for editable validation to Spring MVC
		ConfigurableWebBindingInitializer initializer = (ConfigurableWebBindingInitializer) handlerAdapter
				.getWebBindingInitializer();
		if (initializer.getValidator() != null) {
			// Wrap existing validator
			editableParameterValidator.setInnerValidator(initializer.getValidator());
		}
		initializer.setValidator(editableParameterValidator);

	}
}
