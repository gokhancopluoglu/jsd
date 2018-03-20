package tr.com.almbase.plugin.activeobject;

public interface FieldMappingController {
	FieldMapping getRecordFromAOTableById(String fieldMappingId);
	FieldMapping[] getRecordFromAOTableByIntegrationId(String integrationId);
	FieldMapping[] getRecordFromAOTableByIssueTypeMappingId(String issueTypeMappingId);
	FieldMapping[] getAllEntriesFromAOTable();
	FieldMapping createRecordInAOTable(FieldMappingObject fieldMappingObject);
	FieldMapping updateRecordFromAOTable(FieldMapping fieldMappingRecord, FieldMappingObject fieldMappingObject);
	void deleteRecordFromAOTable(FieldMapping fieldMappingRecord);
}