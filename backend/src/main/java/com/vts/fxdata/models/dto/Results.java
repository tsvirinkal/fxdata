package com.vts.fxdata.models.dto;

import java.util.List;

public class Results {
    private List<Result> results;
    private List<ArchivedResult> archives;

    public Results(List<Result> results, List<ArchivedResult> archives) {
        this.results = results;
        this.archives = archives;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public List<ArchivedResult> getArchives() {
        return archives;
    }

    public void setArchives(List<ArchivedResult> archives) {
        this.archives = archives;
    }
}
