package com.example.actra.views.digest;

import com.example.actra.csv.CsvDigester;
import com.example.actra.csv.Transaction;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Digest")
@Route("digest")
@Menu(order = 1, icon = LineAwesomeIconUrl.FILE_CSV_SOLID)
public class DigestView extends Composite<VerticalLayout> {

    private static final String COL_DATE = "Date";
    private static final String COL_AMOUNT = "Amount";
    private static final String COL_SENDER = "Sender";
    private static final String COL_RECIPIENT = "Recipient";
    private static final String COL_MESSAGE = "Message";
    private static final String UPLOAD = "Upload";
    private static final String DOWNLOAD = "Download";
    private static final String STATUS = "Status";
    private static final String UL_STARTED = "Upload started";
    private static final String UL_SUCCESS = "Upload succeeded";

    public DigestView() {
        getContent().setSizeFull();
        getContent().getStyle().set("flex-grow", "1");
        addContents();
    }

    private void addContents() {

        TextArea statusTA = new TextArea(STATUS);
        statusTA.setWidthFull();
        statusTA.setReadOnly(true);

        Button downloadButton = new Button(DOWNLOAD);
        downloadButton.setEnabled(false);

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);

        List<Transaction> transactions = new ArrayList<>();
        ListDataProvider<Transaction> dataProvider = new ListDataProvider<>(transactions);
        Grid<Transaction> grid = createGrid(dataProvider);

        upload.addStartedListener(event -> {
            statusTA.setValue(UL_STARTED);
            transactions.clear();
            dataProvider.refreshAll();
        });

        upload.addSucceededListener(event -> {
            statusTA.setValue(UL_SUCCESS);
            InputStream inputStream = buffer.getInputStream();
//            String fileName = event.getFileName();
//            long contentLength = event.getContentLength();
//            String mimeType = event.getMIMEType();
            CsvDigester.digest(inputStream, transactions::add);
            // Refresh grid data when all are loaded
            dataProvider.refreshAll();
            grid.recalculateColumnWidths();
        });


        VerticalLayout upVLayout = new VerticalLayout(upload);
        VerticalLayout downVLayout = new VerticalLayout(downloadButton);
        HorizontalLayout controlsHL = new HorizontalLayout(upVLayout, downVLayout);

        HorizontalLayout statusHL = new HorizontalLayout(statusTA);
        statusHL.setWidthFull();

        VerticalLayout gridVL = new VerticalLayout(grid);
        gridVL.setSizeFull();

        VerticalLayout mainLayout = new VerticalLayout(controlsHL, statusHL, gridVL);
        mainLayout.setSizeFull();
        getContent().add(mainLayout);


    }

    private Grid<Transaction> createGrid(ListDataProvider<Transaction> dp) {

        Grid<Transaction> grid = new Grid<>(Transaction.class, false);
        grid.setDataProvider(dp);
        fixColumn(grid.addColumn(new LocalDateRenderer<>(Transaction::getBookingDate,
                        "yyyy/MM/dd")).setHeader(COL_DATE)
                .setComparator(Transaction::getBookingDate));
        fixColumn(grid.addColumn(Transaction::getAmount).setHeader(COL_AMOUNT).setTextAlign(ColumnTextAlign.END));
        fixColumn(grid.addColumn(Transaction::getSender).setHeader(COL_SENDER));
        fixColumn(grid.addColumn(Transaction::getRecipient).setHeader(COL_RECIPIENT));
        fixColumn(grid.addColumn(Transaction::getMessage).setHeader(COL_MESSAGE));
        grid.setMultiSort(true, Grid.MultiSortPriority.APPEND);
        grid.setRowsDraggable(true);
        grid.setSizeFull();
        return grid;

    }

    private void fixColumn(Grid.Column<Transaction> column) {
        column.setSortable(true).setAutoWidth(true).setResizable(true);
    }

}
