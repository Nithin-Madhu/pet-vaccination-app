package service;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import entity.User;

@Stateless
public class UserService {
    @PersistenceContext
    private EntityManager em;

    public boolean authenticate(String username, String password) {
        try {
            em.createQuery("SELECT u FROM User u WHERE u.username = :username AND u.password = :password", User.class)
                .setParameter("username", username)
                .setParameter("password", password)
                .getSingleResult();
            return true;
        } catch (NoResultException e) {
            return false;
        }
    }
}
