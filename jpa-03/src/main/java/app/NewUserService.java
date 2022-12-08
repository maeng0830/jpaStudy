package app;

import domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jpa.EMF;

public class NewUserService {

    public void saveNewUser(User user) {
        EntityManager em = EMF.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // persist(): 새로운 데이터를 저장할 때 사용
            em.persist(user);
            tx.commit();
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}
