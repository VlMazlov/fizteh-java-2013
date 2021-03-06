package ru.fizteh.fivt.students.vlmazlov.strings;

import ru.fizteh.fivt.students.vlmazlov.generics.GenericTable;
import ru.fizteh.fivt.students.vlmazlov.utils.*;

import java.util.Map;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class StringTable extends GenericTable<String> implements DiffCountingTable, Cloneable {

    private StringTableProvider specificProvider;

    public StringTable(StringTableProvider provider, String name) {
        super(provider, name);
        specificProvider = provider;
    }

    public StringTable(StringTableProvider provider, String name, boolean autoCommit) {
        super(provider, name, autoCommit);
        specificProvider = provider;
    }

    public void read(String root, String fileName)
            throws IOException, ValidityCheckFailedException {
        if (root == null) {
            throw new FileNotFoundException("Directory not specified");
        }

        if (fileName == null) {
            throw new FileNotFoundException("File not specified");
        }

        TableReader.readTable(new File(root), new File(root, fileName), this, specificProvider);
    }

    public void write(String root, String fileName)
            throws IOException, ValidityCheckFailedException {
        if (root == null) {
            throw new FileNotFoundException("Directory not specified");
        }

        if (fileName == null) {
            throw new FileNotFoundException("File not specified");
        }

        TableWriter.writeTable(new File(root), new File(root, fileName), this, specificProvider);
    }

    @Override
    protected String getCommited(String key) {
        return commited.get(key);
    }

    @Override
    public int commit() {
        try {
            return super.commit();
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public int size() {
        int size = 0;
        getCommitLock.readLock().lock();

        try {
            size = commited.size();

            for (Map.Entry<String, String> entry : changed.get().entrySet()) {
                if (commited.get(entry.getKey()) == null) {
                    ++size;
                }
            }

            for (String entry : deleted.get()) {
                if (commited.get(entry) != null) {
                    --size;
                }
            }
        } finally {
            getCommitLock.readLock().unlock();
        }

        return size;
    }

    @Override
    public StringTable clone() {
        return new StringTable(specificProvider, getName(), autoCommit);
    }

    @Override
    public void checkRoot(File root) throws ValidityCheckFailedException {
        ValidityChecker.checkMultiTableRoot(root);
    }

    @Override
    protected void storeOnCommit() throws IOException, ValidityCheckFailedException {
        ProviderWriter.writeMultiTable(this, new File(specificProvider.getRoot(), getName()), specificProvider);
    }
}
