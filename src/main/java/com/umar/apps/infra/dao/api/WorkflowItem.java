package com.umar.apps.infra.dao.api;

import java.io.Serializable;

/**
 * Every persistent entity must inherit from WorkflowItem.
 * WorkflowItem is used by JPA to manage persistent entities.
 *
 * @param <ID> The Id type of the persistent entity Integer, Long
 */
public interface WorkflowItem<ID extends Serializable> {
    ID getId();
}
