package ru.fizteh.fivt.students.vlmazlov.multifilemap;

import ru.fizteh.fivt.students.vlmazlov.filemap.GenericTable;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

public abstract class GenericTableProviderFactory<V, T extends GenericTable<V>, S extends GenericTableProvider<V, T>> {
    protected boolean autoCommit;
    //autoCommit disabled by default
    public GenericTableProviderFactory() {
      autoCommit = false;
    }

    public GenericTableProviderFactory(boolean autoCommit) {
      this.autoCommit = autoCommit;
    }

    protected abstract S instantiateTableProvider(String dir) throws ValidityCheckFailedException;

    public S create(String dir) {
      try {
      	return instantiateTableProvider(dir);
      } catch (ValidityCheckFailedException ex) {
        throw new IllegalArgumentException(ex.getMessage());
      }
    }
}