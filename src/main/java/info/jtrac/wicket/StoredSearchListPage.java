package info.jtrac.wicket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import info.jtrac.domain.StoredSearch;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;


/**
 * Created by ncrappe on 8/09/2015.
 */
public class StoredSearchListPage extends BasePage {
    /**
     * Constructor
     *
     * @param selectedParam
     */
    public StoredSearchListPage(final String selectedParam) {

        add(new Link("create") {
            public void onClick() {
                StoredSearchFormPage page = new StoredSearchFormPage();
                setResponsePage(page);
            }
        });

        List<StoredSearch> links = getJtrac().loadAllStoredSearch();

        final SimpleAttributeModifier sam = new SimpleAttributeModifier("class", "alt");
       // List<String> names = new ArrayList<String>(links.keySet());


        add(new ListView("links", links) {
            protected void populateItem(ListItem listItem) {
                final StoredSearch storedSearch = (StoredSearch) listItem.getModelObject();

                if (storedSearch.getName().equals(selectedParam)) {
                    listItem.add(new SimpleAttributeModifier("class", "selected"));
                } else if(listItem.getIndex() % 2 == 1) {
                    listItem.add(sam);
                }
                listItem.add(new Label("id", String.valueOf(storedSearch.getId())));
                listItem.add(new Label("name", storedSearch.getName()));
                listItem.add(new Label("query", storedSearch.getQuery()));

                // Normal text value to be edited
                listItem.add(new Link("link") {
                    public void onClick() {
                        setResponsePage(new StoredSearchFormPage(storedSearch.getName(), storedSearch.getQuery() ,storedSearch.getId()));
                    }
                });
            }
        });

    }
}
