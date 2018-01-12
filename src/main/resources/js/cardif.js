AJS.$(document).ready(function() {
    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e,context) {
        var url = AJS.params.baseURL + "/rest/project/1.0/getRestrictedProjects";
        var projectList;

        AJS.$.ajax({
            url: url,
            type: "GET",
            dataType: "json",
            success: function(data) {
                console.log('dom', self, data);
                if (null != data && data.length > 0) {
                    var issue = AJS.Meta.get("issue-key");
                    var projectKey = issue.substr(0, issue.indexOf('-'));
                    for (i = 0; i < data.length; i++) {
                        if (projectKey == data[i].projectKey) {
                            AJS.$("#edit-issue").hide();
                            AJS.$("#comment-issue").hide()
                            AJS.$("#assign-issue").hide();
                            AJS.$("#assign-to-me").hide();
                        }
                    }
                }
            },
            error: function(xhr) {
                console.log('error', arguments);
            }
        });
    });
});