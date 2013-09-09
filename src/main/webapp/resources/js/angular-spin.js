(function () {
    "use strict";
    angular.module('angular-spin', [])
        .directive('spin', function () {
            return {
                restrict: 'A',
                transclude:true,
                replace:true,
                template: '<div ng-transclude></div>',
                scope: {
                    config: "=spin",
                    spinif: "=spinIf"
                },
                link: function (scope, element, attrs) {
                    var spinner = new Spinner(scope.config),
                        stoped = false;
                    spinner.spin(element[0]);

                    scope.$watch('config', function (newValue, oldValue) {
                        if (newValue == oldValue)
                            return;
                        spinner.stop();
                        spinner = new Spinner(newValue);
                        if (!stoped)
                            spinner.spin(element[0]);
                    }, true);

                    if (attrs.hasOwnProperty("spinIf")) {
                        scope.$watch('spinif', function (newValue) {
                            if (newValue) {
                                spinner.spin(element[0]);
                                stoped = false
                            } else {
                                spinner.stop();
                                stoped = true
                            }
                        });
                    }

                    scope.$on('$destroy', function() {
                        spinner.stop();
                    });
                }
            }
        });
})();
