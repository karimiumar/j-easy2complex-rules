package com.umar.apps.rule.infra.dao.api.core;

import com.umar.simply.jdbc.dml.operations.UpdateOp;

import javax.enterprise.inject.Produces;

public class UpdateFunction {

    @Produces
    public com.umar.simply.jdbc.dml.operations.api.UpdateFunction update() {
        return new UpdateOp();
    }
}
