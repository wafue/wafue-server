
	<link rel="stylesheet" href="${ctxPath}/static/kindeditor/themes/default/default.css" />
	<link rel="stylesheet" href="${ctxPath}/static/kindeditor/plugins/code/prettify.css" />
	<script charset="utf-8" src="${ctxPath}/static/kindeditor/kindeditor.js"></script>
	<script charset="utf-8" src="${ctxPath}/static/kindeditor/lang/zh_CN.js"></script>
	<script charset="utf-8" src="${ctxPath}/static/kindeditor/plugins/code/prettify.js"></script>
	<script>
		KindEditor.ready(function(K) {
			var options={
					cssPath : '${ctxPath}/static/kindeditor/plugins/code/prettify.css',
					uploadJson : '${ctxPath}/kindeditor/upload_json',
					//uploadJson : '${ctxPath}/image/upload_blob',
					fileManagerJson : '${ctxPath}/kindeditor/file_manager_json',
					items: ['source', '|', 'fontname', 'fontsize', '|', 'forecolor', 'bold', 'italic', 'underline', '|', 'justifyleft', 'justifycenter', 'justifyright', '|', 'image', 'multiimage', '|', 'plainpaste', 'wordpaste', '|','fullscreen'],
					allowFileManager : true,
					readonlyMode : ${readOnly!false},
					afterFocus:function(){
						var _name = $("#${id}").attr("name").replace("token_", "");
						$("#${id}").attr("name", _name);
						$("#form_token").val(1);
					},
					afterCreate : function() {
						var self = this;
						$("#btn_save").bind("click",function(){
							$("#${id}").val(editor.html());
						});
					}
				};
			@ var token = "token_";
			@ if (value != ""){
			@ 	token = "";	
			@}
			var editor = K.create('textarea[name="${token}${name}"]', options);
		});
	</script>	
	<textarea id="${id}" name="${token}${name}" ${required!} class="form-control" cols="100" rows="8" style="visibility:hidden;width:100%;height:${height!'200px'};">${value!}</textarea>

