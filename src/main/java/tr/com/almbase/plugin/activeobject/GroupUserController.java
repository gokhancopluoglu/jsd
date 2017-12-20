package tr.com.almbase.plugin.activeobject;

public interface GroupUserController {
	GroupUser getRecordFromAOTableByGroupName(String groupName);
	GroupUser getRecordFromAOTableByUserName(String userName);
	GroupUser getRecordFromAOTableById(String groupUserId);
	GroupUser[] getAllEntriesFromAOTable();
	GroupUser createRecordInAOTable(GroupUserObject groupUserObject);
	void deleteRecordFromAOTable(GroupUser groupUser);
}
