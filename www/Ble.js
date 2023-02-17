var exec = require('cordova/exec');

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'Ble', 'coolMethod', [arg0]);
};

exports.startAdvertising = function (uuid, major, minor, txPower, success, error) {
    exec(success, error, 'Ble', 'startAdvertising', [uuid, major, minor, txPower]);
};
exports.stopAdvertising = function (success, error) {
    exec(success, error, 'Ble', 'stopAdvertising', []);
};
