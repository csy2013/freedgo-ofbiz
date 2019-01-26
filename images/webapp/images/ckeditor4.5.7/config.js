/**
 * @license Copyright (c) 2003-2016, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or http://ckeditor.com/license
 */

CKEDITOR.editorConfig = function( config ) {
	// Define changes to default configuration here. For example=
	config.language = 'zh-cn';
	// config.uiColor = '#AADC6E';

	config.image_previewText=' '; //预览区域显示内容
    //config.uiColor = '#AADC6E';
    config.filebrowserImageBrowseUrl= '/content/control/file?directory=/datasource',
    /*config.filebrowserBrowseUrl= '/content/control/file',
    config.filebrowserImageBrowseUrl= '/content/control/file',
    config.filebrowserFlashBrowseUrl= '/content/control/file',
    config.filebrowserUploadUrl= '/content/control/file',
    config.filebrowserImageUploadUrl= '/content/control/file',
    config.filebrowserFlashUploadUrl= '/content/control/file',*/
/*    config.filebrowserWindowWidth = '1024';
    config.filebrowserWindowHeight = '768';*/

    config.pasteFromWordRemoveFontStyles = false;
    config.pasteFromWordRemoveStyles = false;
    //config.fullPage= true;
    config.allowedContent= true;
    config.forcePasteAsPlainText = false;
    
    config.resize_enabled = true;

    config.toolbar = 'Custom';

    config.toolbar_Custom = [
        ['Save'],
        ['Source'],
        ['Maximize'],
        ['Bold', 'Italic', 'Underline', 'Strike', '-', 'Subscript', 'Superscript'],
        ['NumberedList', 'BulletedList', '-', 'Outdent', 'Indent'],
        ['JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock'],
        ['SpecialChar'],
        '/',
        ['Undo', 'Redo','RemoveFormat'],
        ['Font', 'FontSize'],
        ['TextColor', 'BGColor'],
        ['Link', 'Unlink', 'Anchor'],
        ['Image', 'Table', 'HorizontalRule']
    ];
};
