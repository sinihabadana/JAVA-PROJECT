package jdbc;

import java.sql.*;
import java.util.Scanner;

public class LeaveManagement {

    static final String URL = "jdbc:mysql://localhost:3306/leave_management";
    static final String USER = "root";
    static final String PASS = "Siniha042325";

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connected to Database Successfully!");

            while (true) {
                System.out.println("\n===== LOGIN MENU =====");
                System.out.println("1. Employee Login");
                System.out.println("2. Admin Login");
                System.out.println("3. Exit");
                System.out.print("Enter choice: ");

                int choice = sc.nextInt();

                switch (choice) {
                    case 1:
                        EmployeeModule.employeeLogin(con);
                        break;
                    case 2:
                        AdminModule.adminLogin(con);
                        break;
                    case 3:
                        con.close();
                        System.out.println("Application Closed.");
                        sc.close();
                        return;
                    default:
                        System.out.println("Invalid choice!");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
