package com.ameen.meal_chooser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    @FXML
    private Label mealDisplayLabel;

    @FXML
    private TextField newMealInput;

    @FXML
    private ComboBox<Category> categoryFilterCombo;

    @FXML
    private ComboBox<Category> categoryAddCombo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ObservableList<Category> categories = DatabaseManager.getCategoriesList();

        categoryAddCombo.setItems(categories);
        if (!categories.isEmpty()) {
            categoryAddCombo.getSelectionModel().selectFirst();
        }

        ObservableList<Category> filterList = FXCollections.observableArrayList();
        filterList.add(new Category(-1, "كل التصنيفات 🍽️"));
        filterList.addAll(categories);

        categoryFilterCombo.setItems(filterList);
        categoryFilterCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void onSuggestMealClick() {
        Category selectedCategory = categoryFilterCombo.getSelectionModel().getSelectedItem();
        int categoryId = (selectedCategory != null) ? selectedCategory.getId() : -1;

        String result = DatabaseManager.getRandomMeal(categoryId);

        mealDisplayLabel.setText(result);
    }

    @FXML
    protected void onAddMealClick() {
        String mealName = newMealInput.getText().trim();
        Category selectedCategory = categoryAddCombo.getSelectionModel().getSelectedItem();

        if (selectedCategory == null) {
            mealDisplayLabel.setText("⚠️ تنبيه: يرجى اختيار تصنيف للوجبة أولاً!");
            return;
        }

        if (!mealName.isEmpty()) {
            boolean isAdded = DatabaseManager.addMeal(mealName, selectedCategory.getId());

            if (isAdded) {
                mealDisplayLabel.setText("✅ تم حفظ \"" + mealName + "\" في تصنيف [" + selectedCategory.getName() + "]!");
                newMealInput.clear();
            } else {
                mealDisplayLabel.setText("⚠️ تنبيه: وجبة \"" + mealName + "\" موجودة بالفعل في قائمتك مسبقاً!");
            }
        } else {
            mealDisplayLabel.setText("⚠️ تنبيه: الرجاء إدخال اسم وجبة قبل الحفظ!");
        }
    }

    @FXML
    protected void onDeleteMealClick() {
        String mealName = newMealInput.getText().trim();

        if (!mealName.isEmpty()) {
            boolean isDeleted = DatabaseManager.deleteMeal(mealName);

            if (isDeleted) {
                mealDisplayLabel.setText("🗑️ تم حذف \"" + mealName + "\" من بنك البيانات بنجاح.");
                newMealInput.clear();
            } else {
                mealDisplayLabel.setText("❌ لم يتم العثور على وجبة باسم \"" + mealName + "\" لحذفها!");
            }
        } else {
            mealDisplayLabel.setText("⚠️ يرجى كتابة اسم الوجبة التي تريد حذفها داخل الحقل أولاً.");
        }
    }

    @FXML
    protected void onBrowseMealsClick() {
        ObservableList<Meal> mealsData = DatabaseManager.getMealsList();

        if (mealsData.isEmpty()) {
            mealDisplayLabel.setText("⚠️ بنك البيانات فارغ حالياً!\n يرجى إضافة وجبات أولاً قبل التصفح.");
            return;
        }

        ObservableList<String> displayList = FXCollections.observableArrayList();
        for (Meal m : mealsData) {
            String catName = m.getCategoryName() != null ? m.getCategoryName() : "بدون تصنيف";
            displayList.add(m.getName() + "  🏷️ (" + catName + ")");
        }

        ListView<String> listView = new ListView<>(displayList);
        listView.setPrefHeight(250);

        Button deleteButton = new Button("❌ حذف الوجبة المحددة");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8px 15px;");
        deleteButton.setMaxWidth(Double.MAX_VALUE);

        deleteButton.setOnAction(event -> {
            int selectedIndex = listView.getSelectionModel().getSelectedIndex();

            if (selectedIndex >= 0) {
                Meal selectedMealObj = mealsData.get(selectedIndex);
                boolean success = DatabaseManager.deleteMeal(selectedMealObj.getName());

                if (success) {
                    displayList.remove(selectedIndex);
                    mealsData.remove(selectedIndex);
                    mealDisplayLabel.setText("تم حذف '" + selectedMealObj.getName() + "' بنجاح!");
                } else {
                    mealDisplayLabel.setText("❌ فشل حذف الوجبة من قاعدة البيانات.");
                }
            } else {
                mealDisplayLabel.setText("ℹ️ يرجى تحديد وجبة من القائمة أولاً لحذفها.");
            }
        });

        VBox root = new VBox(12);
        root.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-background-color: #f8fafc;");

        Label titleLabel = new Label("📂 تصفح وإدارة الوجبات");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        root.getChildren().addAll(titleLabel, listView, deleteButton);

        Stage stage = new Stage();
        stage.setTitle("إدارة الوجبات 🛠️");
        stage.setScene(new Scene(root, 300, 300));

        stage.show();
    }
}