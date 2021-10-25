package com.umar.apps.rule.web.rest.events;

import org.springframework.context.ApplicationEvent;

import javax.servlet.http.HttpServletResponse;

public class SingleResourceRetrievedEvent extends ApplicationEvent {

    private final HttpServletResponse response;

    public SingleResourceRetrievedEvent(final Object source, final HttpServletResponse response) {
        super(source);
        this.response = response;
    }

    public HttpServletResponse getResponse() {
        return response;
    }
}
