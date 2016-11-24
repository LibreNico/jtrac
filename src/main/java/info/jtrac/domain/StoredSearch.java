package info.jtrac.domain;

import java.io.Serializable;

/**
 * Created by ncrappe on 8/09/2015.
 */
public class StoredSearch implements Serializable {

    private String name;
    private String query;

    public StoredSearch() {
    }

    public StoredSearch(String name, String query) {
        this.query = query;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
