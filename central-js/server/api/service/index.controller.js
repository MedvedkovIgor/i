'use strict';
var _ = require('lodash');
var activiti = require('../../components/activiti');
var environmentConfig = require('../../config/environment');
var config = environmentConfig.activiti;
var request = require('request');

var sHost = config.protocol + '://' + config.hostname + config.path;

var buildUrl = function(path){
  return sHost + path;
};

module.exports.index = function(req, res) {
  activiti.sendGetRequest(req, res, '/services/getService?nID=' + req.query.nID);
};

module.exports.getServiceStatistics = function(req, res) {
  activiti.sendGetRequest(req, res, '/services/getStatisticServiceCounts?nID_Service=' + req.params.nID);
};

module.exports.setService = function(req, res) {
  var callback = function (error, response, body) {
    res.send(body);
    res.end()
  };

  var url = buildUrl('/services/setService');

  request.post({
    'url': url,
    'auth': {
      'username': config.username,
      'password': config.password
    },
    'qs': {
      'nID_Subject': req.session.subject.nID
    },
    'headers': {
      'Content-Type': 'application/json; charset=utf-8'
    },
    'json': true,
    'body': req.body
  }, callback);
};
