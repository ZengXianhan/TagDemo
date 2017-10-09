var app = angular.module('myApp', ['nfcFilters']);
app.controller('MainCtrl', function ($scope, nfcService) {
    $scope.tag = nfcService.tag;
    
    $scope.clear = function () {
        nfcService.clearTag();
        alert("Tag Cleared Sucessfully");
    };
})

app.factory('nfcService', function ($rootScope) {

    var tag = {};

    document.addEventListener("deviceready", function (e) {
        nfc.addNdefListener(function (nfcEvent) {
            alert("read msg");
            alert(JSON.stringify(nfcEvent.tag, null, 4));
            $rootScope.$apply(function () {
                angular.copy(nfcEvent.tag, tag);
                // if necessary $state.go('some-route')
            });
        }, function () {
            alert("Listening for NDEF Tags.");
        }, function (reason) {
            alert("Error adding NFC Listener " + reason);
        });

    });

    return {
        tag: tag,

        clearTag: function () {
            angular.copy({}, this.tag);
        }
    };
});

