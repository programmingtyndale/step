/*******************************************************************************
 * Copyright (c) 2012, Directors of the Tyndale STEP Project All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of the Tyndale House, Cambridge
 * (www.TyndaleHouse.com) nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior
 * written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
step.search.ui.subject = {
        evaluateQuerySyntax : function(passageId) {
           var query = "s=" + $(".subjectText", step.util.getPassageContainer(passageId)).val();
           
           step.state.subject.subjectQuerySyntax(passageId, query);
           
           return query;
        }
};

$(document).ready(function() {
    var namespace = "subject";
    step.state.trackState([
                           ".subjectText",
                           ".subjectQuerySyntax",
                           ".subjectPageNumber"
                           ], namespace);

    step.util.ui.trackQuerySyntax(".subjectSearchTable", namespace);
   
    $(".subjectClear").click(function() {
        //  reset texts
        var passageId = step.passage.getPassageId(this);
        step.state.subject.subjectText(passageId, "");
        step.state.subject.subjectQuerySyntax(passageId, "");
    });

    
      step.util.ui.searchButton(".subjectSearch",  'SEARCH_SUBJECT', undefined, function(passageId) {
          step.search.ui.subject.evaluateQuerySyntax(passageId);
      });
});

$(step.search.ui).hear("subject-search-state-has-changed", function(s, data) {
    step.search.subject.search(data.passageId);
});
