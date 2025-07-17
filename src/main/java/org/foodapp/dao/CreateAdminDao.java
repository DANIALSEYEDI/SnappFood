package org.foodapp.dao;

import org.foodapp.model.Admin;
import org.foodapp.util.HibernateUtil;
import org.hibernate.Session;

public class CreateAdminDao {

    public Admin findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session
                    .createQuery("FROM Admin WHERE username = :username", Admin.class)
                    .setParameter("username", username)
                    .uniqueResult();
        }
    }
    public Admin findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Admin.class, id);
        }
    }

    public void save(Admin admin) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(admin);
            session.getTransaction().commit();
        }
    }
}
