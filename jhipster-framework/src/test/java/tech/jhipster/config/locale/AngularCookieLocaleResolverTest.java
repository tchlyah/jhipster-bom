/*
 * Copyright 2016-2022 the original author or authors from the JHipster project.
 *
 * This file is part of the JHipster project, see https://www.jhipster.tech/
 * for more information.
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

package tech.jhipster.config.locale;

import tech.jhipster.test.LogbackRecorder;
import tech.jhipster.test.LogbackRecorder.Event;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.TimeZoneAwareLocaleContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.web.servlet.i18n.CookieLocaleResolver.*;

class AngularCookieLocaleResolverTest {

    private static final Locale LOCALE_DEFAULT = Locale.UK;
    private static final Locale LOCALE_CUSTOM = Locale.FRANCE;
    private static final TimeZone TIMEZONE_CUSTOM = TimeZone.getTimeZone(ZoneId.of("GMT"));
    private static final TimeZone TIMEZONE_DEFAULT = TimeZone.getTimeZone(ZoneId.of("GMT+01:00"));

    private HttpServletRequest request;
    private HttpServletResponse response;
    private AngularCookieLocaleResolver resolver;
    private LogbackRecorder recorder;

    @Captor
    private ArgumentCaptor<Cookie> captor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        request = spy(new MockHttpServletRequest());

        response = spy(MockHttpServletResponse.class);
        resolver = new AngularCookieLocaleResolver();
        resolver.setDefaultLocale(LOCALE_DEFAULT);
        resolver.setDefaultTimeZone(TIMEZONE_DEFAULT);

        recorder = LogbackRecorder.forClass(resolver.getClass()).reset().capture("DEBUG");
    }

    @AfterEach
    void teardown() {
        recorder.release();
    }

    @Test
    void testDefaults() {
        when(request.getCookies()).thenReturn(new Cookie[]{});

        LocaleContext context = resolver.resolveLocaleContext(request);

        assertThat(context).isNotNull();
        assertThat(context).isInstanceOf(TimeZoneAwareLocaleContext.class);
        assertThat(((TimeZoneAwareLocaleContext) context).getLocale()).isEqualTo(LOCALE_DEFAULT);
        assertThat(((TimeZoneAwareLocaleContext) context).getTimeZone()).isEqualTo(TIMEZONE_DEFAULT);

        List<Event> events = recorder.play();
        assertThat(events).isEmpty();
    }

    @Test
    void testPresets() {
        when(request.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME)).thenReturn(LOCALE_DEFAULT);
        when(request.getAttribute(TIME_ZONE_REQUEST_ATTRIBUTE_NAME)).thenReturn(TIMEZONE_DEFAULT);

        LocaleContext context = resolver.resolveLocaleContext(request);

        assertThat(context).isNotNull();
        assertThat(context).isInstanceOf(TimeZoneAwareLocaleContext.class);
        Locale locale = ((TimeZoneAwareLocaleContext) context).getLocale();
        TimeZone zone = ((TimeZoneAwareLocaleContext) context).getTimeZone();

        assertThat(locale).isNotNull();
        assertThat(locale).isEqualTo(LOCALE_DEFAULT);
        assertThat(zone).isEqualTo(TIMEZONE_DEFAULT);

        List<Event> events = recorder.play();
        assertThat(events).isEmpty();
    }

    @Test
    void testLocale() {
        String value = LOCALE_CUSTOM.toString();
        Cookie cookie = new Cookie(DEFAULT_COOKIE_NAME, value);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        Locale locale = resolver.resolveLocale(request);

        assertThat(locale).isNotNull();
        assertThat(locale).isEqualTo(LOCALE_CUSTOM);

        List<Event> events = recorder.play();
        assertThat(events).isEmpty();
    }

    @Test
    void testCookieLocaleWithQuotes() {
        String value = resolver.quote(LOCALE_CUSTOM.toString());
        Cookie cookie = new Cookie(DEFAULT_COOKIE_NAME, value);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        Locale locale = resolver.resolveLocale(request);

        assertThat(locale).isNotNull();
        assertThat(locale).isEqualTo(LOCALE_CUSTOM);

        List<Event> events = recorder.play();
        assertThat(events).isEmpty();
    }

    @Test
    void testTimeZone() {
        String value = "- " + TIMEZONE_CUSTOM.getID();
        Cookie cookie = new Cookie(DEFAULT_COOKIE_NAME, value);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        LocaleContext context = resolver.resolveLocaleContext(request);

        assertThat(context).isNotNull();
        assertThat(context).isInstanceOf(TimeZoneAwareLocaleContext.class);
        Locale locale = ((TimeZoneAwareLocaleContext) context).getLocale();
        TimeZone zone = ((TimeZoneAwareLocaleContext) context).getTimeZone();
        assertThat(locale).isEqualTo(LOCALE_DEFAULT);
        assertThat(zone).isEqualTo(TIMEZONE_CUSTOM);

        List<Event> events = recorder.play();
        assertThat(events).isEmpty();
    }

    @Test
    void testTimeZoneWithQuotes() {
        String value = resolver.quote("- " + TIMEZONE_CUSTOM.getID());
        Cookie cookie = new Cookie(DEFAULT_COOKIE_NAME, value);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        LocaleContext context = resolver.resolveLocaleContext(request);

        assertThat(context).isNotNull();
        assertThat(context).isInstanceOf(TimeZoneAwareLocaleContext.class);
        Locale locale = ((TimeZoneAwareLocaleContext) context).getLocale();
        TimeZone zone = ((TimeZoneAwareLocaleContext) context).getTimeZone();
        assertThat(locale).isEqualTo(LOCALE_DEFAULT);
        assertThat(zone).isEqualTo(TIMEZONE_CUSTOM);

        List<Event> events = recorder.play();
        assertThat(events).isEmpty();
    }

    @Test
    void testLocaleAndTimeZone() {
        String value = LOCALE_CUSTOM + " " + TIMEZONE_CUSTOM.getID();
        Cookie cookie = new Cookie(DEFAULT_COOKIE_NAME, value);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        LocaleContext context = resolver.resolveLocaleContext(request);

        assertThat(context).isNotNull();
        assertThat(context).isInstanceOf(TimeZoneAwareLocaleContext.class);
        Locale locale = ((TimeZoneAwareLocaleContext) context).getLocale();
        TimeZone zone = ((TimeZoneAwareLocaleContext) context).getTimeZone();
        assertThat(locale).isEqualTo(LOCALE_CUSTOM);
        assertThat(zone).isEqualTo(TIMEZONE_CUSTOM);

        List<Event> events = recorder.play();
        assertThat(events).isEmpty();
    }

    @Test
    void testLocaleAndTimeZoneWithQuotes() {
        String value = resolver.quote(LOCALE_CUSTOM.toString() + " " + TIMEZONE_CUSTOM.getID());
        Cookie cookie = new Cookie(DEFAULT_COOKIE_NAME, value);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        LocaleContext context = resolver.resolveLocaleContext(request);

        assertThat(context).isNotNull();
        assertThat(context).isInstanceOf(TimeZoneAwareLocaleContext.class);
        Locale locale = ((TimeZoneAwareLocaleContext) context).getLocale();
        TimeZone zone = ((TimeZoneAwareLocaleContext) context).getTimeZone();
        assertThat(locale).isEqualTo(LOCALE_CUSTOM);
        assertThat(zone).isEqualTo(TIMEZONE_CUSTOM);

        List<Event> events = recorder.play();
        assertThat(events).isEmpty();
    }

    @Test
    void testCookieWithQuotes() {
        recorder.release();
        recorder.capture("TRACE");

        String value = LOCALE_CUSTOM.toString();
        resolver.addCookie(response, value);

        verify(response).addCookie(captor.capture());

        Cookie cookie = captor.getValue();
        assertThat(cookie.getName()).isEqualTo(DEFAULT_COOKIE_NAME);
        assertThat(cookie.getValue()).isEqualTo(resolver.quote(value));

        List<Event> events = recorder.play();
        assertThat(events).hasSize(1);

        Event event = events.get(0);
        assertThat(event.getLevel()).isEqualTo("TRACE");
        assertThat(event.getMessage()).isEqualTo("Added cookie [" + DEFAULT_COOKIE_NAME + "=" +
            resolver.quote(value) + "]");
        assertThat(event.getThrown()).isNull();
    }

    @Test
    void testTraceLogLocale() {
        recorder.release();
        recorder.capture("TRACE");

        String value = LOCALE_CUSTOM.toString();
        Cookie cookie = new Cookie(DEFAULT_COOKIE_NAME, value);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        Locale locale = resolver.resolveLocale(request);

        List<Event> events = recorder.play();
        assertThat(events).hasSize(1);

        Event event = events.get(0);
        assertThat(event.getLevel()).isEqualTo("TRACE");
        assertThat(event.getMessage()).isEqualTo("Parsed cookie value [" + value + "] into locale '" + locale + "'");
        assertThat(event.getThrown()).isNull();
    }

    @Test
    void testTraceLogLocaleAndTimeZone() {
        recorder.release();
        recorder.capture("TRACE");

        String value = LOCALE_CUSTOM + " " + TIMEZONE_CUSTOM.getID();
        Cookie cookie = new Cookie(DEFAULT_COOKIE_NAME, value);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        LocaleContext context = resolver.resolveLocaleContext(request);

        assertThat(context).isInstanceOf(TimeZoneAwareLocaleContext.class);
        Locale locale = ((TimeZoneAwareLocaleContext) context).getLocale();
        TimeZone zone = ((TimeZoneAwareLocaleContext) context).getTimeZone();

        List<Event> events = recorder.play();
        assertThat(events).hasSize(1);

        Event event = events.get(0);
        assertThat(event.getLevel()).isEqualTo("TRACE");
        assertThat(event.getMessage()).isEqualTo("Parsed cookie value [" + value + "] into locale '" + locale + "' " +
            "and time zone '" + zone.getID() + "'");
        assertThat(event.getThrown()).isNull();
    }
}
