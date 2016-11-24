package info.jtrac.domain;

import java.io.Serializable;

/**
 * Created by ncrappe on 8/09/2015.
 */
public class StoredSearch implements Serializable {

    private Long id;
    private String name;
    private String query;

    public StoredSearch() {
    }

    public StoredSearch(Long id, String name, String query) {
        this.id = id;
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

    public Long getId() {
        if(id == null){
            return new Long(0);
        }
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
