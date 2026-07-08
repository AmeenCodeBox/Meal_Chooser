package com.ameen.meal_chooser;

import java.sql.*;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:meals_db.db";

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println("an error occurred during connecting to data base: " + e.getMessage());
        }
        return conn;
    }

    public static void createDatabase() {

        String sql = "CREATE TABLE IF NOT EXISTS meals (\n"
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " name TEXT NOT NULL UNIQUE\n"
                + ");";

        try {
            Connection connection = connect();
            Statement stmt = connection.createStatement();
            stmt.execute(sql);
            System.out.println("Database created successfully");
        } catch (SQLException e) {
            System.out.println("an error occurred while creating the database: " + e.getMessage());
        }
    }

    public static boolean addMeal(String mealName) {
        String sql = "INSERT INTO meals (name) VALUES (?)";
        try {
            Connection connection = connect();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, mealName);
            preparedStatement.executeUpdate();
            System.out.println("Meal added successfully" + mealName);
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
        String sql = "DELETE FROM meals WHERE name = ?";

        try (Connection connection = connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

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

    public static String getRandomMeal() {
        String randomMeal = "There is no meal in the database yet !!";
        String sql = "SELECT * FROM meals ORDER BY RANDOM() LIMIT 1";
        try {
            Connection connection = connect();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            if (resultSet.next()) {
                randomMeal = resultSet.getString("name");
            }
        } catch (SQLException e) {
            System.out.println("an error occurred while fetching a random meal: " + e.getMessage());
        }
        return randomMeal;
    }
}
