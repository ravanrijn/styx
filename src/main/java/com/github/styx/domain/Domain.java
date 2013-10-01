package com.github.styx.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Domain extends Identifiable {

    public Domain(String id, String name) {
        super(id, name);
    }

}
