package org.foodapp.dao;

import org.foodapp.model.Restaurant;
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

    public List<Restaurant> findByFilters(String searchText, List<String> keywords) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("""
            SELECT DISTINCT r FROM Restaurant r
            LEFT JOIN FETCH r.menus m
            LEFT JOIN FETCH m.items i
            WHERE 1=1
        """);

            if (searchText != null && !searchText.trim().isEmpty()) {
                hql.append("""
                AND (
                    LOWER(r.name) LIKE :search
                    OR LOWER(i.name) LIKE :search
                )
            """);
            }

            if (keywords != null && !keywords.isEmpty()) {
                hql.append("""
                AND EXISTS (
                    SELECT 1 FROM FoodItem fi2
                    JOIN fi2.keywords k
                    WHERE fi2 = i AND LOWER(k) IN (:keywords)
                )
            """);
            }

            Query<Restaurant> query = session.createQuery(hql.toString(), Restaurant.class);

            if (searchText != null && !searchText.trim().isEmpty()) {
                query.setParameter("search", "%" + searchText.toLowerCase() + "%");
            }

            if (keywords != null && !keywords.isEmpty()) {
                List<String> lowerKeywords = keywords.stream().map(String::toLowerCase).toList();
                query.setParameter("keywords", lowerKeywords);
            }

            return query.getResultList();
        }
    }



    public Restaurant findWithMenusAndItems(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery(
                            "SELECT r FROM Restaurant r " +
                                    "LEFT JOIN FETCH r.menus m " +
                                    "LEFT JOIN FETCH m.items " +
                                    "WHERE r.id = :id", Restaurant.class)
                    .setParameter("id", id)
                    .uniqueResult();
        } finally {
            session.close();
        }
    }
}

