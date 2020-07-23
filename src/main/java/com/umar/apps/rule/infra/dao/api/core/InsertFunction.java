package com.umar.apps.rule.infra.dao.api.core;

import com.umar.simply.jdbc.dml.operations.InsertOp;

import javax.enterprise.inject.Produces;

public class InsertFunction {

    @Produces
    public com.umar.simply.jdbc.dml.operations.api.InsertFunction insert() {
        return InsertOp.create();
    }
}
