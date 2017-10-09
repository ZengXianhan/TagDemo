function config($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.otherwise("/index");
    $stateProvider
        .state('index', {
            url: "/index",
            templateUrl: "views/index.html",
            data: {
                pageTitle: 'index'
            }
        })
}
angular
    .module('neuboard')
    .config(config)
    .run(function($rootScope, $state) {
        $rootScope.$state = $state;
    });
