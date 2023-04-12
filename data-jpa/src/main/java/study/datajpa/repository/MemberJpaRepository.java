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
		return em.createQuery("select m from Member m where m.username = :username and m.age > :age", Member.class)
				.setParameter("username", username)
				.setParameter("age", age)
				.getResultList();
	}

	public List<Member> findByPage(int age, int offset, int limit) {
		return em.createQuery("select m from Member m where m.age = :age order by m.username desc", Member.class)
				.setParameter("age", age)
				.setFirstResult(offset) // 시작 인덱스
				.setMaxResults(limit) // 개수
				.getResultList();
	}

	public long totalCount(int age) {
		return em.createQuery("select count(m) from Member m where m.age = :age", Long.class)
				.setParameter("age", age)
				.getSingleResult();
	}

}
