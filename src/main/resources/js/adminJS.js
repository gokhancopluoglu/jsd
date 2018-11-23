var document, AJS;

if(typeof(CARDIFJSELIB_ADMIN) == "undefined") {
    var CARDIFJSELIB_ADMIN = {};
}

CARDIFJSELIB_ADMIN.addLogo = function() {
var $sign = '<div style="margin:0px 0px 0px 5px; padding:3px 4px 3px 4px; position:relative; bottom:0; right:0; display:inline-block; text-align:right; border:1px solid; border-radius:10px; color:#00A770; font-size:0.5em; font-family:Georgia, Times, serif;" title="Cardif JSD Engine">Cardif - JSE</div>';
var $label = AJS.$('#descriptors_table label[for*="bnp\\.paribas\\.cardif\\.jse"]');

    if($label.size() > 0 ) {
        $label.each(function() {
            AJS.$(this).append($sign);
        });
    }
};

AJS.$(document).ready(function() {
    CARDIFJSELIB_ADMIN.addLogo();
});

