/**
 * Simple filtering of empty expressions 
 */
function onValidate(line) {
    if (line == "") {
        return false;
    }
    else {
        return true;
    }
}

/**
 * Given an expression (in a line) send it to the server in a synchronous GET
 * and return the result to the jQuery Console handler
 */
function onHandle (line, callback) {
    var result;
    $.ajax({
        url: "/repl",
        data: {":line-key": line},
        async: false,
        success: function(response) { result = response; }
    });
    return result;
}

/**
 * User prompt for the REPL window
 */
var welcomeMessageVal = 'Enter some Clojure code, and it will be evaluated ON THE SERVER -- CAREFUL!!!.';

/**
 * Invoke jQuery Console, taking a simple approach. 
 */
$(document).ready(function () {
    $("#console").console({
        promptLabel: 'Clojure> ',
        commandValidate: onValidate,
        commandHandle: onHandle,
        welcomeMessage: welcomeMessageVal,
        autofocus:true,
        animateScroll:true,
        promptHistory:true
    })
});
