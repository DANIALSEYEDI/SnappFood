package org.foodapp.dao;
import org.foodapp.model.User;
import org.foodapp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;
public class UserDao {
    public User findByPhone(String phone) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Query<User> query = session.createQuery("FROM User WHERE phoneNumber = :phone", User.class);
            query.setParameter("phone", phone);
            return query.uniqueResult();
        } finally {
            session.close();
        }
    }

    public void save(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
        }
    }

    public void update(User user) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            session.merge(user);
            session.getTransaction().commit();
        } finally {
            session.close();
        }
    }

    public User findByPhoneAndPassword(String phone, String password) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Query<User> query = session.createQuery("FROM User WHERE phoneNumber = :phone AND password = :password", User.class);
            query.setParameter("phone", phone);
            query.setParameter("password", password);
            return query.uniqueResult();
        } finally {
            session.close();
        }
    }

    public User findById(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = session.get(User.class, id);
        session.close();
        return user;
    }

    public List<User> findAll() {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return session.createQuery("FROM User", User.class).list();
            }
    }

}
