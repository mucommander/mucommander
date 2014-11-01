package com.mucommander.ui.viewer.text;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import javax.swing.text.Utilities;

/**
 * Panel in which the line numbers at a given text component are presented.
 * it is used in JScrollPane as a row header.
 * 
 * @author Arik Hadas
 */
public class TextLineNumbersPanel extends JPanel implements CaretListener, DocumentListener, PropertyChangeListener {
	
	private final static int HEIGHT = Integer.MAX_VALUE - 1000000;
	
	public static enum ALIGNMENT{ LEFT, CENTER, RIGHT }
	
	// Text component this TextTextLineNumber component is in sync with
	private JTextComponent component;

	// Properties that can be changed
	private Color currentLineForeground;
	private int minimumDisplayDigits;
	private double digitAlignment;

	// Keep history information to reduce the number of times the component needs to be repainted
    private int lastDigits;
    private int lastHeight;
    private int lastLine;
    
    private HashMap<String, FontMetrics> fonts;
    
    /**
	 *	Create a line number component for a text component. This minimum
	 *  display width will be based on 3 digits.
	 *
	 *  @param component  the related text component
	 */
	public TextLineNumbersPanel(JTextComponent component) {
		this(component, 3);
	}

	/**
	 *	Create a line number component for a text component.
	 *
	 *  @param component  the related text component
	 *  @param minimumDisplayDigits  the number of digits used to calculate
	 *                               the minimum width of the component
	 */
	public TextLineNumbersPanel(JTextComponent component, int minimumDisplayDigits) {
		this(component, minimumDisplayDigits, new EmptyBorder(0, 0, 0, 2), 4, ALIGNMENT.CENTER);
	}
	
	public TextLineNumbersPanel(JTextComponent component, int minimumDisplayDigits, Border border, int borderGap, 
			ALIGNMENT alignment) {
		this.component = component;

		setBackground(Color.LIGHT_GRAY);
		setForeground(Color.black);
		setCurrentLineForeground(new Color(0,0,255));
		setDigitAlignment(alignment);
		setBorder(border, borderGap);
		setMinimumDisplayDigits( minimumDisplayDigits);
		setFont(component.getFont());
		
		component.getDocument().addDocumentListener(this);
		component.addPropertyChangeListener("font", this);
		component.addCaretListener(this);
	}
	
	/**
	 * Set the alignment of the line numbers strings within the panel
	 * 
	 * @param alignment the line numbers alignment
	 */
	private void setDigitAlignment(ALIGNMENT alignment) {
		switch(alignment) {
		case LEFT:
			digitAlignment = 0;
		case RIGHT:
			digitAlignment = 1;
		case CENTER:
			digitAlignment = 0.5;
		}
	}
	
	/**
	 * If we'll want to highlight current line , we should
	 * set the current line number color using this method
	 * 
	 * @param currentLineForeground current line number color
	 */
	private void setCurrentLineForeground( Color currentLineForeground ) {
		this.currentLineForeground = currentLineForeground;
	}
	
	/**
	 *  The border gap is used in calculating the left and right insets of the
	 *  border. Default value is 5.
	 *
	 *  @param borderGap  the gap in pixels
	 */
	private void setBorder(Border border, int borderGap) {
		Border inner = new EmptyBorder(0, borderGap, 0, borderGap);
		setBorder( new CompoundBorder(border, inner) );
		lastDigits = 0;
		setPreferredWidth();
	}
	
	/**
	 *  Specify the minimum number of digits used to calculate the preferred
	 *  width of the component. Default is 3.
	 *
	 *  @param minimumDisplayDigits  the number digits used in the preferred
	 *                               width calculation
	 */
	private void setMinimumDisplayDigits(int minimumDisplayDigits) {
		this.minimumDisplayDigits = minimumDisplayDigits;
		setPreferredWidth();
	}

	/**
	 *  Calculate the width needed to display the maximum line number
	 */
	private void setPreferredWidth() {
		Element root = component.getDocument().getDefaultRootElement();
		int lines = root.getElementCount();
		int digits = Math.max(String.valueOf(lines).length(), minimumDisplayDigits);

		//  Update sizes when number of digits in the line number changes
		if (lastDigits != digits) {
			lastDigits = digits;
			FontMetrics fontMetrics = getFontMetrics( getFont() );
			int width = fontMetrics.charWidth( '0' ) * digits;
			Insets insets = getInsets();
			int preferredWidth = insets.left + insets.right + width;

			Dimension d = getPreferredSize();
			d.setSize(preferredWidth, HEIGHT);
			setPreferredSize( d );
			setSize( d );
		}
	}
	
	/*
	 *  We need to know if the caret is currently positioned on the line we
	 *  are about to paint so the line number can be highlighted.
	 *  if the current line foreground is not set, just return false
	 */
	private boolean isCurrentLine(int rowStartOffset)
	{
		if (currentLineForeground == null)
			return false;
		
		int caretPosition = component.getCaretPosition();
		Element root = component.getDocument().getDefaultRootElement();

		return root.getElementIndex( rowStartOffset ) == root.getElementIndex(caretPosition);
	}

	/**
	 *  Draw the line numbers
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		//	Determine the width of the space available to draw the line number
		FontMetrics fontMetrics = component.getFontMetrics( component.getFont() );
		Insets insets = getInsets();
		int availableWidth = getSize().width - insets.left - insets.right;

		//  Determine the rows to draw within the clipped bounds.
		Rectangle clip = g.getClipBounds();
		int rowStartOffset = component.viewToModel( new Point(0, clip.y) );
		int endOffset = component.viewToModel( new Point(0, clip.y + clip.height) );

		while (rowStartOffset <= endOffset) {
			try {
    			g.setColor(isCurrentLine(rowStartOffset) ? currentLineForeground : getForeground());

    			//  Get the line number as a string and then determine the
    			//  "X" and "Y" offsets for drawing the string.
    			String lineNumber = getTextLineNumber(rowStartOffset);
    			int stringWidth = fontMetrics.stringWidth( lineNumber );
    			int x = getOffsetX(availableWidth, stringWidth) + insets.left;
				int y = getOffsetY(rowStartOffset, fontMetrics);
    			g.drawString(lineNumber, x, y);

    			//  Move to the next row
    			rowStartOffset = Utilities.getRowEnd(component, rowStartOffset) + 1;
			}
			catch(Exception e) {}
		}
	}
	
	/*
	 *	Get the line number to be drawn. The empty string will be returned
	 *  when a line of text has wrapped.
	 */
	protected String getTextLineNumber(int rowStartOffset) {
		Element root = component.getDocument().getDefaultRootElement();
		int index = root.getElementIndex( rowStartOffset );
		Element line = root.getElement( index );

		return line.getStartOffset() == rowStartOffset ? String.valueOf(index + 1) : "";
	}

	/*
	 *  Determine the X offset to properly align the line number when drawn
	 */
	private int getOffsetX(int availableWidth, int stringWidth) {
		return (int)((availableWidth - stringWidth) * digitAlignment);
	}

	/*
	 *  Determine the Y offset for the current row
	 */
	private int getOffsetY(int rowStartOffset, FontMetrics fontMetrics) throws BadLocationException {
		//  Get the bounding rectangle of the row

		Rectangle r = component.modelToView( rowStartOffset );
		int lineHeight = fontMetrics.getHeight();
		int y = r.y + r.height;
		int descent = 0;

		//  The text needs to be positioned above the bottom of the bounding
		//  rectangle based on the descent of the font(s) contained on the row.


		if (r.height == lineHeight)  {	// default font is being used
			descent = fontMetrics.getDescent();
		}
		else { // We need to check all the attributes for font changes
			if (fonts == null)
				fonts = new HashMap<String, FontMetrics>();

			Element root = component.getDocument().getDefaultRootElement();
			int index = root.getElementIndex( rowStartOffset );
			Element line = root.getElement( index );

			for (int i = 0; i < line.getElementCount(); i++) {
				Element child = line.getElement(i);
				AttributeSet as = child.getAttributes();
				String fontFamily = (String)as.getAttribute(StyleConstants.FontFamily);
				Integer fontSize = (Integer)as.getAttribute(StyleConstants.FontSize);
				String key = fontFamily + fontSize;

				FontMetrics fm = fonts.get( key );

				if (fm == null)
				{
					Font font = new Font(fontFamily, Font.PLAIN, fontSize);
					fm = component.getFontMetrics( font );
					fonts.put(key, fm);
				}

				descent = Math.max(descent, fm.getDescent());
			}
		}

		return y - descent;
	}
	
	/////////////////////////////////////
    // DocumentListener implementation //
    /////////////////////////////////////
	
	public void changedUpdate(DocumentEvent e) {
		documentChanged();
	}

	public void insertUpdate(DocumentEvent e) {
		documentChanged();
	}

	public void removeUpdate(DocumentEvent e) {
		documentChanged();
	}

	/*
	 *  A document change may affect the number of displayed lines of text.
	 *  Therefore the lines numbers will also change.
	 */
	private void documentChanged() {
		//  Preferred size of the component has not been updated at the time
		//  the DocumentEvent is fired
		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
        		int preferredHeight = component.getPreferredSize().height;

				//  Document change has caused a change in the number of lines.
				//  Repaint to reflect the new line numbers
        		if (lastHeight != preferredHeight) {
        			setPreferredWidth();
        			repaint();
        			lastHeight = preferredHeight;
        		}
			}
		});
	}
	
	//////////////////////////////////
    // CaretListener implementation //
    //////////////////////////////////
	
	public void caretUpdate(CaretEvent e)
	{
		if (currentLineForeground == null)
			return;
		
		//  Get the line the caret is positioned on
		int caretPosition = component.getCaretPosition();
		Element root = component.getDocument().getDefaultRootElement();
		int currentLine = root.getElementIndex( caretPosition );

		//  Need to repaint so the correct line number can be highlighted

		if (lastLine != currentLine)
		{
			repaint();
			lastLine = currentLine;
		}
	}

	///////////////////////////////////////////
    // PropertyChangeListener implementation //
    ///////////////////////////////////////////
	
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getNewValue() instanceof Font) {
			setFont((Font) evt.getNewValue());
			lastDigits = 0;
			setPreferredWidth();
		}
	}
}
