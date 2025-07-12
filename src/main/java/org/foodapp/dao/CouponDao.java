package org.foodapp.dao;

import org.foodapp.model.Coupon;
import org.foodapp.util.HibernateUtil;
import org.hibernate.Session;

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
}
