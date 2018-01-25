package tr.com.almbase.plugin.activeobject;

public interface CategoryItemController {
	CategoryItem getRecordFromAOTableByName(String categoryItemName, String categoryId, String subCategoryId);
	CategoryItem getRecordFromAOTableById(String categoryItemId);
	CategoryItem[] getRecordFromAOTableBySubCategoryId(String subCategoryId);
	CategoryItem[] getAllEntriesFromAOTable();
	CategoryItem createRecordInAOTable(CategoryItemObject categoryItemObject);
	void deleteRecordFromAOTable(CategoryItem categoryItem);
	CategoryItem updateRecordInAOTable(CategoryItem categoryItem, CategoryItemObject categoryItemObject);
}
