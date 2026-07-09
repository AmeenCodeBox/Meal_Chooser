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
            String catName = m.getCategoryName() != null ? m.getCategoryName() : "بدون تصنيف ⚠️";
            displayList.add(m.getName() + "  🏷️ (" + catName + ")");
        }

        ListView<String> listView = new ListView<>(displayList);
        listView.setPrefHeight(220);

        Label changeCatLabel = new Label("🔄 تغيير تصنيف الوجبة المحددة إلى:");
        changeCatLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569; -fx-font-weight: bold;");

        ComboBox<Category> changeCategoryCombo = new ComboBox<>(DatabaseManager.getCategoriesList());
        changeCategoryCombo.setMaxWidth(Double.MAX_VALUE);
        changeCategoryCombo.setPromptText("اختر التصنيف الجديد...");

        String comboStyle = "-fx-background-radius: 6; -fx-border-radius: 6; -fx-cursor: hand; -fx-background-color: white; -fx-border-color: #cbd5e1;";
        changeCategoryCombo.setStyle(comboStyle);
        changeCategoryCombo.setOnMouseEntered(e -> changeCategoryCombo.setStyle("-fx-background-radius: 6; -fx-border-radius: 6; -fx-cursor: hand; -fx-background-color: #f1f5f9; -fx-border-color: #3498db;"));
        changeCategoryCombo.setOnMouseExited(e -> changeCategoryCombo.setStyle(comboStyle));

        if (!changeCategoryCombo.getItems().isEmpty()) {
            changeCategoryCombo.getSelectionModel().selectFirst();
        }

        Button updateCatButton = new Button("💾 حفظ التصنيف الجديد");
        updateCatButton.setMaxWidth(Double.MAX_VALUE);

        String updateBtnStyle = "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6;";
        updateCatButton.setStyle(updateBtnStyle);
        updateCatButton.setOnMouseEntered(e -> updateCatButton.setStyle("-fx-background-color: #219a52; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6; -fx-effect: dropshadow(three-pass-box, rgba(33,154,82,0.4), 6, 0, 0, 2);"));
        updateCatButton.setOnMouseExited(e -> updateCatButton.setStyle(updateBtnStyle));

        updateCatButton.setOnAction(e -> {
            int selectedIndex = listView.getSelectionModel().getSelectedIndex();
            Category targetCategory = changeCategoryCombo.getSelectionModel().getSelectedItem();

            if (selectedIndex >= 0 && targetCategory != null) {
                Meal selectedMealObj = mealsData.get(selectedIndex);
                boolean success = DatabaseManager.updateMealCategory(selectedMealObj.getName(), targetCategory.getId());

                if (success) {
                    selectedMealObj.setCategoryName(targetCategory.getName());
                    selectedMealObj.setCategoryId(targetCategory.getId());
                    displayList.set(selectedIndex, selectedMealObj.getName() + "  🏷️ (" + targetCategory.getName() + ")");
                    mealDisplayLabel.setText("✅ تم نقل الوجبة '" + selectedMealObj.getName() + "' إلى تصنيف [" + targetCategory.getName() + "]!");
                } else {
                    mealDisplayLabel.setText("❌ فشل تحديث تصنيف الوجبة.");
                }
            } else {
                mealDisplayLabel.setText("ℹ️ يرجى تحديد وجبة من القائمة وتصنيف جديد أولاً.");
            }
        });

        Button deleteButton = new Button("❌ حذف الوجبة المحددة نهائياً");
        deleteButton.setMaxWidth(Double.MAX_VALUE);

        String deleteBtnStyle = "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6;";
        deleteButton.setStyle(deleteBtnStyle);
        deleteButton.setOnMouseEntered(e -> deleteButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6; -fx-effect: dropshadow(three-pass-box, rgba(192,57,43,0.4), 6, 0, 0, 2);"));
        deleteButton.setOnMouseExited(e -> deleteButton.setStyle(deleteBtnStyle));

        deleteButton.setOnAction(event -> {
            int selectedIndex = listView.getSelectionModel().getSelectedIndex();

            if (selectedIndex >= 0 && selectedIndex < mealsData.size()) {
                Meal selectedMealObj = mealsData.get(selectedIndex);
                boolean success = DatabaseManager.deleteMeal(selectedMealObj.getName());

                if (success) {
                    listView.getSelectionModel().clearSelection();
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

        Button manageCategoriesButton = new Button("⚙️ إدارة التصنيفات العامّة");
        manageCategoriesButton.setMaxWidth(Double.MAX_VALUE);

        String manageBtnStyle = "-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6;";
        manageCategoriesButton.setStyle(manageBtnStyle);
        manageCategoriesButton.setOnMouseEntered(e -> manageCategoriesButton.setStyle("-fx-background-color: #6c7a7d; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 6, 0, 0, 2);"));
        manageCategoriesButton.setOnMouseExited(e -> manageCategoriesButton.setStyle(manageBtnStyle));

        manageCategoriesButton.setOnAction(catEvent -> {
            Stage catStage = new Stage();
            catStage.setTitle("إدارة التصنيفات ⚙️");

            ObservableList<Category> categoriesData = DatabaseManager.getCategoriesList();
            ListView<Category> catListView = new ListView<>(categoriesData);
            catListView.setPrefHeight(150);

            TextField newCatInput = new TextField();
            newCatInput.setPromptText("اسم التصنيف الجديد...");
            newCatInput.setStyle("-fx-background-radius: 5; -fx-padding: 6; -fx-alignment: center_right;");

            Button addCatBtn = new Button("➕ إضافة تصنيف");
            addCatBtn.setMaxWidth(Double.MAX_VALUE);

            String addCatStyle = "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6;";
            addCatBtn.setStyle(addCatStyle);
            addCatBtn.setOnMouseEntered(a -> addCatBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 1);"));
            addCatBtn.setOnMouseExited(a -> addCatBtn.setStyle(addCatStyle));

            Button deleteCatBtn = new Button("🗑️ حذف التصنيف المحدد");
            deleteCatBtn.setMaxWidth(Double.MAX_VALUE);

            String delCatStyle = "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6;";
            deleteCatBtn.setStyle(delCatStyle);
            deleteCatBtn.setOnMouseEntered(d -> deleteCatBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 1);"));
            deleteCatBtn.setOnMouseExited(d -> deleteCatBtn.setStyle(delCatStyle));

            addCatBtn.setOnAction(a -> {
                String catName = newCatInput.getText().trim();
                if (!catName.isEmpty()) {
                    boolean success = DatabaseManager.addCategory(catName);
                    if (success) {
                        newCatInput.clear();
                        categoriesData.setAll(DatabaseManager.getCategoriesList());
                        changeCategoryCombo.setItems(DatabaseManager.getCategoriesList());
                        initialize(null, null);
                    }
                }
            });

            deleteCatBtn.setOnAction(d -> {
                Category selectedCat = catListView.getSelectionModel().getSelectedItem();
                if (selectedCat != null) {
                    boolean success = DatabaseManager.deleteCategory(selectedCat.getId());
                    if (success) {
                        categoriesData.remove(selectedCat);
                        changeCategoryCombo.setItems(DatabaseManager.getCategoriesList());
                        initialize(null, null);
                    }
                }
            });

            VBox catRoot = new VBox(10);
            catRoot.setStyle("-fx-padding: 15; -fx-alignment: center; -fx-background-color: #ffffff;");
            catRoot.getChildren().addAll(new Label("🗂️ القائمة الحالية:"), catListView, newCatInput, addCatBtn, deleteCatBtn);

            // --- التعديل المضاف هنا: ربط الـ CSS بنافذة إدارة التصنيفات ---
            Scene catScene = new Scene(catRoot, 280, 350);
            catScene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

            catStage.setScene(catScene);
            catStage.show();
        });

        Label titleLabel = new Label("📂 تصفح وإدارة الوجبات والتصنيفات");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 15; -fx-alignment: center; -fx-background-color: #f8fafc;");

        root.getChildren().addAll(
                titleLabel,
                listView,
                changeCatLabel,
                changeCategoryCombo,
                updateCatButton,
                new Label("------------------------------------"),
                manageCategoriesButton,
                deleteButton
        );

        Stage stage = new Stage();
        stage.setTitle("إدارة الوجبات والتصنيفات 🛠️");
        Scene scene = new Scene(root, 340, 520);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void onMouseEnteredEffect(javafx.scene.input.MouseEvent event) {
        Object source = event.getSource();

        if (source instanceof Button) {
            Button button = (Button) source;
            String currentStyle = button.getStyle();

            if (currentStyle.contains("#2ecc71")) {
                button.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 5, 0, 0, 2);");
            } else if (currentStyle.contains("#e67e22")) {
                button.setStyle("-fx-background-color: #d35400; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(211,84,0,0.4), 10, 0, 0, 4);");
            } else if (currentStyle.contains("#3498db")) {
                button.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(41,128,185,0.3), 6, 0, 0, 2);");
            } else if (currentStyle.contains("#f1f5f9")) {
                button.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(239,68,68,0.2), 5, 0, 0, 2);");
            }
        } else if (source instanceof ComboBox) {
            ComboBox<?> combo = (ComboBox<?>) source;
            combo.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #3498db; -fx-background-color: #f1f5f9; -fx-alignment: center_right; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(52,152,219,0.2), 5, 0, 0, 1);");
        }
    }

    @FXML
    private void onMouseExitedEffect(javafx.scene.input.MouseEvent event) {
        Object source = event.getSource();

        if (source instanceof Button) {
            Button button = (Button) source;
            String currentStyle = button.getStyle();

            if (currentStyle.contains("#27ae60")) {
                button.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
            } else if (currentStyle.contains("#d35400")) {
                button.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-weight: bold; -fx-cursor: hand;");
            } else if (currentStyle.contains("#2980b9")) {
                button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
            } else if (currentStyle.contains("#ef4444")) {
                button.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #ef4444; -fx-border-color: #fca5a5; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
            }
        } else if (source instanceof ComboBox) {
            ComboBox<?> combo = (ComboBox<?>) source;
            combo.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #cbd5e1; -fx-background-color: white; -fx-alignment: center_right; -fx-cursor: hand;");
        }
    }
}