package tr.com.almbase.plugin.activeobject;

public interface CategoryComponentController {
	CategoryComponent getRecordFromAOTableByName(String categoryComponentName, String categoryId, String subCategoryId, String categoryItemId);
	CategoryComponent getRecordFromAOTableById(String categoryComponentId);
	CategoryComponent[] getRecordFromAOTableByCategoryItemId(String categoryItemId);
	CategoryComponent[] getAllEntriesFromAOTable();
	CategoryComponent createRecordInAOTable(CategoryComponentObject categoryComponentObject);
	void deleteRecordFromAOTable(CategoryComponent categoryComponent);
	CategoryComponent updateRecordInAOTable(CategoryComponent categoryComponent, CategoryComponentObject categoryComponentObject);
}
