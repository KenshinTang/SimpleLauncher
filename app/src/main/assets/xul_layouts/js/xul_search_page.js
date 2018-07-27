
(function(PageName){
	SearchPage_onReady = function() {
        this.submitText = function() {
            this.dispatchEvent("search", "usr_cmd", "doSearch");
        };

        this.inputText = function(x) {
            var searchTextLabel = this.ownerPage.findItemById("item_search_text");
            if(x=="CLEAN"){
                searchTextLabel.setAttr("text","");
                this.submitText();
            } else if(x=="DEL") {
                var text = searchTextLabel.getAttr("text");
                if(text.length>0) {
                    searchTextLabel.setAttr("text", text.substr(0,text.length-1));
                    this.submitText();
                }
            } else if (x.length == 1){
                var text = searchTextLabel.getAttr("text");
                if(18>text.length){
                    searchTextLabel.setAttr("text",text+x);
                    this.submitText();
                }
            } else {
                x = x.substr(4);
                if (x.length>18) {
                    x = x.substr(0,18);
                }
                searchTextLabel.setAttr("text", x);
                this.submitText();
            }
        };
    };

    SearchPage_onInputKeyClick = function() {
        var secondLine = null;
        logger.d("searchbox: ", this.type);
        if (this.type != "image") {
            secondLine = this.findItemById("item_alternative_key");
        }

        if (secondLine == null) {
            // del or clean button
            this.ownerPage.inputText(this.text);
        } else if (secondLine.text == "") {
            // 1 or 0 num
            var key = this.findItemById("item_main_key").text;
            this.ownerPage.inputText(key);
        } else {
            // other num
            var searchContentArea = this.ownerPage.findItemById("area_search_content");
            var expandPadArea = this.ownerPage.findItemById("area_expand_pad");
            var item_t9_keyboard = this.ownerPage.findItemById("item_t9_keyboard");
            var item_full_keyboard = this.ownerPage.findItemById("item_full_keyboard");
            var search_box_bottom_area = this.ownerPage.findItemById("search_box_bottom_area");


            var total_String =[];
            var area_keyboard_mc = this.ownerPage.findItemById("area_keyboard_mc");
                    total_String.push(area_keyboard_mc);
                    total_String.push("setStyle");
                    total_String.push("opacity");
                    total_String.push("0.2");
            var items_num = this.ownerPage.findItemsByClass("font_main_key");
            if(items_num != null && items_num.length > 0){
                 for(idx in items_num){
                    total_String.push(items_num[idx]);
                    total_String.push("setStyle");
                    total_String.push("font-color");
                    total_String.push("33FFFFFF");
                 }
            }
                total_String.push(search_box_bottom_area);
                total_String.push("setStyle");
                total_String.push("opacity");
                total_String.push("0.2");
            var items_num_zero_ten = this.ownerPage.findItemsByClass("font_main_key_zero_one");
            if(items_num_zero_ten != null && items_num_zero_ten.length > 0){
                 for(idx in items_num_zero_ten){
                        total_String.push(items_num_zero_ten[idx]);
                        total_String.push("setStyle");
                        total_String.push("font-color");
                        total_String.push("33FFFFFF");
                 }
            }
            var items_num_english = this.ownerPage.findItemsByClass("font_alternative_key");
            if(items_num_english != null && items_num_english.length > 0){
                 for(idx in items_num_english){
                        total_String.push(items_num_english[idx]);
                        total_String.push("setStyle");
                        total_String.push("font-color");
                        total_String.push("33FFFFFF");
                 }
            }
            var items_clean = this.ownerPage.findItemsByClass("btn_del");

            var items_del = this.ownerPage.findItemsByClass("btn_clean");

            var item_full_keyboard = this.ownerPage.findItemById("item_full_keyboard");

            var item_t9_keyboard = this.ownerPage.findItemById("item_t9_keyboard");

                total_String.push(searchContentArea);
                total_String.push("setDisabled");
                total_String.push(this.parent);
                total_String.push("setDisabled");
                total_String.push(expandPadArea);
                total_String.push("removeClass");
                total_String.push("hide");
                total_String.push(expandPadArea);
                total_String.push("requestFocus");
                total_String.push(item_t9_keyboard);
                total_String.push("setDisabled");
                total_String.push(item_full_keyboard);
                total_String.push("setDisabled");



            this.ownerPage.xulBehavior.refreshBindingByView("expand_keys", this);
            this.ownerPage.pushStates.apply(this.ownerPage, total_String);
//            this.ownerPage.pushStates(searchContentArea, "setDisabled",
//                this.parent, "setDisabled",
//                expandPadArea, "removeClass", "hide",
//                expandPadArea, "requestFocus",
//                item_search_prompt,"setStyle","font-color","66FFFFFF",
//                item_t9_keyboard,"setDisabled",
//                item_full_keyboard,"setDisabled",
//                item_t9_keyboard_area,"setDisabled");
        }
    };

    SearchPage_onFullPadInputKeyClick = function() {
        if ("123" == this.text) {
            var letterPadArea = this.ownerPage.findItemById("area_full_letter_pad");
            var numPadArea = this.ownerPage.findItemById("area_full_num_pad");
            var switchItem = numPadArea.findItemById("item_switch_letter_num_pad");
            this.ownerPage.pushStates(numPadArea, "removeClass", "hide",
                letterPadArea, "addClass", "hide",
                switchItem, "requestFocus");

        } else if ("ABC" == this.text) {
            this.ownerPage.popStates();
        } else {
            this.ownerPage.inputText(this.text);
        }
    };

    SearchPage_onKeyboardSwitchButtonClick = function() {
       var t9KeyboardArea = this.ownerPage.findItemById("area_t9_keyboard");
       var fullKeyboardArea = this.ownerPage.findItemById("area_full_keyboard");
       var item_t9_keyboard = this.ownerPage.findItemById("item_t9_keyboard");
       var item_full_keyboard = this.ownerPage.findItemById("item_full_keyboard");
       if ("item_t9_keyboard" == this.id) {
            t9KeyboardArea.removeClass("hide");
            fullKeyboardArea.addClass("hide");
            /*item_t9_keyboard.addClass("keyboard_switch_btn_checked");
            item_full_keyboard.removeClass("keyboard_switch_btn_checked");*/
       } else {
            t9KeyboardArea.addClass("hide");
            fullKeyboardArea.removeClass("hide");
            /*item_t9_keyboard.removeClass("keyboard_switch_btn_checked");
            item_full_keyboard.addClass("keyboard_switch_btn_checked");*/
       }
    };
})("SearchPage");