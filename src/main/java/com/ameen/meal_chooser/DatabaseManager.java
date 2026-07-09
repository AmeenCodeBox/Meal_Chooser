package com.ameen.meal_chooser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.sql.*;

public class DatabaseManager {

    private static String getDatabaseURL() {
        String userHome = System.getProperty("user.home");
        String folderPath = userHome + File.separator + "meals";
        File directory = new File(folderPath);
        if (!directory.exists()) {
            directory.mkdirs();
            System.out.println("Directory \"meals\" created successfully in userPath :" + folderPath);
        }
        return "jdbc:sqlite:" + folderPath + File.separator + "meals_database.db";
    }

    private static Connection connect() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(getDatabaseURL());
            if (connection != null) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                }
            }
        } catch (SQLException e) {
            System.out.println("an error occurred during connecting to data base: " + e.getMessage());
        }
        return connection;
    }

    protected static void createDatabase() {
        String sqlCategories = "CREATE TABLE IF NOT EXISTS categories (\n"
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " name TEXT NOT NULL UNIQUE\n"
                + ");";

        String sqlMeals = "CREATE TABLE IF NOT EXISTS meals (\n"
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " name TEXT NOT NULL UNIQUE,\n"
                + " category_id INTEGER,\n"
                + " FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL\n"
                + ");";

        try (Connection connection = connect();
             Statement stmt = connection.createStatement()) {

            if (connection != null) {
                stmt.execute(sqlCategories);
                stmt.execute(sqlMeals);
                System.out.println("Database tables initialized successfully");

                insertDefaultCategories(connection);
            }
        } catch (SQLException e) {
            System.out.println("an error occurred while creating the database: " + e.getMessage());
        }
    }

    private static void insertDefaultCategories(Connection connection) {
        String checkSql = "SELECT COUNT(*) FROM categories";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("جدول التصنيفات فارغ تماماً، يتم إدخال القيم الافتراضية لأول مرة...");

                String[] defaults = {"أكل صيني", "أكل إيطالي", "سباغيتي", "وجبات سريعة", "أكل شرقي"};
                for (String cat : defaults) {
                    stmt.execute("INSERT OR IGNORE INTO categories (name) VALUES ('" + cat + "');");
                }
            } else {
                System.out.println("تم تخطي إدخال التصنيفات الافتراضية لأن المستخدم يمتلك بياناته الخاصة.");
            }

        } catch (SQLException e) {
            System.out.println("حدث خطأ أثناء فحص أو إدخال التصنيفات الافتراضية: " + e.getMessage());
        }
    }


    public static ObservableList<Category> getCategoriesList() {
        ObservableList<Category> categoriesList = FXCollections.observableArrayList();
        String sql = "SELECT id, name FROM categories";

        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                categoriesList.add(new Category(
                        resultSet.getInt("id"),
                        resultSet.getString("name")
                ));
            }
        } catch (SQLException e) {
            System.out.println("خطأ في جلب قائمة التصنيفات: " + e.getMessage());
        }
        return categoriesList;
    }


    public static boolean addMeal(String mealName, int categoryId) {
        String sql = "INSERT INTO meals (name, category_id) VALUES (?, ?)";

        try (Connection connection = connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            if (connection == null) return false;

            preparedStatement.setString(1, mealName);
            preparedStatement.setInt(2, categoryId);
            preparedStatement.executeUpdate();
            System.out.println("Meal added successfully: " + mealName);
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE") || e.getErrorCode() == 19) {
                System.out.println("الوجبة موجودة بالفعل في قاعدة البيانات : " + mealName);
            } else {
                System.out.println("an error occurred while adding the meal: " + e.getMessage());
            }
            return false;
        }
    }

    public static boolean deleteMeal(String mealName) {
        String sql = "DELETE FROM meals WHERE TRIM(name) = TRIM(?)";

        try (Connection connection = connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            if (connection == null) return false;

            preparedStatement.setString(1, mealName);
            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("تم حذف الوجبة بنجاح: " + mealName);
                return true;
            } else {
                System.out.println("لم يتم العثور على وجبة بهذا الاسم لحذفها.");
                return false;
            }

        } catch (SQLException e) {
            System.out.println("حدث خطأ أثناء حذف الوجبة: " + e.getMessage());
            return false;
        }
    }

    public static String getRandomMeal(int selectedCategoryId) {
        String randomMeal = "لا يوجد أي طبخات في هذا التصنيف بعد !\nأدخل طبختك الاولى";

        String sql;
        if (selectedCategoryId == -1) {
            sql = "SELECT name FROM meals ORDER BY RANDOM() LIMIT 1";
        } else {
            sql = "SELECT name FROM meals WHERE category_id = ? ORDER BY RANDOM() LIMIT 1";
        }

        try (Connection connection = connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            if (selectedCategoryId != -1) {
                preparedStatement.setInt(1, selectedCategoryId);
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    randomMeal = resultSet.getString("name");
                } else if (selectedCategoryId == -1) {
                    randomMeal = "لا يوجد أي طبخات في قاعدة البيانات بعد !\nأدخل طبختك الاولى";
                }
            }
        } catch (SQLException e) {
            System.out.println("an error occurred while fetching a random meal: " + e.getMessage());
        }
        return randomMeal;
    }

    public static ObservableList<Meal> getMealsList() {
        ObservableList<Meal> mealsList = FXCollections.observableArrayList();
        String sql = "SELECT m.id, m.name, m.category_id, c.name AS category_name " +
                "FROM meals m " +
                "LEFT JOIN categories c ON m.category_id = c.id";

        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                mealsList.add(new Meal(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getInt("category_id"),
                        resultSet.getString("category_name")
                ));
            }
        } catch (SQLException e) {
            System.out.println("خطأ في جلب قائمة الوجبات: " + e.getMessage());
        }
        return mealsList;
    }

    public static boolean addCategory(String categoryName) {
        String sql = "INSERT INTO categories (name) VALUES (?)";

        try (Connection connection = connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            if (connection == null) return false;

            preparedStatement.setString(1, categoryName);
            preparedStatement.executeUpdate();
            System.out.println("Category added successfully: " + categoryName);
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE") || e.getErrorCode() == 19) {
                System.out.println("التصنيف موجود بالفعل: " + categoryName);
            } else {
                System.out.println("حدث خطأ أثناء إضافة التصنيف: " + e.getMessage());
            }
            return false;
        }
    }

    public static boolean deleteCategory(int categoryId) {
        String sql = "DELETE FROM categories WHERE id = ?";

        try (Connection connection = connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            if (connection == null) return false;

            preparedStatement.setInt(1, categoryId);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("حدث خطأ أثناء حذف التصنيف: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateMealCategory(String mealName, int newCategoryId) {
        String sql = "UPDATE meals SET category_id = ? WHERE TRIM(name) = TRIM(?)";

        try (Connection connection = connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            if (connection == null) return false;

            preparedStatement.setInt(1, newCategoryId);
            preparedStatement.setString(2, mealName);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("حدث خطأ أثناء تحديث تصنيف الوجبة: " + e.getMessage());
            return false;
        }
    }
}