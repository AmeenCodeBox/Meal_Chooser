package com.ameen.meal_chooser;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

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
}