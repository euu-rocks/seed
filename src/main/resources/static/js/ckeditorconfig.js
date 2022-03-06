CKEDITOR.editorConfig = function(config) {
    config.toolbar_simple = [ ['Bold', 'Italic', 'Underline', 'Strike'],
    						['JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock'] ];
    config.toolbar_advanced = [ ['Format', 'Font', 'FontSize'],
    						['Bold', 'Italic', 'Underline', 'Strike'],
    						['JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock'], 
    						['NumberedList', 'BulletedList', 'Table'],
    						['Link', 'Unlink', 'SpecialChar', "Maximize"] ];
    config.removePlugins = 'elementspath,resize';
};
