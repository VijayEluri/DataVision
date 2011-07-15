package jimm.datavision.field;
import jimm.datavision.*;
import jimm.datavision.gui.FieldWidget;
import jimm.datavision.gui.UserColumnWidget;
import jimm.datavision.gui.SectionWidget;
import jimm.datavision.source.Column;

import java.util.Collection;
import java.util.Observer;
import java.util.Observable;

/**
 * A user column field represents a user column, which in turn holds some SQL
 * that is put in the SELECT clause of a query. The value of a user column
 * field holds a {@link UserColumn} object. (In the XML, the user column
 * field's value is the id of the user column.)
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class UserColumnField extends Field implements Observer {

protected UserColumn usercol;

/**
 * Constructs a user column field with the specified id in the specified report
 * section whose {@link UserColumn}'s id is <var>value</var>.
 *
 * @param id the new field's id
 * @param report the report containing this element
 * @param section the report section in which the field resides
 * @param value the id of a user column
 * @param visible show/hide flag
 */
public UserColumnField(Long id, Report report, Section section, Object value,
		    boolean visible)
{
    super(id, report, section, value, visible);
    usercol = report.findUserColumn(value);
    usercol.addObserver(this);
}

protected void finalize() throws Throwable {
    usercol.deleteObserver(this);
    super.finalize();
}

public void update(Observable o, Object arg) {
    setChanged();
    notifyObservers(arg);
}

public FieldWidget makeWidget(SectionWidget sw) {
    return new UserColumnWidget(sw, this);
}

/**
 * Not really used; we drag user columns, not user column fields.
 */
public String dragString() {
    return typeString() + ":" + usercol.getId();
}

/**
 * Returns the user column.
 *
 * @return the user column
 */
public UserColumn getUserColumn() { return usercol; }

/**
 * Sets the user column.
 *
 * @param newUsercol the new user column
 */
public void setUserColumn(UserColumn newUsercol) {
    if (usercol != newUsercol) {
	usercol.deleteObserver(this);
	usercol = newUsercol;
	usercol.addObserver(this);
	setChanged();
	notifyObservers();
    }
}

public String typeString() { return "usercol"; }

public String designLabel() { return usercol.designLabel(); }

public String formulaString() { return usercol.formulaString(); }

public boolean refersTo(Field f) {
    return usercol.refersTo(f);
}

public boolean refersTo(Parameter p) {
    return usercol.refersTo(p);
}

/**
 * This override returns <code>true</code> if this user column is in a
 * detail section. We don't really know that this user column returns
 * a number, so we'll err on the side of allowing aggregation.
 *
 * @return <code>true</code> if this field can be aggregated
 */
public boolean canBeAggregated() {
    // Section can be null during dragging.
    return section != null && section.isDetail();
}

/**
 * Returns the value of this field. For user column fields, this is the
 * value generated by evaluating the {@link UserColumn}.
 *
 * @return the result of evaluating the usercol
 */
public Object getValue() { return getReport().columnValue(usercol); }

/**
 * Returns a collection of the columns used in the user column. This is used
 * by the report's query when it is figuring out what columns and tables
 * are used by the report. Calls {@link UserColumn#columnsUsed}.
 *
 * @see jimm.datavision.source.Query#findSelectablesUsed
 */
public Collection<Column> columnsUsed() {
    return usercol.columnsUsed();
}

}
