package tr.com.almbase.plugin.activeobject;

public interface RemoteIssueController {
	RemoteIssue[] getRecordFromAOTableByIssueKey(String issueKey);
	RemoteIssue getRecordFromAOTableByRemoteIssueKey(String remoteIssueKey);
	RemoteIssue getRecordFromAOTableById(String remoteIssueId);
	RemoteIssue[] getRecordFromAOTableByIntegrationId(String integrationId);
	RemoteIssue[] getAllEntriesFromAOTable();
	RemoteIssue createRecordInAOTable(RemoteIssueObject remoteIssueObject);
	RemoteIssue updateRecordFromAOTable(RemoteIssue remoteIssue, RemoteIssueObject remoteIssueObject);
}