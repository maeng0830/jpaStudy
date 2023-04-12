package study.datajpa.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

	List<Member> findTop3By();

	// 메소드명 간략화, 리포지토리 메소드에 바로 JPQL 작성 가능
	// 쿼리에 문법 오류가 있을 경우, 컴파일 예외 발생
	@Query("select m from Member m where m.username = :username and m.age = :age")
	List<Member> findUser(@Param("username") String username, @Param("age") int age);

	// String 타입으로 조회
	@Query("select m.username from Member m")
	List<String> findUsernameList();

	// DTO 타입으로 조회
	// DTO에 해당하는 생성자가 필요하다. MemberDto(m.id, m.username, t.name)
	@Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
	List<MemberDto> findMemberDto();

	// Collection을 통한 in절 처리
	@Query("select m from Member m where m.username in :names")
	List<Member> findByNames(@Param("names") Collection<String> names);

	// 컬렉션 조회
	// 없으면 빈 컬렉션 반환
	List<Member> findListByUsername(String username);

	// 단건 조회
	// 값이 없을 경우, 순수 JPA에서는 예외 발생, 스프링 데이타 JPA에서는 null
	// 여러건인 경우, 예외발생
	Member findMemberByUsername(String username);

	// Optional 단건 조회
	// 여러건인 경우, 예외 발생
	Optional<Member> findOptionalByUsername(String username);

	Page<Member> findPageByAge(int age, Pageable pageable);

	Slice<Member> findSliceByAge(int age, Pageable pageable);

	// join이 필요한 paging 시, count 쿼리 분리(분리하지 않을 경우, count 쿼리에 대해서도 불필요한 조인이 실행된다)
	// paing, sorting과 관련된 쿼리는 작성할 필요가 없다. Pageable 파라미터를 통해 해결된다.
	// sorting 로직이 복잡해질 경우, Pageable에서 Sorting 값을 제거한 뒤 직접 쿼리에 작성해주면 된다.
	@Query(value = "select m from Member m left join m.team t",
			countQuery = "select count(m.username) from Member m")
	Page<Member> findPageCountByAge(int age, Pageable pageable);

	@Modifying(clearAutomatically = true) // jpa의 executeUpdate() 실행, 벌크 연산 후 영속성 컨텍스트 clear()
	@Query("update Member m set m.age = m.age + 1 where m.age >= :age")
	int bulkAgePlus(@Param("age") int age);

	@Query("select m from Member m left join fetch m.team")
	List<Member> findMemberFetchJoin();

	@Query("select m from Member m")
	@EntityGraph(attributePaths = {"team"})
	List<Member> findMemberEntityGraph();

	@Override
	@EntityGraph(attributePaths = {"team"}) // findAll()이 fetchJoin할 수 있도록 오버라이딩
	List<Member> findAll();

	@EntityGraph(attributePaths = {"team"})
	List<Member> findEntityGraphByUsername(String username);

	// @QueryHint를 통해 조회된 데이터를 변경하지 않고, 오로지 조회만 할 것이라고 명시해준다.
	// 이러면 변경감지를 위한 추가 로직이 제외되기 때문에, 프로그램적 비용이 절감될 수 있다.
	@QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
	Member findReadOnlyByUsername(String username);
}
