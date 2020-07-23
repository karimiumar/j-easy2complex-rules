package com.umar.apps.rule.infra.dao.api.core;

import com.umar.simply.jdbc.dml.operations.DeleteOp;

import javax.enterprise.inject.Produces;

public class DeleteFunction {

    @Produces
    public com.umar.simply.jdbc.dml.operations.api.DeleteFunction delete() {
        return DeleteOp.create();
    }
}
