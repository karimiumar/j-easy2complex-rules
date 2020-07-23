package com.umar.apps.rule.infra.dao.api.core;

import com.umar.simply.jdbc.dml.operations.SelectOp;
import com.umar.simply.jdbc.dml.operations.api.SqlFunctions;

import javax.enterprise.inject.Produces;

public class SelectFunction {

    @Produces
    public SqlFunctions<SelectOp> select() {
        return SelectOp.create();
    }
}
