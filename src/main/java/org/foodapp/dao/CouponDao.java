package org.foodapp.dao;

import org.foodapp.model.Coupon;
import org.foodapp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class CouponDao {
    public Coupon findByCode(String code) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery(
                    "FROM Coupon WHERE code = :code", Coupon.class
            ).setParameter("code", code).uniqueResult();
        } finally {
            session.close();
        }
    }

    public Coupon findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Coupon.class, id);
        }
    }
    public void save(Coupon coupon) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(coupon);
            tx.commit();
        }
    }

    public List<Coupon> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Coupon", Coupon.class).list();
        }
    }

    public void delete(Coupon coupon) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.remove(coupon);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(Coupon coupon) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(coupon);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





}
