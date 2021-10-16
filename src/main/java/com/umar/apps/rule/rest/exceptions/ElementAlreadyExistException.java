package com.umar.apps.rule.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 *  An exception to denote HTTP status code 409.
 *  {@link ElementAlreadyExistException} is thrown when a request is received
 *  for to create an element which already exist.
 *
 * @author Mohammad Umar Ali Karimi (karimiumar@gmail.com)
 */
public class ElementAlreadyExistException extends ResponseStatusException {

    /**
     * Constructor for {@link ElementAlreadyExistException}
     *
     * @param reason The reason to be used.
     */
    public ElementAlreadyExistException(String reason) {
        super(HttpStatus.CONFLICT, reason);
    }
}
