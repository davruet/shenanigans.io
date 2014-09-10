package io.shenanigans.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.pdfbox.Overlay;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;


/** PDFBox-based generator for certificates. Uses overlay to combine existing PDF with
 * generated values.
 * FIXME - extract out some hardcoded constants.
 * FIXME - center each line
 * @author dr
 *
 */

public class CertTemplate {

	private static final String PREFERRED_NETWORK_INTRO = "HAVING PREFERRED NETWORK NAMES INCLUDING %s";
	private static final String MAC_FONT_NAME = "/fonts/SnellRoundhand-Bold.ttf";
	private static final String SSID_FONT_NAME = "/fonts/Dosis-Regular.ttf";
	private static final float SSID_FONT_SIZE = 10.5f;
	private static final int MAC_FONT_SIZE = 46;
	
	public static final PDRectangle LETTER_LANDSCAPE = new PDRectangle(
			PDPage.PAGE_SIZE_LETTER.getHeight(), PDPage.PAGE_SIZE_LETTER.getWidth());
	// FIXME - not sure if this is thread safe. Definitely not safe if this object
	// is modified, but we're not changing it. 
	private PDDocument m_template;
	
	public CertTemplate(String path) throws IOException {
		m_template = PDDocument.load(
				getClass().getResource(path));

	}
	
	public void close() throws IOException {
		m_template.close();
	}

	public static void main(String[] args) throws Exception {
		List<String> macs = new ArrayList<String>();
		List<String> ssids = new ArrayList<String>();
		macs.add("DE:AD:BE:EF:03:2F");
		ssids.add("piojasd asd fa sd asdf a fpj apiojsdf opiajs dopo jaspd fjpos jap sodjfppio jasdijpoasd fjiapsf op jadf ojpsd op jaoisdf jipo piojsd asoipjsd fjiof doj jo oi sdjofoaposdf jsoidf ");
		
		macs.add("DE:AD:BE:EF:03:2F");
		ssids.add("piojasd asd fa sd asdf a fpj apiojsdf opiajs dopo jaspd fjpos jap sodjfppio jasdijpoasd fjiapsf op jadf ojpsd op jaoisdf jipo piojsd asoipjsd fjiof doj jo oi sdjofoaposdf jsoidf ");
		
		CertTemplate template = new CertTemplate("/Users/dr/Documents/shenanigans/design/certificate5-template.pdf");
		long start = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++){
			PDDocument dox = template.fillTemplate(macs, ssids);
			//dox.save("Hello World.pdf");
			dox.close();
		}
		System.out.println("Total duration: " + (System.currentTimeMillis() - start));
	}
	
	public PDDocument fillTemplate(List<String> macs, List<String> ssids) throws IOException {
		
		PDDocument cert = new PDDocument();
		
		for (int i = 0; i < macs.size(); i++){
			String mac = macs.get(i);
			String ssid = ssids.get(i);
			
			PDPage page = new PDPage(LETTER_LANDSCAPE);


			//page.setRotation(90);
			//page.se
			cert.addPage(page);
			PDPageContentStream stream = new PDPageContentStream(cert, page, false, false);
			// TODO - Make sure that we can't cache these somehow.
		
			PDTrueTypeFont ssidFont = loadFontResource(cert, SSID_FONT_NAME);
			
			PDTrueTypeFont macFont = loadFontResource(cert, MAC_FONT_NAME);		
			
			stream.beginText();
			stream.setFont( ssidFont, SSID_FONT_SIZE );
			stream.moveTextPositionByAmount( 205, 203 );
			String ssidStr = String.format(PREFERRED_NETWORK_INTRO, ssid);
			Paragraph p = new Paragraph(ssidFont,SSID_FONT_SIZE, 385, ssidStr);
			Iterator<String> it = p.getLines().iterator();
			
		    stream.appendRawCommands(
		    		ssidFont.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * SSID_FONT_SIZE 
		    		+ " TL\n");
			while (it.hasNext()){
				stream.drawString(it.next());
				if (it.hasNext()) {
		            stream.appendRawCommands("T*\n");
		        }
			}
			
			stream.endText();
			
			stream.beginText();
			stream.setFont(macFont, MAC_FONT_SIZE); 
			float macWidth = macFont.getStringWidth(mac) / 1000 * MAC_FONT_SIZE;
			float x = (page.getMediaBox().getWidth() - macWidth) / 2f;
			stream.moveTextPositionByAmount( x, 300 );
			stream.drawString( mac );
			stream.endText();
			// Make sure that the content stream is closed:
			stream.close();
		}
		
		Overlay overlay = new Overlay();
		PDDocument endDoc = overlay.overlay(m_template, cert);

		return endDoc;
		
	}
	
	private PDTrueTypeFont loadFontResource(PDDocument doc, String resourceName) throws IOException{
		//System.out.println(Arrays.toString((((URLClassLoader) CertTemplate.class.getClassLoader()).getURLs())));

		InputStream in = getClass().getResourceAsStream(resourceName);
		if (in == null) throw new IOException("Couldn't find font: " + resourceName);
		try {
			return PDTrueTypeFont.loadTTF(doc, in);
		} finally {
			in.close();
		}
	}
}
