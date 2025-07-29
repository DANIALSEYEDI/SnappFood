package org.foodapp.dao;
import org.foodapp.model.PaymentMethod;
import org.foodapp.model.PaymentStatus;
import org.foodapp.model.Transaction;
import org.foodapp.model.User;
import org.foodapp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.List;
import java.util.Map;

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

    public List<Transaction> findByFilters(Map<String, String> params) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("FROM Transaction t WHERE 1=1");

            if (params.containsKey("user") && !params.get("user").isBlank()) {
                hql.append(" AND (LOWER(t.user.phoneNumber) LIKE :user OR LOWER(t.user.fullName) LIKE :user)");
            }

            if (params.containsKey("method")) {
                hql.append(" AND t.method = :method");
            }
            if (params.containsKey("status")) {
                hql.append(" AND t.status = :status");
            }
            if (params.containsKey("search")) {
                hql.append(" AND (t.user.fullName LIKE :search OR t.user.phoneNumber LIKE :search)");
            }

            Query<Transaction> query = session.createQuery(hql.toString(), Transaction.class);

            if (params.containsKey("user") && !params.get("user").isBlank()) {
                query.setParameter("user", "%" + params.get("user").toLowerCase() + "%");
            }

            if (params.containsKey("method")) {
                try {
                    PaymentMethod method = PaymentMethod.valueOf(params.get("method").toUpperCase());
                    query.setParameter("method", method);
                } catch (IllegalArgumentException e) {
                }
            }
            if (params.containsKey("status")) {
                try {
                    PaymentStatus status = PaymentStatus.valueOf(params.get("status").toUpperCase());
                    query.setParameter("status", status);
                } catch (IllegalArgumentException e) {
                }
            }
            if (params.containsKey("search")) {
                query.setParameter("search", "%" + params.get("search") + "%");
            }
            return query.list();
        }
    }
}