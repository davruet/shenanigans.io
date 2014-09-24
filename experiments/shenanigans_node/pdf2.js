PDFDocument = require('pdfkit');
fs = require('fs');
doc = new PDFDocument('<html><head>\
    <title>PDFKit SVG Test</title>\
  </head><body><embed src="http://www.gnu.org/graphics/official%20gnu.svg" width="100%" height="600px" type="image/svg+xml" pluginspage="http://www.adobe.com/svg/viewer/install/" />\
  <h1>BLAH BLAH BLAH</h1></body></html>');
doc.pipe(fs.createWriteStream('test.pdf'));
doc.font("fonts/england.ttf").fontSize(70).text('0F AB D2 99 21 ED', 0, 300,{ width:doc.page.width, align: 'center'});



doc.end();
