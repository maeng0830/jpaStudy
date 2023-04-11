package study.datajpa.repository;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import study.datajpa.entity.Member;

@Repository
public class MemberJpaRepository {

	@PersistenceContext // 생성자 주입의 역할을 한다
	private EntityManager em;

	public Member save(Member member) {
		em.persist(member);
		return member;
	}

	public void delete(Member member) {
		em.remove(member);
	}

	public List<Member> findAll() {
		return em.createQuery("select m from Member m", Member.class)
				.getResultList();
	}

	public long count() {
		return em.createQuery("select count(m) from Member m", Long.class)
				.getSingleResult();
	}

	public Optional<Member> findById(Long id) {
		Member member = em.find(Member.class, id);
		return Optional.ofNullable(member);
	}

	public List<Member> findByUsernameAndGreaterThan(String username, int age) {
		return em.createQuery("select m from Member m where m.username = :username and m.age > :age")
				.setParameter("username", username)
				.setParameter("age", age)
				.getResultList();
	}

//	public Member find(Long id) {
//		return em.find(Member.class, id);
//	}
}
