package org.foodapp.dao;
import org.foodapp.model.Order;
import org.foodapp.model.Rating;
import org.foodapp.model.User;
import org.foodapp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class RatingDao {

    public void save(Rating rating) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(rating);
            session.getTransaction().commit();
            session.close();
        }
    }

    public Rating findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Rating.class, id);
        }
    }

    public void delete(Rating rating) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.remove(rating);
            session.getTransaction().commit();
        }
    }

    public List<Rating> findByItemId(Long itemId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
            SELECT r
            FROM Rating r
            JOIN r.order.itemsOfOrder oi
            WHERE oi.item.id = :itemId
            """;

            return session.createQuery(hql, Rating.class)
                    .setParameter("itemId", itemId)
                    .list();
        }
    }

    public void update(Rating rating) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.merge(rating);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }


    public boolean existsByUserAndOrder(User user, Order order) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT count(r) FROM Rating r WHERE r.user = :user AND r.order = :order";
            Long count = session.createQuery(hql, Long.class)
                    .setParameter("user", user)
                    .setParameter("order", order)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }
}