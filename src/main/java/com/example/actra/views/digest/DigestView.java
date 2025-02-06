package com.example.actra.views.digest;

import com.example.actra.csv.Transaction;
import com.example.actra.service.CsvService;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.InputStream;

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
    private static final String UL_FAILED = "Upload failed";
    private static final String PROCESSING_FAILED = "Failed to process file. Cause: %1$s";
    private static final String PROCESSING_SUCCESS = "Loaded %1$d transactions.";
    private static final String UNKNOWN_ERROR = "Unknown error";

    private final CsvService service;
    private Grid<Transaction> grid;
    private TextArea statusTA;

    /**
     * @param csvService Provided by the Spring framework
     */
    public DigestView(CsvService csvService) {
        this.service = csvService;
        getContent().setSizeFull();
        getContent().getStyle().set("flex-grow", "1");
        addContents();
    }

    private void addContents() {

        TextArea statusTA = new TextArea(STATUS);
        this.statusTA = statusTA;
        statusTA.setWidthFull();
        statusTA.setReadOnly(true);

        Button downloadButton = new Button(DOWNLOAD);
        downloadButton.setEnabled(false);

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);

        Grid<Transaction> grid = createGrid(service.getDataProvider());
        this.grid = grid;

        upload.addStartedListener(event -> {
            service.clear();
            statusTA.setValue(UL_STARTED);
        });

        upload.addFileRemovedListener(event -> {
            statusTA.setValue("");
        });

        upload.addFailedListener(event -> {
            statusTA.setValue(UL_FAILED);
        });

        UI ui = UI.getCurrent();
        upload.addSucceededListener(event -> {
            statusTA.setValue(UL_SUCCESS);
            InputStream inputStream = buffer.getInputStream();
//            String fileName = event.getFileName();
//            long contentLength = event.getContentLength();
//            String mimeType = event.getMIMEType();
            service.clear();
            service.process(inputStream,
                    ui.accessLater(this::refreshGrid, null),
                    ui.accessLater(this::fileFailed, null));

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

    private void refreshGrid(int count) {
        statusTA.setValue(String.format(PROCESSING_SUCCESS, count));
        service.refresh();
        grid.recalculateColumnWidths();
    }

    private void fileFailed(Throwable e) {
        String msg = e != null ? e.getMessage() : UNKNOWN_ERROR;
        statusTA.setValue(String.format(PROCESSING_FAILED, msg != null ? msg : UNKNOWN_ERROR));
        service.refresh();
        grid.recalculateColumnWidths();
    }

    private Grid<Transaction> createGrid(DataProvider<Transaction, SerializablePredicate<Transaction>> dp) {

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
