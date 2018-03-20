package tr.com.almbase.plugin.activeobject;

public interface IntegrationController {
	Integration getRecordFromAOTableById(String integrationId);
	Integration getRecordFromAOTableByName(String integrationName);
	Integration[] getAllEntriesFromAOTable();
	Integration createRecordInAOTable(IntegrationObject integrationObject);
	Integration updateRecordFromAOTable(Integration integrationRecord, IntegrationObject integrationObject);
	void deleteRecordFromAOTable(Integration integration);
}