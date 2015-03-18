/**
 * http(s) requests
 */

'use strict';


var _ = require('lodash');
var http = require("http");
var https = require('https');

exports.postJSON = function(options, data, onResult) {
	var prot = options.port == 443 ? https : http;
	var dataString = JSON.stringify(data);
	var default_headers = {
		'Content-Type': 'application/json',
		'Content-Length': dataString.length
	}
	options.headers =
		_.merge(options.headers, default_headers) || default_headers

	var req = prot.request(options, function(res) {
		var responseString = '';
		res.setEncoding('utf8');

		res.on('data', function(chunk) {
			responseString += chunk;
		});

		res.on('end', function() {
			var responseObject = JSON.parse(responseString);
			onResult(res.statusCode, responseObject);
		});

		req.on('error', function(err) {
			//res.send('error: ' + err.message);
			console.log(err.message);
			onResult(res.statusCode);
		});
	});

	req.write(dataString);
	req.end();
}

/**
 * getJSON:  REST get request returning JSON object(s)
 * @param options: http options object
 * @param callback: callback to pass the results JSON object(s) back
 */
exports.getJSON = function(options, onResult) {
	var prot = options.port == 443 ? https : http;
	var req = prot.request(options, function(res) {
		var responseString = '';
		res.setEncoding('utf8');

		res.on('data', function(chunk) {
			responseString += chunk;
		});

		res.on('end', function() {
			var responseObject = JSON.parse(responseString);
			onResult(res.statusCode, responseObject);
		});

		req.on('error', function(err) {
			//res.send('error: ' + err.message);
			console.log(err.message);
			onResult(res.statusCode);
		});

	});

	req.end();
};