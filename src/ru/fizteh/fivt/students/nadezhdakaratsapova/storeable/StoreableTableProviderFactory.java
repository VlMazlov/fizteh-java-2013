package ru.fizteh.fivt.students.nadezhdakaratsapova.storeable;


import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.storage.structured.TableProviderFactory;


import java.io.File;
import java.io.IOException;

public class StoreableTableProviderFactory implements TableProviderFactory {

    public StoreableTableProvider create(String dir) throws IOException {
        if ((dir == null) || (dir.trim().isEmpty())) {
            throw new IllegalArgumentException("Not allowed name of DataBaseStorage");
        } else {
            File dataDirectory = new File(dir);
            if (!dataDirectory.isDirectory()) {
                throw new IllegalArgumentException("The root directory should be a directory");
            }
            if (!dataDirectory.exists()) {
                throw new IOException("The working directory is not exist");
            }
            StoreableTableProvider newStorage = new StoreableTableProvider(dataDirectory.getCanonicalFile());
            return newStorage;
        }
    }
}
