package cascade;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import proxy.Member;

public class JpaMain {

	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();

		tx.begin();

		try {

			Child child1 = new Child();
			Child child2 = new Child();

			Parent parent = new Parent();
			parent.addChild(child1);
			parent.addChild(child2);

			// 영속성 전이를 통해 child1, child2도 DB에 저장된다.
			em.persist(parent);

			em.flush();
			em.clear();

			Parent findParent = em.find(Parent.class, parent.getId());
			// 고아객체가 된 자식 엔티티를 DB에서 자동으로 삭제한다.
			// 컬렉션에서 자식 엔티티가 제외되거나
			// 부모객체가 삭제될 때(cascade = remove)
			findParent.getChildList().remove(0);

			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			em.close();
		}

		emf.close();
	}
}
