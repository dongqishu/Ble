var exec = require('cordova/exec');

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'Ble', 'coolMethod', [arg0]);
};

exports.startAdvertising = function (uuid, major, minor, success, error) {
    exec(success, error, 'Ble', 'startAdvertising', [uuid, major, minor]);
};
exports.stopAdvertising = function (success, error) {
    exec(success, error, 'Ble', 'stopAdvertising', []);
};
