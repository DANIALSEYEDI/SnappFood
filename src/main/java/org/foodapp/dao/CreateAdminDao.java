package org.foodapp.dao;
import org.foodapp.model.Admin;
import org.foodapp.util.HibernateUtil;
import org.hibernate.Session;
public class CreateAdminDao {

    public Admin findByPhonenumber(String phone) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session
                    .createQuery("FROM Admin WHERE phonenumber = :phone", Admin.class)
                    .setParameter("phone", phone)
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
