package lp;

import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import java.util.ArrayList;
import java.util.Collection;

public class ComputerProduction {

    public static void main(String[] args) {
        int costToBuildAtRegularPace = 50;
        int monthlyStorageCost = 5;
        int costToBuildOvertime = 75;
        int[] orders = {80, 180, 135, 240, 95, 139};

        MonthlyProduction[] monthlyProductions = optimize(orders, costToBuildAtRegularPace, monthlyStorageCost,
                costToBuildOvertime, 150, 60);

        for (MonthlyProduction monthlyProduction: monthlyProductions) {
            System.out.println(monthlyProduction);
        }
    }

    public static class MonthlyProduction {
        public int stockProduced = 0;
        public int ordersUsingRegPace = 0;
        public int ordersUsingOvertime = 0;
        public int ordersFullFilled = 0;

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("MonthlyProduction{");
            sb.append("stockProduced=").append(stockProduced);
            sb.append(", ordersUsingRegPace=").append(ordersUsingRegPace);
            sb.append(", ordersUsingOvertime=").append(ordersUsingOvertime);
            sb.append(", ordersFullFilled=").append(ordersFullFilled);
            sb.append('}');
            return sb.toString();
        }
    }

    /**
     *
     * This optimization problem can be described with the following
     *
     * Objective function: 50 * (x1 + x2 + x3 + ... + x6) + 75 * (y1 + y2 + y3 + ... + y6) + 5 * (z1 + z2 + z3 + ... + z5)
     * where: x1 is the amount of computers to build at regular pace for the first month
     *        y1 is the amount of computers to build using overtime for the first month
     *        z1 is the amount of stock created for the first month
     *
     * and following constraints equalities:
     *
     *   x1 + y1 = 80 + z1   (provided we start with an empty stock z0 = 0)
     *   z1 + x2 + y2 = 180 + z2
     *   z2 + x3 + y3 = 135 + z3
     *   z3 + x4 + y4 = 240 + z4
     *   z4 + x5 + y5 = 95 + z5
     *   z5 + x6 + y6 = 139  (provided we expect no stock left at the end of the period)
     *
     * and constraints inequalities:
     *
     *   0 <= xn <= 150 for n = 1..6
     *   0 <= yn <= 60 for n = 1..6
     *        zn >= 0 for n = 1..5
     *
     * @param orders
     * @param costToBuildAtRegularPace
     * @param monthlyStorageCost
     * @param costToBuildOvertime
     * @return Details about each month production order.
     */
    public static MonthlyProduction[] optimize(int[] orders, int costToBuildAtRegularPace, int monthlyStorageCost,
                                               int costToBuildOvertime, int maxProduction, int maxOvertimeProduction) {

        if (orders.length != 6) {
            throw new RuntimeException("Only supports a 6 month order plan");
        }

        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {
                costToBuildAtRegularPace, costToBuildAtRegularPace, costToBuildAtRegularPace, costToBuildAtRegularPace, costToBuildAtRegularPace, costToBuildAtRegularPace,
                costToBuildOvertime, costToBuildOvertime, costToBuildOvertime, costToBuildOvertime, costToBuildOvertime, costToBuildOvertime,
                monthlyStorageCost, monthlyStorageCost, monthlyStorageCost, monthlyStorageCost, monthlyStorageCost}, 0);

        Collection<LinearConstraint> constraints = new ArrayList<>();

        constraints.add(new LinearConstraint(new double[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, Relationship.LEQ, maxProduction));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, Relationship.LEQ, maxProduction));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, Relationship.LEQ, maxProduction));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, Relationship.LEQ, maxProduction));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, Relationship.LEQ, maxProduction));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, Relationship.LEQ, maxProduction));

        constraints.add(new LinearConstraint(new double[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, Relationship.GEQ, 0));

        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, Relationship.LEQ, maxOvertimeProduction));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0}, Relationship.LEQ, maxOvertimeProduction));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0}, Relationship.LEQ, maxOvertimeProduction));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0}, Relationship.LEQ, maxOvertimeProduction));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0}, Relationship.LEQ, maxOvertimeProduction));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}, Relationship.LEQ, maxOvertimeProduction));

        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}, Relationship.GEQ, 0));

        constraints.add(new LinearConstraint(new double[] { 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0}, Relationship.EQ, orders[0]));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, -1, 0, 0, 0}, Relationship.EQ, orders[1]));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, -1, 0, 0}, Relationship.EQ, orders[2]));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, -1, 0}, Relationship.EQ, orders[3]));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, -1}, Relationship.EQ, orders[4]));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1}, Relationship.EQ, orders[5]));

        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, Relationship.GEQ, 0));

        PointValuePair solution = new SimplexSolver().optimize(f, new LinearConstraintSet(constraints), GoalType.MINIMIZE);

        //get minimized total production cost
        double max = solution.getValue();
        System.out.println("Optimized production cost: " + (int) max);

        MonthlyProduction[] monthlyProductions = new MonthlyProduction[orders.length];

        for (int i = 0; i < 6; i++) {
            MonthlyProduction monthlyProduction = new MonthlyProduction();
            monthlyProduction.ordersUsingRegPace = (int) solution.getPoint()[i];
            monthlyProduction.ordersUsingOvertime = (int) solution.getPoint()[i + 6];
            monthlyProduction.ordersFullFilled = orders[i];
            if (i != 5) {
                monthlyProduction.stockProduced = (int) solution.getPoint()[i + 12];
            }
            monthlyProductions[i] = monthlyProduction;
        }

        return monthlyProductions;
    }
}
