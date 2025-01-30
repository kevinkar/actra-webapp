package com.example.actra.views.digest;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Digest")
@Route("my-view")
@Menu(order = 1, icon = LineAwesomeIconUrl.FILE_CSV_SOLID)
public class DigestView extends Composite<VerticalLayout> {

    public DigestView() {
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
    }
}
