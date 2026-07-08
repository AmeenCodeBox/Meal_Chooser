package com.ameen.meal_chooser;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
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
    protected void onGenerateMealClick() {
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
            mealDisplayLabel.setText("⚠️ بنك البيانات فارغ حالياً!");
            return;
        }

        ListView<String> listView = new ListView<>(allMeals);
        listView.setPrefHeight(300);

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-background-color: #f4f4f4;");

        Label titleLabel = new Label("قائمة وجباتك المخزنة");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        root.getChildren().addAll(titleLabel, listView);

        Stage stage = new Stage();
        stage.setTitle("تصفح الوجبات 📂");
        stage.setScene(new Scene(root, 300, 400));

        stage.show();
    }
}