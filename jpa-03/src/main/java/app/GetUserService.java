package app;

import domain.User;
import jakarta.persistence.EntityManager;
import jpa.EMF;

public class GetUserService {
    public User getUser(String email) {
        EntityManager em = EMF.createEntityManager();
        try {
            // find(): 데이터를 조회할 때 사용
            // find(엔티티, 식별자 값)
            // 존재하지 않으면 null을 return
            User user = em.find(User.class, email);
            if (user == null) {
                throw new NoUserException();
            }
            return user;
        } finally {
            em.close();
        }
    }
}
