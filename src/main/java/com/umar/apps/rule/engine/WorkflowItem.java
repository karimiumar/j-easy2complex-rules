package com.umar.apps.rule.engine;

import java.io.Serializable;

public interface WorkflowItem<ID extends Serializable>{
    ID getId();
}
