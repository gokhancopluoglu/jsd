package tr.com.almbase.plugin.activeobject;

public interface BusinessRuleController {
	BusinessRule getRecordFromAOTableByUserName(String userName);
	BusinessRule getRecordFromAOTableById(String businessRuleId);
	BusinessRule getRecordFromAOTableByIssueType(String issueType, String selectedCategoryId, String selectedSubCategoryId);
	BusinessRule[] getRecordFromAOTableByCategoryId(String categoryId);
	BusinessRule[] getRecordFromAOTableBySubCategoryId(String subCategoryId);
	BusinessRule getRecordFromAOTableByCategoryItemId(String categoryItemId, String selectedCategoryId, String selectedSubCategoryId);
	BusinessRule[] getAllEntriesFromAOTable();
	BusinessRule createRecordInAOTable(BusinessRuleObject businessRuleObject);
	void deleteRecordFromAOTable(BusinessRule businessRule);
	BusinessRule updateRecordInAOTable(BusinessRule businessRule, BusinessRuleObject businessRuleObject);
}