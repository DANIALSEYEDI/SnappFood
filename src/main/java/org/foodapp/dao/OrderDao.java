package org.foodapp.dao;
import org.foodapp.model.*;
import org.foodapp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.*;
public class OrderDao {
    public List<Order> findByFilters(Long restaurantId, String status, String search, String userId, String courierId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            String hql = "SELECT o FROM Order o WHERE o.restaurant.id = :restaurantId";
            if (status != null) hql += " AND o.status = :status";
            if (search != null) hql += " AND (o.customer.fullName LIKE :search OR o.notes LIKE :search)";
            if (userId != null) hql += " AND o.customer.id = :userId";
            if (courierId != null) hql += " AND o.courier.id = :courierId";

            Query<Order> query = session.createQuery(hql, Order.class);
            query.setParameter("restaurantId", restaurantId);
            if (status != null) query.setParameter("status", status);
            if (search != null) query.setParameter("search", "%" + search + "%");
            if (userId != null) query.setParameter("userId", Long.parseLong(userId));
            if (courierId != null) query.setParameter("courierId", Long.parseLong(courierId));

            return query.list();
        } finally {
            session.close();
        }
    }

    public Order findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("""
            SELECT o FROM Order o
            LEFT JOIN FETCH o.itemsOfOrder i
            LEFT JOIN FETCH i.item
            WHERE o.id = :id
        """, Order.class)
                    .setParameter("id", id)
                    .uniqueResult();
        }
    }


    public void update(Order order) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.merge(order);
        session.getTransaction().commit();
        session.close();
    }

    public List<Order> findHistoryByUser(User user, String search, String vendorName) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        String hql = "FROM Order o WHERE o.user = :user";

        if (search != null && !search.isBlank()) {
            hql += " AND lower(o.deliveryAddress) LIKE :search";
        }
        if (vendorName != null && !vendorName.isBlank()) {
            hql += " AND lower(o.restaurant.name) LIKE :vendor";
        }

        Query<Order> query = session.createQuery(hql, Order.class);
        query.setParameter("user", user);
        if (search != null && !search.isBlank()) {
            query.setParameter("search", "%" + search.toLowerCase() + "%");
        }
        if (vendorName != null && !vendorName.isBlank()) {
            query.setParameter("vendor", "%" + vendorName.toLowerCase() + "%");
        }

        List<Order> results = query.list();
        session.close();
        return results;
    }

    public void save(Order order) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            session.persist(order);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }



    public List<Order> findByStatus(OrderRestaurantStatus status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.itemsOfOrder i
            LEFT JOIN FETCH i.item
            WHERE o.status = :status
        """, Order.class)
                    .setParameter("status", status)
                    .list();
        }
    }







    public Order findByIdWithItems(Long orderId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Order order = session.createQuery(
                        "SELECT o FROM Order o " +
                                "LEFT JOIN FETCH o.itemsOfOrder i " +
                                "LEFT JOIN FETCH i.item " +
                                "LEFT JOIN FETCH o.restaurant " +
                                "LEFT JOIN FETCH o.courier " +
                                "WHERE o.id = :id", Order.class)
                .setParameter("id", orderId)
                .uniqueResult();
        session.close();
        return order;
    }





    public List<Order> findHistoryForUser(Long userId, String search, String vendorName) {
        Session session = HibernateUtil.getSessionFactory().openSession();

        String hql = """
        SELECT DISTINCT o FROM Order o
        LEFT JOIN FETCH o.itemsOfOrder i
        LEFT JOIN FETCH i.item fi
        LEFT JOIN FETCH o.restaurant r
        WHERE o.user.id = :userId
        AND (:search IS NULL OR LOWER(fi.name) LIKE :search)
        AND (:vendor IS NULL OR LOWER(r.name) LIKE :vendor)
        ORDER BY o.id DESC
    """;

        Query<Order> query = session.createQuery(hql, Order.class);
        query.setParameter("userId", userId);
        query.setParameter("search", (search == null || search.isBlank()) ? null : "%" + search.toLowerCase() + "%");
        query.setParameter("vendor", (vendorName == null || vendorName.isBlank()) ? null : "%" + vendorName.toLowerCase() + "%");

        List<Order> orders = query.getResultList();
        session.close();
        return orders;
    }

    public List<Order> findDeliveryHistoryByCourier(User courier, String search, String vendor, String userPhone) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
            SELECT DISTINCT o FROM Order o
            JOIN FETCH o.restaurant r
            LEFT JOIN FETCH o.itemsOfOrder io
            WHERE o.courier = :courier
            AND ( :search IS NULL OR LOWER(o.deliveryAddress) LIKE :search )
            AND ( :vendor IS NULL OR LOWER(r.name) LIKE :vendor )
            AND ( :userPhone IS NULL OR LOWER(o.user.phoneNumber) LIKE :userPhone )
            ORDER BY o.id DESC
        """;

            Query<Order> query = session.createQuery(hql, Order.class);
            query.setParameter("courier", courier);
            query.setParameter("search", search != null ? "%" + search.toLowerCase() + "%" : null);
            query.setParameter("vendor", vendor != null ? "%" + vendor.toLowerCase() + "%" : null);
            query.setParameter("userPhone", userPhone != null ? "%" + userPhone.toLowerCase() + "%" : null);

            return query.list();
        }
    }

    public List<Order> findByAdminFilters(Map<String, String> params) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("FROM Order o WHERE 1=1");

            if (params.containsKey("search")) {
                hql.append(" AND o.deliveryAddress LIKE :search");
            }
            if (params.containsKey("vendor")) {
                hql.append(" AND o.restaurant.name LIKE :vendor");
            }
            if (params.containsKey("courier")) {
                hql.append(" AND o.courier.phoneNumber LIKE :courier");
            }
            if (params.containsKey("customer")) {
                hql.append(" AND o.user.phoneNumber LIKE :customer");
            }
            if (params.containsKey("status")) {
                hql.append(" AND o.status = :status");
            }

            Query<Order> query = session.createQuery(hql.toString(), Order.class);

            if (params.containsKey("search")) {
                query.setParameter("search", "%" + params.get("search") + "%");
            }
            if (params.containsKey("vendor")) {
                query.setParameter("vendor", "%" + params.get("vendor") + "%");
            }
            if (params.containsKey("courier")) {
                query.setParameter("courier", "%" + params.get("courier") + "%");
            }
            if (params.containsKey("customer")) {
                query.setParameter("customer", "%" + params.get("customer") + "%");
            }
            if (params.containsKey("status")) {
                try {
                    OrderStatus status = OrderStatus.valueOf(params.get("status").toUpperCase().replace(" ", "_"));
                    query.setParameter("status", status);
                } catch (IllegalArgumentException e) {
                    return List.of();
                }
            }

            return query.list();
        }
    }



    public int countOrdersByCoupon(Coupon coupon) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                            "SELECT COUNT(o) FROM Order o " +
                                    "WHERE o.coupon = :coupon " +
                                    "AND o.status NOT IN (:cancelStatuses)", Long.class
                    )
                    .setParameter("coupon", coupon)
                    .setParameterList("cancelStatuses", List.of(OrderStatus.CANCELLED, OrderStatus.UNPAID_AND_CANCELLED))
                    .uniqueResult();

            return count != null ? count.intValue() : 0;
        }
    }




}

