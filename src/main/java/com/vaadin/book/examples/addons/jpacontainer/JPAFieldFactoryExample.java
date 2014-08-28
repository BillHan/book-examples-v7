package com.vaadin.book.examples.addons.jpacontainer;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.fieldfactory.FieldFactory;
import com.vaadin.addon.jpacontainer.fieldfactory.MasterDetailEditor;
import com.vaadin.book.examples.BookExampleBundle;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.VerticalLayout;

public class JPAFieldFactoryExample extends CustomComponent implements BookExampleBundle {
    private static final long serialVersionUID = -3205020480634478985L;
    String context;

    public void init(String context) {
        VerticalLayout layout = new VerticalLayout();
        
        if ("masterdetail".equals(context))
            masterdetail(layout);
        else if ("formonetomany".equals(context))
            formonetomany(layout);
        else if ("tableonetomany".equals(context))
            tableonetomany(layout);
        else if ("tableonetomany2".equals(context))
            tableonetomany2(layout);
        
        setCompositionRoot(layout);
    }
    

    public static final String formonetomanyDescription =
            "<h1>Form with a One-to-Many Relationship</h1>"+
            "<p>The <b>Country</b> has <tt>@OneToMany</tt> relationship with the <b>Person</b> entity type " +
            "so the <b>FieldFactory</b> creates a <b>MasterDetailEditor</b> to edit it.</p>";

    void formonetomany(VerticalLayout layout) {
        // Populate with example data
        JPAContainerExample.insertExampleData();

        // BEGIN-EXAMPLE: jpacontainer.fieldfactory.formonetomany
        // Have a persistent container
        final JPAContainer<Country> countries =
            JPAContainerFactory.make(Country.class, "book-examples");

        // For selecting an item to edit
        final ComboBox countrySelect =
                new ComboBox("Select a Country", countries);
        countrySelect.setItemCaptionMode(ItemCaptionMode.PROPERTY);
        countrySelect.setItemCaptionPropertyId("name");

        // Country Editor
        final Form  countryForm  = new Form();
        countryForm.setCaption("Country Editor");
        countryForm.addStyleName("bordered"); // Custom style
        countryForm.setWidth("420px");
        //countryForm.setWriteThrough(false); // Enable buffering
        countryForm.setBuffered(true);
        countryForm.setEnabled(false);

        // When an item is selected from the list...
        countrySelect.addValueChangeListener(new ValueChangeListener() {
            private static final long serialVersionUID = 3371750143781493244L;

            @Override
            public void valueChange(ValueChangeEvent event) {
                // Get the item to edit in the form
                Item countryItem =
                    countries.getItem(event.getProperty().getValue());
                
                // Use a JPAContainer field factory
                //  - no configuration is needed here
                final FieldFactory fieldFactory = new FieldFactory();
                countryForm.setFormFieldFactory(fieldFactory);

                // Edit the item in the form
                countryForm.setItemDataSource(countryItem);
                countryForm.setEnabled(true);
                
                // Handle saves on the form
                final Button save = new Button("Save");
                countryForm.getFooter().removeAllComponents();
                countryForm.getFooter().addComponent(save);
                save.addClickListener(new ClickListener() {
                    private static final long serialVersionUID = 3147385792741616617L;

                    @Override
                    public void buttonClick(ClickEvent event) {
                        try {
                            countryForm.commit();
                            countryForm.setEnabled(false);
                        } catch (InvalidValueException e) {
                        }
                    }
                });
            }
        });
        countrySelect.setImmediate(true);
        countrySelect.setNullSelectionAllowed(false);
        // END-EXAMPLE: jpacontainer.fieldfactory.formonetomany

        layout.setSpacing(true);
        layout.addComponent(countrySelect);
        layout.addComponent(countryForm);
    }

    void tableonetomany(VerticalLayout layout) {
        // Populate with example data
        JPAContainerExample.insertExampleData();

        // BEGIN-EXAMPLE: jpacontainer.fieldfactory.tableonetomany
        // Have a persistent container
        final JPAContainer<Country> countries =
            JPAContainerFactory.make(Country.class, "book-examples");
        
        Table table = new Table(null, countries);
        table.setVisibleColumns(new Object[]{"name", "people"});
        table.setColumnHeaders("Country", "People");
        table.setPageLength(table.size());

        // Use converter to convert a Person set to a string
        class PersonSetConverter implements Converter<String, Set<Person>> {
            private static final long serialVersionUID = -393779375787744184L;

            @Override
            public Set<Person> convertToModel(String value,
                Class<? extends Set<Person>> targetType, Locale locale)
                throws com.vaadin.data.util.converter.Converter.ConversionException {
                return null; // Unused
            }

            @Override
            public String convertToPresentation(Set<Person> value,
                Class<? extends String> targetType, Locale locale)
                throws com.vaadin.data.util.converter.Converter.ConversionException {
                return value.stream().map(x->x.getName()).reduce((a,b) -> a+", "+b).orElse(""); // Java 8
            }

            @Override
            public Class<Set<Person>> getModelType() {
                return (Class<Set<Person>>) new HashSet<Person>().getClass().getSuperclass();
            }

            @Override
            public Class<String> getPresentationType() {
                return String.class;
            }
        }        
        table.setConverter("people", new PersonSetConverter());        
        // END-EXAMPLE: jpacontainer.fieldfactory.tableonetomany

        layout.addComponent(table);
    }

    void tableonetomany2(VerticalLayout layout) {
        // Populate with example data
        JPAContainerExample.insertExampleData();

        // BEGIN-EXAMPLE: jpacontainer.fieldfactory.tableonetomany2
        // Have a persistent container
        final JPAContainer<Country> countries =
            JPAContainerFactory.make(Country.class, "book-examples");
        
        Table table = new Table(null, countries);
        table.addGeneratedColumn("people", new ColumnGenerator() {
            private static final long serialVersionUID = -6600798386651738745L;

            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                Set<Person> people = countries.getItem(itemId).getEntity().getPeople();
                
                Table persontable = new Table();
                persontable.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
                persontable.addContainerProperty("name", String.class, null);
                for (Person p: people)
                    persontable.addItem(new Object[]{p.getName()}, p.getName());
                persontable.setPageLength(persontable.size());
                System.out.println("persontable length " + persontable.size());
                return persontable;
            }
        });
        
        table.setVisibleColumns(new Object[]{"name", "people"});
        table.setColumnHeaders("Country", "People");
        table.setPageLength(table.size());
        // END-EXAMPLE: jpacontainer.fieldfactory.tableonetomany2

        layout.addComponent(table);
    }
    
    public static final String masterdetailDescription =
        "<h1>Master-Detail Editor for a Property</h1>"+
        "<p>The easiest way to create a <b>JPAContainer</b> is to use the <b>JPAContainerFactory</b>.</p>";

    void masterdetail(Layout layout) {
        // Populate with example data
        JPAContainerExample.insertExampleData();

        // BEGIN-EXAMPLE: jpacontainer.fieldfactory.masterdetail
        // Create persistent containers
        final JPAContainer<Country> countries =
            JPAContainerFactory.make(Country.class, "book-examples");

        // Create, configure, and use a field factory
        final FieldFactory fieldFactory = new FieldFactory();

        // A table to display the country list
        Panel masterPanel = new Panel("Master Table");
        final Table masterTable = new Table("Select One",
                                             countries);
        masterTable.setVisibleColumns(new String[]{"name"});
        masterPanel.setContent(masterTable);

        // Have a placeholder for the editor
        final Panel detailPanel = new Panel("The Details");

        // When an item is selected from the table...
        masterTable.addValueChangeListener(new ValueChangeListener() {
            private static final long serialVersionUID = 3371750143781493244L;

            @Override
            public void valueChange(ValueChangeEvent event) {
                // Create the editor
                MasterDetailEditor editor =
                    new MasterDetailEditor(fieldFactory, countries,
                        event.getProperty().getValue(),
                        "people", detailPanel);
                
                // Make the editor visible
                detailPanel.setVisible(true);
                detailPanel.setContent(editor);
            }
        });
        masterTable.setSelectable(true);
        masterTable.setImmediate(true);
        masterTable.setNullSelectionAllowed(false);
        // END-EXAMPLE: jpacontainer.fieldfactory.masterdetail

        HorizontalLayout hlayout = new HorizontalLayout();
        hlayout.setSpacing(true);
        masterTable.setPageLength(5);
        hlayout.addComponent(masterPanel);
        hlayout.addComponent(detailPanel);
        layout.addComponent(hlayout);
    }
}
