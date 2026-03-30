package jdbc;

import java.sql.*;
import java.util.Scanner;

public class LeaveModule {

    static Scanner sc = new Scanner(System.in);

    public static void applyLeave(Connection con, int empId) throws Exception {

        PreparedStatement ps =
            con.prepareStatement(
                "SELECT remaining_leaves, salary FROM employee WHERE emp_id=?");
        ps.setInt(1, empId);
        ResultSet rs = ps.executeQuery();
        rs.next();

        int remaining = rs.getInt("remaining_leaves");
        double salary = rs.getDouble("salary");

        System.out.println("\nLeave Types:");
        System.out.println("1. Sick");
        System.out.println("2. Casual");
        System.out.println("3. Earned");
        System.out.println("4. Other");
        System.out.print("Choose type: ");
        int type = sc.nextInt();

        String leaveType;
        switch (type) {
            case 1: leaveType = "Sick"; break;
            case 2: leaveType = "Casual"; break;
            case 3: leaveType = "Earned"; break;
            default: leaveType = "Other";
        }

        System.out.print("Enter Leave Days: ");
        int days = sc.nextInt();

        double salaryDeducted = 0;

        if (remaining < days) {
            System.out.println(
                "Insufficient leave balance (" + remaining + " days available).");
            System.out.print(
                "Do you want to take leave with salary deduction? (yes/no): ");
            String choice = sc.next();

            if (!choice.equalsIgnoreCase("yes")) {
                System.out.println("Leave not applied.");
                return;
            }

            double perDaySalary = salary / 30;
            salaryDeducted = perDaySalary * (days - remaining);
        }

        PreparedStatement insert =
            con.prepareStatement(
                "INSERT INTO leave_request " +
                "(emp_id, leave_days, leave_type, status, apply_date, salary_deducted) " +
                "VALUES (?, ?, ?, 'Pending', CURDATE(), ?)");

        insert.setInt(1, empId);
        insert.setInt(2, days);
        insert.setString(3, leaveType);
        insert.setDouble(4, salaryDeducted);
        insert.executeUpdate();

        System.out.println("Leave applied successfully!");
        if (salaryDeducted > 0) {
            System.out.println("Salary Deducted: ₹" + salaryDeducted);
        }
    }

    public static void viewEmployeeLeaves(Connection con, int empId) throws Exception {

        PreparedStatement ps =
            con.prepareStatement("SELECT * FROM leave_request WHERE emp_id=?");
        ps.setInt(1, empId);
        ResultSet rs = ps.executeQuery();

        System.out.println("\nLeaveID Days Type    Status SalaryDeducted");
        while (rs.next()) {
            System.out.println(
                rs.getInt("leave_id") + "     " +
                rs.getInt("leave_days") + "    " +
                rs.getString("leave_type") + "   " +
                rs.getString("status") + "   ₹" +
                rs.getDouble("salary_deducted"));
        }
    }
}
