package com.rag.documentloader.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentTitle {

    @JsonProperty("title")
    private String title;

    // Default constructor (required for Jackson)
    public DocumentTitle() {
    }

    public DocumentTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNormalizedTitle() {
        return title != null ? title.replaceAll("[^a-zA-Z0-9]", "_") : "unknown";
    }

    @Override
    public String toString() {
        return "DocumentTitle{" +
                "title='" + title + '\'' +
                '}';
    }
}