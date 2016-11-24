package info.jtrac.wicket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

        final Map<String, String> links = getJtrac().loadAllStoredSearch();

        final SimpleAttributeModifier sam = new SimpleAttributeModifier("class", "alt");
        List<String> names = new ArrayList<String>(links.keySet());


        add(new ListView("links", names) {
            protected void populateItem(ListItem listItem) {
                final String name = (String) listItem.getModelObject();
                final String query = links.get(name);

                if (name.equals(selectedParam)) {
                    listItem.add(new SimpleAttributeModifier("class", "selected"));
                } else if(listItem.getIndex() % 2 == 1) {
                    listItem.add(sam);
                }

                listItem.add(new Label("name", name));
                listItem.add(new Label("query", query));

                // Normal text value to be edited
                listItem.add(new Link("link") {
                    public void onClick() {
                        setResponsePage(new StoredSearchFormPage(name, query));
                    }
                });
            }
        });

    }
}
