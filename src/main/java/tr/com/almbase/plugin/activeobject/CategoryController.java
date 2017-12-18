package tr.com.almbase.plugin.activeobject;

public interface CategoryController {
	Category getRecordFromAOTableByName(String categoryName);
	Category getRecordFromAOTableById(String categoryId);
	Category[] getAllEntriesFromAOTable();
	Category createRecordInAOTable(CategoryObject categoryObject);
	void deleteRecordFromAOTable(Category category);
	Category updateRecordInAOTable(Category category, CategoryObject categoryObject);
}
