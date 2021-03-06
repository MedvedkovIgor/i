(function () {
  'use strict';

  angular.module('app')
    .controller('AnswerContentController', AnswerContentController);

  function AnswerContentController($http, $scope, $state, $location, UserService) {
    var vm = this;

    vm.isLoggedIn = false;
    vm.getRedirectURI = getRedirectURI;
    vm.decryptedIsPresent;
    vm.onDownload = onDownload;
    vm.onView = onView;
    vm.viewContent = {};

    activate();

    function activate() {
      loggedInPending();

      vm.decryptedIsPresent = !!getFileMetadata().fileId;

      // onDownload();

      $scope.$on('event.logout', function () {
        vm.isLoggedIn = false;
      });

    }

    function onDownload() {
      var meta = getFileMetadata();

      downloadFile(meta.fileId, meta.fileName);
    }

    function onView() {
      var meta = getFileMetadata();

      viewContent(meta.fileId, meta.fileName);
    }

    function getFileMetadata() {
      return {
        fileId: $state.params.signedFileID,
        fileName: $state.params.fileName
      };
    }

    function downloadFile(fileId, fileName) {

      if (!fileId) {
        return;
      }

      $http({
        method: 'GET',
        url: '/api/answer/DFS/decrypted?signedFileID=' + fileId + '&fileName=' + fileName
      }).then(function successCallback(response) {
        if (!response.data) {
          return;
        }

        var headers = response.headers();
        var filename = headers['x-filename'];
        var contentType = headers['content-type'];
        var linkElement = document.createElement('a');

        try {
          var blob = new Blob([response.data], {type: contentType});
          var url = window.URL.createObjectURL(blob);

          linkElement.setAttribute('href', url);
          linkElement.setAttribute("download", filename);

          var clickEvent = new MouseEvent("click", {
            "view": window,
            "bubbles": true,
            "cancelable": false
          });
          linkElement.dispatchEvent(clickEvent);
        } catch (ex) {
          console.log(ex);
        }

      }, function errorCallback(response) {
        console.log(response);
      });
    }

    function viewContent(fileId, fileName){
      $http({
        method: 'GET',
        url: '/api/answer/DFS/decrypted/json?signedFileID=' + fileId + '&fileName=' + fileName
      }).then(function successCallback(response) {
        if (!response.data) {
          return;
        }

        vm.viewContent = angular.copy(response.data);
      }, function errorCallback(response) {
        console.log(response);
      });
    }

    function loggedInPending() {
      UserService.isLoggedIn()
        .then(function () {
          vm.isLoggedIn = true;
        })
        .catch(function () {
          vm.isLoggedIn = false;
        });
    }

    function getRedirectURI() {
      var stateForRedirect = $state.href('index.journal.answer', {error: ''});
      return $location.protocol() +
        '://' + $location.host() + ':'
        + $location.port()
        + stateForRedirect;
    }
  }
})();
