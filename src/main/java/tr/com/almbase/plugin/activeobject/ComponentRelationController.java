package tr.com.almbase.plugin.activeobject;

public interface ComponentRelationController {
	ComponentRelation getRecordFromAOTableById(String componentRelationId);
	ComponentRelation getRecordFromAOTableByComponentId(String componentId);
	ComponentRelation[] getAllEntriesFromAOTable();
	ComponentRelation createRecordInAOTable(ComponentRelationObject componentRelationObject);
	void deleteRecordFromAOTable(ComponentRelation componentRelation);
}
