package jimm.datavision.layout;
import jimm.datavision.*;
import jimm.datavision.field.*;
import jimm.datavision.layout.LineDrawer;
import java.io.*;

/**
 * A LaTeX2e layout engine.
 *
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class LaTeXLE extends LayoutEngine implements LineDrawer {

protected double linethickness;

public LaTeXLE(PrintWriter out) {
    super(out);
    linethickness = 0;
}

/**
 * Outputs the beginning of the document.
 */
protected void doStart() {
    String latexPaperSize = report.getPaperFormat().getLaTeXPaperSizeString();
    out.println("\\documentclass[11pt" +
(latexPaperSize == null ? "" : ("," + latexPaperSize)) +
"]{article}\n" +
"\n" +
"\\oddsidemargin -1in\n" +
"\\evensidemargin -1in\n" +
"\\textwidth " + pageWidth() + "pt\n" +
"\\headheight -0.5pt\n" +
"\\headsep 0pt\n" +
"\\topmargin 0.0in\n" +
"\\textheight " + pageHeight() + "pt\n" +
"\n" +
"\\begin{document}\n" +
"\\pagestyle{empty}\n" +
"\\setlength{\\unitlength}{1pt}\n" +
"% Generated by DataVision version " + info.Version + "\n" +
"% " + info.URL);
}

/**
 * Outputs the end of the document.
 */
protected void doEnd() {
    out.println("\\end{document}");
}

/**
 * Start a new page.
 */
protected void doStartPage() {
    if (pageNumber > 1)
	out.println("\\newpage");
    out.println("% ======== Page " + pageNumber + " ========");
    out.println("\\begin{picture}(" + pageWidth() + "," + pageHeight() + ")");
}

/**
 * End a page.
 */
protected void doEndPage() {
    out.println("\\end{picture}");
}

/**
 * Outputs a field.
 *
 * @param field the field to output
 */
protected void doOutputField(Field field) {
    String fieldAsString = field.toString();
    if (fieldAsString == null || fieldAsString.length() == 0) {
	makeBorders(field);
	out.println();
	return;
    }

    Format format = field.getFormat();
    Rectangle bounds = field.getBounds();

    // \put(x, y) {
    putField(field);

    // Calculate font size. Return array; first element for now, second
    // element for later.
    String[] size = selectFontSize(format);
    if (size != null) out.print(size[0]);

    // Align
    String align = "";
    switch (format.getAlign()) {
    case Format.ALIGN_CENTER:
	align = "\\begin{center}";
	break;
    case Format.ALIGN_RIGHT:
	align = "\\raggedleft ";
	break;
    }
    out.print("\\begin{minipage}[t]{" + bounds.width + "pt}" + align);

//  // [NOTE: BROKEN; BALANCING '}' BELOW DOESN'T BALANCE PROPERLY
//  // Nice for debugging: frame the box
//      out.print("\\framebox(" + bounds.width + "," + bounds.height + "){"
//  	      + align + "}{");

    // Bold, italic, underline
    if (format.isBold()) out.print("\\textbf{");
    if (format.isItalic()) out.print("\\textit{");
    if (format.isUnderline()) out.print("\\underline{");

    // The actual, real, this-is-it field data
    out.print(makeSafe(fieldAsString));

    // Close align, bold, italic, underline, and font size
    if (format.isUnderline()) out.print("}");
    if (format.isItalic()) out.print("}");
    if (format.isBold()) out.print("}");

    if (format.getAlign() == Format.ALIGN_CENTER) out.print("\\end{center}");

//  // [NOTE: BROKEN; THIS BALANCING '}' DOESN'T BALANCE PROPERLY
//  // Nice for debugging: close the frame box
//      out.print("}");

    out.print("\\end{minipage}");

    if (size != null) out.print(size[1]);

    // Close \put
    out.print("}");

    // Borders
    makeBorders(field);

    out.println();
}

/**
 * Ignores image output
 *
 * @param field an image field
 */
protected void doOutputImage(ImageField field) {}

/**
 * Outputs a line. Calls {@link #drawLine}.
 *
 * @param line a line
 */
protected void doOutputLine(Line line) {
    drawLine(line, null);
}

/**
 * Returns array of two strings that set/reset font size. If format is
 * <code>null</code> or has no size, return <code>null</code>.
 *
 * @return an array of two strings
 */
protected String[] selectFontSize(Format format) {
    if (format == null || format.getSize() == 0)
	return null;
    String[] size = new String[2];
    size[0] = "{\\fontsize{" + format.getSize() + "pt}{"
	+ (format.getSize() * 1.2) + "pt}";
    size[1] = "}";
    return size;
}

/**
 * Outputs borders.
 */
protected void makeBorders(Field field) {
    field.getBorderOrDefault().eachLine(this, null);
}

/**
 * Draw a single line.
 *
 * @param line a line
 */
public void drawLine(Line line, Object arg) {
    Point p0 = line.getPoint(0);
    Point p1 = line.getPoint(1);
    double xdiff = p1.x - p0.x;
    double ydiff = p1.y - p0.y;

    if (xdiff == 0 && ydiff == 0)
	return;

    double xslope, yslope;
    if ( ydiff == 0) {		// Horizontal
	if (p1.x < p0.x)
	    xslope = -1;
	else if (p1.x > p0.x)
	    xslope = 1;
	else
	    xslope = 0;
	yslope = 0;
    }
    else if (xdiff == 0) {	// Vertical
	xslope = 0;
	if (p1.y < p0.y)
	    yslope = 1;
	else if (p1.y > p0.y)
	    yslope = -1;
	else
	    yslope = 0;
    }
    else {
//  	xslope = xdiff / ydiff;
//  	yslope = ydiff / xdiff;
	xslope = xdiff;
	yslope = ydiff;
// FIX
	double[] slopes = pickNearestSlope(xslope / yslope);
	if (slopes == null) {
	    xslope = 1;
	    yslope = 0;
	}
	else {
	    xslope = slopes[0];
	    yslope = slopes[1];
	}
    }
    setLineThickness(line.getThickness());
    putLine(line, xslope, yslope);
}

/**
 * Returns an array containing two doubleing-point values representing
 * the x and y values needed for a LaTeX2e line. LaTeX2e insists that
 * these two values be integers, but we use doubles for accuracy and
 * let <code>putLine</code> truncate them.
 *
 * @param slope a double
 * @return an array containing x and y values
 */
protected double[] pickNearestSlope(double slope) {
    boolean found = false;
    double xySlopeAbs = 0, xSlope = 0, ySlope = 0;
    for (int x = -6; x <= 6; ++x) {
	for (int y = -6; y <= 6; ++y) {
	    if (!found || xySlopeAbs > (slope - Math.abs(x / y))) {
		xySlopeAbs = Math.abs(slope - (x / y));
		xSlope = x;
		ySlope = y;
		found = true;
	    }
	}
    }
    if (!found) return null;

    double[] answer = new double[2];
    answer[0] = xSlope;
    answer[1] = ySlope;
    return answer;
}

/**
 * Sets the line thickness for drawing.
 *
 * @param t thickness
 */
protected void setLineThickness(double t) {
    if (linethickness != t) {
	linethickness = t;
	out.print("\\linethickness{" + linethickness + "pt}");
    }
}

/**
 * Outputs the LaTeX2e code that places the field on the page.
 */
protected void putField(Field field) {
    double y;
    Rectangle bounds = field.getBounds();
    if (currentSection.getArea().getArea() == SectionArea.PAGE_FOOTER) {
	// Page footers are anchored to the bottom of the page
	y = currentSection.getOutputHeight() - bounds.y;
    }
    else {
	y = pageHeight() - (pageHeightUsed + bounds.y);
    }

    // In a \put command, x and y determine the bottom left corner of
    // whatever you are placing.
    y -= field.getOutputHeight();

    out.print("\\put(" + bounds.x + "," + y + "){");
}	    

/**
 * Outputs the LaTeX2e code that places the line on the page. Both xslope
 * and yslope are cast to ints because that's what LaTeX2e requires.
 *
 * @param line the line to draw
 * @param xslope the X slope of the line; must be from -6 through 6
 * @param yslope the Y slope of the line; must be from -6 through 6
 */
protected void putLine(Line line, double xslope, double yslope) {
    double y;
    Point p0 = line.getPoint(0);
    if (currentSection.getArea().getArea() == SectionArea.PAGE_FOOTER) {
	// Page footers are anchored to the bottom of the page
	y = p0.y;
    }
    else {
	y = pageHeight() - (pageHeightUsed + p0.y);
    }

    // In a \put command, x and y determine the bottom left corner of
    // whatever you are placing.
    out.println("\\put(" + p0.x + "," + y + "){\\line(" + (int)xslope + ","
		+ (int)yslope + "){" + line.length() + "}}");
}	    

/**
 * Returns a new string with all LaTeX2e special characters replaced
 * by their printable equivalents.
 *
 * @param str
 * @return a string safe for LaTeX2e output
 */
protected String makeSafe(String str) {
    StringBuffer buf = new StringBuffer();
    int len = str.length();
    for (int i = 0; i < len; ++i) {
	char c = str.charAt(i);
	switch (c) {
	case '\\':
	    buf.append("$\\backslash$");
	    break;
	case '~':
	    buf.append("\\textasciitilde");
	    break;
	case '#': case '$': case '%': case '&': case '_':
	case '^': case '{': case '}':
	    buf.append('\\');
	    buf.append(c);
	    break;
	default:
	    buf.append(c);
	    break;
	}
    }
    return buf.toString();
}

}