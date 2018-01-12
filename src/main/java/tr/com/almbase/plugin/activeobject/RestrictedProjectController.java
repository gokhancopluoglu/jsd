package tr.com.almbase.plugin.activeobject;

public interface RestrictedProjectController {
	RestrictedProject[] getRecordFromAOTableByProjectKey(String projectKey);
	RestrictedProject getRecordFromAOTableByGroupName(String groupName);
	RestrictedProject getRecordFromAOTableById(String restrictedProjectId);
	RestrictedProject[] getAllEntriesFromAOTable();
	RestrictedProject createRecordInAOTable(RestrictedProjectObject restrictedProjectObject);
	void deleteRecordFromAOTable(RestrictedProject restrictedProject);
}
