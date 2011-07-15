package jimm.datavision.source;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator over the columns in a list of tables. Not used by all data
 * sources.
 */
public class ColumnIterator implements Iterator<Column> {

Iterator<Table> tableIter;
Table table;
Iterator<Column> colIter;
Column nextCol;

/**
 * Constructor.
 */
public ColumnIterator(Iterator<Table> tableIter) {
    this.tableIter = tableIter;
    findNext();
}

public boolean hasNext() {
    return nextCol != null;
}

public Column next() throws NoSuchElementException {
    if (nextCol == null)
	throw new NoSuchElementException();

    Column returnValue = nextCol;
    findNext();			// Fill nextCol for next time
    return returnValue;
}

public void remove() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
}

/**
 * Sets <var>col</var> to the next available column. If there is none,
 * <var>col</var> will be set to <code>null</code>.
 */
protected void findNext() {
    nextCol = null;

    if (colIter == null) {	// First time through
	if (!tableIter.hasNext()) // No more tables
	    return;		// nextCol will be null
	table = (Table)tableIter.next();
	colIter = table.columns();
    }

    while (!colIter.hasNext()) {
	if (!tableIter.hasNext()) // No more tables
	    return;		// nextCol will be null
	table = (Table)tableIter.next();
	colIter = table.columns();
    }

    nextCol = (Column)colIter.next();
}

}
