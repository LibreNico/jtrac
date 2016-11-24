package info.jtrac.wicket;

import info.jtrac.domain.StoredSearch;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
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
    public StoredSearchFormPage(String name, String query, Long id) {
        add(new StoredSearchForm("form", name, query, id));
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
        private Long idSearchLink;

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

        public StoredSearchForm(String pageId) {
            this(pageId, new String(), new String(), new Long(-1));
        }
        public StoredSearchForm(String pageId, final String name, final String query, final Long idSearchLink) {

            super(pageId);

            this.name = name;
            this.query = query;
            this.idSearchLink = idSearchLink;

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
                            getJtrac().removeStoredSearch(idSearchLink);
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

            final HiddenField idSearchLinkField =new HiddenField("idSearchLink");
            add(idSearchLinkField);

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
            Long idLink = null;
            if(!this.idSearchLink.equals(new Long(-1))){
                idLink = this.idSearchLink;
            }

            System.out.println("idLink:"+String.valueOf(idLink));


            getJtrac().storeStoredSearch(new StoredSearch(idLink, name, query));
            setResponsePage(new StoredSearchListPage(name));
        }

        public void setId(long idSearchLink) {
            this.idSearchLink = idSearchLink;
        }


    }
}