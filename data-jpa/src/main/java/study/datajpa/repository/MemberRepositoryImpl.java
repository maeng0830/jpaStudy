package study.datajpa.repository;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import study.datajpa.entity.Member;

// 커스텀 리포지토리 인터페이스의 구현체는, 해당 구현체를 상속할 스프링 데이터 JPA 리포지토리의 이름+Impl로 맞춰야한다.
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

	private final EntityManager em;

	@Override
	public List<Member> findMemberCustom() {
		return em.createQuery("select m from Member m", Member.class)
				.getResultList();
	}
}
