package tr.com.almbase.plugin.activeobject;

public interface SubCategoryController {
	SubCategory getRecordFromAOTableByName(String subCategoryName);
	SubCategory getRecordFromAOTableById(String subCategoryId);
	SubCategory[] getRecordFromAOTableByCategoryId(String categoryId);
	SubCategory[] getAllEntriesFromAOTable();
	SubCategory createRecordInAOTable(SubCategoryObject subCategoryObject);
	void deleteRecordFromAOTable(SubCategory subCategory);
	SubCategory updateRecordInAOTable(SubCategory subCategory, SubCategoryObject subCategoryObject);
}
