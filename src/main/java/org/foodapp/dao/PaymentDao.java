package org.foodapp.dao;

import org.foodapp.model.Payment;
import org.foodapp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class PaymentDao {

    public void save(Payment payment) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.saveOrUpdate(payment);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public Payment findByOrderId(Long orderId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Payment WHERE order.id = :orderId", Payment.class)
                    .setParameter("orderId", orderId)
                    .uniqueResult();
        }
    }
}
