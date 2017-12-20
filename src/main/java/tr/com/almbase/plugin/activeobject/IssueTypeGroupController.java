package tr.com.almbase.plugin.activeobject;

public interface IssueTypeGroupController {
	IssueTypeGroup getRecordFromAOTableByIssueType(String issueType);
	IssueTypeGroup getRecordFromAOTableByGroupName(String groupName);
	IssueTypeGroup getRecordFromAOTableById(String issueTypeGroupId);
	IssueTypeGroup[] getAllEntriesFromAOTable();
	IssueTypeGroup createRecordInAOTable(IssueTypeGroupObject issueTypeGroupObject);
	void deleteRecordFromAOTable(IssueTypeGroup issueTypeGroup);
}
