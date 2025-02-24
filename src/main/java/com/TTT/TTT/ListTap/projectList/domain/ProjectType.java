package com.TTT.TTT.ListTap.projectList.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectType {
    DB,BACKEND,FRONTEND,DEVOPS,FINAL;

    @JsonCreator
    public static ProjectType from(String value){
        return ProjectType.valueOf(value.toUpperCase());
    }
    @JsonValue
    public String toValue(){
        return this.name();
    }
}
