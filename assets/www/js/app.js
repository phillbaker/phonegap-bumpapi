/* Author: 
  Phill Baker
*/

(function(){

  // App namespace
  var App;
  if (typeof exports !== 'undefined') {
    App = exports;
  } else {
    App = this.App = {};
  }
  //constants

  // options
  App.options = {};
    
  // init
  App.init = function(ops) {
    _.extend(App.options, ops);
    
    window.plugins.bumpAPI.construct(App.bumpReceiver, App.bumpSender);
    window.plugins.bumpAPI.disable();
    // ...
    //window.plugins.bumpAPI.enable();
    //window.plugins.bumpAPI.debug();
  };

  
  /**
  * Called on connection. {confirmed: '...'}
  **/
  App.bumpSender = function(options) {
    //TODO 
    //no-op...maybe push it to the server?
    window.plugins.bumpAPI.send('helloworld');
  };
  App.bumpReceiver = function(user, data) {
    console.log("JD-js-bumped");
  };
})(this);

window.addEventListener('load', function () {
  document.addEventListener('deviceready', function () {
    App.init({});//assume jquery-mobile is ready?
    $(document).bind('pageinit', function() {
    });
  }, false);
}, false);
