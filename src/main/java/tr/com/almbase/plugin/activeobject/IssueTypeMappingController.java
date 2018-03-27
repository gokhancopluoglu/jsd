package tr.com.almbase.plugin.activeobject;

public interface IssueTypeMappingController {
	IssueTypeMapping getRecordFromAOTableById(String issueTypeMapId);
	IssueTypeMapping getRecordFromAOTableByName(String issueTypeMapName);
	IssueTypeMapping getRecordWithAllParameters(String integrationId, String localProjectId, String localIssueTypeId, String remoteProjectId, String remoteIssueTypeId);
	IssueTypeMapping[] getRecordFromAOTableByIntegrationId(String integrationId);
	IssueTypeMapping[] getAllEntriesFromAOTable();
	IssueTypeMapping createRecordInAOTable(IssueTypeMappingObject issueTypeMapObject);
	IssueTypeMapping updateRecordFromAOTable(IssueTypeMapping issueTypeMapRecord, IssueTypeMappingObject issueTypeMapObject);
	void deleteRecordFromAOTable(IssueTypeMapping issueTypeMapRecord);
}