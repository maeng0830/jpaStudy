package app;

import domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jpa.EMF;

public class RemoveUserService {
    public void removeUser(String email) {
        EntityManager em = EMF.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            User user = em.find(User.class, email);
            if (user == null) {
                throw new NoUserException();
            }
            // remove(): 데이터를 삭제 할 때 사용
            // find를 통해 가져온 객체를 인자로 전달하면, db에서 삭제 된다.
            em.remove(user);
            tx.commit();
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}