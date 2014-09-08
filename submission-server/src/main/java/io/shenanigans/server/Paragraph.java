package io.shenanigans.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.font.PDFont;

/** Basic word-wrapping functionality for PDFBox. Splits a paragraph into
 * a {@link java.util.List} of lines.
 * @author dr
 *
 */
public class Paragraph {
	private float m_maxWidth;
	private String m_text;
	private PDFont m_font;
	private float m_fontSize;
	
	public Paragraph(PDFont font, float fontSize, float width, String text){
		m_maxWidth = width;
		m_text = text;
		m_font = font;
		m_fontSize = fontSize;
	}
	
	/** Gets the list of lines in this paragraph, split according to length and the parameters
	 * supplied in the constructor.
	 * @return
	 * @throws IOException
	 */
	public List<String> getLines() throws IOException {
		String[] strings = m_text.split(" ");
		
		StringBuilder builder = new StringBuilder();
		float currentWidth = 0;
		List<String> lines = new ArrayList<String>();
		for (String s : strings){
			float wordWidth = m_font.getStringWidth(s) / 1000 * m_fontSize;
			if ((currentWidth + wordWidth) > m_maxWidth && builder.length() > 0){
				lines.add(builder.toString());
				builder = new StringBuilder();
				
			}
			if (wordWidth >= m_maxWidth){
				// new line has already been added, no need to reset builder or length
				lines.add(s);
			} else {
				
				builder.append(s).append(" ");
				currentWidth = m_font.getStringWidth(builder.toString())/ 1000 * m_fontSize;
			}
		}
		if (builder.length() > 0) lines.add(builder.toString());

		return lines;
	}
}