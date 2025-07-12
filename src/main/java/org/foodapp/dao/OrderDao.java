package org.foodapp.dao;
import org.foodapp.model.Order;
import org.foodapp.model.OrderStatus;
import org.foodapp.model.User;
import org.foodapp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;
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
        Session session = HibernateUtil.getSessionFactory().openSession();
        Order o = session.get(Order.class, id);
        session.close();
        return o;
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
            session.persist(order);  // با توجه به @OneToMany روی OrderItemها، خودکار ذخیره می‌شوند
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }
}

