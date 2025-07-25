package org.foodapp.dao;
import org.foodapp.model.*;
import org.foodapp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.*;
public class OrderDao {


    public List<Order> findByFilters(Long restaurantId, String status, String search, String user, String courier) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.itemsOfOrder i
            LEFT JOIN FETCH i.item fi
            LEFT JOIN FETCH o.user u
            LEFT JOIN FETCH o.courier c
            WHERE o.restaurant.id = :restaurantId
        """;

            if (status != null && !status.isBlank()) {
                hql += " AND o.status = :status";
            }

            if (user != null && !user.isBlank()) {
                hql += """
            AND (
                LOWER(u.fullName) LIKE :user
                OR u.phoneNumber LIKE :user
                OR CAST(u.id AS string) = :user
            )
            """;
            }

            if (courier != null && !courier.isBlank()) {
                hql += """
            AND (
                LOWER(c.fullName) LIKE :courier
                OR c.phoneNumber LIKE :courier
                OR CAST(c.id AS string) = :courier
            )
            """;
            }

            if (search != null && !search.isBlank()) {
                hql += """
            AND (
                CAST(o.id AS string) LIKE :search
                OR LOWER(o.deliveryAddress) LIKE :search
                OR LOWER(fi.name) LIKE :search
            )
            """;
            }

            Query<Order> query = session.createQuery(hql, Order.class);
            query.setParameter("restaurantId", restaurantId);

            if (status != null && !status.isBlank()) {
                try {
                    OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase().replace(" ", "_"));
                    query.setParameter("status", orderStatus);
                } catch (IllegalArgumentException e) {
                    return List.of();
                }
            }

            if (user != null && !user.isBlank()) {
                query.setParameter("user", "%" + user.toLowerCase() + "%");
            }

            if (courier != null && !courier.isBlank()) {
                query.setParameter("courier", "%" + courier.toLowerCase() + "%");
            }

            if (search != null && !search.isBlank()) {
                query.setParameter("search", "%" + search.toLowerCase() + "%");
            }

            return query.getResultList();
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

    public List<Order> findAvailableForDelivery() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT o FROM Order o " +
                                    "LEFT JOIN FETCH o.itemsOfOrder i " +
                                    "WHERE o.status = :status", Order.class)
                    .setParameter("status", OrderStatus.FINDING_COURIER)
                    .getResultList();
        }
    }

    public List<Order> findOrdersAssignedToCourier(Long courierId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT o FROM Order o " +
                                    "LEFT JOIN FETCH o.itemsOfOrder i " +
                                    "WHERE o.status = :status " +
                                    "AND o.courier.id = :courierId", Order.class)
                    .setParameter("status", OrderStatus.ON_THE_WAY)
                    .setParameter("courierId", courierId)
                    .getResultList();
        }
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
            StringBuilder hql = new StringBuilder("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.itemsOfOrder i
            LEFT JOIN FETCH i.item f
            LEFT JOIN FETCH o.user u
            LEFT JOIN FETCH o.restaurant r
            LEFT JOIN FETCH o.courier c
            WHERE 1=1
        """);

            if (params.containsKey("search")) {
                hql.append("""
                AND (
                    LOWER(o.deliveryAddress) LIKE :search
                    OR LOWER(f.name) LIKE :search
                    OR STR(o.id) = :searchExact
                )
            """);
            }

            if (params.containsKey("vendor")) {
                hql.append("""
                AND (
                    LOWER(r.name) LIKE :vendor
                    OR STR(r.id) = :vendor
                )
            """);
            }

            if (params.containsKey("courier")) {
                hql.append("""
                AND (
                    LOWER(c.fullName) LIKE :courier
                    OR STR(c.id) = :courier
                )
            """);
            }

            if (params.containsKey("customer")) {
                hql.append("""
                AND (
                    LOWER(u.fullName) LIKE :customer
                    OR STR(u.id) = :customer
                )
            """);
            }

            if (params.containsKey("status")) {
                hql.append(" AND o.status = :status ");
            }

            Query<Order> query = session.createQuery(hql.toString(), Order.class);

            if (params.containsKey("search")) {
                String search = params.get("search").toLowerCase();
                query.setParameter("search", "%" + search + "%");
                query.setParameter("searchExact", search);
            }

            if (params.containsKey("vendor")) {
                query.setParameter("vendor", params.get("vendor").toLowerCase());
            }

            if (params.containsKey("courier")) {
                query.setParameter("courier", params.get("courier").toLowerCase());
            }

            if (params.containsKey("customer")) {
                query.setParameter("customer", params.get("customer").toLowerCase());
            }

            if (params.containsKey("status")) {
                try {
                    OrderStatus status = OrderStatus.valueOf(params.get("status").toUpperCase().replace(" ", "_"));
                    query.setParameter("status", status);
                } catch (IllegalArgumentException e) {
                    return List.of();
                }
            }

            return query.getResultList();
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

