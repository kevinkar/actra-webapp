package com.example.actra.service;

import com.example.actra.csv.CsvDigester;
import com.example.actra.csv.DigesterException;
import com.example.actra.csv.Transaction;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;

/**
 * CSV business logic, including background thread for long-running tasks.
 * <>p</>
 * Handles data and has DataProvider for use with Grid.
 * <>p</>
 * Registered with Spring as @Service
 */
@Service
public class CsvService {

    private final Collection<Transaction> transactions;

    private final DataProvider<Transaction, SerializablePredicate<Transaction>> dataProvider;

    public CsvService() {
        this.transactions = new Vector<>();
        this.dataProvider = new ListDataProvider<>(transactions);
    }

    public DataProvider<Transaction, SerializablePredicate<Transaction>> getDataProvider() {
        return dataProvider;
    }

    public void refresh() {
        dataProvider.refreshAll();
    }

    public void clear() {
        transactions.clear();
        dataProvider.refreshAll();
    }

    public void add(Transaction... transactions) {
        this.transactions.addAll(List.of(transactions));
        dataProvider.refreshAll();
    }

    public int getCount() {
        return transactions.size();
    }

    @Async
    public void process(InputStream inputStream, Consumer<Integer> onComplete, Consumer<Throwable> onError ) {
        try {
            CsvDigester.digest(inputStream, transactions::add);
        } catch (DigesterException e) {
            onError.accept(e);
            return;
        }
        onComplete.accept(transactions.size());
    }

}
