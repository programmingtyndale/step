/**************************************************************************************************
 * Copyright (c) 2013, Directors of the Tyndale STEP Project                                      *
 * All rights reserved.                                                                           *
 *                                                                                                *
 * Redistribution and use in source and binary forms, with or without                             *
 * modification, are permitted provided that the following conditions                             *
 * are met:                                                                                       *
 *                                                                                                *
 * Redistributions of source code must retain the above copyright                                 *
 * notice, this list of conditions and the following disclaimer.                                  *
 * Redistributions in binary form must reproduce the above copyright                              *
 * notice, this list of conditions and the following disclaimer in                                *
 * the documentation and/or other materials provided with the                                     *
 * distribution.                                                                                  *
 * Neither the name of the Tyndale House, Cambridge (www.TyndaleHouse.com)                        *
 * nor the names of its contributors may be used to endorse or promote                            *
 * products derived from this software without specific prior written                             *
 * permission.                                                                                    *
 *                                                                                                *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS                            *
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT                              *
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS                              *
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE                                 *
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                           *
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,                           *
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;                               *
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER                               *
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT                             *
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING                                 *
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF                                 *
 * THE POSSIBILITY OF SUCH DAMAGE.                                                                *
 **************************************************************************************************/

var QuickLexicon = Backbone.View.extend({
    template: '<div id="quickLexicon"><div>' +
        '<div>' +
        '<button type="button" class="close" aria-hidden="true">&times;</button>' +
        '<% _.each(data, function(item) { %>' +
        '<div><h1>' +
        '<span class="<%= fontClass %>"><%= item.accentedUnicode %></span> (' +
        '<span class="stepTransliteration"><%= item.stepTransliteration %>): <%= item.stepGloss %>, ' +
        '</h1><span><%= item.shortDef == undefined ? "" : item.shortDef %></span><span class="strongCount"> (<%= sprintf(__s.stats_occurs, item.count) %>)</span>' +
        '<% }); %>' +
        '</div><span class="infoTagLine"><%= __s.more_info_on_click_of_word %></span></div></div></div>',
    initialize: function (opts) {
        this.strong = opts.strong;
        this.morph = opts.morph;
        this.position = opts.position;
        this.touchEvent = opts.touchEvent || false;
        this.passageContainer = step.util.getPassageContainer(opts.target);
        this.render();
    },

    /**
     * Updates the text and shows it
     * @param strongNumbers
     */
    render: function (event) {
        var self = this;

        //remove all quick lexicons

//                    self.showRelatedNumbers($.data(self.qtip, "relatedNumbers"));
        //make request to server
        $.getSafe(MODULE_GET_QUICK_INFO, [this.strong, this.morph], function (data) {
            $("#quickLexicon").remove();
            if (data.vocabInfos) {
                var lexicon = $(_.template(self.template)({ data: data.vocabInfos, fontClass: step.util.ui.getFontForStrong(self.strong) }));
                if (self.position > 0.66) {
                    lexicon.css("padding-top", "2px");
                    lexicon.css("height", "1px");
                    lexicon.css("top", "0");
                }
                self.passageContainer.append(lexicon);
                if (self.touchEvent) {
                    lexicon.click(function () {
                        step.util.ui.showDef({ strong: self.strong, morph: self.morph });
                        lexicon.remove();
                    });
                }

                lexicon.find(".close").click(function () {
                    lexicon.remove();
                });
            }
            
            for(var i = 0; i < (data.vocabInfos || []).length; i++) {
                self.showRelatedNumbers(data.vocabInfos[i].rawRelatedNumbers);
            }
        });

    },
    /**
     * @param rawRelatedNumbers the related raw numbers
     */
    showRelatedNumbers: function (rawRelatedNumbers) {
        if (rawRelatedNumbers) {
            step.passage.highlightStrong(null, rawRelatedNumbers.replace(/,/ig, ""), "relatedWordEmphasisHover");
        }
    }
});
