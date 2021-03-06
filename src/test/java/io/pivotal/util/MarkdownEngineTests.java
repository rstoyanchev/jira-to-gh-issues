/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.pivotal.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Rob Winch
 *
 */
public class MarkdownEngineTests {
	MarkdownEngine engine;

	@Before
	public void setup() {
		engine = new MarkdownEngine();
		engine.setJiraBaseUrl("https://jira.spring.io");
	}

	@Test
	public void h1() {
		String body = "h1. Some Text\nMore";
		assertThat(engine.convert(body)).isEqualTo("# Some Text\nMore");
	}

	@Test
	public void h2() {
		String body = "h2. Some Text\nMore";
		assertThat(engine.convert(body)).isEqualTo("## Some Text\nMore");
	}

	@Test
	public void h3() {
		String body = "h3. Some Text\nMore";
		assertThat(engine.convert(body)).isEqualTo("### Some Text\nMore");
	}

	@Test
	public void h4() {
		String body = "h4. Some Text\nMore";
		assertThat(engine.convert(body)).isEqualTo("#### Some Text\nMore");
	}

	@Test
	public void h5() {
		String body = "h5. Some Text\nMore";
		assertThat(engine.convert(body)).isEqualTo("##### Some Text\nMore");
	}

	@Test
	public void h6() {
		String body = "h6. Some Text\nMore";
		assertThat(engine.convert(body)).isEqualTo("###### Some Text\nMore");
	}

	@Test
	public void twoLinks() {
		String body =
				" a [fix|https://fisheye.springsource.org/changelog/spring-security?cs=ffe2834f4cd900d99c4a490af62613d087c9aceb] the [Spring Security forums|http://forum.springsource.org/forumdisplay.php?33-Security]";

		assertThat(engine.convert(body)).contains("[fix](https://fisheye.springsource.org/changelog/spring-security?cs=ffe2834f4cd900d99c4a490af62613d087c9aceb)");
	}

	@Test
	public void userMention() {
		String body = "[~awilkinson] Thanks for the report. This should now be fixed.";

		assertThat(engine.convert(body)).startsWith("[awilkinson](https://jira.spring.io/secure/ViewProfile.jspa?name=awilkinson)");
	}

	@Test
	public void inlineCode() {
		String body = "The {{AttributesMapper}} methods are excluded on purpose – we normally discourage usage of {{AttributesMapper}} since there is typically no reason for using that rather than the {{ContextMapper}}.\n" +
				"\n" +
				"For the occasion where you need more control and want to access the more generic methods in {{LdapTemplate}}, these are accessible using {{SimpleLdapTemplate#getLdapOperations()}}, completely analogous with {{SimpleJdbcTemplate}} and {{JdbcTemplate}}";

		assertThat(engine.convert(body)).startsWith("The `AttributesMapper` methods are excluded");
	}

	@Test
	public void blockCode() {
		String body = "I've made a custom NtlmProcessingFilterEntryPoint that provides the functionality. Something probably needs to be done in the NtlmProcessingFilter's logon method  for a less hack'ish fix.\n" +
				"{code}\n" +
				"class AcmeNtlmProcessingFilterEntryPoint extends NtlmProcessingFilterEntryPoint {\n" +
				"	public static final String STATE_ATTR = \"SpringSecurityNtlm\";\n" +
				"\n" +
				"\n" +
				"	public void commence(final ServletRequest request, final ServletResponse response, final AuthenticationException authException) throws IOException, ServletException {\n" +
				"		if (authException instanceof BadCredentialsException) {\n" +
				"			((HttpServletRequest) request).getSession().removeAttribute(STATE_ATTR);\n" +
				"			authException = new NtlmBeginHandshakeException();\n" +
				"		}\n" +
				"		super.commence(request, response, authException)\n" +
				"	}\n" +
				"\n" +
				"{code}";

		String convert = engine.convert(body);
		assertThat(convert).endsWith("``` ");
	}

	@Test
	public void blockCodeWithTitle() {
		String body = "{code:title=web.xml}\n" +
				"    <filter-mapping>\n" +
				"      <filter-name>springSecurityFilterChain</filter-name>\n" +
				"      <url-pattern>/*</url-pattern>\n" +
				"      <dispatcher>REQUEST</dispatcher>\n" +
				"      <dispatcher>FORWARD</dispatcher>\n" +
				"    </filter-mapping>\n{code}";

		String convert = engine.convert(body);
		assertThat(convert).isEqualTo("``` \n" +
				"    <filter-mapping>\n" +
				"      <filter-name>springSecurityFilterChain</filter-name>\n" +
				"      <url-pattern>/*</url-pattern>\n" +
				"      <dispatcher>REQUEST</dispatcher>\n" +
				"      <dispatcher>FORWARD</dispatcher>\n" +
				"    </filter-mapping>\n" +
				"``` ");
	}

	@Test
	public void blockCodeWithTypeAndTitle() {
		String body = "{code:xml|title=title}\n" +
				"    <filter-mapping>\n" +
				"      <filter-name>springSecurityFilterChain</filter-name>\n" +
				"      <url-pattern>/*</url-pattern>\n" +
				"      <dispatcher>REQUEST</dispatcher>\n" +
				"      <dispatcher>FORWARD</dispatcher>\n" +
				"    </filter-mapping>\n{code}";

		String convert = engine.convert(body);
		assertThat(convert).isEqualTo("```xml \n" +
				"    <filter-mapping>\n" +
				"      <filter-name>springSecurityFilterChain</filter-name>\n" +
				"      <url-pattern>/*</url-pattern>\n" +
				"      <dispatcher>REQUEST</dispatcher>\n" +
				"      <dispatcher>FORWARD</dispatcher>\n" +
				"    </filter-mapping>\n" +
				"``` ");
	}

	@Test
	public void blockCodeNoNewLineAfterOrBefore() {
		String body = "{code}" +
				"    <filter-mapping>\n" +
				"      <filter-name>springSecurityFilterChain</filter-name>\n" +
				"      <url-pattern>/*</url-pattern>\n" +
				"      <dispatcher>REQUEST</dispatcher>\n" +
				"      <dispatcher>FORWARD</dispatcher>\n" +
				"    </filter-mapping>{code}";

		String convert = engine.convert(body);
		assertThat(convert).isEqualTo("```\n" +
				"    <filter-mapping>\n" +
				"      <filter-name>springSecurityFilterChain</filter-name>\n" +
				"      <url-pattern>/*</url-pattern>\n" +
				"      <dispatcher>REQUEST</dispatcher>\n" +
				"      <dispatcher>FORWARD</dispatcher>\n" +
				"    </filter-mapping>\n" +
				"```");
	}
}
