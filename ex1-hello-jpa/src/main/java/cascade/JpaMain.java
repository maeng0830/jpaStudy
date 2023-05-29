package cascade;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import proxy.Member;

/**
 * cascade: 영속성 전이
 * 영속성 전이란 부모 엔티티에 대한 작업이 자식 엔티티에도 적용되는 기능이다.
 * // 자식 엔티티와 연관된 엔티티가 1개일 때만 사용해야한다.
 * 예) 부모 엔티티를 저장할 때 자식 엔티티도 함께 저장한다.
 * All(전체), Persist(영속), remove(삭제), MERGE(병합)
 */
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

			// 영속성 전이를 적용하지 않을 경우, 각각 persist를 호출해야한다.
//			em.persist(parent);
//			em.persist(child1);
//			em.persist(child2);

			// 영속성 전이를 통해 child1, child2도 DB에 저장된다.
			em.persist(parent); // DB -> (parent_PK: 1), (child_PK: 1, 2)

			em.flush();
			em.clear();

			Parent findParent = em.find(Parent.class, parent.getId());

			// parent의 childList에서 첫 번째 child가 제거된다. 첫 번째 child는 고아객체가 되는 것이다.
			// 고아 객체는 삭제된다.
			findParent.getChildList().remove(0); // DB -> (parent_PK: 1), (child_PK: 2)

			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			em.close();
		}

		emf.close();
	}
}
