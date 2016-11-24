package info.jtrac.wicket;

import info.jtrac.domain.Config;
import info.jtrac.domain.StoredSearch;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.BoundCompoundPropertyModel;
import org.apache.wicket.markup.html.form.Button;



/**
 * Created by ncrappe on 8/09/2015.
 */
public class StoredSearchFormPage extends BasePage {
    /**
     * Constructor
     *
     * @param name
     * @param query
     */
    public StoredSearchFormPage(String name, String query) {
        add(new StoredSearchForm("form", name, query));
    }

    public StoredSearchFormPage() {
        add(new StoredSearchForm("form"));
    }

    /**
     * wicket form
     */
    private class StoredSearchForm extends Form {

        private String name;
        private String query;

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

        public StoredSearchForm(String id) {
            this(id, new String(), new String());
        }
        public StoredSearchForm(String id, final String name, final String query) {

            super(id);

            this.name = name;
            this.query = query;

            final BoundCompoundPropertyModel model = new BoundCompoundPropertyModel(this);
            setModel(model);

            // delete button only if edit ======================================
            Button delete = new Button("delete") {
                @Override
                public void onSubmit() {

                    String heading = localize("storedsearch_form.confirm");
                    String line1 = localize("storedsearch_form.line1");
                    String warning = localize("storedsearch_form.line2");
                    ConfirmPage confirm = new ConfirmPage(StoredSearchFormPage.this, heading, warning, new String[] {line1}) {
                        public void onConfirm() {
                            getJtrac().removeStoredSearch(name);
                            setResponsePage(new StoredSearchListPage(null));
                        }
                    };
                    setResponsePage(confirm);
                }
            };
            delete.setDefaultFormProcessing(false);

            if(!getPrincipal().isSuperUser()) {
                delete.setVisible(false);
            }
            add(delete);


            // nameField ======================================================
            final TextField nameField = new TextField("name");
            nameField.setRequired(true);
            nameField.add(new ErrorHighlighter());
            add(nameField);

            // queryField ======================================================
            final TextField queryField = new TextField("query");
            queryField.setRequired(true);
            queryField.add(new ErrorHighlighter());
            add(queryField);

            // cancel ==========================================================
            add(new Link("cancel") {
                public void onClick() {
                    setResponsePage(new StoredSearchListPage(name));
                }
            });
        }

        @Override
        protected void onSubmit() {
            getJtrac().storeStoredSearch(new StoredSearch(name, query));
            setResponsePage(new StoredSearchListPage(name));
        }
    }
}