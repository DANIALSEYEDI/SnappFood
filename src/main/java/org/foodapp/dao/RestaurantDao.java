package org.foodapp.dao;

import org.foodapp.model.Restaurant;
import org.foodapp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;


import java.util.ArrayList;
import java.util.HashSet;
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
        Session session = HibernateUtil.getSessionFactory().openSession();

        String hql = """
        SELECT DISTINCT r FROM Restaurant r
        JOIN r.menus m
        JOIN m.items i
        WHERE 
            (:search IS NULL OR 
             LOWER(r.name) LIKE LOWER(:search) OR 
             LOWER(i.name) LIKE LOWER(:search))
            AND (
                :keywordsEmpty = true OR 
                EXISTS (
                    SELECT 1 FROM FoodItem fi2 
                    WHERE fi2 = i 
                    AND :keyword MEMBER OF fi2.keywords
                )
            )
        """;

        List<Restaurant> result = new ArrayList<>();
        if (keywords == null || keywords.isEmpty()) {
            var q = session.createQuery(hql, Restaurant.class);
            q.setParameter("search", searchText != null ? "%" + searchText.toLowerCase() + "%" : null);
            q.setParameter("keywordsEmpty", true);
            q.setParameter("keyword", "");
            result = q.getResultList();
        } else {
            for (String kw : keywords) {
                var q = session.createQuery(hql, Restaurant.class);
                q.setParameter("search", searchText != null ? "%" + searchText.toLowerCase() + "%" : null);
                q.setParameter("keywordsEmpty", false);
                q.setParameter("keyword", kw.toLowerCase());
                result.addAll(q.getResultList());
            }
        }

        session.close();
        return new ArrayList<>(new HashSet<>(result));
    }







}

