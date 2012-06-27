/**
* Available at window.plugins.bumpAPI.xxx()
**/
var BumpAPI = function() {}

BumpAPI.prototype.options = {};

/**
* Initialize the connection of the BumpAPI in the native layer.
* @param reciever callback function of the form: reciever(user, data)
**/
BumpAPI.prototype.construct = function(reciever, sender) {
  //store reciever for calling from java
  window.plugins.bumpAPI.options.reciever = reciever;
  window.plugins.bumpAPI.options.sender = sender;
  return PhoneGap.exec(null, null, 'BumpAPI',	'construct', []);
};
/*BumpAPI.prototype.construct = function(title, body) {
    return PhoneGap.exec(null, null, 'BumpAPI',	'notify', [title, body]);
};*/

/**
* Clean up the connection of the BumpAPI in the native layer.
**/
BumpAPI.prototype.destroy = function() {
  return PhoneGap.exec(null, null, 'BumpAPI', 'destroy', []);
};

BumpAPI.prototype.send = function(params, successCallback, failureCallback) {
  return PhoneGap.exec(null, null, 'BumpAPI', 'send', [params]);
};

BumpAPI.prototype.debug = function() {
  return PhoneGap.exec(null, null, 'BumpAPI', 'debug', []);
};

BumpAPI.prototype.disable = function() {
  return PhoneGap.exec(null, null, 'BumpAPI', 'disable', []);
};

BumpAPI.prototype.enable = function() {
  return PhoneGap.exec(null, null, 'BumpAPI', 'enable', []);
};

/**
 * 	Load BumpAPI into PhoneGap
 **/
PhoneGap.addConstructor(function() {
	PhoneGap.addPlugin('bumpAPI', new BumpAPI());
});