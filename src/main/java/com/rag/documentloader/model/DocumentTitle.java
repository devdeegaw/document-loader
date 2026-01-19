package com.rag.documentloader.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class DocumentTitle {

    @JsonProperty("title")
    private String title;

    public String getNormalizedTitle() {
        return title != null ? title.replaceAll("[^a-zA-Z0-9]", "_") : "unknown";
    }

}