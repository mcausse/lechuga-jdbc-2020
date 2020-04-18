//// TODO
//package org.lechuga.mql.typesafe;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.StringJoiner;
//
//import org.lechuga.Order;
//import org.lechuga.PropertyConfig;
//import org.lenteja.jdbc.query.IQueryObject;
//
//public class TOrders implements IQueryObject {
//
//    private final List<TOrder> orders;
//
//    private TOrders(List<TOrder> orders) {
//        super();
//        this.orders = orders;
//    }
//
//    public List<TOrder> getOrders() {
//        return orders;
//    }
//
//    @SafeVarargs
//    public static TOrders by(TOrder... orders) {
//        return new TOrders(Arrays.asList(orders));
//    }
//
//    public static TOrders by(List<TOrder> orders) {
//        return new TOrders(orders);
//    }
//
//    ///////////////////////////////////////////////////
//
//    @Override
//    public String getQuery() {
//
//    	StringJoiner j = new StringJoiner(", ");
//		for (Order o : orders) {
//			PropertyConfig p = entityConfig.getAllPropsMap().get(o.getPropName());
//			j.add(p.getColumnName() + o.getOrder());
//		}
//		return " order by " + j.toString();
//
//
//
//        StringBuilder s = new StringBuilder();
//        int c = 0;
//        for (TOrder o : orders) {
//            if (c > 0) {
//                s.append(", ");
//            }
//            c++;
//            FieldDef<?, ?> metac = o.getField();
//            s.append("{" + metac.getAlias() + "." + metac.getPropertyName() + "} " + o.getOrder());
//        }
//        return s.toString();
//    }
//
//    @Override
//    public Object[] getArgs() {
//        return new Object[] {};
//    }
//
//    @Override
//    public List<Object> getArgsList() {
//        return Collections.emptyList();
//    }
//
//    ///////////////////////////////////////////////////
//
//    public static class TOrder {
//
//        final FieldDef<?, ?> field;
//        final String order;
//
//        private TOrder(FieldDef<?, ?> field, String order) {
//            super();
//            this.field = field;
//            this.order = order;
//        }
//
//        public static TOrder asc(FieldDef<?, ?> field) {
//            return new TOrder(field, " ASC");
//        }
//
//        public static TOrder desc(FieldDef<?, ?> field) {
//            return new TOrder(field, " DESC");
//        }
//
//        public FieldDef<?, ?> getField() {
//            return field;
//        }
//
//        public String getOrder() {
//            return order;
//        }
//    }
//
//}