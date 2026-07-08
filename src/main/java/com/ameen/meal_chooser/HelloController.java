package com.ameen.meal_chooser;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HelloController {

    @FXML
    private Label mealDisplayLabel;

    @FXML
    private TextField newMealInput;

    @FXML
    protected void onSuggestMealClick() {
        String suggestedMeal = DatabaseManager.getRandomMeal();
        mealDisplayLabel.setText(suggestedMeal);
    }

    @FXML
    protected void onAddMealClick() {
        String mealName = newMealInput.getText().trim();

        if (!mealName.isEmpty()) {

            boolean isAdded = DatabaseManager.addMeal(mealName);

            if (isAdded) {

                mealDisplayLabel.setText("✅ تم حفظ \"" + mealName + "\" في بنك البيانات!");
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
        ObservableList<String> allMeals = DatabaseManager.getMealsList();

        if (allMeals.isEmpty()) {
            mealDisplayLabel.setText("⚠️ بنك البيانات فارغ حالياً!\n يرجى إضافة وجبات أولاً قبل التصفح.");
            return;
        }

        ListView<String> listView = new ListView<>(allMeals);
        listView.setPrefHeight(250);

        Button deleteButton = new Button("❌ حذف الوجبة المحددة");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8px 15px;");
        deleteButton.setMaxWidth(Double.MAX_VALUE);

        deleteButton.setOnAction(event -> {
            String selectedMeal = listView.getSelectionModel().getSelectedItem();

            if (selectedMeal != null) {
                boolean success = DatabaseManager.deleteMeal(selectedMeal);

                if (success) {
                    allMeals.remove(selectedMeal);
                    mealDisplayLabel.setText("تم حذف '" + selectedMeal + "' بنجاح!");
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
        stage.setScene(new Scene(root, 250, 300));

        stage.show();
    }
}