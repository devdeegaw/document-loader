package com.rag.documentloader.model;

public record DocumentTitle(String title) {

    public String getNormalizedTitle() {
        return title != null ? title.toLowerCase().replace(" ", "_") : "";
    }

}
