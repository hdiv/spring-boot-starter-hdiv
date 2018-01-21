/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.context.embedded;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.boot.web.servlet.ServletContextInitializer;

/**
 * Mock {@link EmbeddedServletContainerFactory}.
 *
 * @author Phillip Webb
 */
public class MockEmbeddedServletContainerFactory extends AbstractEmbeddedServletContainerFactory {

	private MockEmbeddedServletContainer container;

	@Override
	public EmbeddedServletContainer getEmbeddedServletContainer(final ServletContextInitializer... initializers) {
		container = spy(new MockEmbeddedServletContainer(initializers, getPort()));
		return container;
	}

	public MockEmbeddedServletContainer getContainer() {
		return container;
	}

	public ServletContext getServletContext() {
		return getContainer() == null ? null : getContainer().servletContext;
	}

	public RegisteredServlet getRegisteredServlet(final int index) {
		return getContainer() == null ? null : getContainer().getRegisteredServlets().get(index);
	}

	public RegisteredFilter getRegisteredFilter(final int index) {
		return getContainer() == null ? null : getContainer().getRegisteredFilters().get(index);
	}

	public static class MockEmbeddedServletContainer implements EmbeddedServletContainer {

		private ServletContext servletContext;

		private final ServletContextInitializer[] initializers;

		private final List<RegisteredServlet> registeredServlets = new ArrayList<RegisteredServlet>();

		private final List<RegisteredFilter> registeredFilters = new ArrayList<RegisteredFilter>();

		private final int port;

		public MockEmbeddedServletContainer(final ServletContextInitializer[] initializers, final int port) {
			this.initializers = initializers;
			this.port = port;
			initialize();
		}

		private void initialize() {
			try {
				servletContext = mock(ServletContext.class);
				given(servletContext.addServlet(anyString(), (Servlet) anyObject())).willAnswer(new Answer<ServletRegistration.Dynamic>() {
					@Override
					public ServletRegistration.Dynamic answer(final InvocationOnMock invocation) throws Throwable {
						RegisteredServlet registeredServlet = new RegisteredServlet((Servlet) invocation.getArguments()[1]);
						registeredServlets.add(registeredServlet);
						return registeredServlet.getRegistration();
					}
				});
				given(servletContext.addFilter(anyString(), (Filter) anyObject())).willAnswer(new Answer<FilterRegistration.Dynamic>() {
					@Override
					public FilterRegistration.Dynamic answer(final InvocationOnMock invocation) throws Throwable {
						RegisteredFilter registeredFilter = new RegisteredFilter((Filter) invocation.getArguments()[1]);
						registeredFilters.add(registeredFilter);
						return registeredFilter.getRegistration();
					}
				});
				given(servletContext.getInitParameterNames()).willReturn(MockEmbeddedServletContainer.<String> emptyEnumeration());
				given(servletContext.getAttributeNames()).willReturn(MockEmbeddedServletContainer.<String> emptyEnumeration());
				given(servletContext.getNamedDispatcher("default")).willReturn(mock(RequestDispatcher.class));
				for (ServletContextInitializer initializer : initializers) {
					initializer.onStartup(servletContext);
				}
			}
			catch (ServletException ex) {
				throw new RuntimeException(ex);
			}
		}

		@SuppressWarnings("unchecked")
		public static <T> Enumeration<T> emptyEnumeration() {
			return (Enumeration<T>) EmptyEnumeration.EMPTY_ENUMERATION;
		}

		private static class EmptyEnumeration<E> implements Enumeration<E> {
			static final EmptyEnumeration<Object> EMPTY_ENUMERATION = new EmptyEnumeration<Object>();

			@Override
			public boolean hasMoreElements() {
				return false;
			}

			@Override
			public E nextElement() {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void start() throws EmbeddedServletContainerException {
		}

		@Override
		public void stop() {
			servletContext = null;
			registeredServlets.clear();
		}

		public Servlet[] getServlets() {
			Servlet[] servlets = new Servlet[registeredServlets.size()];
			for (int i = 0; i < servlets.length; i++) {
				servlets[i] = registeredServlets.get(i).getServlet();
			}
			return servlets;
		}

		public List<RegisteredServlet> getRegisteredServlets() {
			return registeredServlets;
		}

		public List<RegisteredFilter> getRegisteredFilters() {
			return registeredFilters;
		}

		@Override
		public int getPort() {
			return port;
		}
	}

	public static class RegisteredServlet {

		private final Servlet servlet;

		private final ServletRegistration.Dynamic registration;

		public RegisteredServlet(final Servlet servlet) {
			this.servlet = servlet;
			registration = mock(ServletRegistration.Dynamic.class);
		}

		public ServletRegistration.Dynamic getRegistration() {
			return registration;
		}

		public Servlet getServlet() {
			return servlet;
		}
	}

	public static class RegisteredFilter {

		private final Filter filter;

		private final FilterRegistration.Dynamic registration;

		public RegisteredFilter(final Filter filter) {
			this.filter = filter;
			registration = mock(FilterRegistration.Dynamic.class);
		}

		public FilterRegistration.Dynamic getRegistration() {
			return registration;
		}

		public Filter getFilter() {
			return filter;
		}
	}
}
