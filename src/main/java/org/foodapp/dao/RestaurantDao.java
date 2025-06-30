package org.foodapp.dao;

import org.foodapp.model.Restaurant;
import org.foodapp.model.RestaurantStatus;
import org.foodapp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class RestaurantDao {

    public void save(Restaurant restaurant) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.persist(restaurant);
        session.getTransaction().commit();
        session.close();
    }

    public List<Restaurant> findBySeller(Long sellerId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Query<Restaurant> query = session.createQuery("FROM Restaurant WHERE seller.id = :sellerId", Restaurant.class);
        query.setParameter("sellerId", sellerId);
        List<Restaurant> restaurants = query.list();
        session.close();
        return restaurants;
    }

    public Restaurant findById(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Restaurant restaurant = session.get(Restaurant.class, id);
        session.close();
        return restaurant;
    }

    public void update(Restaurant restaurant) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.merge(restaurant);
        session.getTransaction().commit();
        session.close();
    }

    public List<Restaurant> findByStatus(RestaurantStatus status) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Restaurant> result = session.createQuery("FROM Restaurant WHERE status = :status", Restaurant.class)
                .setParameter("status", status)
                .list();
        session.close();
        return result;
    }

}
