
// Shenanigans server

var express = require('express');
var app = express();
var logger = require('morgan');

app.use(logger('combined'));

app.use(function(req, res, next) {
  var data = new Buffer('');
  req.on('data', function(chunk) {
      data = Buffer.concat([data, chunk]);
  });
  req.on('end', function() {
    req.rawBody = data;
    next();
  });
});

app.use('/submitFingerprint', function(req,res){
      if (req.rawBody){
        res.type('json');
        res.json({
          token: req.ip,
          url: 'http://shenanigans.io/certificate/' + req.ip
        });
      } else {
        res.send(400);
      }
});

var server = app.listen(8000, function(){
	console.log('Listening on port %d', server.address().port);
});


