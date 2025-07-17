package org.foodapp.dao;

import org.foodapp.model.Transaction;
import org.foodapp.model.User;
import org.foodapp.util.HibernateUtil;
import org.hibernate.Session;


import java.util.List;

public class TransactionDao {

    public List<Transaction> findByUser(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT t FROM Transaction t LEFT JOIN FETCH t.order WHERE t.user.id = :uid ORDER BY t.createdAt DESC",
                    Transaction.class
            ).setParameter("uid", user.getId()).list();
        }
    }

    public void save(Transaction tx) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            org.hibernate.Transaction hibernateTx = session.beginTransaction();
            session.persist(tx);
            hibernateTx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
