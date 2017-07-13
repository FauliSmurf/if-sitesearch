package net.loxal.quizzer.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Collections;
import java.util.List;

@Document(indexName = "hit", type = "hit", shards = 1, replicas = 0, refreshInterval = "-1")
public class Hit {
    @Id
    private String id;

    private List<Object> facets = Collections.emptyList();

    public List<Object> getFacets() {
        return facets;
    }

    public void setFacets(List<Object> facets) {
        this.facets = facets;
    }

    private List<Result> results;

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    private String query;

    public Hit() {
    }

    private String title;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Hit(String query, List<Result> results) {
        this.query = query;
        this.results = results;
    }
}
