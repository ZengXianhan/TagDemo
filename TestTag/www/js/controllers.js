app.controller('MainCtrl', function ($scope) {

});
//NFC Services
app.controller('NFCController', function ($scope, $timeout, $modal) {
    $scope.clear = function () {
        $scope.trayID = "";
        $scope.machineId = "";
    };
    $scope.trayIdShow = true;
    $scope.machineIdShow = false;
    $scope.tListening = false;
    $scope.mListening = false;
    $scope.tEmpty = false;
    $scope.mEmpty = false;
    $scope.tSuccess = false;
    $scope.mSuccess = false;

    // init();


    $scope.RetrieveTray = function () {
        $scope.ttag = "";
        initTray();
        $scope.trayIdShow = true;
        $scope.machineIdShow = false;
        $scope.tListening = true;
        $timeout(function () {
            $scope.tListening = false;
        }, 5000);
    }
    $scope.RetrieveMachine = function () {
        $scope.mtag = "";
        initMachine();
        $scope.trayIdShow = false;
        $scope.machineIdShow = true;
        $scope.mListening = true;
        $timeout(function () {
            $scope.mListening = false;
        }, 5000);
    }
    //ReadNFC();
    function initTray() {
        nfc.addNfcVListener(
            function (nfcEvent) {
                alert("Read TrayId Success");
                //alert("TrayIdHex: " + nfcEvent);
                $scope.ttag = nfcEvent;
            }, function (nfcEvent) {
                alert("error");
                alert(nfcEvent);
            });
    }

    function initMachine() {
        nfc.removeNfcVListener(
            function (nfcEvent) {
                alert("Read MachineId Success.");
                //alert("Machine Id: " + nfcEvent);
                $scope.mtag = nfcEvent;

            }, function (nfcEvent) {
                alert("error");
                alert(nfcEvent);
            });
    }

    function init() {
        //nfc.addNfcVListener(function (nfcEvent) {
        //    var idStr = nfcEvent;
        //    if (idStr.length == 5) {
        //        alert("Read TrayId Success, please wait few second.");
        //    }
        //    //alert("TrayIdHex: " + nfcEvent);
        //    $scope.ttag = nfcEvent;
        //}, function (nfcEvent) {
        //    alert("error");
        //    alert(nfcEvent);   
        //});

        // alert("test0");
        // console.log("nfc",nfc);
        //console.log(module);
        //console.log("nfc1", nfc1);
        // console.log("nfc", nfc);
        // alert("test");

        //nfc.addNfcVListener(function (nfcEvent) {
        //    console.log(nfcEvent);
        //    //console.log(error);
        //    alert("test4");
        //}
        //, function (nfcEvent) {
        //    console.log(nfcEvent);
        //    alert("test5");
        //}
        //);

        //nfc.addNfcVListener(
        //    function (nfcEvent2) {
        //    console.log("nfc event",nfcEvent2);
        //    alert("test4");
        //}
        //);




        // cordova.exec(success, error, "NfcVPlugin", "startReadingNfcV", []);

        //nfc.isAvailable(function (nfcEvent1) {
        //    // alert("test1");
        //    alert(nfcEvent1);
        //  //  console.log(nfcEvent1);
        //    if (nfcEvent1 == "NFC_OK") {
        //        alert("success1");


        //        nfc.addNfcVListener(function (nfcEvent) {
        //            alert(nfcEvent);
        //            console.log(nfcEvent);
        //            //   alert("test4");
        //        }, function (nfcEvent) {
        //            console.log(nfcEvent);
        //            //  alert("test5");
        //        }

        //        //nfc.addNfcVListener(function (nfcEvent) {
        //        //    alert(nfcEvent);
        //        //    console.log(nfcEvent);
        //        // //   alert("test4");
        //        //}, function (nfcEvent) {
        //        //    console.log(nfcEvent);
        //        //  //  alert("test5");
        //        //}
        //    );
        //    }
        //    }, function (win) {
        //        console.log(win);
        //    alert("test2");
        //}, function (fail) {
        //    alert("test3");
        //});

        // nfc;

    }

    //function ReadNFC() {
    //    document.addEventListener("deviceready", function (e) {
    //        nfc1.addNfcVListener(function (nfcEvent) {
    //            $scope.nfcEvent = nfcEvent;
    //           // nfc.addNdefListener(function (nfcEvent) {
    //            console.log(nfcEvent);
    //            console.log(nfc.bytesToString(nfcEvent.tag.ndefMessage[0].payload));
    //            console.log('tag json' + JSON.stringify(nfcEvent.tag));
    //            $scope.tag = nfc.bytesToString(nfcEvent.tag.ndefMessage[0].payload);
    //            console.log('tag message : ' + $scope.tag);
    //            if ($scope.tag == "") {
    //                $scope.msgListening = false;
    //                $scope.msgEmpty = true;
    //                $timeout(function () {
    //                    $scope.msgEmpty = false;
    //                }, 3000);
    //            }
    //            $scope.$apply();
    //        }, function () {
    //            $scope.msgListening = true;
    //            //$scope.$apply();
    //            $timeout(function () {
    //                $scope.msgListening = false;
    //            }, 3000);
    //        }, function (reason) {
    //            console.log("Error adding NFC Listener " + reason);
    //        });

    //    });
    //}
    $scope.Confirm = function () {
        $scope.openAlert();
    }
    function DisplaySuccessMsg() {
        $scope.msgSuccess = true;
        //$scope.$apply();
        $timeout(function () {
            $scope.msgSuccess = false;
        }, 3000);
    }
    function Update() {
        var tnf = ndef.TNF_EXTERNAL_TYPE,
            recordType = "application/json",
            payload = $scope.trayID,
            record,
            message = [
               ndef.mimeMediaRecord(recordType, payload)
            ];

        //to update nfc1
        //nfc.write(
        //   message,
        //   function (success) {
        //       $scope.msgWriting = true;
        //       //$scope.$apply();
        //       $timeout(function () {
        //           $scope.msgWriting = false;
        //           DisplaySuccessMsg();
        //       }, 2500);
        //       console.log("Wrote data to tag.");
        //   },
        //   function (reason) {
        //       console.log("There was a problem " + reason);
        //   }
        //);
        ReadNFC();
    }
    $scope.openAlert = function () {
        var modalInstance = $modal.open({
            templateUrl: 'views/modal_3.html',
            controller: ModalInstanceCtrl
        });
    };
    function ModalInstanceCtrl($scope, $modalInstance) {

        $scope.ok = function () {
            $modalInstance.close();
            Update();
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    };
    function TagDiscover() {
        document.addEventListener("deviceready", function (e) {
            //nfc.addTagDiscoveredListener(function)
        });
    }
});


